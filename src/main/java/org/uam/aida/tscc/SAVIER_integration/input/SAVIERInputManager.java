/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration.input;

import SAVIER_integration.data_log.STANAG4586.STANAGMessage;
import SAVIER_integration.data_log.STANAG4586.STANAGMessageList;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author victor
 */
public class SAVIERInputManager {

    private static final Logger LOG = Logger.getLogger(SAVIERInputManager.class.getName());
    
    private static STANAG4586Log loadXLSXLogFile(String filePath) throws FileNotFoundException, IOException {
        
        File logFile = new File(filePath);
        FileInputStream fis = new FileInputStream(logFile);
        STANAG4586Log resultLog;
        
        // Finds the workbook instance for XLSX file
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        
        resultLog = new STANAG4586Log(filePath);
        for (int i = 0; i< wb.getNumberOfSheets(); i++) {
            XSSFSheet sheet = wb.getSheetAt(i);
                        
            //Get iterator to all the rows in the current sheet
            STANAGMessageList messages = new STANAGMessageList();
            for (Row row : sheet) {
                if (row.getRowNum() > 0) {
                    Map<String,Object> fieldsMap = new HashMap<>();
                    for (Cell cell : row) {
                        Object cellValue = null;
                        switch(cell.getCellType()) {
                            case Cell.CELL_TYPE_STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                cellValue = cell.getNumericCellValue();
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                cellValue = cell.getBooleanCellValue();
                                break;
                            default:
                                LOG.log(Level.INFO,"Not recognized cell type!");
                        }
                            
                        fieldsMap.put(
                                sheet.getRow(0).getCell(cell.getColumnIndex()).getStringCellValue(),
                                cellValue);
                    }
                    
                    //Transform the timestamp to long                    
                    messages.add(new STANAGMessage(
                            new BigDecimal(String.valueOf(fieldsMap.get("timeStamp"))).longValue(), 
                            fieldsMap));                    
                }
            }
            resultLog.getMessagesMap().put(
                    wb.getSheetName(i),
                    messages);
        }
        
        return resultLog;
    }
    
    
    public static STANAG4586Log loadLogFile(String filePath) throws FileNotFoundException, IOException {
        return loadXLSXLogFile(filePath);
    }
}
