package lat.luisdias.stock_app_main_service.stock.repositories.inventory;

import lat.luisdias.stock_app_main_service.stock.dto.inventory.GetInventoryBasicProjection;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.InventoryOverviewStatsProjection;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    boolean existsByName(String name);

    @Query("SELECT i.id AS id, i.name AS name, i.active AS active FROM Inventory i")
    List<GetInventoryBasicProjection> getInventoryBasicProjection();

    String OVERVIEW_STATS_QUERY = """
                SELECT
                    COUNT(i) AS totalInventory,
                    SUM(CASE WHEN i.active = true THEN 1 ELSE 0 END) AS activeInventory,
                    SUM(CASE WHEN i.active = false THEN 1 ELSE 0 END) AS inactiveInventory
                FROM Inventory i
            """;

    @Query(OVERVIEW_STATS_QUERY)
    InventoryOverviewStatsProjection getItemOverviewStatsProjection();
}
