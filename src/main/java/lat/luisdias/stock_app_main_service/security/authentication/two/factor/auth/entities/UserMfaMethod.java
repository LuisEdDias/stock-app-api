package lat.luisdias.stock_app_main_service.security.authentication.two.factor.auth.entities;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.infra.exceptions.DomainInvariantViolationException;
import lat.luisdias.stock_app_main_service.security.identity.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_mfa_method")
public class UserMfaMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaType mfaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaStatus mfaStatus = MfaStatus.PENDING;

    private String credentialEncrypted;

    private String destination;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant verifiedAt;

    protected UserMfaMethod() {}

    public UserMfaMethod(
            UUID publicId,
            User user,
            MfaType mfaType,
            String credentialEncrypted,
            String destination
    ) {
        String normalizedDestination = normalizeDestination(destination);
        validateConfiguration(mfaType, credentialEncrypted, normalizedDestination);
        this.publicId = publicId;
        this.user = user;
        this.mfaType = mfaType;
        this.credentialEncrypted = credentialEncrypted;
        this.destination = normalizedDestination;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void activate(Instant now) {
        if (now == null)
            throw new DomainInvariantViolationException("exception.missing_timestamp");
        if (mfaStatus != MfaStatus.PENDING)
            throw new DomainInvariantViolationException("exception.mfa.invalid_state_transition");
        if (this.verifiedAt != null)
            throw new DomainInvariantViolationException("exception.mfa.already_verified");

        validateConfiguration(this.mfaType, this.credentialEncrypted, this.destination);
        this.mfaStatus = MfaStatus.ACTIVE;
        this.verifiedAt = now;
    }

    public void disable() {
        if (this.mfaStatus == MfaStatus.DISABLED) return;
        this.mfaStatus = MfaStatus.DISABLED;
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public User getUser() {
        return user;
    }

    public MfaType getMfaType() {
        return mfaType;
    }

    public String getCredentialEncrypted() {
        return credentialEncrypted;
    }

    public String getDestination() {
        return destination;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
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

    private void validateConfiguration(MfaType mfaType, String credentialEncrypted, String destination) {
        if (mfaType == MfaType.TOTP) {
            if (credentialEncrypted == null || credentialEncrypted.isBlank())
                throw new DomainInvariantViolationException("exception.totp.secret_required");
            if (destination != null)
                throw new DomainInvariantViolationException("exception.totp.destination_not_allowed");
        } else {
            if (destination == null || destination.isBlank())
                throw new DomainInvariantViolationException("exception.destination_required");
            if (credentialEncrypted != null && !credentialEncrypted.isBlank())
                throw new DomainInvariantViolationException("exception.mfa.credential_not_allowed");
        }
    }

    private String normalizeDestination(String destination) {
        if (destination == null) return null;
        String normalized = destination.trim();
        return normalized.isEmpty() ? null : normalized.toLowerCase();
    }
}
