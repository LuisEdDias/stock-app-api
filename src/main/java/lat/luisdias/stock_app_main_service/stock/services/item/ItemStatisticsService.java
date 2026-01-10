package lat.luisdias.stock_app_main_service.stock.services.item;

import lat.luisdias.stock_app_main_service.stock.dto.Item.*;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemStatisticsService {
    private final ItemRepository itemRepository;

    public ItemStatisticsService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemOverviewStatsProjection getItemOverviewStats() {
        return itemRepository.getItemOverviewStatsProjection();
    }

    public List<ItemCountByBoxProjection> getItemCountByBox() {
        return itemRepository.countItemsByBox();
    }

    public List<ItemCountByCategoryProjection> getItemCountByCategory() {
        return itemRepository.countItemsByCategory();
    }

    public List<ItemCountByInventoryProjection> getItemCountByInventory() {
        return itemRepository.countItemsByInventory();
    }

    public List<ItemCountByModelProjection> getItemCountByModel() {
        return itemRepository.countItemsByModel();
    }
}
