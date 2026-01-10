package lat.luisdias.stock_app_main_service.stock.entities.item;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "item_img_link")
public class ItemImgLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "storage_key", nullable = false)
    private String storageKey;
    @Column(name = "storage_url", nullable = false)
    private String storageUrl;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created;

    public ItemImgLink() {
    }

    public ItemImgLink(String storageUrl, String storageKey) {
        this.storageUrl = storageUrl;
        this.storageKey = storageKey;
        this.created = new Timestamp(Instant.now().toEpochMilli());
    }

    public Long getId() {
        return id;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public Timestamp getCreated() {
        return created;
    }
}
