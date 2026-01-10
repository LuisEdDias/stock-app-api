package lat.luisdias.stock_app_main_service.stock.dto.Item;

import java.sql.Timestamp;

public interface GetItemProjection {
    Long getId();
    String getComment();
    String getStatus();
    boolean isAvailable();
    Timestamp getCreated();
    Timestamp getUpdated();
    InventoryDTO getInventory();
    ItemModelDTO getItemModel();
    BoxDTO getBox();

    interface InventoryDTO {
        Long getId();
        String getName();
    }

    interface ItemModelDTO {
        Long getId();
        String getModel();
    }

    interface BoxDTO {
        Long getId();
        String getName();
    }
}
