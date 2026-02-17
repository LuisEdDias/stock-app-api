package lat.luisdias.stockapp.modules.identity.domain.model;

import jakarta.persistence.*;
import lat.luisdias.stockapp.shared.exception.exceptions.DomainInvariantViolationException;
import lat.luisdias.stockapp.shared.domain.support.RateLimitLockableEntity;
import lat.luisdias.stockapp.shared.exception.exceptions.ResourceLockedException;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a specific Multi-Factor Authentication (MFA) method associated with a user.
 * <p>This entity follows a Rich Domain Model pattern and specializes {@link RateLimitLockableEntity}
 * to provide security logic for verification attempts, including failed attempt tracking
 * and temporary lockout management.</p>
 * <p>Each method is bound to a single {@link User} and can be of different
 * types (e.g., TOTP, EMAIL, SMS) as defined in {@link UserMfaType}. It is responsible
 * for its own lifecycle, from pending setup to active verification.</p>
 * @see RateLimitLockableEntity
 * @see UserMfaType
 */
@Entity
@Table(name = "user_mfa_method")
public class UserMfaMethod extends RateLimitLockableEntity {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{8,14}$");

    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserMfaType userMfaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserMfaStatus userMfaStatus = UserMfaStatus.PENDING;

    private String secretEncrypted;

    private String destination;

    private Instant verifiedAt;

    private Instant disabledAt;

    protected UserMfaMethod() {}

    public UserMfaMethod(
            UUID publicId,
            User user,
            UserMfaType userMfaType,
            String secretEncrypted,
            String destination
    ) {
        this.publicId = Objects.requireNonNull(publicId, "publicId");
        this.user = Objects.requireNonNull(user, "user");
        this.userMfaType = Objects.requireNonNull(userMfaType, "userMfaType");
        validateConfiguration(userMfaType, secretEncrypted, destination);
    }

    /**
     * Transitions the method state to {@link UserMfaStatus#ACTIVE} upon successful verification.
     * <p>
     * This method performs a critical security handshake:
     * <ul>
     * <li>Asserts that the resource is not currently locked due to rate limiting.</li>
     * <li>Validates that the transition is occurring from a {@link UserMfaStatus#PENDING} state.</li>
     * <li>Resets the failed attempts counter (inherited from {@link RateLimitLockableEntity})
     * to ensure a clean slate after proof of ownership.</li>
     * </ul>
     * </p>
     *
     * @param now The timestamp to be recorded as the verification date.
     * @throws ResourceLockedException           if the method is currently locked due to too many failed setup attempts.
     * @throws DomainInvariantViolationException if the method is not in PENDING state or has already been verified.
     * @throws NullPointerException              if the provided timestamp is null.
     */
    public void verifyAndActivate(Instant now) {
        Objects.requireNonNull(now, "now");
        assertNotLocked(now);

        if (userMfaStatus != UserMfaStatus.PENDING) {
            throw new DomainInvariantViolationException("exception.mfa.invalid_state_transition");
        }
        if (this.verifiedAt != null) {
            throw new DomainInvariantViolationException("exception.mfa.already_verified");
        }

        this.userMfaStatus = UserMfaStatus.ACTIVE;
        this.verifiedAt = now;
        resetAttempts();
    }

    /**
     * Deactivates this MFA method, marking it as {@link UserMfaStatus#DISABLED}.
     * <p>
     * This operation is <b>idempotent</b>: calling it on an already disabled method
     * has no side effects and simply ignores the request. Once disabled, the method
     * can no longer be used for authentication challenges.
     * </p>
     *
     * @param now The timestamp to be recorded as the disablement date.
     * @throws NullPointerException if the provided timestamp is null.
     */
    public void disable(Instant now) {
        if (this.userMfaStatus == UserMfaStatus.DISABLED) return;

        Objects.requireNonNull(now, "now");
        this.userMfaStatus = UserMfaStatus.DISABLED;
        this.disabledAt = now;
        resetAttempts();
    }

    /**
     * Validates and applies the specific configuration for each MFA type.
     * <p>
     * This method ensures domain invariants are met:
     * <ul>
     * <li><b>TOTP:</b> Requires an encrypted secret and forbids a destination.</li>
     * <li><b>EMAIL/SMS:</b> Requires a destination and forbids an encrypted secret.</li>
     * </ul>
     * </p>
     *
     * @param userMfaType         The type of MFA being configured.
     * @param secretEncrypted The encrypted secret (required for TOTP).
     * @param destination     The target address (required for EMAIL/SMS).
     * @throws DomainInvariantViolationException if the configuration rules for the given type are violated.
     */
    private void validateConfiguration(UserMfaType userMfaType, String secretEncrypted, String destination) {
        switch (userMfaType) {
            case TOTP -> {
                if (secretEncrypted == null || secretEncrypted.isBlank()) {
                    throw new DomainInvariantViolationException("exception.mfa.totp.secret_required");
                }
                if (destination != null) {
                    throw new DomainInvariantViolationException("exception.mfa.totp.destination_not_allowed");
                }

                this.secretEncrypted = secretEncrypted;
                this.destination = null;
            }
            case EMAIL -> {
                String normalizedEmail = normalizeEmail(destination);
                if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
                    throw new DomainInvariantViolationException("exception.mfa.email_invalid");
                }
                if (secretEncrypted != null && !secretEncrypted.isBlank()) {
                    throw new DomainInvariantViolationException("exception.mfa.credential_not_allowed");
                }

                this.destination = normalizedEmail;
                this.secretEncrypted = null;
            }
            case SMS -> {
                String normalizedPhone = normalizePhoneNumber(destination);
                if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
                    throw new DomainInvariantViolationException("exception.mfa.phone_number_invalid");
                }
                if (secretEncrypted != null && !secretEncrypted.isBlank()) {
                    throw new DomainInvariantViolationException("exception.mfa.credential_not_allowed");
                }

                this.destination = normalizedPhone;
                this.secretEncrypted = null;
            }
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new DomainInvariantViolationException("exception.mfa.destination_required");
        }

        return email.trim().replaceAll(" ", "").toLowerCase(Locale.ROOT);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new DomainInvariantViolationException("exception.mfa.destination_required");
        }

        return "+" + phoneNumber.replaceAll("\\D", "");
    }

    public UUID getPublicId() {
        return publicId;
    }

    public User getUser() {
        return user;
    }

    public UserMfaType getMfaType() {
        return userMfaType;
    }

    public String getSecretEncrypted() {
        return secretEncrypted;
    }

    public UserMfaStatus getMfaStatus() {
        return userMfaStatus;
    }

    public String getDestination() {
        return destination;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getDisabledAt() {
        return disabledAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMfaMethod that = (UserMfaMethod) o;
        return Objects.equals(publicId, that.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(publicId);
    }
}
