package lat.luisdias.stockapp.modules.identity.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Component responsible for creating secure, one-way cryptographic hashes of verification tokens.
 * <p>It utilizes the <b>HMAC-SHA256</b> algorithm to ensure that tokens stored in the
 * database cannot be reversed or used if the database is compromised (Protection against
 * Database Leaks). The hashing process is keyed, meaning only this application, with
 * the correct secret key, can validate the tokens.</p>
 * <p><b>Thread Safety:</b> This class uses a {@link ThreadLocal} pattern to manage
 * {@link Mac} instances. Since {@code Mac} is not thread-safe and its instantiation
 * via {@code getInstance()} can be expensive, this approach provides high performance
 * in concurrent environments without the overhead of constant re-allocation or synchronization.</p>
 */
@Component
public class VerificationTokenHasher {
    private final SecretKeySpec keySpec;
    private final ThreadLocal<Mac> macThreadLocal;

    public VerificationTokenHasher(
            @Value("${app.security.verification-token.token-key}") String secretKey
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Missing app.security.mfa.token-key");
        }
        this.keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.macThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                return Mac.getInstance("HmacSHA256");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Computes a URL-safe Base64 encoded HMAC-SHA256 hash of the provided raw string.
     * <p>The input is typically a "scoped" string that combines the raw token with
     * contextual data (like user ID and purpose) to prevent token reuse across
     * different security contexts.</p>
     *
     * @param scopedRaw The plain-text string to be hashed (usually combined with context).
     * @return A URL-safe Base64 encoded string representing the cryptographic hash.
     * @throws NullPointerException if scopedRaw is null.
     */
    public String hash(String scopedRaw) {
        Objects.requireNonNull(scopedRaw, "scopedRaw");
        Mac mac = macThreadLocal.get();
        try {
            mac.init(keySpec);
            byte[] out = mac.doFinal(scopedRaw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
