package lat.luisdias.stock_app_main_service.stock.repositories.inventory;

import lat.luisdias.stock_app_main_service.stock.entities.logs.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
}
