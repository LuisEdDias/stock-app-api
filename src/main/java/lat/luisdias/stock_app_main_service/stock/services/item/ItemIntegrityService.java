package lat.luisdias.stock_app_main_service.stock.services.item;

import lat.luisdias.stock_app_main_service.stock.entities.item.Item;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemIntegrityService {
    private final ItemRepository itemRepository;

    public ItemIntegrityService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void ensureNotRegistered(Long itemId) {
        if (itemRepository.existsById(itemId)) {
            throw new IllegalArgumentException(I18n.get("exception.id_already_registered"));
        }
    }

    public List<Item> findAllOrThrow(List<Long> itemIds) {
        List<Long> distinctIds = itemIds.stream().distinct().toList();
        List<Item> items = itemRepository.findAllById(distinctIds);

        if (items.size() != distinctIds.size()) {
            Set<Long> foundIds = items.stream().map(Item::getId).collect(Collectors.toSet());
            List<String> notFound = distinctIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .map(Object::toString)
                    .toList();

            throw new IllegalArgumentException(I18n.get("exception.not_found.range", String.join(", ", notFound)));
        }
        return items;
    }

    public Item findByIdOrThrow(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(
                () -> new NoSuchElementException(I18n.get("exception.not_found"))
        );
    }
}
