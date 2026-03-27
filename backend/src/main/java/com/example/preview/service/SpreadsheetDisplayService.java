package com.example.preview.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SpreadsheetDisplayService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/M/d");

    public SpreadsheetDisplayResponse extractDisplayValues(Path workbookPath, String locale) throws Exception {
        Locale targetLocale = "zh".equalsIgnoreCase(locale) ? Locale.SIMPLIFIED_CHINESE : Locale.US;
        DataFormatter formatter = new DataFormatter(targetLocale, true);

        try (InputStream inputStream = Files.newInputStream(workbookPath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            List<SheetDisplay> sheets = new ArrayList<>();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex += 1) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                List<String> headers = extractHeaders(sheet, formatter, evaluator);
                List<CellDisplay> cells = new ArrayList<>();

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell == null || cell.getCellType() == CellType.BLANK) {
                            continue;
                        }

                        String display = formatter.formatCellValue(cell, evaluator);
                        if (display == null || display.isBlank()) {
                            continue;
                        }

                        if (shouldRenderAsDate(cell, headers)) {
                            display = DATE_FORMATTER.format(
                                    DateUtil.getJavaDate(cell.getNumericCellValue(), false)
                                            .toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                            );
                        }

                        cells.add(new CellDisplay(row.getRowNum(), cell.getColumnIndex(), display));
                    }
                }

                sheets.add(new SheetDisplay(sheet.getSheetName(), cells));
            }

            return new SpreadsheetDisplayResponse(sheets);
        }
    }

    private List<String> extractHeaders(Sheet sheet, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> headers = new ArrayList<>();
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        if (headerRow == null) {
            return headers;
        }

        short lastCell = headerRow.getLastCellNum();
        for (int columnIndex = 0; columnIndex < lastCell; columnIndex += 1) {
            Cell cell = headerRow.getCell(columnIndex);
            String header = cell == null ? "" : formatter.formatCellValue(cell, evaluator);
            headers.add(header == null ? "" : header.trim().toLowerCase(Locale.ROOT));
        }
        return headers;
    }

    private boolean shouldRenderAsDate(Cell cell, List<String> headers) {
        if (cell.getRowIndex() == 0 || cell.getCellType() != CellType.NUMERIC) {
            return false;
        }

        int columnIndex = cell.getColumnIndex();
        if (columnIndex >= headers.size()) {
            return false;
        }

        String header = headers.get(columnIndex);
        if (!header.contains("date")) {
            return false;
        }

        double value = cell.getNumericCellValue();
        return value > 20000 && value < 90000;
    }

    public record SpreadsheetDisplayResponse(List<SheetDisplay> sheets) {
    }

    public record SheetDisplay(String name, List<CellDisplay> cells) {
    }

    public record CellDisplay(int row, int col, String display) {
    }
}
