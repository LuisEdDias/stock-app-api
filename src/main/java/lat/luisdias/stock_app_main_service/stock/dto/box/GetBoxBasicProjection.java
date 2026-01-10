package lat.luisdias.stock_app_main_service.stock.dto.box;

import java.sql.Timestamp;

public interface GetBoxBasicProjection {
    Long getId();
    String getName();
    InventoryDTO getInventory();
    String getDescription();
    String getStatus();
    Timestamp getCreated();
    Timestamp getUpdated();

    interface InventoryDTO{
        Long getId();
        String getName();
    }
}
