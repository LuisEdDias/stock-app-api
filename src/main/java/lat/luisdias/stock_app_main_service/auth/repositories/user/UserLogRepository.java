package lat.luisdias.stock_app_main_service.auth.repositories.user;

import lat.luisdias.stock_app_main_service.auth.entities.logs.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {
}
