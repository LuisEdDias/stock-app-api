package lat.luisdias.stock_app_main_service.stock.dto.Item;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemStatus;
import lat.luisdias.stock_app_main_service.stock.infra.util.TimestampUtil;

import java.sql.Timestamp;

public class GetFilteredItemDTO {
    private Long itemModelId;
    private Long boxId;
    private Long inventoryId;
    private ItemStatus status;
    private Boolean available;
    private Long fromItemId;
    private Long toItemId;
    private Timestamp fromUpdated;
    private Timestamp toUpdated;

    public Long getItemModelId() {
        return itemModelId;
    }

    public Long getBoxId() {
        return boxId;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public Boolean getAvailable() {
        return available;
    }

    public Long getFromItemId() {
        return fromItemId;
    }

    public Long getToItemId() {
        return toItemId;
    }

    public Timestamp getFromUpdated() {
        return fromUpdated;
    }

    public Timestamp getToUpdated() {
        return toUpdated;
    }

    public void setItemModelId(@Min(value = 1, message = "{validation.only_positive_number}") Long itemModelId) {
        this.itemModelId = itemModelId;
    }

    public void setBoxId(@Min(value = 1, message = "{validation.only_positive_number}") Long boxId) {
        this.boxId = boxId;
    }

    public void setInventoryId(@Min(value = 1, message = "{validation.only_positive_number}") Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public void setFromItemId(@Min(value = 1, message = "{validation.only_positive_number}") Long fromItemId) {
        this.fromItemId = fromItemId;
    }

    public void setToItemId(@Min(value = 1, message = "{validation.only_positive_number}") Long toItemId) {
        this.toItemId = toItemId;
    }

    public void setFromUpdated(String fromUpdated) {
        this.fromUpdated = TimestampUtil.toTimestampFromLocalDateTime(fromUpdated);
    }

    public void setToUpdated(String toUpdated) {
        this.toUpdated = TimestampUtil.toTimestampFromLocalDateTime(toUpdated);
    }

    @AssertTrue(message = "{validation.item.id_range}")
    public boolean isValidItemIdRange() {
        return fromItemId == null || toItemId == null || fromItemId <= toItemId;
    }

    @AssertTrue(message = "{validation.date.date_range}")
    public boolean isValidDateRange() {
        return fromUpdated == null || toUpdated == null || !fromUpdated.after(toUpdated);
    }
}
