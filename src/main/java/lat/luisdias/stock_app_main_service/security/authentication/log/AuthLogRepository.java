package lat.luisdias.stock_app_main_service.security.repositories.auth;

import lat.luisdias.stock_app_main_service.security.entities.logs.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLogRepository extends JpaRepository<AuthLog, Long> {
}
