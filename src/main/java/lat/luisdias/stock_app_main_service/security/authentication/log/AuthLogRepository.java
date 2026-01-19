package lat.luisdias.stock_app_main_service.security.authentication.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLogRepository extends JpaRepository<AuthLog, Long> {
}
