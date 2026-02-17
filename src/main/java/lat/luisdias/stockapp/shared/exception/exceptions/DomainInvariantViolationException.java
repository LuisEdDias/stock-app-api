package lat.luisdias.stockapp.shared.exception.exceptions;

public class DomainInvariantViolationException extends RuntimeException {
    private final String messageKey;

    public DomainInvariantViolationException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() { return messageKey; }
}
