package lat.luisdias.stock_app_main_service.stock.entities.item;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.StoreItemModelDTO;
import lat.luisdias.stock_app_main_service.stock.dto.ItemModel.UpdateItemModelDTO;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "item_model")
public class ItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String model;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ItemCategory itemCategory;
    private String description;
    @Column(nullable = false)
    private boolean active;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "item_model_id")
    List<ItemImgLink> imgLinks;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp updated;

    public ItemModel() {}

    public ItemModel(StoreItemModelDTO storeItemModelDTO, ItemCategory itemCategory) {
        this.model = storeItemModelDTO.model();
        this.description = storeItemModelDTO.description();
        this.itemCategory = itemCategory;
        this.imgLinks = new ArrayList<>();
        this.active = true;
    }

    public void update(UpdateItemModelDTO updateItemModelDTO, ItemCategory itemCategory) {
        this.model = updateItemModelDTO.model();
        this.description = updateItemModelDTO.description();
        this.itemCategory = itemCategory;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void addImgLink(ItemImgLink itemImgLink) {
        this.imgLinks.add(itemImgLink);
    }

    public void removeImgLink(ItemImgLink itemImgLink) {
        this.imgLinks.remove(itemImgLink);
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Timestamp(Instant.now().toEpochMilli());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = new Timestamp(Instant.now().toEpochMilli());
    }

    public Long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public ItemCategory getCategory() {
        return itemCategory;
    }

    public String getDescription() {
        return description;
    }

    public List<ItemImgLink> getImgLinks() {
        return imgLinks;
    }

    public boolean isActive() {
        return active;
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
        ItemModel itemModel = (ItemModel) o;
        return Objects.equals(id, itemModel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
