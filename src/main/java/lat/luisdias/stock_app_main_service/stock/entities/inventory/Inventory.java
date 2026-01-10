package lat.luisdias.stock_app_main_service.stock.entities.inventory;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.StoreInventoryDTO;
import lat.luisdias.stock_app_main_service.stock.entities.vo.Address;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private boolean active;
    @Embedded
    private Address address;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp created;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp updated;

    public Inventory() {}

    public Inventory(StoreInventoryDTO inventoryDTO) {
        this.name = nameFormatter(inventoryDTO.name());
        this.address = new Address(inventoryDTO.address());
        this.active = true;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void activate(){
        this.active = true;
    }

    public void deactivate(){
        this.active = false;
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

    public Address getAddress() {
        return address;
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

    private String nameFormatter(String name) {
        return name.replaceAll("\\h{2,}", " ").toUpperCase().trim();
    }
}
