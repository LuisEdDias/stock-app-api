package lat.luisdias.stock_app_main_service.stock.dto.inventory;

public interface InventoryOverviewStatsProjection {
    Long getTotalInventory();
    Long getActiveInventory();
    Long getInactiveInventory();
}
