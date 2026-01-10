package lat.luisdias.stock_app_main_service.stock.services.inventory;

import lat.luisdias.stock_app_main_service.stock.entities.logs.InventoryLog;
import lat.luisdias.stock_app_main_service.stock.repositories.inventory.InventoryLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryLogService {
    private final InventoryLogRepository inventoryLogRepository;

    public InventoryLogService(InventoryLogRepository inventoryLogRepository) {
        this.inventoryLogRepository = inventoryLogRepository;
    }

    public void save(InventoryLog inventoryLog) {
        inventoryLogRepository.save(inventoryLog);
    }

    public List<InventoryLog> getAll() {
        return inventoryLogRepository.findAll();
    }
}
