/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import org.uam.aida.tscc.APFE.OPTriggering;
import org.uam.aida.tscc.APFE.OPResponse;
import org.uam.aida.tscc.APFE.Evaluation;
import SAVIER_integration.SAVIEROperation;
import SAVIER_integration.SAVIEROperator;
import examples.SAVIERIntegration;
import org.uam.aida.tscc.APFE.TSWFnet.DummyOP;
import org.uam.aida.tscc.APFE.TSWFnet.TSWFNet;
import org.uam.aida.tscc.APFE.TSWFnet.MarkingOld;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import SAVIER_integration.input.SAVIERInputManager;
import org.uam.aida.tscc.business.Global;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uam.aida.tscc.APFE.utils.Pair;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author victor
 */
public class APFE_Scalability {
    
    private static final Logger LOG = Logger.getLogger(SAVIERIntegration.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
                
        if (args.length == 0 || args.length > 4) {
            LOG.log(Level.SEVERE, "Argument mismatch. Re-execute the program with the corret arguments");
        }
        String input_file_path = args[0];
        Integer min_nsteps = Integer.valueOf(args[1]);
        Integer max_nsteps = Integer.valueOf(args[2]);
        String output_file_path = args[3];
        
        //Result
        List<Pair<Integer,Long>> time_results = new ArrayList<Pair<Integer,Long>>();
        
        //Try to load the log file
        STANAG4586Log log = null;
        LOG.log(Level.INFO, "*** READING LOGS ***");
        try {
            //Prueba : cargar un fichero .xlsx de log
            log = SAVIERInputManager.loadLogFile(input_file_path);
        } catch (IOException ex) {
            Logger.getLogger(SAVIERIntegration.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOG.log(Level.INFO, "*** DONE ****");
        Integer logsize = log.getMessages("Sheet1").size();
        
        /**
         * LOOP
         */
        for (int nsteps = min_nsteps; nsteps <= max_nsteps; nsteps++) {
            
            //Create the operation object (The id is extracted from the log file path)
            SAVIEROperation operation = new SAVIEROperation(
                    "dummyOperation",
                    log,
                    new SAVIEROperator("dummyOperator")
            );

            //Create the Dummy OP
            TSWFNet eop = new DummyOP(nsteps);
            Global.petriNet = eop;

            //Dummy alert and alert response
            OPTriggering alert = new OPTriggering(
                    "dummyAlert",
                    operation.getStartTime(),
                    operation.getEndTime(),
                    eop
            );
            OPResponse ar = new OPResponse(alert, operation);
            
            Pair<MarkingOld,Long> eval = null;
            long startTime = System.nanoTime();
            try {
                //Evaluation
                eval = Evaluation.basicAPFE(
                        eop.createInitialMarking(Arrays.asList(ar)),
                        alert,
                        eop
                );

            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            long estimatedTime = System.nanoTime() - startTime;
            time_results.add(new Pair<Integer,Long>(nsteps,estimatedTime));
            
            LOG.log(Level.INFO,
            "-----------------ENDING PROCEDURE FOLLOWING EVALUATION [{0}], timeRange=[{1},{2}])---------------",
            new Object[]{alert.getId(), alert.getTriggerTs(), alert.getEndTime()});
            LOG.log(Level.INFO, "Last marking: {0}", eval.getKey());
            LOG.log(Level.INFO, "Time spent: {0}", eval.getValue());
            
            /*
            LOG.log(Level.INFO,
                        "-----------------ENDING PROCEDURE FOLLOWING EVALUATION [{0}], timeRange=[{1},{2}])---------------",
                        new Object[]{alert.getId(), alert.getTriggerTs(), alert.getEndTime()});
                LOG.log(Level.INFO, "1- RIGHT ACTIONS: {0}", eval.getRightActions());

                LOG.log(Level.INFO, "2- MISSING ACTIONS: {0}", eval.getMissingActions());
                LOG.log(Level.INFO, "3- SEQUENTIAL MISMATCHES: {0}", eval.getSequentialMismatches());
                LOG.log(Level.INFO, "4- OVERREACTIONS: {0}", eval.getOverreactions());
                LOG.log(Level.INFO, "--------------------------------------------------");
*/
        }
        
        //Save performance results
        // Finds the workbook instance for XLSX file
        XSSFWorkbook wb = new XSSFWorkbook();
        String sheetName = "Sheet1";//name of sheet
        XSSFSheet sheet = wb.createSheet(sheetName);
        
        //colnames
        XSSFRow row;
        row = sheet.createRow(0);
            XSSFCell cell;
            cell = row.createCell(0);cell.setCellValue("logsize");
            cell = row.createCell(1);cell.setCellValue("nsteps");
            cell = row.createCell(2);cell.setCellValue("time");
        
        //iterating r number of rows
        for (int r=0;r < time_results.size(); r++ )
        {
                row = sheet.createRow(r+1);
                cell = row.createCell(0);cell.setCellValue(logsize);
                cell = row.createCell(1);cell.setCellValue(time_results.get(r).getKey());
                cell = row.createCell(2);cell.setCellValue(time_results.get(r).getValue());
        }
        
        FileOutputStream fos = new FileOutputStream(output_file_path);
        //write this workbook to an Outputstream.
        wb.write(fos);
        fos.flush();
        fos.close();
    }
    
}
