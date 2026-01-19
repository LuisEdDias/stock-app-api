package lat.luisdias.stock_app_main_service.security.authentication.two.factor.auth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorAuthService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public void isCodeValid(String secret, int code) {
        if (!gAuth.authorize(secret, code)) {
            throw new BadCredentialsException(I18n.get("exception.authentication_credentials"));
        }
    }
}