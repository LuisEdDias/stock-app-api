package lat.luisdias.stock_app_main_service.stock.services.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StorageServiceInterface {
    Map<String, String> uploadFileToFolder(MultipartFile file, String folderName);

    void deleteFileFromFolder(String key);
}
