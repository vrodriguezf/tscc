/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.examples;

import de.siegmar.fastcsv.writer.CsvWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.uam.aida.tscc.APFE.Evaluation;
import org.uam.aida.tscc.APFE.TSWFnet.SyntheticTSWFNet;
import org.uam.aida.tscc.APFE.evaluation.ConformanceFunction;
import org.uam.aida.tscc.APFE.time_series_log.SyntheticIndexedLog;
import org.uam.aida.tscc.APFE.utils.Pair;

/**
 *
 * @author victor
 */
public class TSCC_Scalability {

    private static final Logger LOG = Logger.getLogger(TSCC_Scalability.class.getName());
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0 || args.length > 6) {
            LOG.log(Level.SEVERE, "Argument mismatch. Re-execute the program with the correct arguments");
        }
        
        Integer minLogSize = Integer.valueOf(args[0]);
        Integer stepLogSize = Integer.valueOf(args[1]);
        Integer maxLogSize = Integer.valueOf(args[2]);
        Integer minTasks= Integer.valueOf(args[3]);
        Integer stepTasks= Integer.valueOf(args[4]);
        Integer maxTasks = Integer.valueOf(args[5]);
        
        // Data structure for results
        Map<Pair<Integer,Integer>, Long> time_results = new HashMap<>();
        
        for (int ntasks = minTasks; ntasks <= maxTasks; ntasks += stepTasks) {
            
            //Generate synthetic process model
            SyntheticTSWFNet model = new SyntheticTSWFNet(ntasks);
            
            for (int logSize = minLogSize; logSize <= maxLogSize; logSize += stepLogSize) {
                
                // Generate Synthetic Log
                SyntheticIndexedLog log = new SyntheticIndexedLog(logSize);
                
                // Run test
                long startTime = System.nanoTime();
                /**
                 * Call the CC algorithm
                 */
                ConformanceFunction CCResult = null;
                try {
                    CCResult = Evaluation.completeCCTS(
                        model, 
                        log, 
                        null, 
                        model.createInitialMarking(new Long[]{0L})
                    );
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error executing TSCC algorithm");
                    e.printStackTrace();
                    return;
                }
                long estimatedTime = System.nanoTime() - startTime;
                time_results.put(new Pair<Integer,Integer>(ntasks,logSize), estimatedTime);
            }
        }
        
        // Save the results in a CSV file
        File file = new File("/home/victor/foo.csv");
        CsvWriter csvWriter = new CsvWriter();
        Collection<String[]> data = time_results.entrySet().stream()
                .map((Entry<Pair<Integer, Integer>, Long> e) -> { 
                    String[] ss = new String[]{
                        e.getKey().getKey().toString(),
                        e.getKey().getValue().toString(),
                        e.getValue().toString()
                    };
                    return ss;
                })
                .collect(Collectors.toList());
        
        try {
            csvWriter.write(file, StandardCharsets.UTF_8, data);
        } catch (IOException ex) {
            Logger.getLogger(TSCC_Scalability.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return;
    }
    
}
