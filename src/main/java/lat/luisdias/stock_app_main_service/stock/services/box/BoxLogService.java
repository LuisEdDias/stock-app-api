package lat.luisdias.stock_app_main_service.stock.services.box;

import lat.luisdias.stock_app_main_service.stock.entities.logs.BoxLog;
import lat.luisdias.stock_app_main_service.stock.repositories.box.BoxLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoxLogService {
    private final BoxLogRepository boxLogRepository;

    public BoxLogService(BoxLogRepository boxLogRepository) {
        this.boxLogRepository = boxLogRepository;
    }

    public void save(BoxLog boxLog) {
        boxLogRepository.save(boxLog);
    }

    public List<BoxLog> findAll() {
        return boxLogRepository.findAll();
    }
}
