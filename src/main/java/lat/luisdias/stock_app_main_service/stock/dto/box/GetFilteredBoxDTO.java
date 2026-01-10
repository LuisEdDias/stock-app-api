package lat.luisdias.stock_app_main_service.stock.dto.box;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lat.luisdias.stock_app_main_service.stock.entities.box.BoxStatus;
import lat.luisdias.stock_app_main_service.stock.infra.util.TimestampUtil;

import java.sql.Timestamp;

public class GetFilteredBoxDTO {
    private Long inventoryId;
    private BoxStatus boxStatus;
    private Long fromBoxId;
    private Long toBoxId;
    private Timestamp fromUpdated;
    private Timestamp toUpdated;

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(@Min(value = 1, message = "{validation.only_positive_number}") Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public BoxStatus getBoxStatus() {
        return boxStatus;
    }

    public void setBoxStatus(BoxStatus boxStatus) {
        this.boxStatus = boxStatus;
    }

    public Long getFromBoxId() {
        return fromBoxId;
    }

    public void setFromBoxId(@Min(value = 1, message = "{validation.only_positive_number}") Long fromBoxId) {
        this.fromBoxId = fromBoxId;
    }

    public Long getToBoxId() {
        return toBoxId;
    }

    public void setToBoxId(@Min(value = 1, message = "{validation.only_positive_number}") Long toBoxId) {
        this.toBoxId = toBoxId;
    }

    public Timestamp getFromUpdated() {
        return fromUpdated;
    }

    public void setFromUpdated(String fromUpdated) {
        this.fromUpdated = TimestampUtil.toTimestampFromLocalDateTime(fromUpdated);
    }

    public Timestamp getToUpdated() {
        return toUpdated;
    }

    public void setToUpdated(String toUpdated) {
        this.toUpdated = TimestampUtil.toTimestampFromLocalDateTime(toUpdated);
    }

    @AssertTrue(message = "{validation.item.id_range}")
    public boolean isValidItemIdRange() {
        return fromBoxId == null || toBoxId == null || fromBoxId <= toBoxId;
    }

    @AssertTrue(message = "{validation.date.date_range}")
    public boolean isValidDateRange() {
        return fromUpdated == null || toUpdated == null || !fromUpdated.after(toUpdated);
    }
}
