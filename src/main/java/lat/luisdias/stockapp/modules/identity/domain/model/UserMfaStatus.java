package lat.luisdias.stockapp.modules.identity.domain.model;

/**
 * Represents the lifecycle stages of a user's MFA method.
 * <p>The typical lifecycle flow is:
 * {@code PENDING} &rarr; {@code ACTIVE} &rarr; {@code DISABLED}.</p>
 */
public enum UserMfaStatus {
    /**
     * The method has been created and configured but not yet verified.
     * <p>Challenges can be sent, but the method cannot be used for
     * final authentication until it is activated.</p>
     */
    PENDING,

    /**
     * The method is verified and fully operational.
     * <p>This status indicates that the user has successfully proven
     * ownership of the delivery channel or secret.</p>
     */
    ACTIVE,

    /**
     * The method has been manually or automatically deactivated.
     * <p>Disabled methods are kept for audit purposes but are
     * ignored during authentication challenges.</p>
     */
    DISABLED
}
