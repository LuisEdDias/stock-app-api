package lat.luisdias.stock_app_main_service.stock.entities.logs;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "item_model_log")
public class ItemModelLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "item_model_id")
    private Long itemModelId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_nickname")
    private String userNickname;
    private String action;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public ItemModelLog() {}

    public ItemModelLog(Builder builder) {
        this.itemModelId = builder.itemModelId;
        this.userId = builder.userId;
        this.userNickname = builder.userNickname;
        this.action = builder.action;
        this.timestamp = builder.timestamp;
    }

    public static class Builder{
        private Long itemModelId;
        private Long userId;
        private String userNickname;
        private String action;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder itemModelId(Long itemModelId) {
            this.itemModelId = itemModelId;
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

        public ItemModelLog build() {
            return new ItemModelLog(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getItemModelId() {
        return itemModelId;
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
