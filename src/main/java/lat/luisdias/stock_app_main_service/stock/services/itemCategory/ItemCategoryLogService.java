package lat.luisdias.stock_app_main_service.stock.services.itemCategory;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemCategoryLog;
import lat.luisdias.stock_app_main_service.stock.repositories.itemCategory.ItemCategoryLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemCategoryLogService {
    private final ItemCategoryLogRepository itemCategoryLogRepository;

    public ItemCategoryLogService(ItemCategoryLogRepository itemCategoryLogRepository) {
        this.itemCategoryLogRepository = itemCategoryLogRepository;
    }

    public void save(ItemCategoryLog itemCategoryLog) {
        itemCategoryLogRepository.save(itemCategoryLog);
    }
}
