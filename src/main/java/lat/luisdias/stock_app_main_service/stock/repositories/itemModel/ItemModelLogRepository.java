package lat.luisdias.stock_app_main_service.stock.repositories.itemModel;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemModelLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemModelLogRepository extends JpaRepository<ItemModelLog, Long> {
}
