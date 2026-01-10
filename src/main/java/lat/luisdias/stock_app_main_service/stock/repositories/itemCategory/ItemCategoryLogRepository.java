package lat.luisdias.stock_app_main_service.stock.repositories.itemCategory;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemCategoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCategoryLogRepository extends JpaRepository<ItemCategoryLog, Long> {
}
