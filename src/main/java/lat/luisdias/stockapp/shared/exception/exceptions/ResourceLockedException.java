package lat.luisdias.stockapp.shared.exception.exceptions;

import java.time.Instant;

public class ResourceLockedException extends RuntimeException {
    private final String messageKey;
    private final Instant lockedAt;

    public ResourceLockedException(String messageKey, Instant lockedAt) {
        super(messageKey);
        this.messageKey = messageKey;
        this.lockedAt = lockedAt;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }
}
