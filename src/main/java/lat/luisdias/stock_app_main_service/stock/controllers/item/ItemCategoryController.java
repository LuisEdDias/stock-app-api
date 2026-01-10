package lat.luisdias.stock_app_main_service.stock.controllers.item;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.GetItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.StoreItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.UpdateItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemCategoryLog;
import lat.luisdias.stock_app_main_service.stock.services.itemCategory.ItemCategoryService;
import lat.luisdias.stock_app_main_service.stock.services.itemCategory.ItemCategoryLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("v1/item-category")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemCategoryController {
    private final ItemCategoryService itemCategoryService;
    private final ItemCategoryLogService logService;
    private static final Logger logger = LoggerFactory.getLogger(ItemCategoryController.class);

    public ItemCategoryController(ItemCategoryService itemCategoryService, ItemCategoryLogService logService) {
        this.itemCategoryService = itemCategoryService;
        this.logService = logService;
    }

    @GetMapping
    public ResponseEntity<List<GetItemCategoryDTO>> getAll(@AuthenticationPrincipal User user) {
        logger.info("User ID {}( {} ) GOT ALL ITEM CATEGORIES", user.getId(), user.getNickname());
        return ResponseEntity.ok(itemCategoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetItemCategoryDTO> getById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long id,
            @AuthenticationPrincipal User user
    ) {
        GetItemCategoryDTO categoryDTO = itemCategoryService.getById(id);
        logger.info("User ID {}( {} ) GOT the ITEM CATEGORY {} with name {}",
                user.getId(),
                user.getNickname(),
                categoryDTO.id(),
                categoryDTO.name()
        );
        return ResponseEntity.ok(itemCategoryService.getById(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<GetItemCategoryDTO> create(
            @RequestBody @Valid StoreItemCategoryDTO storeItemCategoryDTO,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        GetItemCategoryDTO categoryDTO = itemCategoryService.create(storeItemCategoryDTO);
        URI uri = uriComponentsBuilder.path("/v1/item-category/{id}").buildAndExpand(categoryDTO.id()).toUri();
        logger.info("User ID {}( {} ) CREATED the ITEM CATEGORY {} with name {}",
                user.getId(),
                user.getNickname(),
                categoryDTO.id(),
                categoryDTO.name()
        );
        entryLog(categoryDTO, "CREATED ITEM CATEGORY", user);
        return ResponseEntity.created(uri).body(categoryDTO);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<GetItemCategoryDTO> update(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long id,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UpdateItemCategoryDTO updateItemCategoryDTO
    ) {
        GetItemCategoryDTO categoryDTO = itemCategoryService.update(id, updateItemCategoryDTO);
        logger.info("User ID {}( {} ) UPDATED the ITEM CATEGORY {} with name {}",
                user.getId(),
                user.getNickname(),
                id,
                categoryDTO.name()
        );
        entryLog(categoryDTO, "UPDATED ITEM CATEGORY", user);
        return ResponseEntity.ok(categoryDTO);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long id,
            @AuthenticationPrincipal User user
    ) {
        GetItemCategoryDTO categoryDTO = itemCategoryService.delete(id);
        logger.info("User ID {}( {} ) DELETED ITEM CATEGORY {} with name {}",
                user.getId(),
                user.getNickname(),
                id,
                categoryDTO.name()
        );
        entryLog(categoryDTO, "DELETED ITEM CATEGORY", user);
        return ResponseEntity.noContent().build();
    }

    private void entryLog(GetItemCategoryDTO categoryDTO, String action, User user) {
        logService.save(
                new ItemCategoryLog.Builder()
                        .userId(user.getId())
                        .userNickname(user.getNickname())
                        .categoryId(categoryDTO.id())
                        .categoryName(categoryDTO.name())
                        .action(action)
                        .build()
        );
    }
}
