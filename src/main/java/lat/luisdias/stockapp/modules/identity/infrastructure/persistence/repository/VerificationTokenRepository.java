package lat.luisdias.stockapp.modules.identity.infrastructure.persistence.repository;

import lat.luisdias.stockapp.modules.identity.domain.model.User;
import lat.luisdias.stockapp.modules.identity.domain.model.VerificationToken;
import lat.luisdias.stockapp.modules.identity.domain.model.VerificationTokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link VerificationToken} persistence operations.
 * <p>Provides specialized methods for security-sensitive token lookups and
 * atomic cleanup operations using JPQL.</p>
 */
@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Retrieves the token based on a scoped token hash.
     *
     * @param tokenHash The owner of the token.
     * @return An Optional containing the token, if any.
     */
    Optional<VerificationToken> findByTokenHash(String tokenHash);

    /**
     * Retrieves the most recent active token for a specific user and purpose.
     *
     * @param user    The owner of the token.
     * @param ownerId The associated resource ID.
     * @param purpose The token's business intent.
     * @param now     Current timestamp to filter out expired tokens.
     * @return An Optional containing the latest valid token, if any.
     */
    Optional<VerificationToken> findFirstByUserAndOwnerIdAndPurposeAndExpiryDateAfterOrderByCreatedAtDesc(
            User user, UUID ownerId, VerificationTokenPurpose purpose, Instant now
    );

    /**
     * Deletes all tokens for a specific scope, regardless of expiration.
     * <p>This method clears and flushes the persistence context to ensure that
     * subsequent queries do not return stale, deleted data.</p>
     *
     * @param user    The owner of the token.
     * @param ownerId The associated resource ID.
     * @param purpose The token's business intent.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
              delete from VerificationToken t
              where t.user = :user
                and t.ownerId = :ownerId
                and t.purpose = :purpose
            """)
    void deleteAllForScope(
            @Param("user") User user,
            @Param("ownerId") UUID ownerId,
            @Param("purpose") VerificationTokenPurpose purpose
    );

    /**
     * Purges all expired tokens from the database.
     * <p>This operation is performed as a bulk delete at the database level.
     * To ensure data consistency, the persistence context is automatically
     * flushed and cleared, preventing the application from working with
     * stale entity instances still present in the L1 cache.</p>
     *
     * @param now The reference timestamp to determine expiration.
     *            Any token with an expiry date equal to or before this time will be deleted.
     * @return The total number of expired tokens successfully removed.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from VerificationToken t
                where t.expiryDate <= :now
            """)
    int deleteExpired(@Param("now") Instant now);
}
