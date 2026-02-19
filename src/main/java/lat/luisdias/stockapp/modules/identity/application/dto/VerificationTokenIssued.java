package lat.luisdias.stockapp.modules.identity.application.dto;

import java.time.Instant;

public record VerificationTokenIssued(
        String code,
        Instant expiresAt,
        Instant retryAt
) {
}
