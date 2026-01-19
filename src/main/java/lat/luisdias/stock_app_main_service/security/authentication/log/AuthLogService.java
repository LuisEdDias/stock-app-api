package lat.luisdias.stock_app_main_service.security.authentication.log;

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
