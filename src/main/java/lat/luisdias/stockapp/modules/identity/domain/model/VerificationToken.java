package lat.luisdias.stockapp.modules.identity.domain.model;

import jakarta.persistence.*;
import lat.luisdias.stockapp.shared.domain.support.BaseEntity;
import lat.luisdias.stockapp.shared.exception.exceptions.DomainInvariantViolationException;
import lat.luisdias.stockapp.shared.exception.exceptions.ExpiredTokenException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a short-lived security token used for out-of-band identity verification.
 * <p>This entity acts as a secure container for hashed verification codes. To prevent
 * sensitive data exposure, the plain-text code is never stored; only its salted hash
 * is persisted. Each token is strictly bound to a user, a specific resource owner,
 * and a business purpose.</p>
 * <p>The lifecycle of this entity is governed by a strict expiration policy.
 * Once a token is consumed or expires, it should be considered invalid and
 * purged from the system.<b>The underlying table is explicitly indexed to support
 * high-performance bulk cleanup operations and rapid scoping queries for rate-limiting.</b></p>
 *
 * @see BaseEntity
 * @see VerificationTokenPurpose
 */
@Entity
@Table(
        name = "verification_tokens",
        indexes = {
                // For cleanup job
                @Index(name = "idx_token_expiry", columnList = "expiry_date"),

                // For scoped transactions
                @Index(name = "idx_token_scope", columnList = "user_id, owner_id, purpose")
        }
)
public class VerificationToken extends BaseEntity {

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(name = "purpose", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private VerificationTokenPurpose purpose;

    @Column(unique = true, nullable = false, updatable = false)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false, updatable = false)
    private Instant expiryDate;

    protected VerificationToken() {
    }

    public VerificationToken(
            UUID ownerId,
            VerificationTokenPurpose purpose,
            String tokenHash,
            User user,
            Duration lifetime,
            Instant now
    ) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(purpose, "purpose");
        Objects.requireNonNull(now, "now");
        validateConfiguration(tokenHash, lifetime, now);

        this.ownerId = ownerId;
        this.purpose = purpose;
        this.user = user;
    }

    /**
     * Asserts that the token is still valid solely based on time.
     * Note: Does not verify the hash match.
     *
     * @param now The current timestamp.
     * @throws ExpiredTokenException if the token has expired.
     * @throws NullPointerException  if the provided timestamp is null.
     */
    public void assertNotExpired(Instant now) {
        Objects.requireNonNull(now, "now");
        if (isExpired(now)) {
            throw new ExpiredTokenException("exception.verification_token.expired");
        }
    }

    /**
     * Validates the integrity of the token configuration and calculates the expiration date.
     * <p>This internal guard ensures that a token cannot be created in an inconsistent state,
     * such as having a null hash or an invalid lifetime (zero or negative). By calculating
     * the {@code expiryDate} here, we ensure the entity is fully initialized and valid
     * upon construction.</p>
     *
     * @param tokenHash The secure hash of the raw verification code.
     * @param lifetime  The duration for which the token will be valid.
     * @param now       The reference point in time to calculate the expiration.
     * @throws DomainInvariantViolationException if the tokenHash is empty or the lifetime is not positive.
     * @throws NullPointerException              if any required parameter is null.
     */
    private void validateConfiguration(String tokenHash, Duration lifetime, Instant now) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new DomainInvariantViolationException("exception.verification_token.token_required");
        }
        if (lifetime == null || lifetime.isZero() || lifetime.isNegative()) {
            throw new DomainInvariantViolationException("exception.verification_token.lifetime_invalid");
        }

        this.tokenHash = tokenHash;
        this.expiryDate = now.plus(lifetime);
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now");
        return !expiryDate.isAfter(now);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public VerificationTokenPurpose getPurpose() {
        return purpose;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }
}
