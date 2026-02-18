package lat.luisdias.stockapp.modules.identity.domain.model;

/**
 * Defines the available strategies for Multi-Factor Authentication.
 * <p>Each type dictates how the security challenge is delivered and validated,
 * impacting both the user experience and the security model of the identity flow.</p>
 */
public enum UserMfaType {
    /**
     * Time-based One-Time Password (RFC 6238).
     */
    TOTP,

    /**
     * Delivery of a verification code via an authenticated email address.
     */
    EMAIL,

    /**
     * Delivery of a verification code via SMS. Requires a verified mobile number in E.164 format.
     */
    SMS
}
