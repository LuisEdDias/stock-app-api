package lat.luisdias.stock_app_main_service.stock.controllers.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemStatus;
import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemLog;
import lat.luisdias.stock_app_main_service.stock.dto.Item.*;
import lat.luisdias.stock_app_main_service.stock.services.item.ItemLogService;
import lat.luisdias.stock_app_main_service.stock.services.item.ItemService;
import lat.luisdias.stock_app_main_service.stock.services.item.ItemStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("v1/item")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemController {
    private final ItemService itemService;
    private final ItemStatisticsService itemStatisticsService;
    private final ItemLogService itemLogService;
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    public ItemController(
            ItemService itemService,
            ItemLogService itemLogService,
            ItemStatisticsService itemStatisticsService
    ) {
        this.itemService = itemService;
        this.itemLogService = itemLogService;
        this.itemStatisticsService = itemStatisticsService;
    }

    @GetMapping
    public ResponseEntity<List<GetItemProjection>> getAll(@AuthenticationPrincipal User user) {
        logger.info("USER ID {}( {} ) GOT ALL ITEMS",user.getId(), user.getNickname());
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<GetItemProjection> getById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long itemId,
            @AuthenticationPrincipal User user
    ) {
        logger.info("USER ID {}( {} ) GOT ITEM with ID {}",user.getId(), user.getNickname(), itemId);
        return ResponseEntity.ok(itemService.findById(itemId));
    }

    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<List<GetItemProjection>> getByInventoryId(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @AuthenticationPrincipal User user
    ) {
        logger.info(
                "USER ID {}( {} ) GOT ALL ITEMS with INVENTORY ID {}",
                user.getId(),
                user.getNickname(),
                inventoryId
        );
        return ResponseEntity.ok(itemService.findAllByInventory(inventoryId));
    }

    @GetMapping("/item-status")
    public ResponseEntity<ItemStatus[]> getItemStatus(@AuthenticationPrincipal User user) {
        logger.info("USER ID {}( {} ) GOT ITEM STATUS",user.getId(), user.getNickname());
        return ResponseEntity.ok(ItemStatus.values());
    }

    @PostMapping
    public ResponseEntity<GetItemDTO> create(
            @RequestBody @Valid StoreItemDTO storeItemDTO,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        GetItemDTO itemDTO = new GetItemDTO(itemService.create(storeItemDTO));
        URI uri = uriComponentsBuilder.path("v1/item/{id}").buildAndExpand(itemDTO.id()).toUri();
        logger.info("USER ID {}( {} ) CREATED ITEM with ID {}",
                user.getId(),
                user.getNickname(),
                itemDTO.id()
        );
        entryLog(itemDTO.id(), "CREATE ITEM", itemDTO.comment(), user);
        return ResponseEntity.created(uri).body(itemDTO);
    }

    @PostMapping("/create-all")
    public ResponseEntity<List<GetItemDTO>> createAll(
            @RequestBody List<@Valid StoreItemDTO> storeItemDTOs,
            @AuthenticationPrincipal User user
    ) {
        List<GetItemDTO> itemDTOs = itemService.createAll(storeItemDTOs).stream().map(GetItemDTO::new).toList();

        String ids = itemDTOs.stream().map(item -> item.id().toString()).limit(20).collect(
                Collectors.joining(", ")
        );
        if (itemDTOs.size() > 20) ids += ", ...";
        logger.info("USER ID {}( {} ) CREATED ITEMS with IDS {}", user.getId(), user.getNickname(), ids);

        List<ItemLog> logs = itemDTOs.stream().map(
                item -> getItemLog(item.id(), "CREATE ITEM", item.comment(), user)
        ).toList();
        entryLog(logs);

        return ResponseEntity.status(HttpStatus.CREATED).body(itemDTOs);
    }

    @PutMapping("/{itemId}/update-model")
    public ResponseEntity<GetItemDTO> updateModel(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long itemId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateItemModelDTO updateItemModelDTO
    ) {
        GetItemDTO itemDTO = itemService.updateModel(itemId, updateItemModelDTO);
        logger.info(
                "USER ID {}( {} ) UPDATED ITEM with ID {} SET MODEL ID {}",
                user.getId(),
                user.getNickname(),
                itemDTO.id(),
                itemDTO.itemModel().id()
        );
        entryLog(
                itemId,
                "UPDATE ITEM MODEL TO ID " + itemDTO.itemModel().id(),
                updateItemModelDTO.comment(),
                user
        );
        return ResponseEntity.ok(itemDTO);
    }

    @PutMapping("/{itemId}/update-comment")
    public ResponseEntity<GetItemDTO> updateComment(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long itemId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateItemCommentDTO commentDTO
    ) {
        GetItemDTO itemDTO = itemService.updateComment(itemId, commentDTO.comment());
        logger.info(
                "USER ID {}( {} ) UPDATED ITEM with ID {} SET new COMMENT",
                user.getId(),
                user.getNickname(),
                itemId
        );
        entryLog(
                itemId,
                "UPDATE ITEM COMMENT",
                commentDTO.comment(),
                user
        );
        return ResponseEntity.ok(itemDTO);
    }

    @PutMapping("/{itemId}/update-status")
    public ResponseEntity<GetItemDTO> updateStatus(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long itemId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateItemStatusDTO updateItemStatusDTO
    ) {
        GetItemDTO itemDTO = itemService.updateStatus(itemId, updateItemStatusDTO);
        logger.info(
                "USER ID {}( {} ) UPDATED ITEM with ID {} SET STATUS {}",
                user.getId(),
                user.getNickname(),
                itemDTO.id(),
                updateItemStatusDTO.status()
        );
        entryLog(
                itemId,
                "UPDATE ITEM STATUS to " + updateItemStatusDTO.status(),
                updateItemStatusDTO.comment(),
                user
        );
        return ResponseEntity.ok(itemDTO);
    }

    @PutMapping("/move-to-inventory/{inventoryId}")
    public ResponseEntity<Void> moveToInventory(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @RequestBody @Valid ItemIdListDTO idListDTO,
            @AuthenticationPrincipal User user
    ) {
        List<Long> movedItemsId = itemService.moveItem(idListDTO, inventoryId);
        logger.info(
                "USER ID {}( {} ) MOVE ITEMS with IDS {} to INVENTORY ID {}",
                user.getId(),
                user.getNickname(),
                String.join(", ",  movedItemsId.stream().map(Objects::toString).toList()),
                inventoryId
        );
        List<ItemLog> logs = movedItemsId.stream().map(id -> getItemLog(
                id,
                "MOVE ITEM TO INVENTORY ID " + inventoryId,
                idListDTO.comment(),
                user
                )).toList();
        entryLog(logs);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{itemId}/set-available/{available}")
    public ResponseEntity<GetItemDTO> setAvailable(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long itemId,
            @AuthenticationPrincipal User user,
            @PathVariable @NotNull(message = "{validation.not_blank}") boolean available
    ) {
        GetItemDTO itemDTO = new GetItemDTO(itemService.setAvailable(itemId, available));
        logger.info(
                "USER ID {}( {} ) UPDATED ITEM with ID {} SET AVAILABLE {}",
                user.getId(),
                user.getNickname(),
                itemId,
                available
        );
        entryLog(itemId, "UPDATE ITEM SET AVAILABLE " + available, itemDTO.comment(), user);
        return ResponseEntity.ok(itemDTO);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<GetItemProjection>> getFilteredItems(
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid GetFilteredItemDTO filterDTO,
            Pageable pageable
    ){
        Page<GetItemProjection> page = itemService.filterItems(
                filterDTO.getItemModelId(),
                filterDTO.getBoxId(),
                filterDTO.getInventoryId(),
                filterDTO.getStatus(),
                filterDTO.getAvailable(),
                filterDTO.getFromItemId(),
                filterDTO.getToItemId(),
                filterDTO.getFromUpdated(),
                filterDTO.getToUpdated(),
                pageable
        );
        logger.info("USER ID {}( {} ) GOT FILTERED ITEMS", user.getId(), user.getNickname());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{itemId}/logs")
    public ResponseEntity<List<GetItemLogDTO>> getItemLogs(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long itemId,
            @AuthenticationPrincipal User user
    ) {
        logger.info("USER ID {}( {} ) GOT ALL ITEM LOGS from ITEM ID {}",
                user.getId(),
                user.getNickname(),
                itemId
        );
        return ResponseEntity.ok(itemLogService.findById(itemId).stream().map(GetItemLogDTO::new).toList());
    }

    @GetMapping("/stats-overview")
    public ResponseEntity<ItemOverviewStatsProjection> getItemOverviewStats() {
        return ResponseEntity.ok(itemStatisticsService.getItemOverviewStats());
    }

    @GetMapping("/stats-item-by-box")
    public ResponseEntity<List<ItemCountByBoxProjection>> getItemByBoxStats() {
        return ResponseEntity.ok(itemStatisticsService.getItemCountByBox());
    }

    @GetMapping("/stats-item-by-category")
    public ResponseEntity<List<ItemCountByCategoryProjection>> getItemByCategoryStats() {
        return ResponseEntity.ok(itemStatisticsService.getItemCountByCategory());
    }

    @GetMapping("/stats-item-by-inventory")
    public ResponseEntity<List<ItemCountByInventoryProjection>> getItemByInventoryStats() {
        return ResponseEntity.ok(itemStatisticsService.getItemCountByInventory());
    }

    @GetMapping("/stats-item-by-model")
    public ResponseEntity<List<ItemCountByModelProjection>> getItemByModelStats() {
        return ResponseEntity.ok(itemStatisticsService.getItemCountByModel());
    }

    private void entryLog(Long itemId, String action, String userComment, User user) {
        itemLogService.save(
                new ItemLog.Builder()
                        .userId(user.getId())
                        .userNickname(user.getNickname())
                        .itemId(itemId)
                        .action(action)
                        .userComment(userComment).build()
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
