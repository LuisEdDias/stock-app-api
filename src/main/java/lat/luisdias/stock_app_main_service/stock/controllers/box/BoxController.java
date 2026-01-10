package lat.luisdias.stock_app_main_service.stock.controllers.box;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.stock.dto.Item.GetFilteredItemDTO;
import lat.luisdias.stock_app_main_service.stock.dto.Item.ItemIdListDTO;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;
import lat.luisdias.stock_app_main_service.stock.entities.box.BoxStatus;
import lat.luisdias.stock_app_main_service.stock.dto.box.*;
import lat.luisdias.stock_app_main_service.stock.entities.logs.BoxLog;
import lat.luisdias.stock_app_main_service.stock.services.box.BoxLogService;
import lat.luisdias.stock_app_main_service.stock.services.box.BoxService;
import lat.luisdias.stock_app_main_service.stock.services.box.ExportBoxService;
import lat.luisdias.stock_app_main_service.stock.services.boxItem.BoxItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@Validated
@RequestMapping("v1/box")
@CrossOrigin(origins = "http://localhost:4200")
public class BoxController {
    private final BoxService boxService;
    private final BoxItemService boxItemService;
    private final ExportBoxService exportBoxService;
    private final BoxLogService boxLogService;
    private final Logger logger = LoggerFactory.getLogger(BoxController.class);

    public BoxController(
            BoxService boxService,
            BoxItemService boxItemService,
            ExportBoxService exportBoxService,
            BoxLogService boxLogService
    ) {
        this.boxService = boxService;
        this.boxItemService = boxItemService;
        this.exportBoxService = exportBoxService;
        this.boxLogService = boxLogService;
    }

    @GetMapping
    public ResponseEntity<List<GetBoxBasicProjection>> getAll(@AuthenticationPrincipal User user) {
        logger.info("USER ID {}( {} ) GOT ALL BOXES", user.getId(), user.getNickname());
        return ResponseEntity.ok(boxService.getAll());
    }

    @GetMapping("/reference-list")
    public ResponseEntity<List<GetBoxReferenceProjection>> getAllReference(@AuthenticationPrincipal User user) {
        logger.info("USER ID {}( {} ) GOT ALL BOX REFERENCES", user.getId(), user.getNickname());
        return ResponseEntity.ok(boxService.getAllReferences());
    }

    @GetMapping("/{boxId}")
    public ResponseEntity<GetBoxWithItemsDTO> getById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @AuthenticationPrincipal User user
    ) {
        logger.info("USER ID {}( {} ) GOT BOX with ID {}", user.getId(), user.getNickname(), boxId);
        return ResponseEntity.ok(boxService.getById(boxId));
    }

    @PostMapping
    public ResponseEntity<GetBoxDTO> create(
            @RequestBody @Valid StoreBoxDTO storeBoxDTO,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal User user
    ) {
        GetBoxDTO boxDTO = new GetBoxDTO(boxService.create(storeBoxDTO));
        URI uri = uriBuilder.path("v1/box/{id}").buildAndExpand(boxDTO.id()).toUri();
        logger.info("USER ID {}( {} ) CREATED BOX ID {}", user.getId(), user.getNickname(), boxDTO.id());
        entryLog(boxDTO.id(), storeBoxDTO.inventoryId(), user.getId(), user.getNickname(), "CREATE BOX");
        return ResponseEntity.created(uri).body(boxDTO);
    }

    @PutMapping("/{boxId}")
    public ResponseEntity<Void> update(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @RequestBody @Valid UpdateBoxDTO updateBoxDTO,
            @AuthenticationPrincipal User user
    ) {
        Box box = boxService.update(boxId, updateBoxDTO);
        logger.info("USER ID {}( {} ) UPDATED BOX {}", user.getId(), user.getNickname(), boxId);
        entryLog(boxId, box.getInventory().getId(), user.getId(), user.getNickname(), "UPDATE BOX");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{boxId}/move")
    public ResponseEntity<Void> move(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @RequestBody @Valid MoveBoxDTO moveBoxDTO,
            @AuthenticationPrincipal User user
    ) {
        boxService.move(boxId, moveBoxDTO.inventoryId());
        logger.info(
                "USER ID {}( {} ) MOVED BOX {} to INVENTORY ID {}",
                user.getId(),
                user.getNickname(),
                boxId,
                moveBoxDTO.inventoryId()
        );
        entryLog(boxId, moveBoxDTO.inventoryId(), user.getId(), user.getNickname(), "MOVE BOX");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{boxId}/add-items")
    public ResponseEntity<Void> addItems(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @RequestBody @Valid ItemIdListDTO itemIdListDTO,
            @AuthenticationPrincipal User user
    ) {
        Box box = boxItemService.addItems(boxId, itemIdListDTO, user);
        String addedItems = String.join(
                ", ",
                itemIdListDTO.items().stream().map(Objects::toString).toList()
        );
        logger.info(
                "USER ID {}( {} ) ADDED ITEMS({}) to BOX ID {}",
                user.getId(),
                addedItems,
                user.getNickname(),
                boxId
        );

        entryLog(
                boxId,
                box.getInventory().getId(),
                user.getId(),
                user.getNickname(),
                "ADD ITEMS(" + addedItems + ")"
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{boxId}/remove-items")
    public ResponseEntity<Void> removeItems(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @RequestBody @Valid ItemIdListDTO itemIdListDTO,
            @AuthenticationPrincipal User user
    ) {
        Box box = boxItemService.removeItems(boxId, itemIdListDTO, user);
        String removedItems = String.join(
                ", ",
                itemIdListDTO.items().stream().map(Objects::toString).toList()
        );
        logger.info(
                "USER ID {}( {} ) REMOVED ITEMS({}) from BOX ID {}",
                user.getId(),
                user.getNickname(),
                removedItems,
                boxId
        );

        entryLog(
                boxId,
                box.getInventory().getId(),
                user.getId(),
                user.getNickname(),
                "REMOVE ITEMS(" + removedItems + ")"
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{oldBoxId}/move-items-to/{newBoxId}")
    public ResponseEntity<Void> moveItemsTo(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long oldBoxId,
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long newBoxId,
            @RequestBody @Valid ItemIdListDTO itemIdListDTO,
            @AuthenticationPrincipal User user
    ) {
        Box old = boxItemService.moveItemsToBox(oldBoxId, newBoxId, itemIdListDTO, user);
        String movedItems = String.join(
                ", ",
                itemIdListDTO.items().stream().map(Objects::toString).toList()
        );
        logger.info(
                "USER ID {}( {} ) MOVED ITEMS({}) from BOX ID {} to BOX ID {}",
                user.getId(),
                user.getNickname(),
                movedItems,
                oldBoxId,
                newBoxId
        );

        entryLog(
                oldBoxId,
                old.getInventory().getId(),
                user.getId(),
                user.getNickname(),
                "REMOVE ITEMS(" + movedItems + ")"
        );

        entryLog(
                newBoxId,
                old.getInventory().getId(),
                user.getId(),
                user.getNickname(),
                "ADD ITEMS(" + movedItems + ")"
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{boxId}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @AuthenticationPrincipal User user
    ) {
        boxService.delete(boxId);
        logger.info("USER ID {}( {} ) DELETED BOX ID {}", user.getId(), user.getNickname(), boxId);
        entryLog(boxId, null, user.getId(), user.getNickname(), "DELETE");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<GetBoxBasicProjection>> getFilteredBoxes(
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid GetFilteredBoxDTO filteredBoxDTO,
            Pageable pageable
    ) {
        Page<GetBoxBasicProjection> page = boxService.filterBoxes(filteredBoxDTO, pageable);
        logger.info("USER ID {}( {} ) GOT FILTERED BOXES", user.getId(), user.getNickname());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/status-list")
    public ResponseEntity<BoxStatus[]> getStatusList(@AuthenticationPrincipal User user) {
        logger.info("USER ID {}( {} ) GET STATUS LIST", user.getId(), user.getNickname());
        return ResponseEntity.ok(BoxStatus.values());
    }

    @GetMapping("{boxId}/export")
    public ResponseEntity<Resource> export(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @AuthenticationPrincipal User user
    ) {
        Resource resource = exportBoxService.exportBox(boxId);

        if (resource == null) {
            logger.info("USER ID {}( {} ) FAIL TO EXPORT BOX ID {}", user.getId(), user.getNickname(), boxId);
            return ResponseEntity.notFound().build();
        }

        String fileName = "CAIXA_" + boxId + ".xlsx";
        logger.info("USER ID {}( {} ) EXPORT BOX ID {}", user.getId(), user.getNickname(), boxId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping("{boxId}/check")
    public ResponseEntity<GetBoxCheckDTO> check(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long boxId,
            @RequestBody ItemIdListDTO itemIdListDTO,
            @AuthenticationPrincipal User user
    ) {
        logger.info("USER ID {}( {} ) CHECK BOX {}", user.getId(), user.getNickname(), boxId);
        return ResponseEntity.ok(boxService.boxCheck(boxId, itemIdListDTO));
    }

    private void entryLog(Long boxId, Long inventoryId, Long userId, String userNickname, String action) {
        boxLogService.save(
                new BoxLog.Builder()
                        .boxId(boxId)
                        .inventoryId(inventoryId)
                        .userId(userId)
                        .userNickname(userNickname)
                        .action(action)
                        .build()
        );
    }
}
