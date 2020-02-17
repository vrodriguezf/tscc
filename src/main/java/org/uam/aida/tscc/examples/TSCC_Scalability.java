/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.examples;

import com.google.common.collect.Range;
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
import java.util.stream.Stream;
import org.uam.aida.tscc.APFE.Evaluation;
import org.uam.aida.tscc.APFE.TSWFnet.SyntheticTSWFNet;
import org.uam.aida.tscc.APFE.evaluation.ConformanceFunction;
import org.uam.aida.tscc.APFE.time_series_log.SyntheticIndexedLog;
import org.uam.aida.tscc.APFE.timeseries_guards.ComposedTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.EqualsTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.TSG;
import org.uam.aida.tscc.APFE.timeseries_guards.VariableConstantTSG;
import org.uam.aida.tscc.APFE.utils.Pair;
import org.uam.aida.tscc.business.Transition;

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
        if (args.length == 0 || args.length > 7) {
            LOG.log(Level.SEVERE, "Argument mismatch. Re-execute the program with the correct arguments");
            return;
        }
        
        Integer minTSGLoad = Integer.valueOf(args[0]);
        Integer stepTSGLoad = Integer.valueOf(args[1]);
        Integer maxTSGLoad = Integer.valueOf(args[2]);
        Integer minTasks= Integer.valueOf(args[3]);
        Integer stepTasks= Integer.valueOf(args[4]);
        Integer maxTasks = Integer.valueOf(args[5]);
        String outputFilePath = args[6];
        
        LOG.log(Level.INFO, 
                "Running scalability test with parameters {0}, {1}, {2}, {3}, {4}, {5}, {6}",
                new Object[]{minTSGLoad, stepTSGLoad, maxTSGLoad, minTasks, stepTasks, maxTasks, outputFilePath}
        );
        
        // Data structure for results
        Map<Pair<Integer,Long>, Long> time_results = new HashMap<>();
        
        for (int ntasks = minTasks; ntasks <= maxTasks; ntasks += stepTasks) {
            
            for (long tsgLoad = minTSGLoad; tsgLoad <= maxTSGLoad; tsgLoad += stepTSGLoad) {
                
                Long aux_TSGLoad = (long) tsgLoad;
                Long aux_ntasks = (long) ntasks;
                
                //Generate synthetic process model
                SyntheticTSWFNet model = new SyntheticTSWFNet(
                        ntasks,
                        new EqualsTSG("V", 0.0, true)
                );
                
                // Generate Synthetic Log
                SyntheticIndexedLog log = new SyntheticIndexedLog(tsgLoad*ntasks + 1, "V", 0.0);

                // Set the time scope equals to the log size
                model.getTransitions().forEach(t -> {
                    t.setTimeScope(Range.closed(0L, log.getLogSize()));
                });
                
                //configure guards for the test. The minimum fullfilment duration 
                // will vary in terms of the log size and the number of tasks
                model.getTransitions().stream()
                .map(Transition::getTimeSeriesGuard)
                .forEach((TSG tsg) -> {
                    EqualsTSG cast_tsg = (EqualsTSG) tsg;
                    cast_tsg.setGranularity(1L);
                    cast_tsg.setMinFulfillmentDuration(aux_TSGLoad);
                });
                
                // Run test
                LOG.log(Level.INFO,
                        "Running test with {0} tasks and a TSG load of {1} records",
                        new Object[]{ntasks, tsgLoad});
                
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
                time_results.put(new Pair<Integer,Long>(ntasks, tsgLoad), estimatedTime);
            }
        }
        
        // Save the results in a CSV file
        File fileOld = new File(outputFilePath);
        fileOld.delete();
        File file = new File(outputFilePath);
        CsvWriter csvWriter = new CsvWriter();
        Collection<String[]> data = time_results.entrySet().stream()
                .map((Entry<Pair<Integer, Long>, Long> e) -> { 
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
