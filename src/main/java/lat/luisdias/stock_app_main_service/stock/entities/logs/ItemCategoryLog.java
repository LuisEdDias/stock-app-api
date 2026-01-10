package lat.luisdias.stock_app_main_service.stock.entities.logs;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "item_category_log")
public class ItemCategoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "category_name")
    private String categoryName;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_nickname")
    private String userNickname;
    private String action;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public ItemCategoryLog() {}

    public ItemCategoryLog(Builder builder) {
        this.categoryId = builder.categoryId;
        this.categoryName = builder.categoryName;
        this.userId = builder.userId;
        this.userNickname = builder.userNickname;
        this.action = builder.action;
        this.timestamp = builder.timestamp;
    }

    public static class Builder {
        private Long userId;
        private String userNickname;
        private String categoryName;
        private Long categoryId;
        private String action;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder userNickname(String userNickname) {
            this.userNickname = userNickname;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public ItemCategoryLog build() {
            return new ItemCategoryLog(this);
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

    public String getCategoryName() {
        return categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getAction() {
        return action;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
