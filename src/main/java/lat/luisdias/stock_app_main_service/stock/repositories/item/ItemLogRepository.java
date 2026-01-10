package lat.luisdias.stock_app_main_service.stock.repositories.item;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemLogRepository extends JpaRepository<ItemLog, Long> {
    @Query("SELECT i FROM ItemLog i WHERE i.itemId = :itemId")
    List<ItemLog> findAllByItemId(@Param("itemId") Long itemId);
}
