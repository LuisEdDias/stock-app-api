package lat.luisdias.stock_app_main_service.stock.dto.Item;

import lat.luisdias.stock_app_main_service.stock.entities.logs.ItemLog;

import java.sql.Timestamp;

public record GetItemLogDTO(
        Long userId,
        String userNickname,
        Long itemId,
        String action,
        String userComment,
        Timestamp timestamp
) {
    public GetItemLogDTO (ItemLog itemLog) {
        this(
                itemLog.getUserId(),
                itemLog.getUserNickname(),
                itemLog.getItemId(),
                itemLog.getAction(),
                itemLog.getUserComment(),
                itemLog.getTimestamp()
        );
    }
}
