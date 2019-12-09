/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.examples;

import com.google.common.collect.Range;
import com.moandjiezana.toml.Toml;
import java.io.BufferedWriter;
import org.uam.aida.tscc.APFE.Evaluation;
import org.uam.aida.tscc.APFE.TSWFnet.TSWFNet;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import org.uam.aida.tscc.OP_models.sheerer.SHEERER_SINGLE_CYCLE;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.naming.ConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.uam.aida.tscc.APFE.evaluation.ConformanceCategory;
import org.uam.aida.tscc.APFE.evaluation.ConformanceFunction;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.timeseries_guards.ComposedTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.CountinuousConditionTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.MonotonicTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.UnivariateTSG;
import org.uam.aida.tscc.APFE.timeseries_guards.TSG;
import org.uam.aida.tscc.APFE.utils.Pair;
import org.uam.aida.tscc.business.Global;
import org.uam.aida.tscc.business.NetClass;
import org.uam.aida.tscc.business.Transition;
import org.uam.aida.tscc.data.FileManager;

/**
 *
 * @author victor
 */
public class ShearerExperimentation {

    private static final Logger LOG = Logger.getLogger(ShearerExperimentation.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ConfigurationException {

        //Constants
        Long data_log_record_frequency = 0L; //milliseconds

        // Error control for command line arguments
        if (args.length != 3) {
            LOG.log(Level.SEVERE,
                    "Wrong number of command line arguments (Correct is {0})",
                    new Object[]{3});
            throw new IllegalArgumentException("Wrong call to the main program");
        }

        //Read the data log
        LOG.log(Level.INFO, "Loading Data log: {0}", args[0]);
        /*
        TimeSeriesLog Delta = TimeSeriesLog.loadFromXLSX(
                args[0],
                "Sheet1",
                "time_index",
                "variable",
                "value");
        */
        IndexedTSLog L = IndexedTSLog.loadFromCSV(
                args[0], 
                "time_index", 
                "variable", 
                "value"
        );
        
        LOG.log(Level.INFO, "Data log successfully loaded");
        
        //Read the config file
        LOG.log(Level.INFO, "Reading configuration file: {0}", args[2]);
        Toml config = new Toml().read(Files.newInputStream(Paths.get(args[2])));

        //Load the corresponding TSWFNet (from package OP_models)
        //TSWFNet W = new SHEERER_SINGLE_CYCLE();
        FileManager handler = new FileManager();
        NetClass n = new NetClass();
        handler.loadFile(new File("/home/victor/Desktop/sheerer_single_cycle_extended_2.pnml"));
        String netCode = n.generateNetSource();
        Files.write(Paths.get("/home/victor/Git/Github/tscc/src/main/java/org/uam/aida/tscc/OP_models/SHEERER_SINGLE_CYCLE.java"), netCode.getBytes());
        TSWFNet W;
        try {
            W = (TSWFNet) Class.forName("org.uam.aida.tscc.OP_models.SHEERER_SINGLE_CYCLE").newInstance();
        } catch (Exception ex) {
            Logger.getLogger(ShearerExperimentation.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        //Time to start the conformance checking
        //(Earliest timestamp in the log)
        Long x_0;
        try {
            x_0 = L.getEntries().stream()
                    .mapToLong(LogEntry::getTs)
                    .min()
                    .orElseThrow(Exception::new);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Exception while retrieving x0 from the log"
                    + ex.getStackTrace().toString()
            );
            return;
        }

        //Ensure that every variable has been recorded when executing APFE
        x_0 += data_log_record_frequency;
        
        /**
         * GENERAL CONFIGURATION
         */
        Global.R = Optional.ofNullable(config.getLong("reversing_time"))
                .orElseThrow(
                        () -> new ConfigurationException(
                                "Missing config parameter reversing_time")
                );
        
        boolean automaticTimeScope = 
                Optional.ofNullable(config.getBoolean("automatic_time_scope"))
                .orElse(false);
        
        if (automaticTimeScope) {
            //The automatic time scope spans the whole time series
            Long maximumTimeSeriesDuration
                    = L.getIndexedEntries().values().stream()
                            .mapToLong(ts -> ts.lastKey() - ts.firstKey())
                            .max()
                            .orElseThrow(() -> new RuntimeException("debug me!"));

            W.getTransitions().forEach(t -> {
                t.setTimeScope(
                        Range.closed(0L, maximumTimeSeriesDuration)
                );
            });
        }
        
        Long automaticGranularityMaxPoints =
                Optional.ofNullable(config.getLong("automatic_granularity_max_points"))
                .orElse(0L);
        
        if (automaticGranularityMaxPoints != 0L) {
            Long maximumTimeSeriesDuration
                    = L.getIndexedEntries().values().stream()
                            .mapToLong(ts -> ts.lastKey() - ts.firstKey())
                            .max()
                            .orElseThrow(() -> new RuntimeException("debug me!"));
            
            Long automaticGranularity = 
                    (long) Math.ceil((double) maximumTimeSeriesDuration/automaticGranularityMaxPoints);

            W.getTransitions().stream()
                    .map(Transition::getTimeSeriesGuard)
                    .flatMap((TSG tsg) -> {
                        if (tsg instanceof ComposedTSG) {
                            return ((ComposedTSG) tsg).flatten().stream();
                        } else {
                            return Stream.of(tsg);
                        }
                    })
                    .filter(x -> x instanceof CountinuousConditionTSG)
                    .map(x -> (CountinuousConditionTSG) x)
                    .forEach((CountinuousConditionTSG x) -> {
                        x.setGranularity(automaticGranularity);
                    });
        }
        
        /**
         * TRANSITION CONFIGURATION
         */
        Optional<Toml> configTransitions = Optional.ofNullable(
                config.getTable("transitions")
        );
        
        //Time scope of every transition in the model
        List<Object> timeScope = 
                configTransitions
                .flatMap((Toml cfg) -> {
                    return Optional.ofNullable(
                            cfg.getList("time_scope")
                    );
                })
                .orElse(null);
        
        if (timeScope != null && !automaticTimeScope) {
            W.getTransitions().forEach(t -> {
                t.setTimeScope(
                        Range.closed(
                                (Long) timeScope.get(0), 
                                (Long) timeScope.get(1))
                );
            });
        }
        
        /**
         * TSG configuration (all TSGs)
         */
        Optional.ofNullable(config.getTable("TSG")).ifPresent(cfg -> {
            W.getTransitions().stream()
                .map(Transition::getTimeSeriesGuard)
                .flatMap((TSG tsg) -> {
                    if (tsg instanceof ComposedTSG) {
                        return ((ComposedTSG) tsg).flatten().stream();
                    } else {
                        return Stream.of(tsg);
                    }
                })                    
                .forEach((TSG x) -> {
                    x.configure(cfg);
                });
        });
        
        //Monotonic TSGs (deprectaed)
        /*
        Optional<Toml> configTable = Optional.ofNullable(
                config.getTable("monotonicTSG")
        );
        
        Optional<Long> minFulfillmentDuration = configTable
                .flatMap(x -> Optional.ofNullable(x.getLong("min_fulfillment_duration")));
        Optional<Double> epsilon = configTable
                .flatMap(x -> Optional.ofNullable(x.getDouble("epsilon")));
        Optional<Long> granularity = configTable
                .flatMap(x -> Optional.ofNullable(x.getLong("granularity")));
        Optional<Double> maxUnfulfillmentPercentage = configTable
                .flatMap(x -> Optional.ofNullable(x.getDouble("max_unfulfillment_percentage")));
        
        W.getTransitions()
                .stream()
                .map(Transition::getTimeSeriesGuard)
                .flatMap((TSG tsg) -> {
                    if (tsg instanceof ComposedTSG) {
                        return ((ComposedTSG) tsg).flatten().stream();
                    } else {
                        return Stream.of(tsg);
                    }
                })
                .filter(x -> x instanceof MonotonicTSG)
                .map(x -> (MonotonicTSG) x)
                .forEach(x -> {
                    minFulfillmentDuration.ifPresent(value -> x.setMinFulfillmentDuration(value));
                    maxUnfulfillmentPercentage.ifPresent(value -> x.setMaxUnfulfillmentPercentage(value));
                    epsilon.ifPresent(value -> x.setEpsilon(value));
                    granularity.ifPresent(value -> x.setGranularity(value));
                });
        */
        
        /**
         * Process model configuration
         */
        
        //Specific configuration for parts of the process model
        Optional.ofNullable(config.getTable("processModel"))
                .ifPresent(processModelCfg -> W.configure(processModelCfg));
        
        
        /**
         * Call the CC algorithm
         */
        ConformanceFunction CCResult = null;
        try {
            CCResult = Evaluation.completeCCTS(
                W, 
                L, 
                null, 
                W.createInitialMarking(new Long[]{x_0})
            );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error executing CC algorithm");
            e.printStackTrace();
            return;
        }

        //display the results
        LOG.log(Level.INFO,
                "CC algorithm returns :\n Evaluations: {0}\n>",
                new Object[]{
                    Arrays.toString(
                        Optional.ofNullable(CCResult)
                        .map(x -> x.values().toArray())
                        .orElse(Collections.EMPTY_SET.toArray())
                    )
                });

        //Save the results into a CSV file
        if (args.length >= 2) {
            try {
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]));
                CSVPrinter csvPrinter = new CSVPrinter(
                        writer,
                        CSVFormat.DEFAULT.withHeader(
                                "type", 
                                "task", 
                                "timeOfFirstFulfillment"
                        )
                );
                
                for (Map.Entry<Pair<Transition, Long>, ConformanceCategory >  c : 
                        Optional.ofNullable(CCResult)
                                .orElse(new ConformanceFunction())
                                .entrySet()
                        ) {
                    csvPrinter.printRecord(
                            c.getValue().toString(),
                            c.getKey().getKey().getLabel(),
                            c.getKey().getValue()
                    );
                }
                
                csvPrinter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        } else {
            LOG.log(Level.INFO, "A CSV output filepath was not specified.");
        }

        LOG.log(Level.INFO, "The experiment has run successfully");
        return;
    }

}
