package lat.luisdias.stockapp.modules.identity.application.service;

import lat.luisdias.stockapp.modules.identity.application.dto.VerificationTokenIssued;
import lat.luisdias.stockapp.shared.exception.exceptions.ExpiredTokenException;
import lat.luisdias.stockapp.shared.exception.exceptions.TokenRateLimitReachedException;
import lat.luisdias.stockapp.modules.identity.domain.model.User;
import lat.luisdias.stockapp.modules.identity.domain.model.VerificationToken;
import lat.luisdias.stockapp.modules.identity.domain.model.VerificationTokenPurpose;
import lat.luisdias.stockapp.modules.identity.infrastructure.persistence.repository.VerificationTokenRepository;
import lat.luisdias.stockapp.shared.security.tools.VerificationCodeGenerator;
import lat.luisdias.stockapp.modules.identity.infrastructure.security.VerificationTokenHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service responsible for the lifecycle management of short-lived verification tokens.
 *
 * <p>This service provides a secure and robust mechanism for handling verification codes
 * (e.g., OTPs) used in sensitive flows like MFA enrollment, password recovery, and email verification.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 * <li><b>Secure Storage:</b> Tokens are hashed before persistence using {@link VerificationTokenHasher}.</li>
 * <li><b>Contextual Scoping:</b> Each token is cryptographically bound to a specific user, method,
 * and purpose to prevent cross-context replay attacks.</li>
 * <li><b>Rate Limiting:</b> Enforces strict intervals between token requests to mitigate spamming.</li>
 * <li><b>Atomic Consumption:</b> Ensures tokens are verified and deleted in a single transactional
 * operation to prevent multiple uses of the same code.</li>
 * </ul>
 *
 * @see VerificationToken
 * @see VerificationTokenHasher
 */
@Service
@Transactional
public class VerificationTokenService {
    private static final Pattern CODE = Pattern.compile("^[A-Z0-9]{6}$");
    private final VerificationTokenRepository verificationTokenRepository;
    private final Duration tokenExpiration;
    private final Duration tokenRateLimit;
    private final VerificationTokenHasher tokenHasher;

    public VerificationTokenService(
            VerificationTokenRepository verificationTokenRepository,
            @Value("${app.security.verification-token.token-expire-minutes:10}") long expirationMinutes,
            @Value("${app.security.verification-token.rate-limit-ms:60000}") long tokenRateLimitMs,
            VerificationTokenHasher tokenHasher
    ) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.tokenExpiration = Duration.ofMinutes(expirationMinutes);
        this.tokenRateLimit = Duration.ofMillis(tokenRateLimitMs);
        this.tokenHasher = tokenHasher;
    }

    /**
     * Generates a new verification token for the specified scope and returns the raw code.
     * This method enforces rate limits to prevent spamming.
     *
     * @param user    The user requesting the token.
     * @param ownerId The ID of the object this token belongs to.
     * @param purpose The business purpose for this token.
     * @return {@link VerificationTokenIssued} with the generated raw verification code to be sent to the user.
     * @throws TokenRateLimitReachedException if a token was recently requested and the retry limit is active.
     * @throws IllegalStateException          if the code generator produces an invalid format.
     * @throws NullPointerException           if any required parameter is null.
     */
    public VerificationTokenIssued createVerificationToken(User user, UUID ownerId, VerificationTokenPurpose purpose) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(purpose, "purpose");

        Instant now = Instant.now();
        verificationTokenRepository
                .findFirstByUserAndOwnerIdAndPurposeAndExpiryDateAfterOrderByCreatedAtDesc(user, ownerId, purpose, now)
                .ifPresent(
                        lastToken -> {
                            Instant retryAt = lastToken.getCreatedAt().plus(tokenRateLimit);
                            if (now.isBefore(retryAt)) {
                                throw new TokenRateLimitReachedException("exception.verification_token.rate_limit", retryAt);
                            }
                        }
                );

        String rawCode = VerificationCodeGenerator.generate().trim().toUpperCase(Locale.ROOT);

        if (!CODE.matcher(rawCode).matches()) {
            throw new IllegalStateException("VerificationCodeGenerator produced an invalid format");
        }

        String tokenHash = tokenHasher.hash(scope(user, ownerId, purpose, rawCode));

        VerificationToken token = new VerificationToken(
                ownerId,
                purpose,
                tokenHash,
                user,
                tokenExpiration,
                now
        );
        verificationTokenRepository.deleteAllForScope(user, ownerId, purpose);
        verificationTokenRepository.saveAndFlush(token);

        return new VerificationTokenIssued(rawCode, token.getExpiryDate(), now.plus(tokenRateLimit));
    }

    /**
     * Verifies the provided raw code against the stored hashed token.
     * If valid, the token is consumed (deleted) to prevent replay attacks.
     *
     * @param user    The owner of the verification token.
     * @param ownerId The ID of the MFA method linked to this challenge.
     * @param purpose The intended use of this token (e.g., MFA_AUTH, MFA_ENABLE).
     * @param rawCode The plain-text code provided by the user.
     * @throws BadCredentialsException if the token is invalid or doesn't match the scope.
     * @throws ExpiredTokenException   if the token is valid but expired.
     * @throws NullPointerException    if any required parameter is null.
     */
    public void verifyAndConsume(User user, UUID ownerId, VerificationTokenPurpose purpose, String rawCode) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(purpose, "purpose");

        Instant now = Instant.now();

        String code = normalizeAndValidateCode(rawCode);

        String tokenHash = tokenHasher.hash(scope(user, ownerId, purpose, code));

        VerificationToken token = verificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("exception.verification_token.invalid"));

        if (!token.getOwnerId().equals(ownerId) || !token.getPurpose().equals(purpose)) {
            throw new BadCredentialsException("exception.verification_token.invalid");
        }

        token.assertNotExpired(now);
        verificationTokenRepository.delete(token);
    }

    private String normalizeAndValidateCode(String raw) {
        String code = raw == null ? null : raw.trim().toUpperCase(Locale.ROOT);
        if (code == null || !CODE.matcher(code).matches()) {
            throw new BadCredentialsException("exception.verification_token.invalid");
        }
        return code;
    }

    private String scope(User user, UUID ownerId, VerificationTokenPurpose purpose, String rawCode) {
        Objects.requireNonNull(user.getSubject(), "user.subject");
        return rawCode + "|" + user.getSubject() + "|" + ownerId + "|" + purpose.name();
    }
}
