package lat.luisdias.stock_app_main_service.security.identity.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {
}
