package lat.luisdias.stock_app_main_service.stock.services.storage;

import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.*;

@Component
public class S3StorageService implements StorageServiceInterface {
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    private final S3Client s3Client;
    private final Logger logger;

    public S3StorageService(S3ClientProvider s3ClientProvider) {
        this.s3Client = s3ClientProvider.getClient;
        this.logger = LoggerFactory.getLogger(S3StorageService.class);
    }

    @Override
    public Map<String, String> uploadFileToFolder(MultipartFile file, String folderName) {
        try {
            String fileExt = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String storageId = UUID.randomUUID().toString();
            String key = folderName + "/" + storageId + fileExt;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            String storageUrl = "https://" + bucketName + ".s3.amazonaws.com/";
            Map<String, String> map = new HashMap<>();
            map.put("url", storageUrl);
            map.put("storageKey", key);
            return map;
        } catch (RuntimeException | IOException e) {
            logger.error("S3 storage error: {}", e.getMessage());
            throw new RuntimeException(I18n.get("exception.runtime"));
        }
    }

    @Override
    public void deleteFileFromFolder(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            logger.error("S3 file deletion error: {}", e.getMessage());
            throw new RuntimeException(I18n.get("exception.runtime"));
        }
    }
}
