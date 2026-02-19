package lat.luisdias.stockapp.shared.exception.exceptions;

import java.time.Instant;

public class TokenRateLimitReachedException extends ResourceLockedException {
    public TokenRateLimitReachedException(String messageKey, Instant lockedAt) {
        super(messageKey, lockedAt);
    }
}
