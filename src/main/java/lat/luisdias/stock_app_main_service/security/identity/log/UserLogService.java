package lat.luisdias.stock_app_main_service.security.identity.log;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLogService {
    private final UserLogRepository userLogRepository;

    public UserLogService(UserLogRepository userLogRepository) {
        this.userLogRepository = userLogRepository;
    }

    public void save(UserLog userLog) {
        userLogRepository.save(userLog);
    }

    public List<UserLog> findAll() {
        return userLogRepository.findAll();
    }
}
