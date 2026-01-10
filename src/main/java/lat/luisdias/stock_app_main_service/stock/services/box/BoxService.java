package lat.luisdias.stock_app_main_service.stock.services.box;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.stock.dto.Item.GetItemBasicDTO;
import lat.luisdias.stock_app_main_service.stock.dto.Item.ItemIdListDTO;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.box.BoxRepository;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemRepository;
import lat.luisdias.stock_app_main_service.stock.dto.box.*;
import lat.luisdias.stock_app_main_service.stock.services.inventory.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoxService {
    private final BoxRepository boxRepository;
    private final InventoryService inventoryService;
    private final ItemRepository itemRepository;

    public BoxService(
            BoxRepository boxRepository,
            InventoryService inventoryService,
            ItemRepository itemRepository
    ) {
        this.boxRepository = boxRepository;
        this.inventoryService = inventoryService;
        this.itemRepository = itemRepository;
    }

    public List<GetBoxBasicProjection> getAll() {
        return boxRepository.getAllBoxBasicProjections();
    }

    public List<GetBoxReferenceProjection> getAllReferences() {
        return boxRepository.getAllBoxReferenceProjections();
    }

    @Transactional
    public GetBoxWithItemsDTO getById(Long boxId) {
        return new GetBoxWithItemsDTO(getBoxById(boxId));
    }

    @Transactional
    public Box create(StoreBoxDTO boxDTO) {
        alreadyRegistered(boxDTO.id());
        Inventory inventory = inventoryService.getById(boxDTO.inventoryId());
        Box box = new Box(boxDTO, inventory);
        return boxRepository.save(box);
    }

    @Transactional
    public Box update(Long boxId, UpdateBoxDTO boxDTO) {
        Box box = getBoxById(boxId);
        box.update(boxDTO);
        return boxRepository.save(box);
    }

    @Transactional
    public void move(Long boxId, Long inventoryId) {
        Box box = getBoxById(boxId);
        Inventory inventory = inventoryService.getById(inventoryId);
        box.move(inventory);
        boxRepository.save(box);
    }

    @Transactional
    public void delete(Long boxId) {
        Box box = getBoxById(boxId);
        box.getItems().forEach(item -> item.moveToBox(null));
        itemRepository.saveAll(box.getItems());
        boxRepository.delete(box);
    }

    @Transactional
    public GetBoxCheckDTO boxCheck(Long boxId, ItemIdListDTO itemIdsDTO) {
        Map<Long, GetItemBasicDTO> itemsMap = getBoxById(boxId).getItems().stream()
                .map(GetItemBasicDTO::new)
                .collect(Collectors.toMap(GetItemBasicDTO::id, item -> item));

        Set<Long> idsToCheck = new HashSet<>(itemIdsDTO.items());
        List<Long> unboxedItems = new ArrayList<>();

        idsToCheck.forEach(item -> {
            if (itemsMap.remove(item) == null){
                unboxedItems.add(item);
            }
        });
        return new GetBoxCheckDTO(new ArrayList<>(itemsMap.values()), unboxedItems);
    }

    public Page<GetBoxBasicProjection> filterBoxes(GetFilteredBoxDTO filteredBoxDTO, Pageable pageable) {
        return boxRepository.findFiltered(
                filteredBoxDTO.getInventoryId(),
                filteredBoxDTO.getBoxStatus(),
                filteredBoxDTO.getFromBoxId(),
                filteredBoxDTO.getToBoxId(),
                filteredBoxDTO.getFromUpdated(),
                filteredBoxDTO.getToUpdated(),
                pageable
        );
    }

    public Box getBoxById(Long boxId) {
        return boxRepository.findByIdWithItems(boxId).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    public List<Box> getAllById(List<Long> ids) {
        return boxRepository.findAllById(ids);
    }

    private void alreadyRegistered(Long boxId) {
        if (boxRepository.existsById(boxId)) {
            throw new IllegalArgumentException(I18n.get("exception.id_already_registered"));
        }
    }
}
