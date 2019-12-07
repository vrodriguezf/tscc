/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import java.util.ArrayList;
import java.util.Arrays;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.utils.Pair;

/**
 *
 * @author victor
 */
public abstract class MonotonicTSG extends CountinuousConditionTSG {

    public static final Double DEFAULT_EPSILON = 1e-4;   

    public static <T> List<Pair<T, T>> consecutive(List<T> list) {
        List<Pair<T, T>> pairs = new LinkedList<>();
        list.stream().reduce((a, b) -> {
            pairs.add(new Pair<>(a, b));
            return b;
        });
        return pairs;
    }

    public static <T> List<Pair<T, T>> consecutive(Stream<T> list) {
        List<Pair<T, T>> pairs = new LinkedList<>();
        list.reduce((a, b) -> {
            pairs.add(new Pair<>(a, b));
            return b;
        });
        return pairs;
    }

    /**
     * TODO: Esto esta bien??
     *
     * @return
     */
    @Override
    public TSG getComplementary() {

        MonotonicTSG complementaryTSG = new MonotonicTSG(
                getEpsilon(),
                getEventType(),
                getMinFulfillmentDuration(), 
                getMaxUnfulfillmentPercentage(),
                getGranularity(), 
                getVarname()
        ) {

            @Override
            protected boolean evaluateConsecutiveRecords(Object r1, Object r2) {
                //return !f_evaluate_consecutive_records.apply(r1, r2);
                return !MonotonicTSG.this.evaluateConsecutiveRecords(r1, r2);
            }
        };
        return complementaryTSG;
    }

    //CONSTRUCTORS
    public MonotonicTSG(Double epsilon, LifecycleStage eventType, Long minFulfillmentDuration, Double maxUnfulfillmentPercentage, Long granularity, String varname) {
        super(eventType, minFulfillmentDuration, maxUnfulfillmentPercentage, granularity, varname);
        this.epsilon = epsilon;
    }

    public MonotonicTSG(String varname, LifecycleStage eventType) {
        super(varname, eventType);
        this.epsilon = DEFAULT_EPSILON;
    }

    public MonotonicTSG(String varname) {
        super(varname);
        this.epsilon = DEFAULT_EPSILON;
    }

    @Override
    public NavigableMap<Long, Boolean> evaluationMap(
            IndexedTSLog indexedTSLog,
            Long x1,
            Long x2
    ) {
        NavigableMap<Long, LogEntry> completeTimeSeries
                = indexedTSLog.getIndexedEntries().get(this.getVarname())
                        .subMap(x1, true, x2, true);

        List<Pair<LogEntry, LogEntry>> completeConsecutiveRecords
                = getConsecutiveRecordList(completeTimeSeries);

        return IntStream.range(0, completeConsecutiveRecords.size())
                .boxed()
                .collect(
                        Collectors.toMap(
                                (Integer i) -> completeConsecutiveRecords.get(i).getValue().getTs(),
                                (Integer i) -> evaluateListOfConsecutiveRecords(
                                        completeConsecutiveRecords.subList(0, i)
                                ),
                                (v1, v2) -> {
                                    throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                                },
                                TreeMap::new
                        )
                );
    }

    @Override
    public boolean evaluateCountinuousCondition(NavigableMap<Long, LogEntry> sortedLogEntries) {
        //Check extreme cases
        if (isExtremeCase(sortedLogEntries)) {
            return false;
        }

        // Apply granularity to the sorted log entries (filter)                
        // Apply the function over the stream of consecutive pairs
        // To get the stream of consecutive pairs, we first filter the input 
        // logEntries by varname and sort them by timestamp
        List<Pair<LogEntry, LogEntry>> consecutiveRecords
                = getConsecutiveRecordList(sortedLogEntries);

        return evaluateListOfConsecutiveRecords(consecutiveRecords);
    }

    @Override
    public Long timeOfFirstFulfillment(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        
        NavigableMap<Long, LogEntry> timeSeries = 
                applyGranularity(
                        indexedTSLog.getIndexedEntries().get(this.getVarname())
                ).subMap(x1, true, x2, true);
        
        List<Pair<LogEntry, LogEntry>> completeConsecutiveRecords
                = getConsecutiveRecordList(timeSeries);

        boolean TSG_fulfilled = false;
        int i;
        for (i = 0; i < completeConsecutiveRecords.size() && !TSG_fulfilled; i++) {
            TSG_fulfilled = evaluateListOfConsecutiveRecords(
                    completeConsecutiveRecords.subList(0, i)
            );
        }
        
        if (TSG_fulfilled) {
            Long fulfillmentTimeIndex = 
                    completeConsecutiveRecords.get(i).getValue().getTs();
            
            
            if (this.getEventType().equals(LifecycleStage.START))
                //Substract the minimum_fulfillment_duration
                //TODO: Esto no es formal!
                return timeSeries.ceilingKey(
                        fulfillmentTimeIndex - this.getMinFulfillmentDuration()
                );
            else 
                return fulfillmentTimeIndex;
        } else {
            return -1L;
        }
    }
    
    public boolean evaluateListOfConsecutiveRecords(
            List<Pair<LogEntry, LogEntry>> consecutiveRecords
    ) {

        TreeMap<Pair<Long, Long>, Boolean> consecutiveMonotonicity
                = consecutiveRecords.parallelStream()
                        .collect(
                                Collectors.toMap(
                                        (Pair<LogEntry, LogEntry> x) -> new Pair<>(x.getKey().getTs(), x.getValue().getTs()),
                                        (Pair<LogEntry, LogEntry> x) -> evaluateConsecutiveRecords(x.getKey().getRecord(), x.getValue().getRecord()),
                                        (v1, v2) -> {
                                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                                        },
                                        () -> new TreeMap<>((x1, x2) -> {
                                            return (int) (x1.getKey() - x2.getKey());
                                        })
                                )
                        );

        //Check if the monocity is fulfilled for a sufficient interval of time, 
        // according to the minimum state duratin of this guard (minFulfillmentDuration)
        // and the maximumNonFulfilmentPercentage
        Long fulfillmentAccumulator = 0L;
        Long unfulfillmentAccumulator = 0L;
        Long timeSpanBetweenRecords;
        for (Map.Entry<Pair<Long, Long>, Boolean> entry : consecutiveMonotonicity.entrySet()) {
            timeSpanBetweenRecords = (entry.getKey().getValue() - entry.getKey().getKey());
            if (entry.getValue() == true) {
                fulfillmentAccumulator += timeSpanBetweenRecords;
                if (fulfillmentAccumulator >= this.getMinFulfillmentDuration()) {
                    return true;
                }
            } else {
                unfulfillmentAccumulator += timeSpanBetweenRecords;
                if (((double) unfulfillmentAccumulator/getMinFulfillmentDuration()) > 
                        getMaxUnfulfillmentPercentage()) {
                    fulfillmentAccumulator = 0L;
                    unfulfillmentAccumulator = 0L;
                }
            }
        }

        return false;
    }

    @Override
    public void configure(Toml cfg) {
        super.configure(cfg);
        Optional.ofNullable(cfg.getDouble("epsilon")).ifPresent(x -> setEpsilon(x));
    }

    @Override
    protected boolean preEvaluationArrangements(IndexedTSLog L) {
        if (super.preEvaluationArrangements(L)) {
            //Set consecutive records in use
            setConsecutiveRecordsInUse(
                    getConsecutiveRecordList(getTimeSeriesInUse()).stream()
                            .collect(Collectors.toMap(
                                    x -> x.getKey().getTs(),
                                    Function.identity(),
                                    (x, y) -> {
                                        throw new RuntimeException("debug me!");
                                    },
                                    TreeMap::new
                            ))
            );
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean evaluate(IndexedTSLog L, Long x1, Long x2) {
        preEvaluationArrangements(L);

        return evaluateListOfConsecutiveRecords(
                new ArrayList<>(
                        getConsecutiveRecordsInUse().subMap(
                                x1,
                                true,
                                x2,
                                true
                        ).values()
                )
        );
    }
    

    /**
     * Getters & Setters
     */
    public Double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(Double epsilon) {
        if (epsilon < 0.0) throw new RuntimeException(
                "Epsilon must be greater or equal than zero"
        );
        this.epsilon = epsilon;
    }

    /**
     * Protected things
     */
    //e1 preceds e2
    protected abstract boolean evaluateConsecutiveRecords(Object r1, Object r2);

    protected NavigableMap<Long, Pair<LogEntry, LogEntry>> getConsecutiveRecordsInUse() {
        return consecutiveRecordsInUse;
    }

    protected void setConsecutiveRecordsInUse(
            NavigableMap<Long, Pair<LogEntry, LogEntry>> consecutiveRecordsInUse) {
        this.consecutiveRecordsInUse = consecutiveRecordsInUse;
    }
    
    /**
     * Private things
     */

    private List<Pair<LogEntry, LogEntry>> getConsecutiveRecordList(
            NavigableMap<Long, LogEntry> timeSeries
    ) {
        List<Pair<LogEntry, LogEntry>> result = new ArrayList<>();

        if (!timeSeries.isEmpty()) {
            result = consecutive(new ArrayList(timeSeries.values()));
        }

        /*
        if (getGranularity() != null) {
            result = consecutive(
                    LongStreamEx.iterate(timeSeries.firstKey(), l -> l + this.getGranularity())
                            .takeWhile(l -> l <= timeSeries.lastKey())
                            .mapToObj(timeIndex -> {
                                return timeSeries
                                        .ceilingEntry(timeIndex)
                                        .getValue();
                            })
                            .distinct()
            );
        } else {
            //Take every record of the object completeTimeSeries
            result = consecutive(
                    new ArrayList(timeSeries.values())
            );
        }
*/
        return result;
    }
    
    
    private Double epsilon;
    private NavigableMap<Long, Pair<LogEntry, LogEntry>> consecutiveRecordsInUse;
}
