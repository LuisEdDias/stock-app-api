package lat.luisdias.stock_app_main_service.stock.services.itemCategory;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.GetItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.StoreItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.UpdateItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemCategory;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.itemCategory.ItemCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ItemCategoryService {
    private final ItemCategoryRepository itemCategoryRepository;

    public ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
        this.itemCategoryRepository = itemCategoryRepository;
    }

    public List<GetItemCategoryDTO> getAll() {
        return itemCategoryRepository.findAll().stream().map(GetItemCategoryDTO::new).toList();
    }

    public GetItemCategoryDTO getById(Long id) {
        return new GetItemCategoryDTO(findById(id));
    }

    @Transactional
    public GetItemCategoryDTO create(StoreItemCategoryDTO storeItemCategoryDTO) {
        ItemCategory itemCategory = new ItemCategory(storeItemCategoryDTO);
        alreadyRegistered(itemCategory);
        return new GetItemCategoryDTO(itemCategoryRepository.save(itemCategory));
    }

    @Transactional
    public GetItemCategoryDTO update(Long id, UpdateItemCategoryDTO updateItemCategoryDTO) {
        ItemCategory itemCategory = findById(id);
        itemCategory.update(updateItemCategoryDTO);
        alreadyRegistered(itemCategory, id);
        return new GetItemCategoryDTO(itemCategoryRepository.save(itemCategory));
    }

    @Transactional
    public GetItemCategoryDTO delete(Long id) {
        ItemCategory itemCategory = findById(id);
        itemCategoryRepository.delete(itemCategory);
        return new GetItemCategoryDTO(itemCategory);
    }

    private ItemCategory findById(Long id) {
        return itemCategoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    private void alreadyRegistered(ItemCategory itemCategory) {
        alreadyRegistered(itemCategory, null);
    }

    private void alreadyRegistered(ItemCategory itemCategory, Long id) {
        Optional<ItemCategory> itemCategoryAux = itemCategoryRepository.findByName(itemCategory.getName());
        if (itemCategoryAux.isPresent() && !itemCategoryAux.get().getId().equals(id)) {
            throw new IllegalArgumentException(I18n.get("exception.name_already_registered"));
        }
    }
}
