package lat.luisdias.stock_app_main_service.stock.services.itemModel;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemModelLog;
import lat.luisdias.stock_app_main_service.stock.repositories.itemModel.ItemModelLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemModelLogService {
    private final ItemModelLogRepository itemModelLogRepository;

    public ItemModelLogService(ItemModelLogRepository itemModelLogRepository) {
        this.itemModelLogRepository = itemModelLogRepository;
    }

    public void save(ItemModelLog itemModelLog) {
        itemModelLogRepository.save(itemModelLog);
    }
}
