package lat.luisdias.stockapp.modules.identity.domain.model;

/**
 * Specifies the business context and security scope for a {@link VerificationToken}.
 * <p>Using distinct purposes prevents "cross-purpose reuse" attacks, ensuring
 * that a token generated for one operation cannot be successfully applied
 * to a different, potentially more sensitive one.</p>
 */
public enum VerificationTokenPurpose {
    /**
     * Scope for initial MFA setup and verification.
     */
    MFA_ENABLE,

    /**
     * Scope for authorizing the deactivation of an existing MFA method.
     */
    MFA_DISABLE,

    /**
     * Scope for authenticating a login attempt using an MFA second factor.
     */
    MFA_AUTH
}
