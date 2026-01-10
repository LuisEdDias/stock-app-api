package lat.luisdias.stock_app_main_service.stock.services.inventory;

import lat.luisdias.stock_app_main_service.stock.dto.Item.GetItemProjection;
import lat.luisdias.stock_app_main_service.stock.dto.inventory.InventoryCheckDTO;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import lat.luisdias.stock_app_main_service.stock.infra.util.TimestampUtil;
import lat.luisdias.stock_app_main_service.stock.repositories.item.ItemRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class InventoryCheckService {
    private final ItemRepository itemRepository;
    private final Logger logger;

    public InventoryCheckService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        this.logger = LoggerFactory.getLogger(InventoryCheckService.class);
    }

    public Resource checkInventory(Long inventoryId, InventoryCheckDTO checkDTO) {
        if (checkDTO.file().isEmpty()) {
            logger.info("Inventory file is empty");
            throw new IllegalArgumentException(I18n.get("exception.file.required"));
        }

        XSSFWorkbook principalWorkbook;
        try (InputStream fileInputStream = checkDTO.file().getInputStream()) {
            principalWorkbook = new XSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            logger.warn("Error trying read file: {}", e.getMessage());
            throw new IllegalArgumentException(I18n.get("exception.content_type.not_supported", ".xlsx"));
        }

        List<GetItemProjection> items;
        if (checkDTO.boxedItemsOnly()) {
            items = itemRepository.findAllWithProjectionByInventoryIdAndBoxNotNull(inventoryId);
        } else {
            items = itemRepository.findAllWithProjectionByInventoryId(inventoryId);
        }

        return execute(items, principalWorkbook, checkDTO.itemIdColumn());
    }


    private Resource execute(List<GetItemProjection> items, XSSFWorkbook principalWorkbook, Integer columnToSearch) {
         try {
             XSSFSheet sheetPrincipal = principalWorkbook.getSheetAt(0);
             List<GetItemProjection> itemsToBeChecked = new ArrayList<>();

             Row header = sheetPrincipal.getRow(0);
             if (columnToSearch == null) {
                 for (Cell cell : header) {
                     if (cell.getStringCellValue().toLowerCase().contains("<id>")) {
                         columnToSearch = cell.getColumnIndex();
                         break;
                     }
                 }
                 if (columnToSearch == null) {
                     throw new RuntimeException(I18n.get("exception.inventory_check.id_column_not_found"));
                 }
             }
             Map<Double, Row> rowIndex = buildRowIndex(sheetPrincipal, columnToSearch);
             CellStyle style = createCellStyle(principalWorkbook, IndexedColors.BRIGHT_GREEN);

             for (GetItemProjection item : items) {
                 Row row = rowIndex.get((double) item.getId());
                 if (row != null) {
                     row.getCell(2).setCellStyle(style);
                 } else {
                     itemsToBeChecked.add(item);
                 }
             }
             ByteArrayOutputStream principalBaos = new ByteArrayOutputStream();
             principalWorkbook.write(principalBaos);

             ByteArrayOutputStream itemsToCheckBaos = null;
             if (!itemsToBeChecked.isEmpty()) {
                 XSSFWorkbook toCheckWorkbook = createItemsToCheckWorkbook(itemsToBeChecked);
                 itemsToCheckBaos = new ByteArrayOutputStream();
                 toCheckWorkbook.write(itemsToCheckBaos);
                 toCheckWorkbook.close();
             }

             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                 String date = TimestampUtil.toStringWithoutTime(new Timestamp(Instant.now().toEpochMilli()));
                 addByteArrayToZip(principalBaos.toByteArray(), "PEL - FECHAMENTO ESTOQUE " + date + ".xlsx", zipOut);
                 if (itemsToCheckBaos != null) {
                     addByteArrayToZip(itemsToCheckBaos.toByteArray(), "ITENS À VERIFICAR " + date + ".xlsx", zipOut);
                 }
             }
             return new ByteArrayResource(baos.toByteArray());
         } catch (IOException e){
             logger.error("Inventory check error: {}", e.getMessage());
             throw new RuntimeException(I18n.get("exception.runtime"));
         }
    }

    private Map<Double, Row> buildRowIndex(XSSFSheet sheet, int searchColumn) {
        Map<Double, Row> rowMap = new HashMap<>();
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(searchColumn);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    rowMap.put(cell.getNumericCellValue(), row);
                }
            }
        }
        return rowMap;
    }

    private CellStyle createCellStyle(XSSFWorkbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        if (color != null) {
            style.setFillForegroundColor(color.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
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

    private XSSFWorkbook createItemsToCheckWorkbook(List<GetItemProjection> itemsToBeChecked) throws IOException {
        InputStream toCheckFileInputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("ITENS À VERIFICAR.xlsx");

        assert toCheckFileInputStream != null;
        XSSFWorkbook workbook = new XSSFWorkbook(toCheckFileInputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        CellStyle style = createCellStyle(workbook, null);

        int rowIndex = 1;

        for (GetItemProjection item : itemsToBeChecked) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, item.getItemModel().getModel(), style);
            createCell(row, 1, (double) item.getId(), style);
            createCell(row, 2, item.getComment(), style);
            createCell(row, 3, (double) item.getBox().getId(), style);
            createCell(row, 4, item.getUpdated(), style);
        }
        return workbook;
    }

    private void addByteArrayToZip(byte[] data, String fileName, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        zipOut.write(data);
        zipOut.closeEntry();
    }
}
