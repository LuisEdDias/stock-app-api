package lat.luisdias.stock_app_main_service.stock.services.boxItem;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.stock.dto.Item.ItemIdListDTO;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;
import lat.luisdias.stock_app_main_service.stock.entities.item.Item;
import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemLog;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.box.BoxRepository;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemRepository;
import lat.luisdias.stock_app_main_service.stock.services.item.ItemIntegrityService;
import lat.luisdias.stock_app_main_service.stock.services.item.ItemLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BoxItemService {
    private final BoxRepository boxRepository;
    private final ItemIntegrityService itemIntegrityService;
    private final ItemLogService itemLogService;
    private final ItemRepository itemRepository;

    public BoxItemService(
            BoxRepository boxRepository,
            ItemIntegrityService itemIntegrityService,
            ItemLogService itemLogService,
            ItemRepository itemRepository) {
        this.boxRepository = boxRepository;
        this.itemIntegrityService = itemIntegrityService;
        this.itemLogService = itemLogService;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Box addItems(Long boxId, ItemIdListDTO itemIdListDTO, User user) {
        Box box = getBoxById(boxId);
        List<Item> items = itemIntegrityService.findAllOrThrow(itemIdListDTO.items());

        validateBoxing(box, items);
        items.forEach(item -> {
            item.moveToBox(box);
            box.addItem(item);
        });
        itemRepository.saveAll(items);
        List<ItemLog> itemLogs = items.stream().map(item ->
                getItemLog(item.getId(),
                        "ADDED TO BOX ID " + boxId,
                        itemIdListDTO.comment(),
                        user
                )
        ).toList();
        entryLog(itemLogs);
        return box;
    }

    @Transactional
    public Box removeItems(Long boxId, ItemIdListDTO itemIdListDTO, User user) {
        Box box = getBoxById(boxId);
        if (itemIdListDTO.items().isEmpty()) {
            throw new EntityNotFoundException(I18n.get("validation.not_empty"));
        }
        Set<Long> idsToRemove = new HashSet<>(itemIdListDTO.items());
        List<Item> itemsToRemove = box.getItems().stream().filter(item -> idsToRemove.contains(item.getId())).toList();
        List<Long> missingIds = idsToRemove.stream()
                .filter(id -> itemsToRemove.stream().noneMatch(item -> item.getId().equals(id)))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException(I18n.get("exception.not_found.range", String.join(", ", missingIds.toString())));
        }
        itemsToRemove.forEach(item -> item.moveToBox(null));
        itemRepository.saveAll(itemsToRemove);
        List<ItemLog> itemLogs = itemsToRemove.stream().map(item ->
            getItemLog(item.getId(),
                    "REMOVED from BOX ID " + boxId,
                    itemIdListDTO.comment(),
                    user
            )
        ).toList();
        entryLog(itemLogs);
        return box;
    }

    @Transactional
    public Box moveItemsToBox(Long fromBoxId, Long toBoxId, ItemIdListDTO itemIdListDTO, User user) {
        Box from = getBoxById(fromBoxId);
        Box to = getBoxById(toBoxId);
        if (!from.getInventory().equals(to.getInventory())) {
            throw new IllegalArgumentException(I18n.get("exception.inventory.not_match"));
        }
        if (itemIdListDTO.items().isEmpty()) {
            throw new EntityNotFoundException(I18n.get("validation.not_empty"));
        }
        Set<Long> idsToMove = new HashSet<>(itemIdListDTO.items());
        List<Item> itemsToMove = from.getItems().stream()
                .filter(item -> idsToMove.contains(item.getId()))
                .toList();
        List<Long> missingIds = idsToMove.stream()
                .filter(id -> itemsToMove.stream().noneMatch(item -> item.getId().equals(id)))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException(I18n.get("exception.not_found.range", String.join(", ", missingIds.toString())));
        }
        itemsToMove.forEach(item -> item.moveToBox(to));
        itemRepository.saveAll(itemsToMove);
        List<ItemLog> itemLogs = itemsToMove.stream().map(item ->
                getItemLog(item.getId(),
                        "MOVED from BOX ID " + fromBoxId + " to BOX ID " + toBoxId,
                        itemIdListDTO.comment(),
                        user
                )
        ).toList();
        entryLog(itemLogs);
        return from;
    }

    private void validateBoxing(Box box, List<Item> items) {
        List<String> alreadyBoxed = items.stream()
                .filter(item -> item.getBox() != null)
                .map(item -> item.getId().toString())
                .toList();

        if (!alreadyBoxed.isEmpty()) {
            throw new IllegalArgumentException(I18n.get("exception.item.already_boxed", String.join(", ", alreadyBoxed)));
        }

        List<String> inventoryMismatch = items.stream()
                .filter(item -> !item.getInventory().equals(box.getInventory()))
                .map(item -> item.getId().toString())
                .toList();

        if (!inventoryMismatch.isEmpty()) {
            throw new IllegalArgumentException(I18n.get("exception.item.box_inventory.not_match", String.join(", ", inventoryMismatch)));
        }
    }

    private Box getBoxById(Long boxId) {
        return boxRepository.findByIdWithItems(boxId).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    private void entryLog(List<ItemLog> logs) {
        itemLogService.saveAll(logs);
    }

    private ItemLog getItemLog(Long itemId, String action, String userComment, User user) {
        return new ItemLog.Builder()
                .userId(user.getId())
                .userNickname(user.getNickname())
                .itemId(itemId)
                .action(action)
                .userComment(userComment).build();
    }
}
