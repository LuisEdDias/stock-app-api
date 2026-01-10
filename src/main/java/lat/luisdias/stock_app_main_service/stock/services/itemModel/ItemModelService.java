package lat.luisdias.stock_app_main_service.stock.services.itemModel;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.GetModelReferenceProjection;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.ItemModelStatisticsProjection;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.StoreItemModelDTO;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.UpdateItemModelDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemModelImg.DeleteItemModelImgDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemModelImg.StoreItemModelImgDTO;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemCategory;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemImgLink;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemModel;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.itemCategory.ItemCategoryRepository;
import lat.luisdias.stock_app_main_service.stock.repositories.itemModel.ItemModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ItemModelService {
    private final ItemModelRepository modelRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemModelImgStorageService itemModelImgStorageService;

    public ItemModelService(
            ItemModelRepository modelRepository,
            ItemCategoryRepository categoryRepository,
            ItemModelImgStorageService itemModelImgStorageService
    ) {
        this.modelRepository = modelRepository;
        this.categoryRepository = categoryRepository;
        this.itemModelImgStorageService = itemModelImgStorageService;
    }

    @Transactional
    public List<ItemModel> getAll() {
        return modelRepository.findAllBasicInfo();
    }

    @Transactional
    public List<ItemModel> getAllById(List<Long> ids) {
        return modelRepository.findAllById(ids);
    }

    @Transactional
    public List<GetModelReferenceProjection> getAllModelReferences() {
        return modelRepository.getAllModelReferences();
    }

    @Transactional
    public ItemModel getById(Long id) {
        return modelRepository.findByIdWithImages(id).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    @Transactional
    public ItemModel create(StoreItemModelDTO modelDTO) {
        ItemCategory category = categoryById(modelDTO.categoryId());
        ItemModel itemModel = new ItemModel(modelDTO, category);
        alreadyRegistered(itemModel);
        if (modelDTO.images() != null && !modelDTO.images().isEmpty()){
            List<ItemImgLink> imgLinks = itemModelImgStorageService.save(modelDTO.images());
            imgLinks.forEach(itemModel::addImgLink);
        }
        return modelRepository.save(itemModel);
    }

    @Transactional
    public ItemModel update(Long id, UpdateItemModelDTO modelDTO) {
        ItemModel itemModel = getById(id);
        ItemCategory category = categoryById(modelDTO.categoryId());
        itemModel.update(modelDTO, category);
        alreadyRegistered(itemModel, id);
        return modelRepository.save(itemModel);
    }

    @Transactional
    public void deactivate(Long id) {
        ItemModel itemModel = getById(id);
        itemModel.deactivate();
        modelRepository.save(itemModel);
    }

    @Transactional
    public void activate(Long id) {
        ItemModel itemModel = getById(id);
        itemModel.activate();
        modelRepository.save(itemModel);
    }

    @Transactional
    public void storeItemModelImages(Long modelId, StoreItemModelImgDTO imgDTO) {
        ItemModel itemModel = getById(modelId);
        List<ItemImgLink> imgLinks = itemModelImgStorageService.save(imgDTO.images());
        imgLinks.forEach(itemModel::addImgLink);
        modelRepository.save(itemModel);
    }

    @Transactional
    public List<Long> deleteItemModelImages(Long modelId, DeleteItemModelImgDTO deleteDTO) {
        ItemModel itemModel = getById(modelId);
        List<ItemImgLink> linksToDelete = itemModel.getImgLinks()
                .stream()
                .filter(itemImgLink -> deleteDTO.imgIds().contains(itemImgLink.getId()))
                .toList();
        if (linksToDelete.isEmpty()) {
            throw new EntityNotFoundException(I18n.get("exception.not_found"));
        }
        itemModelImgStorageService.delete(linksToDelete);
        linksToDelete.forEach(itemModel::removeImgLink);
        modelRepository.save(itemModel);
        return linksToDelete.stream().map(ItemImgLink::getId).toList();
    }

    public List<ItemModelStatisticsProjection> getItemModelStatistics() {
        return modelRepository.getItemModelStats();
    }

    private ItemCategory categoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    private void alreadyRegistered(ItemModel model){
        alreadyRegistered(model, null);
    }

    private void alreadyRegistered(ItemModel model, Long id) {
        Optional<ItemModel> modelAux = modelRepository.findByModel(model.getModel());
        if (modelAux.isPresent() && !modelAux.get().getId().equals(id)) {
            throw new IllegalArgumentException(I18n.get("exception.name_already_registered"));
        }
    }
}
