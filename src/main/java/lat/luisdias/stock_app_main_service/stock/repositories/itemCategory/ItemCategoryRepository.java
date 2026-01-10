package lat.luisdias.stock_app_main_service.stock.repositories.itemCategory;

import lat.luisdias.stock_app_main_service.stock.entities.item.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    Optional<ItemCategory> findByName(String name);
}
