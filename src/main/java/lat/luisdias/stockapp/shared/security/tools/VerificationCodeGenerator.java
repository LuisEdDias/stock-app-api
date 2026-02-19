package lat.luisdias.stockapp.shared.security.tools;

import java.security.SecureRandom;

public abstract class VerificationCodeGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int CODE_LENGTH = 6;

    public static String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(randomIndex));
        }
        return sb.toString();
    }
}
