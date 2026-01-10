package lat.luisdias.stock_app_main_service.stock.dto.Item;

public interface ItemCountByModelProjection {
    String getModel();
    Long getModelId();
    Long getCount();
}
