package lat.luisdias.stock_app_main_service.stock.services.itemModel;

import lat.luisdias.stock_app_main_service.stock.entities.item.ItemImgLink;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.services.storage.StorageServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemModelImgStorageService {
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/jpg");
    private final StorageServiceInterface fileStorage;
    @Value("${aws.s3.folder.images}")
    private String folderName;

    public ItemModelImgStorageService(
            StorageServiceInterface fileStorage
    ) {
        this.fileStorage = fileStorage;
    }

    public List<ItemImgLink> save(List<MultipartFile> images) {
        images.forEach(img -> {
            if (!ALLOWED_CONTENT_TYPES.contains(img.getContentType())) {
                throw new IllegalArgumentException(I18n.get("exception.content_type.not_supported", ALLOWED_CONTENT_TYPES));
            }
        });
        List<ItemImgLink> imgLinks = new ArrayList<>();
        images.forEach(
                image -> {
                    var imageMap = fileStorage.uploadFileToFolder(image, folderName);
                    imgLinks.add(new ItemImgLink(imageMap.get("url"), imageMap.get("storageKey")));
                }
        );
        return imgLinks;
    }

    public void delete(List<ItemImgLink> imgLinks){
        imgLinks.forEach(imgLink -> fileStorage.deleteFileFromFolder(imgLink.getStorageKey()));
    }
}
