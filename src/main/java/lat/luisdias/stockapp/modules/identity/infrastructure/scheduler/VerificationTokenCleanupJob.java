package lat.luisdias.stockapp.modules.identity.infrastructure.scheduler;

import lat.luisdias.stockapp.modules.identity.domain.model.VerificationToken;
import lat.luisdias.stockapp.modules.identity.infrastructure.persistence.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Background job responsible for the automated cleanup of expired verification tokens.
 * <p>This job acts as a system garbage collector, ensuring that the database does not
 * keep stale {@link VerificationToken} records indefinitely. This helps maintain
 * query performance and database health.</p>
 *
 * <p>The execution frequency is configurable via application properties,
 * allowing for fine-tuning based on system load and token volume.</p>
 */
@Service
public class VerificationTokenCleanupJob {
    private final VerificationTokenRepository tokenRepository;
    private final Logger logger = LoggerFactory.getLogger(VerificationTokenCleanupJob.class);

    public VerificationTokenCleanupJob(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Periodically triggers the purging of all expired tokens.
     * <p>This operation is transactional to ensure atomicity and uses
     * the repository's bulk delete capabilities for efficiency.</p>
     */
    @Scheduled(fixedDelayString = "${app.security.verification-token.cleanup.fixed-delay-ms:600000}")
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int deleted = tokenRepository.deleteExpired(now);

        logger.info("Deleted {} expired verification tokens", deleted);
    }
}
