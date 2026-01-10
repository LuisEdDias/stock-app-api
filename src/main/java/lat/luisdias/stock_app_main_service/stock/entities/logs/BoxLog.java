package lat.luisdias.stock_app_main_service.stock.entities.logs;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "box_log")
public class BoxLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "box_id")
    private Long boxId;
    @Column(name = "inventory_id")
    private Long inventoryId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_nickname")
    private String userNickname;
    @Column(name = "action")
    private String action;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public BoxLog() {}

    public BoxLog(Builder builder) {
        this.boxId = builder.boxId;
        this.inventoryId = builder.inventoryId;
        this.userId = builder.userId;
        this.userNickname = builder.userNickname;
        this.action = builder.action;
        this.timestamp = builder.timestamp;
    }

    public static class Builder {
        private Long boxId;
        private Long inventoryId;
        private Long userId;
        private String userNickname;
        private String action;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder boxId(Long boxId) {
            this.boxId = boxId;
            return this;
        }

        public Builder inventoryId(Long inventoryId) {
            this.inventoryId = inventoryId;
            return this;
        }

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

        public BoxLog build() {
            return new BoxLog(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getBoxId() {
        return boxId;
    }

    public Long getInventoryId() {
        return inventoryId;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
