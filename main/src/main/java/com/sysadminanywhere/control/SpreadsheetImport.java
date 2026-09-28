package com.sysadminanywhere.control;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class SpreadsheetImport {
    private SpreadsheetImport() { }

    public static String firstSheetAsCsv(byte[] content) throws IOException {
        try (InputStream input = new ByteArrayInputStream(content);
             var workbook = WorkbookFactory.create(input)) {
            var sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            int lastColumn = sheet.getRow(0) == null ? 0 : sheet.getRow(0).getLastCellNum();
            StringBuilder csv = new StringBuilder();
            for (Row row : sheet) {
                for (int column = 0; column < lastColumn; column++) {
                    if (column > 0) csv.append(',');
                    String value = formatter.formatCellValue(row.getCell(column));
                    csv.append('"').append(value.replace("\"", "\"\"")).append('"');
                }
                csv.append('\n');
            }
            return csv.toString();
        } catch (Exception exception) {
            if (exception instanceof IOException ioException) throw ioException;
            throw new IOException("Unable to read Excel workbook", exception);
        }
    }
}
