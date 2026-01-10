package lat.luisdias.stock_app_main_service.stock.controllers.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.stock.dto.address.StoreAddressDTO;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.*;
import lat.luisdias.stock_app_main_service.stock.entities.logs.InventoryLog;
import lat.luisdias.stock_app_main_service.stock.services.inventory.InventoryCheckService;
import lat.luisdias.stock_app_main_service.stock.services.inventory.InventoryLogService;
import lat.luisdias.stock_app_main_service.stock.services.inventory.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("v1/inventory")
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryCheckService inventoryCheckService;
    private final InventoryLogService inventoryLogService;
    private final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    public InventoryController(
            InventoryService inventoryService,
            InventoryCheckService inventoryCheckService,
            InventoryLogService inventoryLogService
    ) {
        this.inventoryService = inventoryService;
        this.inventoryCheckService = inventoryCheckService;
        this.inventoryLogService = inventoryLogService;
    }

    @GetMapping
    public ResponseEntity<List<GetInventoryBasicProjection>> getAll(@AuthenticationPrincipal User user) {
        logger.info("USER ID {}( {} ) GOT ALL INVENTORIES", user.getId(), user.getNickname());
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<GetInventoryDTO> getById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @AuthenticationPrincipal User user
    ) {
        GetInventoryDTO inventoryDTO =new GetInventoryDTO(inventoryService.getById(inventoryId));
        logger.info("USER ID {}( {} ) GOT INVENTORY ID {}", user.getId(), user.getNickname(), inventoryId);
        return ResponseEntity.ok(inventoryDTO);
    }

    @PostMapping
    public ResponseEntity<GetInventoryDTO> create(
            @RequestBody @Valid StoreInventoryDTO inventoryDTO,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal User user
    ) {
        GetInventoryDTO inventory = new GetInventoryDTO(inventoryService.create(inventoryDTO));
        URI uri = uriBuilder.path("v1/inventory/{id}").buildAndExpand(inventory.id()).toUri();
        logger.info(
                "USER ID {}( {} ) CREATED INVENTORY ID {}( {} )",
                user.getId(),
                user.getNickname(),
                inventory.id(),
                inventory.name()
        );
        return ResponseEntity.created(uri).body(inventory);
    }

    @PostMapping("/{inventoryId}/check")
    public ResponseEntity<Resource> check(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @ModelAttribute @Valid InventoryCheckDTO checkDTO,
            @AuthenticationPrincipal User user
    ) {
        Resource resource = inventoryCheckService.checkInventory(inventoryId, checkDTO);
        if (resource == null) {
            logger.warn(
                    "USER ID {}( {} ) CHECK INVENTORY FAIL with INVENTORY ID {}",
                    user.getId(),
                    user.getNickname(),
                    inventoryId
            );
            return ResponseEntity.notFound().build();
        }
        logger.info("USER ID {}( {} ) CHECK INVENTORY ID {}", user.getId(), user.getNickname(), inventoryId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"conferencia.zip\"")
                .body(resource);
    }

    @PutMapping("/{inventoryId}/address")
    public ResponseEntity<GetInventoryDTO> update(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @RequestBody @Valid StoreAddressDTO addressDTO,
            @AuthenticationPrincipal User user
    ) {
        GetInventoryDTO inventoryDTO = new GetInventoryDTO(inventoryService.setAddress(inventoryId, addressDTO));
        logger.info(
                "USER ID {}( {} ) UPDATE INVENTORY ID {} SET ADDRESS",
                user.getId(),
                user.getNickname(),
                inventoryId
        );
        entryLog(user.getId(), user.getNickname(), "SET ADDRESS", inventoryDTO.id());
        return ResponseEntity.ok(inventoryDTO);
    }

    @PutMapping("/{inventoryId}/activate")
    public ResponseEntity<Void> activate(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @AuthenticationPrincipal User user
    ) {
        inventoryService.activate(inventoryId);
        logger.info("USER ID {}( {} ) ACTIVATED INVENTORY ID {}", user.getId(), user.getNickname(), inventoryId);
        entryLog(user.getId(), user.getNickname(), "ACTIVATE INVENTORY", inventoryId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{inventoryId}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long inventoryId,
            @AuthenticationPrincipal User user
    ) {
        inventoryService.deactivate(inventoryId);
        logger.info("USER ID {}( {} ) DEACTIVATED INVENTORY ID {}", user.getId(), user.getNickname(), inventoryId);
        entryLog(user.getId(), user.getNickname(), "DEACTIVATE INVENTORY", inventoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overview-stats")
    public ResponseEntity<InventoryOverviewStatsProjection> overviewStats() {
        return ResponseEntity.ok(inventoryService.getInventoryOverviewStats());
    }

    public void entryLog(Long userId, String userNickname, String action, Long inventoryId){
        inventoryLogService.save(
                new InventoryLog.Builder()
                        .userId(userId)
                        .userNickname(userNickname)
                        .action(action)
                        .inventoryId(inventoryId)
                        .build()
        );
    }
}
