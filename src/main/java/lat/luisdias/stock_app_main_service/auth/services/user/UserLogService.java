package lat.luisdias.stock_app_main_service.auth.services.user;

import lat.luisdias.stock_app_main_service.auth.entities.logs.UserLog;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserLogRepository;
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
