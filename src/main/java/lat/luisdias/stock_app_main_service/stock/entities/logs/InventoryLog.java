package lat.luisdias.stock_app_main_service.stock.entities.logs;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "inventory_log")
public class InventoryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "userId")
    private Long userId;
    @Column(name = "user_nickname")
    private String userNickname;
    @Column( name = "action")
    private String action;
    @Column(name = "inventory_id")
    private Long inventoryId;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public InventoryLog() {}

    public InventoryLog(Builder builder) {
        this.userId = builder.userId;
        this.userNickname = builder.userNickname;
        this.action = builder.action;
        this.inventoryId = builder.inventoryId;
        this.timestamp = builder.timestamp;
    }

    public static class Builder {
        private  Long userId;
        private String userNickname;
        private String action;
        private Long inventoryId;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder userNickname(String userNickname) {
            this.userNickname = userNickname;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder inventoryId(Long inventoryId) {
            this.inventoryId = inventoryId;
            return this;
        }

        public InventoryLog build() {
            return new InventoryLog(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public String getAction() {
        return action;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
