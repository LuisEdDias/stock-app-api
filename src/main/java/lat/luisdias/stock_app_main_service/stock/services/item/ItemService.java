package lat.luisdias.stock_app_main_service.stock.services.item;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.stock.dto.Item.*;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;
import lat.luisdias.stock_app_main_service.stock.entities.item.Item;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemModel;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemStatus;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemRepository;
import lat.luisdias.stock_app_main_service.stock.services.box.BoxService;
import lat.luisdias.stock_app_main_service.stock.services.inventory.InventoryService;
import lat.luisdias.stock_app_main_service.stock.services.itemModel.ItemModelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemIntegrityService itemIntegrityService;
    private final InventoryService inventoryService;
    private final ItemModelService itemModelService;
    private final BoxService boxService;

    public ItemService(
            ItemRepository itemRepository,
            ItemIntegrityService itemIntegrityService,
            InventoryService inventoryService,
            ItemModelService itemModelService,
            BoxService boxService
    ) {
        this.itemRepository = itemRepository;
        this.itemIntegrityService = itemIntegrityService;
        this.inventoryService = inventoryService;
        this.itemModelService = itemModelService;
        this.boxService = boxService;
    }

    public List<GetItemProjection> findAll() {
        return itemRepository.findAllWithProjection();
    }

    public List<GetItemProjection> findAllByInventory(Long inventoryId) {
        return itemRepository.findAllWithProjectionByInventoryId(inventoryId);
    }

    public GetItemProjection findById(Long itemId) {
        GetItemProjection itemProjection = itemRepository.findByIdWithProjection(itemId);
        if (itemProjection == null) {
            throw new EntityNotFoundException(I18n.get("exception.not_found"));
        }
        return itemProjection;
    }

    @Transactional
    public Item create(StoreItemDTO itemDTO) {
        itemIntegrityService.ensureNotRegistered(itemDTO.id());
        Item item = new Item(
                itemDTO,
                itemModelService.getById(itemDTO.modelId()),
                inventoryService.getById(itemDTO.inventoryId())
        );

        if (itemDTO.boxId() != null) {
            Box box = boxService.getBoxById(itemDTO.boxId());
            if (Objects.equals(box.getInventory().getId(), itemDTO.inventoryId())) {
                item.moveToBox(box);
            } else {
                throw new IllegalArgumentException(I18n.get("exception.inventory.not_match"));
            }
        }
        return itemRepository.save(item);
    }

    @Transactional
    public List<Item> createAll(List<StoreItemDTO> itemDTOs) {
        Set<Long> itemIdSet = new HashSet<>(
                itemDTOs.stream()
                        .map(StoreItemDTO::id)
                        .filter(Objects::nonNull)
                        .toList()
        );

        if (itemIdSet.isEmpty()) {throw new IllegalArgumentException(I18n.get("validation.not_empty"));}

        if (itemIdSet.size() != itemDTOs.size()) {throw new IllegalArgumentException(I18n.get("exception.duplicated_id"));}

        List<String> alreadyRegistered = itemRepository.findAllById(itemIdSet)
                .stream()
                .map(item -> item.getId().toString())
                .toList();

        if (!alreadyRegistered.isEmpty()) {
            throw new IllegalArgumentException(
                    I18n.get(
                            "exception.id_already_registered.range",
                            String.join(", ", alreadyRegistered)
                    )
            );
        }

        Map<Long, ItemModel> models = itemModelService.getAllById(
                itemDTOs.stream().map(StoreItemDTO::modelId).distinct().toList()
        ).stream().collect(Collectors.toMap(ItemModel::getId, itemModel -> itemModel, (a, b) -> a));
        if (models.isEmpty()) {throw new IllegalArgumentException(I18n.get("validation.not_found"));}

        Map<Long, Inventory> inventories = inventoryService.getAllById(
                itemDTOs.stream().map(StoreItemDTO::inventoryId).distinct().toList()
        ).stream().collect(Collectors.toMap(Inventory::getId, inventory -> inventory, (a, b) -> a));
        if (inventories.isEmpty()) {throw new IllegalArgumentException(I18n.get("validation.not_found"));}

        Map<Long, Box> boxes = boxService.getAllById(
                itemDTOs.stream().map(StoreItemDTO::boxId).filter(Objects::nonNull).distinct().toList()
        ).stream().collect(Collectors.toMap(Box::getId, box -> box, (a, b) -> a));

        List<Item> itemsToSave = new ArrayList<>();
        for (StoreItemDTO dto : itemDTOs) {
            ItemModel model = models.get(dto.modelId());
            Inventory inventory = inventories.get(dto.inventoryId());
            if (model == null || inventory == null) {
                throw new EntityNotFoundException(I18n.get("exception.not_found"));
            }

            Item item = new Item(dto, model, inventory);

            if (dto.boxId() != null) {
                Box box = boxes.get(dto.boxId());
                if (!Objects.equals(box.getInventory().getId(), dto.inventoryId())) {
                    throw new IllegalArgumentException(I18n.get("exception.inventory.not_match"));
                }
                item.moveToBox(box);
            }

            itemsToSave.add(item);
        }
        return itemRepository.saveAll(itemsToSave);
    }

    @Transactional
    public List<Long> moveItem(ItemIdListDTO idListDTO, Long inventoryId) {
        Inventory inventory = inventoryService.getById(inventoryId);
        List<Item> items = itemIntegrityService.findAllOrThrow(idListDTO.items());
        List<String> boxedItemsIds =  items.stream()
                .filter(item -> item.getBox() != null)
                .map(item -> item.getBox().getId().toString())
                .toList();

        if (!boxedItemsIds.isEmpty()) {
            throw new IllegalArgumentException(I18n.get("exception.item.move_item_boxed", String.join(", ", boxedItemsIds)));
        }

        items.forEach(item -> item.moveToInventory(inventory));
        itemRepository.saveAll(items);
        return items.stream().map(Item::getId).collect(Collectors.toList());
    }

    @Transactional
    public GetItemDTO updateModel(Long itemId, UpdateItemModelDTO itemModelDTO) {
        Item item = itemIntegrityService.findByIdOrThrow(itemId);
        ItemModel model = itemModelService.getById(itemModelDTO.newModelId());
        item.updateModel(model);
        return new GetItemDTO(itemRepository.saveAndFlush(item));
    }

    @Transactional
    public GetItemDTO updateComment(Long itemId, String comment) {
        Item item = itemIntegrityService.findByIdOrThrow(itemId);
        item.updateComment(comment);
        return new GetItemDTO(itemRepository.saveAndFlush(item));
    }

    @Transactional
    public GetItemDTO updateStatus(Long itemId, UpdateItemStatusDTO statusDTO) {
        Item item = itemIntegrityService.findByIdOrThrow(itemId);
        item.updateStatus(statusDTO.status());
        return new GetItemDTO(itemRepository.saveAndFlush(item));
    }

    @Transactional
    public Item setAvailable(Long itemId, boolean available) {
        Item item = itemIntegrityService.findByIdOrThrow(itemId);
        if (available) {
            item.setAvailable();
        } else {
            item.setUnavailable();
        }
        return itemRepository.save(item);
    }

    public Page<GetItemProjection> filterItems(
            Long itemModelId,
            Long boxId,
            Long inventoryId,
            ItemStatus status,
            Boolean available,
            Long fromItemId,
            Long toItemId,
            Timestamp fromUpdated,
            Timestamp toUpdated,
            Pageable pageable
    ) {
        return itemRepository.findFiltered(
                itemModelId,
                boxId,
                inventoryId,
                status,
                available,
                fromItemId,
                toItemId,
                fromUpdated,
                toUpdated,
                pageable
        );
    }
}
