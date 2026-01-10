package lat.luisdias.stock_app_main_service.stock.controllers.item;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.*;
import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemModelLog;
import lat.luisdias.stock_app_main_service.stock.dto.itemModelImg.DeleteItemModelImgDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemModelImg.StoreItemModelImgDTO;
import lat.luisdias.stock_app_main_service.stock.services.itemModel.ItemModelLogService;
import lat.luisdias.stock_app_main_service.stock.services.itemModel.ItemModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("v1/item-model")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemModelController {
    private final ItemModelService itemModelService;
    private final ItemModelLogService itemModelLogService;
    private static final Logger logger = LoggerFactory.getLogger(ItemModelController.class);

    public ItemModelController(
            ItemModelService itemModelService,
            ItemModelLogService itemModelLogService
    ) {
        this.itemModelService = itemModelService;
        this.itemModelLogService = itemModelLogService;
    }

    @GetMapping
    public ResponseEntity<List<GetItemModelDTO>> getAll(@AuthenticationPrincipal User user) {
        logger.info("User ID {}( {} ) GOT ALL ITEM MODELS", user.getId(), user.getNickname());
        List<GetItemModelDTO> itemModelDTOList = itemModelService.getAll()
                .stream()
                .map(GetItemModelDTO::new)
                .toList();
        return ResponseEntity.ok(itemModelDTOList);
    }

    @GetMapping("/reference-list")
    public ResponseEntity<List<GetModelReferenceProjection>> getAllModelReference(@AuthenticationPrincipal User user) {
        logger.info("USER {}( {} ) GOT ALL MODEL REFERENCE LIST", user.getId(), user.getNickname());
        return ResponseEntity.ok(itemModelService.getAllModelReferences());
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<GetItemModelWithImagesDTO> getById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long modelId,
            @AuthenticationPrincipal User user
    ) {
        GetItemModelWithImagesDTO itemModel = new GetItemModelWithImagesDTO(itemModelService.getById(modelId));
        logger.info("User ID {}( {} ) GOT the ITEM MODEL {}",
                user.getId(),
                user.getNickname(),
                modelId
        );
        return ResponseEntity.ok(itemModel);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Transactional
    public ResponseEntity<GetItemModelDTO> create(
            @ModelAttribute @Valid StoreItemModelDTO storeItemModelDTO,
            @AuthenticationPrincipal User user,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        GetItemModelDTO itemModel = new GetItemModelDTO(itemModelService.create(storeItemModelDTO));
        URI uri = uriComponentsBuilder.path("/v1/item-itemModel/{id}").buildAndExpand(itemModel.id()).toUri();
        logger.info("User ID {}( {} ) CREATED the ITEM MODEL {} with name {}",
                user.getId(),
                user.getNickname(),
                itemModel.id(),
                itemModel.model()
        );
        entryLog(itemModel.id(), "CREATED ITEM MODEL", user);
        return ResponseEntity.created(uri).body(itemModel);
    }

    @PostMapping("/{modelId}/image")
    public ResponseEntity<Void> storeImage(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long modelId,
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid StoreItemModelImgDTO imgDTO
    ) {
        itemModelService.storeItemModelImages(modelId, imgDTO);
        logger.info("User ID {}( {} ) SAVED IMAGES for ITEM MODEL {}",
                user.getId(),
                user.getNickname(),
                modelId
        );
        entryLog(modelId, "IMAGES STORED", user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{modelId}")
    @Transactional
    public ResponseEntity<GetItemModelDTO> update(
            @RequestBody @Valid UpdateItemModelDTO updateItemModelDTO,
            @AuthenticationPrincipal User user,
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long modelId
    ) {
        GetItemModelDTO itemModel = new GetItemModelDTO(itemModelService.update(modelId, updateItemModelDTO));
        logger.info("User ID {}( {} ) UPDATED the ITEM MODEL {}",
                user.getId(),
                user.getNickname(),
                modelId
        );
        entryLog(modelId, "UPDATED ITEM MODEL", user);
        return ResponseEntity.ok(itemModel);
    }

    @PutMapping("/{modelId}/activate")
    @Transactional
    public ResponseEntity<Void> activate(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long modelId,
            @AuthenticationPrincipal User user
    ) {
        itemModelService.activate(modelId);
        logger.info("User ID {}( {} ) has ACTIVATED ITEM MODEL {}",
                user.getId(),
                user.getNickname(),
                modelId
        );
        entryLog(modelId, "ACTIVATED ITEM MODEL", user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{modelId}/deactivate")
    @Transactional
    public ResponseEntity<Void> deactivate(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long modelId,
            @AuthenticationPrincipal User user
    ) {
        itemModelService.deactivate(modelId);
        logger.info("User ID {}( {} ) has DEACTIVATED ITEM MODEL {}",
                user.getId(),
                user.getNickname(),
                modelId
        );
        entryLog(modelId, "DEACTIVATED ITEM MODEL", user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{modelId}/image")
    @Transactional
    public ResponseEntity<Void> deleteImage(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long modelId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid DeleteItemModelImgDTO deleteImgDTO
    ) {
        List<Long> deletedIds = itemModelService.deleteItemModelImages(modelId, deleteImgDTO);
        logger.info("User ID {}( {} ) DELETED ITEM IMAGE {} from ITEM MODEL {}",
                user.getId(),
                user.getNickname(),
                deletedIds,
                modelId
        );
        entryLog(modelId, "DELETED ITEM IMAGE " + deletedIds, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ItemModelStatisticsProjection>> getStats() {
        return ResponseEntity.ok(itemModelService.getItemModelStatistics());
    }

    private void entryLog(Long itemModelId, String action, User user) {
        itemModelLogService.save(
                new ItemModelLog.Builder()
                        .userId(user.getId())
                        .userNickname(user.getNickname())
                        .itemModelId(itemModelId)
                        .action(action)
                        .build()
        );
    }
}
