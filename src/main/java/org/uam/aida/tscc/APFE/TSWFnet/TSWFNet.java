/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import com.google.common.collect.Maps;
import com.moandjiezana.toml.Toml;
import org.uam.aida.tscc.APFE.OPResponse;
import org.uam.aida.tscc.APFE.input.models.OPInfo;
import org.uam.aida.tscc.APFE.WFnet.WorkflowNet;
import org.uam.aida.tscc.APFE.time_series_log.TimeSeriesLog;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import org.uam.aida.tscc.APFE.utils.Helpers;
import org.uam.aida.tscc.APFE.utils.SetOperations;
import org.uam.aida.tscc.business.Place;
import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.Transition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import one.util.streamex.LongStreamEx;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.timeseries_guards.MonotonicTSG;
import static org.uam.aida.tscc.APFE.timeseries_guards.MonotonicTSG.consecutive;
import org.uam.aida.tscc.APFE.timeseries_guards.UnivariateTSG;
import org.uam.aida.tscc.APFE.utils.Pair;
import org.uam.aida.tscc.config.Configurable;

/**
 *
 * @author victor
 */
public class TSWFNet extends WorkflowNet implements Configurable<Toml> {

    private static final Logger LOG = Logger.getLogger(TSWFNet.class.getName());

    public TSWFNet(TimeSeriesLog dataLog) {
        this.dataLog = dataLog;
        TFFCache = new HashMap<>();
    }
    
    public TSWFNet() {
        this(null);
    }

    /**
     *
     * @return Estimated Procedure Duration (seconds)
     */
    public long getEstimatedDuration() {
        return 15000;
    }

    /**
     * Create initial marking of empty tokens (no object associated)
     *
     * @param initialTimestamps
     * @return
     */
    public Marking createInitialMarking(Long[] initialTimestamps) {
        Marking m0;
        m0 = new Marking();

        for (Long ts : initialTimestamps) {
            m0.add(getInputPlace(), new Token(null, ts));
        }

        return m0;
    }

    /**
     *
     * @param responses
     * @return
     * @deprecated
     */
    public MarkingOld createInitialMarking(Collection<OPResponse> responses) {
        MarkingOld m0;
        m0 = new MarkingOld();

        for (OPResponse response : responses) {
            m0.add(getInputPlace(), new Token(response, response.OPTriggering.getTriggerTs()));
        }

        /*
        responses.forEach((response) -> {
            m0.add(getInputPlace(), new Token(response,response.OPTriggering.getTriggerTs()));
        });
         */
        return m0;
    }

    public MarkingOld createFinalMarking(Collection<OPResponse> responses) {
        MarkingOld m = new MarkingOld();
        responses.stream().forEach((OPResponse response) -> {
            m.add(getOutputPlace(),
                    new Token(
                            response,
                            response.OPTriggering.getTriggerTs() + getEstimatedDuration())
            );
        });

        return m;
    }

    /**
     * Gets the net place of a given place (NOTE: If the place has multiple next
     * places, only the first will be retrieved.
     *
     * @param p
     * @return
     */
    public Place getNextPlace(Place p) {
        return (Place) super.nextPlaces(p).toArray()[0];
    }

    public StepType getStepType(Transition t) {

        StepType result;

        //AND routing blocks are part of concurrent container steps
        if (t.getLabel().startsWith("AND")) {
            return StepType.CONCURRENT_CONTAINER;
        }

        Optional<OPInfo> opInfo = this.getOPInfo();
        if (opInfo.isPresent()) {
            result = Optional.ofNullable(opInfo.get().findStep(t.getLabel()))
                    .flatMap(s -> Optional.ofNullable(s.getType()))
                    .map(s -> StepType.valueOf(s))
                    .orElse(StepType.UNKNOWN);
        } else {
            // The transition label assigns the step type
            if (t.getLabel().startsWith("ACTION")) {
                result = StepType.ACTION;
            } else if (t.getLabel().startsWith("CHECK") || t.getLabel().startsWith("!CHECK")) {
                result = StepType.CHECK;
            } else if (t.getLabel().startsWith("VERIFY") || t.getLabel().startsWith("!VERIFY")
                    || t.getLabel().startsWith("SUPERVISION")) {
                result = StepType.SUPERVISION;
            } else {
                result = StepType.UNKNOWN;
            }
        }

        return result;
    }

    /**
     * Retrieve the maximum step duration of a transition from the OP info. the
     * OP info is stored in a MongoDB database The link between a WFNET_OP
     * object and the OP in the database comes from the label of each transition
     * with the label of each step in the OP
     *
     * @param t
     * @return
     */
    /*
    public long getStepDurationMean(Transition t) throws Exception {
        long result, defaultStepDuration;
        
        if (getStepType(t).equals(StepType.CHECK))
            defaultStepDuration = Constants.DEFAULT_CHECK_DURATION;
        else if (getStepType(t).equals(StepType.ACTION))
            defaultStepDuration = Constants.DEFAULT_ACTION_DURATION;
        else if (getStepType(t).equals(StepType.SUPERVISION))
            defaultStepDuration = Constants.DEFAULT_SUPERVISION_DURATION;
        else if (getStepType(t).equals(StepType.CONCURRENT_CONTAINER))
            defaultStepDuration = Constants.DEFAULT_CONCURRENT_CONTAINER_DURATION;
        else
            throw new Exception("Transition " + t.getLabel() + " has no associated Step Type");
        
        //Get information about the procedure
        OPInfo eopInfo = this.getOPInfo().get();
        if (eopInfo == null) {
            LOG.log(Level.WARNING,"EOP information for {0} not found.",
                    new Object[]{
                        this.getClass().getSimpleName()
                    }
            );
            result = defaultStepDuration;
        } else {
            OPInfo.Step s = eopInfo.findStep(t.getLabel());
            if (s == null) {
                LOG.log(
                    Level.WARNING,"Step {0} not found in EOP info.",
                    new Object[]{t.getLabel()}
                );                    
                result = defaultStepDuration;
            } else {             
                if (s.getMaximumStepDuration()== null) {
                    LOG.log(
                        Level.WARNING,"Step {0} has no Maximum Step Duration (returning default).",
                        new Object[]{t.getLabel()}
                    );
                    result = defaultStepDuration;
                } else {
                    result = s.getMaximumStepDuration().longValue();
                }
            }
        }
        return result;
    }
     */
    /**
     *
     * @param t
     * @return
     */
    public long getStepDurationDeviation(Transition t) {
        if (!getStepType(t).equals(StepType.ACTION)) {
            return 0;
        } else {
            return 10000;
        }
    }

    /**
     * @deprecated @param t
     * @param M
     * @param chi
     * @param x_out
     * @return
     */
    public Marking fireTransition(Transition t,
            Marking M,
            Set<Token> chi,
            Long x_out) {

        Marking Mprime = new Marking();
        Set<Place> input_t, t_output; //Input and output places of t
        input_t = new HashSet<Place>(t.getInputPlaces(this));
        t_output = new HashSet<Place>(t.getOutputPlaces());

        this.getPlaces().forEach((Place p) -> {
            Set<Token> chiPrime;
            if (SetOperations.difference(input_t, t_output).contains(p)) {
                // M(p)\kappa if p in .t\t.
                chiPrime = SetOperations.difference(
                        M.getOrDefault(p, Collections.<Token>emptySet()),
                        chi
                );
            } else if (SetOperations.difference(t_output, input_t).contains(p)) {
                // M(p) U kappa if p in t.\.t
                chiPrime = SetOperations.union(
                        M.getOrDefault(p, Collections.<Token>emptySet()),
                        new HashSet<Token>(Arrays.asList(new Token(null, x_out)))
                );
            } else if (SetOperations.intersection(input_t, t_output).contains(p)) {
                chiPrime = SetOperations.union(
                        SetOperations.difference(
                                M.getOrDefault(p, Collections.<Token>emptySet()),
                                chi
                        ),
                        new HashSet<Token>(Arrays.asList(new Token(null, x_out)))
                );
            } else {
                chiPrime = M.getOrDefault(
                        p,
                        Collections.<Token>emptySet()
                );
            }

            if (!chiPrime.isEmpty()) {
                Mprime.put(p, chiPrime);
            }
        });

        return Mprime;
    }

    /**
     * @param t
     * @param M
     * @param I
     * @param x_out
     * @return
     */
    public Marking fireTransition(Transition t,
            Marking M,
            Map<Place, Token> I,
            Long x_out) {

        Marking Mprime = new Marking();
        Set<Place> input_t, t_output; //Input and output places of t
        input_t = new HashSet<Place>(t.getInputPlaces(this));
        t_output = new HashSet<Place>(t.getOutputPlaces());

        //Check that the input funcion is well defined
        if (input_t.stream().anyMatch(p -> !I.containsKey(p))) {
            throw new IllegalArgumentException("Bad argument I");
        }

        this.getPlaces().forEach((Place p) -> {
            Set<Token> chiPrime;
            if (SetOperations.difference(input_t, t_output).contains(p)) {
                // M(p)\kappa if p in .t\t.
                chiPrime = SetOperations.difference(
                        M.getOrDefault(p, Collections.<Token>emptySet()),
                        new HashSet<Token>(Arrays.asList(I.get(p)))
                );
            } else if (SetOperations.difference(t_output, input_t).contains(p)) {
                // M(p) U kappa if p in t.\.t
                chiPrime = SetOperations.union(
                        M.getOrDefault(p, Collections.<Token>emptySet()),
                        new HashSet<Token>(Arrays.asList(new Token(null, x_out)))
                );
            } else if (SetOperations.intersection(input_t, t_output).contains(p)) {
                chiPrime = SetOperations.union(
                        SetOperations.difference(
                                M.getOrDefault(p, Collections.<Token>emptySet()),
                                new HashSet<Token>(Arrays.asList(I.get(p)))
                        ),
                        new HashSet<Token>(Arrays.asList(new Token(null, x_out)))
                );
            } else {
                chiPrime = M.getOrDefault(
                        p,
                        Collections.<Token>emptySet()
                );
            }

            if (!chiPrime.isEmpty()) {
                Mprime.put(p, chiPrime);
            }
        });

        return Mprime;
    }

    /**
     * TODO: Esta funcion deberia hcer queries de acciones teniendo en cuenta
     * una cola de timestamps con los ultimos firings del modelo
     *
     * @deprecated
     * @param alertResp
     * @param startTime
     * @param endTime
     * @param params
     * @param aEvaluator
     * @return
     */
    public static Long getActionTriggerTimestamp(
            OPResponse alertResp,
            Long startTime,
            Long endTime,
            Map<String, Object> params,
            ActionEvaluator aEvaluator) {

        Long result = null;
        Long resultBeforeStart = null;

        //TODO: ¡Establecer las condiciones extremas de start time y end time desde aqui?
        if (endTime == null) {
            endTime = alertResp.getOPTriggering().getEndTime();
        }

        result = aEvaluator.getFirstActionTimestamp(alertResp,
                startTime,
                endTime,
                params);

        if (result == null) {
            return result;
        } else {
            //Busco la accion desde el comienzo de la alerta hasta el start time
            //TODO Mejorar esto con la cola de firing timestamps
            resultBeforeStart = aEvaluator.getFirstActionTimestamp(
                    alertResp,
                    alertResp.getOPTriggering().getTriggerTs(),
                    startTime,
                    params
            );

            if (resultBeforeStart == null) {
                return result;
            } else {
                return null; //si se encuentra antes, no cuenta
            }
        }
    }

    /**
     * TFF optimized with cache
     * @param t
     * @param x_in
     * @return
     * @throws Exception 
     */
    public Long timeOfFirstFulfillment(Transition t, Long x_in) {

        Pair<Transition, Long> cacheKey = Pair.create(t, x_in);
        Long cachedTFF = this.getTFFCache().get(cacheKey);
        Long result;
        
        if (cachedTFF == null) {
            result = _timeOfFirstFulfillment(t, x_in);
            this.getTFFCache().put(cacheKey, result);
        } else {
            result = cachedTFF;
        }
        
        return result;
    }

    /**
     * NOTE: It is assumed that the log and the case to use is set Time of first
     * fulfillment
     *
     * @param delta
     * @param t
     * @param x_in
     * @param GC
     * @return
     * @throws Exception
     */
    private Long _timeOfFirstFulfillment(Transition t, Long x_in) {

        Long result = null;

        //Convert relative timescope to absoute time scope
        Long x1_absolute = x_in + Optional.ofNullable(t.getTimeScope())
                .map(ts -> ts.lowerEndpoint())
                .orElse(0L);
        Long x2_absolute = x_in + Optional.ofNullable(t.getTimeScope())
                .map(ts -> ts.upperEndpoint())
                .orElse(0L);

        //Optimization for IndexedTSLogs
        if (this.getTimeSeriesLog() instanceof IndexedTSLog) {
            result = t.getTimeSeriesGuard().timeOfFirstFulfillment(
                    (IndexedTSLog) this.getTimeSeriesLog(),
                    x1_absolute,
                    x2_absolute);
        } else {
            // Since we have to compute the log state from x_in to x_in+mu_t 
            //increasingly, we do it for every timestamp when the log state has some 
            // new records
            Collection<String> involvedVariables = t.getTimeSeriesGuard().getInvolvedVariables();
            List<Long> record_times = new ArrayList<>(Arrays.asList(x_in));
            if (involvedVariables != null) {
                record_times.addAll(
                        this.getTimeSeriesLog().getEntries().stream()
                                .filter(e -> e.getTs() > x1_absolute)
                                .filter(e -> e.getTs() <= (x2_absolute))
                                .filter(e -> involvedVariables.contains(e.getVarname()))
                                .map(LogEntry::getTs)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList())
                );
            } else {
                record_times.addAll(
                        this.getTimeSeriesLog().getEntries().stream()
                                .filter(e -> e.getTs() > x1_absolute)
                                .filter(e -> e.getTs() <= (x2_absolute))
                                .map(LogEntry::getTs)
                                .distinct()
                                .sorted()
                                .collect(Collectors.toList())
                );
            }

            boolean TSG_fulfilled = false;
            int i = 1;
            while (i <= record_times.size() && !TSG_fulfilled) {
                Collection<Long> subRecordTimes = record_times.subList(0, i);
                Collection<LogEntry> logState;

                if (involvedVariables != null) {

                    Collection<List<Object>> xvPerms
                            = Helpers.<Object>permutations(
                                    Arrays.asList(
                                            subRecordTimes.stream().map(o -> (Object) o).collect(Collectors.toSet()),
                                            involvedVariables.stream().map(o -> (Object) o).collect(Collectors.toSet())
                                    )
                            );

                    logState = xvPerms.stream()
                            .map((List<Object> xv) -> {
                                return this.getTimeSeriesLog().logState(
                                        (Long) xv.get(0),
                                        (String) xv.get(1)
                                );
                            })
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet());
                } else {
                    logState = subRecordTimes.stream()
                            .map(x -> this.getTimeSeriesLog().logState(x))
                            .flatMap(x -> x.stream())
                            .collect(Collectors.toSet());
                }

                TSG_fulfilled = t.getTimeSeriesGuard().evaluate(logState);
                if (!TSG_fulfilled) {
                    i++;
                }
            }

            if (TSG_fulfilled) {
                result = record_times.get(i - 1);
            } else {
                result = -1L;
            }
        }

        LOG.log(Level.INFO, "TFF({0},{1}, [{2},{3}]) = {4}",
                new Object[]{t.getLabel(), x_in, x1_absolute, x2_absolute, result});
        return result;
    }

    /**
     *
     * @param m
     * @return
     */
    public Marking executeDynamics(Marking M_in, List<Firing> firingRecord) {

        Marking M = M_in;

        //Invalid conditions (IC)
        Collection<Pair<Transition, Token>> IC = new HashSet<>();

        //Main loop (WFNETOP execution)
        boolean finished = false;

        while (!finished) {
            Marking M_loop = M;
            Collection<Transition> enabledTransitions = this.enabledTransitions(M);
            Collection<Transition> validTransitions = enabledTransitions.stream()
                    .filter((Transition t) -> {
                        return t.getInputPlaces(this).stream()
                                .allMatch((Place p) -> {
                                    return M_loop.get(p).stream()
                                            .anyMatch((Token k) -> {
                                                return !IC.contains(new Pair(t, k));
                                            });
                                });
                    })
                    .collect(Collectors.toList());

            if (validTransitions.isEmpty()) {
                finished = true;
            } else {
                Transition t = Helpers.getRandom(validTransitions).get();

                //Input mapper for the possible firing
                Map<Place, Token> I = Maps.newHashMap();

                //Set of valid tokens (kappa)
                //Set<Token> kappa = new HashSet<>();
                //Times of completion (tau)
                Collection<Pair<Token, Long>> tau = new ArrayList<>();

                for (Place p : t.getInputPlaces(this)) {
                    Token k = M.get(p).stream()
                            .filter((Token kprime) -> {
                                return !IC.contains(new Pair(t, kprime));
                            })
                            .max((Token k1, Token k2) -> {
                                return (int) (k1.getTimestamp() - k2.getTimestamp());
                            })
                            .orElseThrow(RuntimeException::new);

                    I.put(p, k);
                    tau.add(new Pair<Token, Long>(
                            k,
                            this.timeOfFirstFulfillment(t, k.getTimestamp())
                    )
                    );
                }

                //Fire only if every input valid token has a time of completion 
                //different than -1, i.e, chech if there is any invalid condition.
                //if so, add them to the set of invalid conditions
                Collection<Pair<Transition, Token>> newInvalidConditions
                        = tau.stream()
                                .filter((Pair<Token, Long> x) -> x.getValue() == -1L)
                                .map(Pair::getKey)
                                .map((Token k) -> new Pair<>(t, k))
                                .collect(Collectors.toList());

                if (newInvalidConditions.isEmpty()) {
                    //Firing
                    Long x_out = tau.stream()
                            .mapToLong(Pair<Token, Long>::getValue)
                            .max()
                            .orElseThrow(RuntimeException::new);

                    Marking Mprime = this.fireTransition(t, M, I, x_out);
                    if (firingRecord != null) {
                        firingRecord.add(new Firing(t, M, I, x_out));
                    }
                    M = Mprime;
                } else {
                    IC.addAll(newInvalidConditions);
                }
            }
        } //endwhile

        //Get final Marking
        return M;
    }

    /**
     * TODO: Este metodo tiene que usar TFFs cacheados
     *
     * @param M
     * @return
     * @throws Exception
     */
    public Collection<Deadlock> getDeadlocks(Marking M) throws Exception {
        return M.entrySet()
                .stream()
                .filter(e -> !e.getValue().isEmpty())
                .filter(e -> !e.getKey().equals(this.getOutputPlace()))
                .flatMap((Map.Entry<Place, Set<Token>> e) -> {
                    //Create the deadlock for each locked token
                    return e.getValue()
                            .stream()
                            .flatMap((Token x) -> {
                                //Each token may have several deadlocks if its
                                //place is connected to more than one output transition (p-bullet)
                                return e.getKey().getNextTransitions(this)
                                        .stream()
                                        .filter((Transition t) -> {
                                            Long TFF;
                                            try {
                                                //Check if TFF(t,x) == 1
                                                TFF = timeOfFirstFulfillment(t, x.getTimestamp());
                                            } catch (Exception ex) {
                                                throw new RuntimeException(ex);
                                            }
                                            return (TFF.equals(-1L));
                                        })
                                        .map(t -> new Deadlock(t, e.getKey(), x));
                            });
                })
                .collect(Collectors.toList());
    }

    /**
     *
     * @param t
     * @param x
     * @return
     */
    public boolean isValidContext(Transition t, Long x) {
        Long tff;
        try {
            tff = timeOfFirstFulfillment(t, x);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return (tff != -1L);
    }

    public boolean isValidContext(Transition t, Marking M) {
        return t.getInputPlaces(this).stream()
                .allMatch((Place p) -> {
                    return M.get(p).stream()
                            .map(Token::getTimestamp)
                            .anyMatch(x -> isValidContext(t, x));
                });
    }

    /**
     * Getters & Setters
     */
    public TimeSeriesLog getTimeSeriesLog() {
        return dataLog;
    }

    public void setDataLog(TimeSeriesLog dataLog) {
        this.dataLog = dataLog;
    }

    public Optional<OPInfo> getOPInfo() {
        return Optional.ofNullable(opInfo);
    }

    public void setOpInfo(OPInfo opInfo) {
        this.opInfo = opInfo;
    }

    public Map<Pair<Transition, Long>, Long> getTFFCache() {
        return TFFCache;
    }

    @Override
    public void configure(Toml cfg) {

        Optional.ofNullable(cfg.getString("class_name")).ifPresent(s -> {
            Optional.ofNullable(cfg.getTables("activities"))
                    .orElse(new ArrayList<Toml>())
                    .forEach((Toml activity_cfg) -> {
                        Optional.ofNullable(activity_cfg.getString("transition.id"))
                                .ifPresent(tId -> {
                                    this.getTransitions().stream()
                                            .filter(t -> t.getId().equals(tId))
                                            .findFirst()
                                            .ifPresent(t -> t.configure(
                                            activity_cfg.getTable("transition")
                                    ));
                                });
                    });
        });
    }

    /**
     * Call directly to evaluateUnivariateTimeSeries. Avoid sorting the log
     * entries in each iteration of the tff loop ASsume an Indexed TSLog
     *
     * @param sTSG
     * @param absolute_x1
     * @param absolute_x2
     * @return
     */
    private Long TFFStateDrivenTSG(
            UnivariateTSG sTSG,
            Long absolute_x1,
            Long absolute_x2) {

        //Check if more optimization is possible for subclasses of UnivariateTSG
        if (sTSG instanceof MonotonicTSG) {
            return TFFMonotonicTSG((MonotonicTSG) sTSG,
                    absolute_x1,
                    absolute_x2
            );
        }

        IndexedTSLog indexedTSLog = (IndexedTSLog) this.getTimeSeriesLog();
        String varname = sTSG.getInvolvedVariables().iterator().next();

        NavigableMap<Long, LogEntry> completeTimeSeries
                = indexedTSLog.getIndexedEntries().get(varname)
                        .subMap(absolute_x1, true, absolute_x2, true);

        Iterator<Long> record_times = completeTimeSeries.navigableKeySet().iterator();
        Long upper_time_index = -1L;
        boolean TSG_fulfilled = false;
        while (record_times.hasNext() && !TSG_fulfilled) {
            upper_time_index = record_times.next();
            TSG_fulfilled = sTSG.evaluateUnivariateTimeSeries(
                    completeTimeSeries.subMap(
                            absolute_x1,
                            true,
                            upper_time_index,
                            true)
            );
        }

        return (TSG_fulfilled ? upper_time_index : -1L);
    }

    /**
     * Call directly to evaluateconsecutiveRecords (list). Avoid computing the
     * set of consecutive records for each iteration of the TFF loop
     *
     * @param monTSG
     * @param absolute_x1
     * @param absolute_x2
     * @return
     */
    private Long TFFMonotonicTSG(
            MonotonicTSG monTSG,
            Long absolute_x1,
            Long absolute_x2
    ) {
        IndexedTSLog indexedTSLog = (IndexedTSLog) this.getTimeSeriesLog();
        String varname = monTSG.getInvolvedVariables().iterator().next();

        NavigableMap<Long, LogEntry> completeTimeSeries
                = indexedTSLog.getIndexedEntries().get(varname)
                        .subMap(absolute_x1, true, absolute_x2, true);

        // Apply granularity to the sorted log entries (filter)                
        // Apply the function over the stream of consecutive pairs
        // To get the stream of consecutive pairs, we first filter the input 
        // logEntries by varname and sort them by timestamp
        List<Pair<LogEntry, LogEntry>> completeConsecutiveRecords = consecutive(
                LongStreamEx.iterate(completeTimeSeries.firstKey(), l -> l + monTSG.getGranularity())
                        .takeWhile(l -> l <= completeTimeSeries.lastKey())
                        .mapToObj(timeIndex -> {
                            return completeTimeSeries
                                    .ceilingEntry(timeIndex)
                                    .getValue();
                        })
                        .distinct()
        );

        boolean TSG_fulfilled = false;
        int i;
        for (i = 0; i < completeConsecutiveRecords.size() && !TSG_fulfilled; i++) {
            TSG_fulfilled = monTSG.evaluateListOfConsecutiveRecords(
                    completeConsecutiveRecords.subList(0, i)
            );
        }

        return (TSG_fulfilled
                ? completeConsecutiveRecords.get(i).getValue().getTs()
                : -1L);
    }

    /**
     * Private things
     */
    private TimeSeriesLog dataLog;
    private OPInfo opInfo;
    private Map<Pair<Transition, Long>, Long> TFFCache;
}
