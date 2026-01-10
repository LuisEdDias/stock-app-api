package lat.luisdias.stock_app_main_service.stock.entities.item;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.StoreItemCategoryDTO;
import lat.luisdias.stock_app_main_service.stock.dto.itemCategory.UpdateItemCategoryDTO;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "category")
public class ItemCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp updated;

    public ItemCategory() {}

    public ItemCategory(StoreItemCategoryDTO storeItemCategoryDTO) {
        this.name = nameFormatter(storeItemCategoryDTO.name());
        this.description = storeItemCategoryDTO.description();
    }

    public void update(UpdateItemCategoryDTO updateItemCategoryDTO) {
        this.name = nameFormatter(updateItemCategoryDTO.name());
        this.description = updateItemCategoryDTO.description();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Timestamp(Instant.now().toEpochMilli());
        this.updated = this.created;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = new Timestamp(Instant.now().toEpochMilli());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCategory itemCategory = (ItemCategory) o;
        return Objects.equals(id, itemCategory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private String nameFormatter(String name) {
        return name.replaceAll("\\h{2,}", " ").toUpperCase().trim();
    }
}
