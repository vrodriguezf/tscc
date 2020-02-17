/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import one.util.streamex.LongStreamEx;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;

/**
 *
 * @author victor
 */
public abstract class CountinuousConditionTSG extends UnivariateTSG {

    public static final Long DEFAULT_GRANULARITY = null;
    public static final Long DEFAULT_MIN_FULFILLMENT_DURATION = 0L;
    public static final Double DEFAULT_MAX_UNFULFILLMENT_PERCENTAGE = 0.0;

    //CONSTRUCTORS
    //All constructor
    public CountinuousConditionTSG(LifecycleStage eventType, Long minFulfillmentDuration, Double maxUnfulfillmentPercentage, Long granularity, String varname) {
        super(varname);
        this.eventType = eventType;
        this.minFulfillmentDuration = minFulfillmentDuration;
        this.maxUnfulfillmentPercentage = maxUnfulfillmentPercentage;
        this.granularity = granularity;
    }

    //Constructor with default values
    public CountinuousConditionTSG(String varname, LifecycleStage stage) {
        super(varname);
        this.eventType = stage;

        //Default values
        this.minFulfillmentDuration = DEFAULT_MIN_FULFILLMENT_DURATION;
        this.maxUnfulfillmentPercentage = DEFAULT_MAX_UNFULFILLMENT_PERCENTAGE;
        this.granularity = DEFAULT_GRANULARITY;
    }

    public CountinuousConditionTSG(String varname) {
        this(varname, LifecycleStage.END);
    }

    @Override
    public boolean evaluateUnivariateTimeSeries(
            NavigableMap<Long, LogEntry> timeSeries) {

        // Filter the sorted log entries by granularity
        NavigableMap<Long, LogEntry> filteredTimeSeries
                = applyGranularity(timeSeries);

        //Call the abstract method to evaluate
        return evaluateCountinuousCondition(filteredTimeSeries);

        //TODO: Make the minFulfilmnetDuration - maxUnfulilment thign here?
        //generally?
    }

    @Override
    protected boolean preEvaluationArrangements(IndexedTSLog L) {
        if (super.preEvaluationArrangements(L)) {
            setTimeSeriesInUse(
                    applyGranularity(L.getIndexedEntries().get(this.getVarname()))
            );
            return true;
        } else 
            return false;
    }
    

    /**
     * Overrided for efficiency (use of cached apply granularity).
     *
     * @param L
     * @param x1
     * @param x2
     * @return
     */
    @Override
    public boolean evaluate(IndexedTSLog L, Long x1, Long x2) {

        preEvaluationArrangements(L);

        return evaluateCountinuousCondition(
                getTimeSeriesInUse().subMap(x1, true, x2, true)
        );
    }

    @Override
    public Long timeOfFirstFulfillment(
            IndexedTSLog indexedTSLog,
            Long x1,
            Long x2) {

        //Apply granularity in an efficient way (use always the base indexedTSLog
        NavigableMap<Long, LogEntry> timeSeries
                = applyGranularity(
                        indexedTSLog.getIndexedEntries().get(this.getVarname())
                ).subMap(x1, true, x2, true);

        Iterator<Long> record_times = timeSeries.navigableKeySet().iterator();
        Long upper_time_index = -1L;
        boolean TSG_fulfilled = false;
        while (record_times.hasNext() && !TSG_fulfilled) {
            upper_time_index = record_times.next();
            TSG_fulfilled = evaluateCountinuousCondition(
                    timeSeries.subMap(
                            x1,
                            true,
                            upper_time_index,
                            true)
            );
        }

        return (TSG_fulfilled ? upper_time_index : -1L);
    }

    public abstract boolean evaluateCountinuousCondition(
            NavigableMap<Long, LogEntry> timeSeries);

    @Override
    public void configure(Toml cfg) {
        super.configure(cfg); //To change body of generated methods, choose Tools | Templates.
        //Event type
        Optional.ofNullable(cfg.getString("event_type")).ifPresent(x -> {
            Arrays.stream(LifecycleStage.values())
                    .filter(eType -> eType.toString().equals(x))
                    .findFirst()
                    .ifPresent(x2 -> setEventType(x2));
        });
        Optional.ofNullable(cfg.getLong("min_fulfillment_duration")).ifPresent(x -> setMinFulfillmentDuration(x));
        Optional.ofNullable(cfg.getDouble("max_unfulfillment_percentage")).ifPresent(x -> setMaxUnfulfillmentPercentage(x));
        Optional.ofNullable(cfg.getLong("granularity")).ifPresent(x -> setGranularity(x));
    }

    /**
     * Getters & Setters
     */
    public LifecycleStage getEventType() {
        return eventType;
    }

    public void setEventType(LifecycleStage eventType) {
        this.eventType = eventType;
    }

    public Long getGranularity() {
        return granularity;
    }

    public void setGranularity(Long granularity) {
        this.granularity = granularity;
    }

    public Long getMinFulfillmentDuration() {
        return minFulfillmentDuration;
    }

    public void setMinFulfillmentDuration(Long minDuration) {
        this.minFulfillmentDuration = minDuration;
    }

    public Double getMaxUnfulfillmentPercentage() {
        return maxUnfulfillmentPercentage;
    }

    public void setMaxUnfulfillmentPercentage(Double maxUnfulfillmentPercentage) {
        this.maxUnfulfillmentPercentage = maxUnfulfillmentPercentage;
    }

    /**
     * Protected things
     */
    /**
     * Protected things
     */
    protected boolean isExtremeCase(NavigableMap<Long, LogEntry> sortedLogEntries) {
        //Caso extremo: 1 solo registro -> No se pede evaluar monotonia
        if (sortedLogEntries.size() <= 1) {
            return true;
        }

        //Extreme case 2: the distance between the earliest and the latest entry
        //is less than the granularity of this TSG
        if ((sortedLogEntries.lastKey() - sortedLogEntries.firstKey()) < this.getGranularity()) {
            return true;
        }

        //If it does not hold the previous constraints, it is a normal case
        return false;
    }

    protected NavigableMap<Long, LogEntry> applyGranularity(
            NavigableMap<Long, LogEntry> timeSeries) {

        NavigableMap<Long, LogEntry> result;
        NavigableMap<Long, LogEntry> cachedResult = granularityCache.get(timeSeries);

        if (timeSeries == null) {
            result = new TreeMap<Long, LogEntry>();
        } else if (timeSeries.isEmpty()) {
            result = timeSeries;
        } else if (cachedResult != null) {
            result = cachedResult;
        } else {
            NavigableMap<Long, LogEntry> tsWithGranularity
                    = LongStreamEx.iterate(timeSeries.firstKey(), l -> l + this.getGranularity())
                            .takeWhile(l -> l <= timeSeries.lastKey())
                            .mapToObj(timeIndex -> timeSeries.ceilingEntry(timeIndex))
                            .distinct()
                            .collect(
                                    Collectors.toMap(
                                            Map.Entry::getKey,
                                            Map.Entry::getValue,
                                            (x, y) -> {
                                                throw new RuntimeException("debug me!");
                                            },
                                            TreeMap::new
                                    )
                            );

            granularityCache.put(timeSeries, tsWithGranularity);
            result = tsWithGranularity;
        }

        return result;
    }

    /**
     * Private things
     */
    private LifecycleStage eventType;
    private Long minFulfillmentDuration;
    private Double maxUnfulfillmentPercentage;
    private Long granularity;
    private Map<NavigableMap<Long, LogEntry>, NavigableMap<Long, LogEntry>> 
            granularityCache = new HashMap<>();
}
