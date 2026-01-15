package lat.luisdias.stock_app_main_service.security.services.auth;

import lat.luisdias.stock_app_main_service.security.entities.logs.AuthLog;
import lat.luisdias.stock_app_main_service.security.repositories.auth.AuthLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthLogService {
    private final AuthLogRepository authLogRepository;

    public AuthLogService(AuthLogRepository authLogRepository) {
        this.authLogRepository = authLogRepository;
    }

    public void save(AuthLog authLog) {
        authLogRepository.save(authLog);
    }

    public List<AuthLog> findAll() {
        return authLogRepository.findAll();
    }
}
