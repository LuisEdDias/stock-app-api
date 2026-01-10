package lat.luisdias.stock_app_main_service.stock.repositories.box;

import lat.luisdias.stock_app_main_service.stock.entities.logs.BoxLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoxLogRepository extends JpaRepository<BoxLog, Long> {
}
