package lat.luisdias.stock_app_main_service.stock.services.box;

import lat.luisdias.stock_app_main_service.stock.dto.Item.GetItemBasicDTO;
import lat.luisdias.stock_app_main_service.stock.dto.box.GetBoxWithItemsDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@Service
public class ExportBoxService {
    private final BoxService boxService;
    private final Logger logger = LoggerFactory.getLogger(ExportBoxService.class);

    public ExportBoxService(BoxService boxService) {
        this.boxService = boxService;
    }

    @Transactional
    public Resource exportBox(Long boxId) {
        GetBoxWithItemsDTO boxDTO = boxService.getById(boxId);
        File file;

        try {
            file = Files.createTempFile("temp_box", ".xlsx").toFile();
        } catch (IOException e) {
            logger.error("Error creating temp file {}", e.getMessage());
            throw new InternalError();
        }

        try(FileOutputStream fos = new FileOutputStream(file);
            XSSFWorkbook workbook = createItemsToCheckWorkbook(boxDTO.items())
        ) {
            workbook.write(fos);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return new ByteArrayResource(fileContent);
        } catch (IOException e) {
            logger.error("Exception while exporting box {}", e.getMessage());
            throw new InternalError();
        } finally {
            if (file.delete()) {
                logger.info("Deleted temp file");
            } else {
                logger.warn("Failed to delete temp file");
            }
        }
    }

    private XSSFWorkbook createItemsToCheckWorkbook(List<GetItemBasicDTO> items) throws IOException {
        InputStream exportFileInputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("EXPORTAR CAIXA.xlsx");

        if (exportFileInputStream == null) {
            logger.error("Could not find EXPORTAR CAIXA.xlsx");
            throw new InternalError();
        }

        XSSFWorkbook workbook = new XSSFWorkbook(exportFileInputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        int rowIndex = 1;

        for (GetItemBasicDTO item : items) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, item.model(), style);
            createCell(row, 1, (double) item.id(), style);
            createCell(row, 2, item.comment(), style);
            createCell(row, 3, item.status(), style);
            createCell(row, 4, item.updated(), style);
        }

        return workbook;
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number){
            cell.setCellValue((double) value);
        }
        cell.setCellStyle(style);
    }
}
