package lat.luisdias.stock_app_main_service.security.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class KeyUtil {
    private final Logger logger;
    @Value("${app.security.jwt.key.private}")
    private String privateKey;

    @Value("${app.security.jwt.key.public}")
    private String publicKey;

    public KeyUtil() {
        this.logger = LoggerFactory.getLogger(KeyUtil.class);
    }

    public RSAPrivateKey loadPrivateKey() throws RuntimeException {
        try {
            String key = new String(Files.readAllBytes(Paths.get(privateKey)));
            key = key.replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            logger.error("Error loading private key: {}", e.getMessage());
            throw new InternalError();
        }
    }

    public RSAPublicKey loadPublicKey() throws RuntimeException {
        try {
            String key = new String(Files.readAllBytes(Paths.get(publicKey)));
            key = key.replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            logger.error("Error loading public key: {}", e.getMessage());
            throw new InternalError();
        }
    }
}
