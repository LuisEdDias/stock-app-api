package lat.luisdias.stock_app_main_service.stock.dto.Item;

public interface ItemOverviewStatsProjection {
    Long getTotalItems();
    Long getAvailableItems();
    Long getUnavailableItems();
    Long getTestedOk();
    Long getToTest();
    Long getFaulty();
}
