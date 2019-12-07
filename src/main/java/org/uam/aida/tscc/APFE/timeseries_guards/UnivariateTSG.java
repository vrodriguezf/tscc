/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;

/**
 * An UnivariateRSG evaluates a condition in a single variable of
 * a time series log.
 *
 * @author victor
 */
public abstract class UnivariateTSG extends TSG {
    
    public UnivariateTSG(String varname) {
        this.varname = varname;
    }

    @Override
    public Collection<String> getInvolvedVariables() {
        return Arrays.asList(this.getVarname());
    }

    @Override
    /**
     * NOTE: Calling this function in a loop is inefficient due to
     * it performs a sorting of the log entries
     */
    public boolean evaluate(Collection<LogEntry> logEntries) {
        
        if (logEntries.size() == 0) return false;
        
        TreeMap<Long, LogEntry> sortedLogEntries = logEntries.stream()
                .filter(le -> le.getVarname().equals(this.getVarname()))
                .collect(
                        Collectors.toMap(
                              (LogEntry le) -> le.getTs(),
                              (LogEntry le) -> le,
                              (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));},
                              TreeMap::new
                        )
                );
        
        return evaluateUnivariateTimeSeries(sortedLogEntries);
    }

    /**
     * Useful for efficiency
     * @param L
     * @return 
     */
    @Override
    public boolean evaluate(IndexedTSLog L) {
        return evaluateUnivariateTimeSeries(
                L.getIndexedEntries().getOrDefault(
                        getVarname(),
                        new TreeMap<Long, LogEntry>()
                )
        );
    }
    

    @Override
    public NavigableMap<Long, Boolean> evaluationMap(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        NavigableMap<Long, LogEntry> completeTimeSeries
                = indexedTSLog.getIndexedEntries().get(varname)
                        .subMap(x1, true, x2, true);
        
        return 
                completeTimeSeries.navigableKeySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                (Long upperTimeIndex) -> this.evaluateUnivariateTimeSeries(
                                        completeTimeSeries.subMap(
                                                x1,
                                                true,
                                                upperTimeIndex,
                                                true)
                                ),
                                (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));},
                                TreeMap::new
                        )
                );
    }

    /**
     * @param indexedTSLog
     * @param x1
     * @param x2
     * @return 
     */
    @Override
    public Long timeOfFirstFulfillment(IndexedTSLog indexedTSLog, Long x1, Long x2) {
                NavigableMap<Long, LogEntry> completeTimeSeries
                = indexedTSLog.getIndexedEntries().get(varname)
                        .subMap(x1, true, x2, true);

        Iterator<Long> record_times = completeTimeSeries.navigableKeySet().iterator();
        Long upper_time_index = -1L;
        boolean TSG_fulfilled = false;
        while (record_times.hasNext() && !TSG_fulfilled) {
            upper_time_index = record_times.next();
            TSG_fulfilled = evaluateUnivariateTimeSeries(
                    completeTimeSeries.subMap(
                            x1,
                            true,
                            upper_time_index,
                            true)
            );
        }

        return (TSG_fulfilled ? upper_time_index : -1L);
    }

    //logEntries here are sorted and filtered (only one varname is included)
    public abstract boolean evaluateUnivariateTimeSeries(
            NavigableMap<Long, LogEntry> timeSeries
    );

    @Override
    public TSG getComplementary() {
        UnivariateTSG self = this;
        
        return new UnivariateTSG(varname) {
            @Override
            public boolean evaluateUnivariateTimeSeries(NavigableMap<Long, LogEntry> sortedLogEntries) {
                return !self.evaluateUnivariateTimeSeries(sortedLogEntries);
            }
        };
    }

    @Override
    public void configure(Toml cfg) {
        super.configure(cfg);
    }
    

    public String getVarname() {
        return varname;
    }
    
    /**
     * Protected things
     */
    protected NavigableMap<Long, LogEntry> getTimeSeriesInUse() {
        return timeSeriesInUse;
    }

    protected void setTimeSeriesInUse(NavigableMap<Long, LogEntry> lastTimeSeriesEvaluated) {
        this.timeSeriesInUse = lastTimeSeriesEvaluated;
    }
    
    /**
     * Private things
     */

    private String varname;
    private NavigableMap<Long, LogEntry> timeSeriesInUse;
}
