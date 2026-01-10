package lat.luisdias.stock_app_main_service.stock.entities.logs;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "item_log")
public class ItemLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "item_id")
    private Long itemId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_nickname")
    private String userNickname;
    private String action;
    @Column(name = "user_comment")
    private String userComment;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public ItemLog() {}

    public ItemLog(Builder builder) {
        this.itemId = builder.itemId;
        this.userId = builder.userId;
        this.userNickname = builder.userNickname;
        this.action = builder.action;
        this.userComment = builder.userComment;
        this.timestamp = builder.timestamp;
    }

    public static class Builder {
        private Long itemId;
        private Long userId;
        private String userNickname;
        private String action;
        private String userComment;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder itemId(Long itemId) {
            this.itemId = itemId;
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

        public Builder userComment(String userComment) {
            this.userComment = userComment;
            return this;
        }

        public ItemLog build() {
            return new ItemLog(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
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

    public String getUserComment() {
        return userComment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
