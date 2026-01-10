package lat.luisdias.stock_app_main_service.stock.entities.item;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lat.luisdias.stock_app_main_service.stock.dto.Item.StoreItemDTO;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "item")
public class Item {
    @Id
    @Column(nullable = false, unique = true)
    @NotNull
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_model_id", nullable = false)
    @NotNull
    private ItemModel itemModel;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Box box;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @NotNull
    private Inventory inventory;
    private String comment;
    @Column(nullable = false)
    @NotNull
    private ItemStatus status;
    @Column(nullable = false)
    @NotNull
    private boolean available;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp updated;

    public Item() {}

    public Item(StoreItemDTO storeItemDTO, ItemModel itemModel, Inventory inventory) {
        this.id = storeItemDTO.id();
        this.comment = storeItemDTO.comment();
        this.itemModel = itemModel;
        this.status = storeItemDTO.status();
        this.inventory = inventory;
        this.available = true;
    }

    public void updateModel(ItemModel itemModel) {
        this.itemModel = itemModel;
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }

    public void updateStatus(ItemStatus status) {
        this.status = status;
    }

    public void moveToInventory(Inventory inventory) {
        if (this.box != null && this.box.getInventory() != inventory) {
            throw new IllegalArgumentException(I18n.get("exception.inventory.not_match"));
        }
        this.inventory = inventory;
    }

    public void moveToBox(Box box) {
        this.box = box;
    }

    public void setAvailable(){
        this.available = true;
    }

    public void setUnavailable(){
        this.available = false;
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

    public ItemModel getItemModel() {
        return itemModel;
    }

    public Box getBox() {
        return box;
    }

    public String getComment() {
        return comment;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean isAvailable() {
        return available;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Timestamp getUpdated() {
        return updated;
    }
}
