package lat.luisdias.stock_app_main_service.stock.services.inventory;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.stock.dto.address.StoreAddressDTO;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.GetInventoryBasicProjection;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.InventoryOverviewStatsProjection;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.StoreInventoryDTO;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;
import lat.luisdias.stock_app_main_service.stock.entities.vo.Address;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public List<GetInventoryBasicProjection> getAll() {
        return inventoryRepository.getInventoryBasicProjection();
    }

    @Transactional
    public List<Inventory> getAllById(List<Long> ids) {
        return inventoryRepository.findAllById(ids);
    }

    @Transactional
    public Inventory getById(Long id) {
        return inventoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    @Transactional
    public Inventory create(StoreInventoryDTO inventoryDTO) {
        Inventory inventory = new Inventory(inventoryDTO);
        alreadyRegistered(inventory.getName());
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory setAddress(Long inventoryId, StoreAddressDTO addressDTO) {
        Inventory inventory = getById(inventoryId);
        inventory.setAddress(new Address(addressDTO));
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public void activate(Long inventoryId) {
        Inventory inventory = getById(inventoryId);
        inventory.activate();
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void deactivate(Long inventoryId) {
        Inventory inventory = getById(inventoryId);
        inventory.deactivate();
        inventoryRepository.save(inventory);
    }

    @Transactional
    public InventoryOverviewStatsProjection getInventoryOverviewStats() {
        return inventoryRepository.getItemOverviewStatsProjection();
    }

    private void alreadyRegistered(String name) {
        if (inventoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(I18n.get("exception.name_already_registered"));
        }
    }
}
