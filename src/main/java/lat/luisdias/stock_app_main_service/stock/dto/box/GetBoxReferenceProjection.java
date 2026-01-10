package lat.luisdias.stock_app_main_service.stock.dto.box;

public interface GetBoxReferenceProjection {
    Long getId();
    String getName();
    Long getInventoryId();
    String getInventoryName();
}
