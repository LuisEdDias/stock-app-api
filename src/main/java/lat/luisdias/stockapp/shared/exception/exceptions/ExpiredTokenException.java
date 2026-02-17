package lat.luisdias.stockapp.shared.exception.exceptions;

public class ExpiredTokenException extends RuntimeException {
    private final String messageKey;

    public ExpiredTokenException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
