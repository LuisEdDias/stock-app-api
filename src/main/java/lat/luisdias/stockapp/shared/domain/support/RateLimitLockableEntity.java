package lat.luisdias.stockapp.shared.domain.support;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lat.luisdias.stock_app_main_service.infra.exceptions.DomainInvariantViolationException;
import lat.luisdias.stock_app_main_service.infra.exceptions.ResourceLockedException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Base abstract class for domain entities that require rate-limiting and temporary lockout capabilities.
 * <p>This class extends {@link BaseEntity} to add security-related state, such as
 * failed attempt counters and lockout timestamps. It provides atomic methods to
 * validate and manage access restrictions, preventing brute-force attacks at the domain level.</p>
 * @see BaseEntity
 */
@MappedSuperclass
public abstract class RateLimitLockableEntity extends BaseEntity {

    @Column(nullable = false)
    private int failedAttempts = 0;

    private Instant lockedUntil;

    /**
     * Checks if the entity is currently under a lockout period and resets the lockout if its expired.
     *
     * @param now The current timestamp for validation.
     * @throws ResourceLockedException if the lockout period is still active.
     * @throws NullPointerException    if any required parameter is null.
     */
    public void assertNotLocked(Instant now) {
        Objects.requireNonNull(now, "now");
        if (lockedUntil == null) return;

        if (now.isBefore(lockedUntil)) {
            throw new ResourceLockedException("exception.resource_locked", lockedUntil);
        }

        resetAttempts();
    }

    /**
     * Resets the failed attempts counter and clears any active lockout period.
     * <p>
     * This method should be called after a successful verification or an administrative
     * unlock action to restore the resource to its initial functional state.
     * </p>
     */
    public void resetAttempts() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    /**
     * Registers a failed attempt and checks if a lockout should be applied.
     *
     * @param maxAttempts   The maximum number of allowed attempts before locking.
     * @param lockDuration  The amount of time which the method should remain locked if the limit is reached.
     * @param now           The current timestamp for validation.
     * @throws ResourceLockedException           if the method is already locked.
     * @throws DomainInvariantViolationException if maxAttempts is invalid or lockDuration is zero/negative.
     * @throws NullPointerException              if any required parameter is null.
     */
    public void registerFailedAttempt(int maxAttempts, Duration lockDuration, Instant now) {
        Objects.requireNonNull(now, "now");
        assertNotLocked(now);

        if (maxAttempts <= 0) {
            throw new DomainInvariantViolationException("exception.max_failed_attempts_invalid");
        }

        this.failedAttempts++;

        if (failedAttempts >= maxAttempts) {
            Objects.requireNonNull(lockDuration, "lockDuration");
            if (lockDuration.isZero() || lockDuration.isNegative()) {
                throw new DomainInvariantViolationException("exception.lock_duration_invalid");
            }

            this.lockedUntil = now.plus(lockDuration);
            this.failedAttempts = 0;
        }
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isLocked(Instant now) {
        Objects.requireNonNull(now, "now");
        return lockedUntil != null && now.isBefore(lockedUntil);
    }
}
