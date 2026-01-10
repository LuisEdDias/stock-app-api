package lat.luisdias.stock_app_main_service.stock.services.item;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemLog;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemLogService {
    private final ItemLogRepository itemLogRepository;

    public ItemLogService(ItemLogRepository itemLogRepository) {
        this.itemLogRepository = itemLogRepository;
    }

    @Async
    public void save(ItemLog itemLog) {
        itemLogRepository.save(itemLog);
    }

    @Async
    public void saveAll(List<ItemLog> itemLogs) {
        itemLogRepository.saveAll(itemLogs);
    }

    public List<ItemLog> findById(Long itemId) {
        return itemLogRepository.findAllByItemId(itemId);
    }
}
