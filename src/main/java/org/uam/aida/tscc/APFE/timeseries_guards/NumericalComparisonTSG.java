/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;

/**
 *
 * @author victor
 */
public abstract class NumericalComparisonTSG extends PointByPointTSG {

    public static final Double DEFAULT_EPSILON = 1e-4;
    public static final boolean DEFAULT_STRICT = false;   
    
    public NumericalComparisonTSG(
            Double value, 
            boolean strict, 
            Double epsilon, 
            LifecycleStage eventType, 
            Long minFulfillmentDuration, 
            Double maxUnfulfillmentPercentage, 
            Long granularity, 
            String varname) {
        super(eventType, minFulfillmentDuration, maxUnfulfillmentPercentage, granularity, varname);
        this.value = value;
        this.strict = strict;
        
        if (epsilon < 0.0) 
            throw new RuntimeException("Epsilon must be greater or equal than zero");        
        this.epsilon = epsilon;
    }

    public NumericalComparisonTSG(Double value, String varname, LifecycleStage stage) {
        super(varname, stage);
        this.value = value;
    }

    public NumericalComparisonTSG(Double value, String varname) {
        super(varname);
        this.value = value;
    }

    public NumericalComparisonTSG(Double value, boolean strict, String varname) {
        super(varname);
        this.value = value;
        this.strict = strict;
    }

    public NumericalComparisonTSG(Double value, boolean strict, String varname, LifecycleStage stage) {
        super(varname, stage);
        this.value = value;
        this.strict = strict;
    }

    public abstract boolean isComparisonFulfilled(Double timeSeriesRecord, Double value);

    @Override
    public NavigableMap<Long, Boolean> pointByPointEvaluation(
            NavigableMap<Long, LogEntry> sortedLogEntries) {
        
        return 
                sortedLogEntries.entrySet().stream()
                .collect(Collectors.toMap(
                        (Map.Entry<Long, LogEntry> e) -> e.getKey(),
                        (Map.Entry<Long, LogEntry> e) -> isComparisonFulfilled(
                                e.getValue().getRecordAsDouble(), 
                                value
                        ),
                        (x,y) -> {throw new RuntimeException("debug me!");},
                        TreeMap::new)
                );
    }

    @Override
    public void configure(Toml cfg) {
        super.configure(cfg); //To change body of generated methods, choose Tools | Templates.
        Optional.ofNullable(cfg.getDouble("epsilon")).ifPresent(x -> setEpsilon(x));
        Optional.ofNullable(cfg.getBoolean("strict")).ifPresent(x -> setStrict(x));
        Optional.ofNullable(cfg.getDouble("value")).ifPresent(x -> setValue(x));
    }
    
    /**
     * Getters & Setters
     */
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public Double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(Double epsilon) {
        if (epsilon < 0.0) 
            throw new RuntimeException("Epsilon must be greater or equal than zero");        
        this.epsilon = epsilon;
    }
    
    private Double value;
    private boolean strict = DEFAULT_STRICT;
    private Double epsilon = DEFAULT_EPSILON;
}
