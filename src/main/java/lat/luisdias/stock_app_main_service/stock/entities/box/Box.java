package lat.luisdias.stock_app_main_service.stock.entities.box;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lat.luisdias.stock_app_main_service.stock.dto.box.StoreBoxDTO;
import lat.luisdias.stock_app_main_service.stock.dto.box.UpdateBoxDTO;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;
import lat.luisdias.stock_app_main_service.stock.entities.item.Item;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "boxes")
public class Box {
    @Id
    @Column(nullable = false, unique = true)
    @NotNull
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @NotNull
    private Inventory inventory;
    @OneToMany(mappedBy = "box", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Item> items;
    private String name;
    private String description;
    private BoxStatus status;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp updated;

    public Box() {}

    public Box(StoreBoxDTO boxDTO, Inventory inventory) {
        this.id = boxDTO.id();
        this.name = boxDTO.name().toUpperCase();
        this.description = boxDTO.description();
        this.status = boxDTO.status();
        this.inventory = inventory;
    }

    public void update(UpdateBoxDTO boxDTO) {
        this.name = boxDTO.name().toUpperCase();
        this.description = boxDTO.description();
        this.status = boxDTO.status();
    }

    public void move(Inventory inventory) {
        this.inventory = inventory;
        this.getItems().forEach(item -> item.moveToInventory(inventory));
    }

    public void addItem(Item item) {
        if (item.getInventory() != this.inventory) {
            throw new IllegalArgumentException(I18n.get("exception.inventory.not_match"));
        }

        item.moveToBox(this);
        this.items.add(item);
    }

    public void removeItem(Item item) {
        boolean removed = this.items.remove(item);
        if (removed) {
            item.moveToBox(null);
        }
    }

    public void moveItemToBox(Item item, Box newBox) {
        if (this.inventory != newBox.getInventory()) {
            throw new IllegalArgumentException(I18n.get("exception.inventory.not_match"));
        }
        boolean removed = this.items.remove(item);
        if (removed) {
            item.moveToBox(newBox);
        }
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

    public List<Item> getItems() {
        return items;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public BoxStatus getStatus() {
        return status;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
