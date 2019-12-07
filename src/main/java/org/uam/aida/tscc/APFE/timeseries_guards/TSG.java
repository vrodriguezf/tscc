/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.config.Configurable;

/**
 * Time series-dependent guards (TSG)
 *
 * @author victor
 */
public abstract class TSG implements Configurable<Toml> {

    private static final Logger LOG = Logger.getLogger(TSG.class.getName());
    
    @Override
    public void configure(Toml cfg) {
        Optional.ofNullable(cfg.getString("description")).ifPresent(
                x -> setDescription(x)
        );
        return;
    }

    public abstract boolean evaluate(Collection<LogEntry> logEntries);
    
    /**
     * Secondary interface for calling the abstract function evaluate.
     * It is useful to override this function if some optimization
     * wants to be applied over the evaluation
     * @param L
     * @return 
     */
    public boolean evaluate(IndexedTSLog L) {
        preEvaluationArrangements(L);
        return evaluate(
                L.getIndexedEntries().values().stream()
                        .flatMap(x -> x.values().stream())
                        .collect(Collectors.toList())
        );
    }
    
    public boolean evaluate(IndexedTSLog L, Long x1, Long x2) {
        return evaluate(L.subLog(x1, x2));
    }

    /**
     * By default, all variables are involved (null)
     * @return 
     */
    public Collection<String> getInvolvedVariables() {
        return null;
    }

    /**
     * Evaluates an indexed data log from x1 to x2, returning the evaluation
     * results for every intermediate time index
     *
     * @param indexedTSLog
     * @param x1
     * @param x2
     * @return
     */
    public NavigableMap<Long, Boolean> evaluationMap(
            IndexedTSLog indexedTSLog,
            Long x1,
            Long x2
    ) {
        throw new UnsupportedOperationException("TODO: Not implemented yet");
    }

    public Long timeOfFirstFulfillment(
            IndexedTSLog indexedTSLog,
            Long x1,
            Long x2) {
        
        IndexedTSLog subLog = Optional.ofNullable(getInvolvedVariables())
                .map(x -> indexedTSLog.subLog(x, x1, x2))
                .orElse(indexedTSLog.subLog(x1, x2));
        
        Iterator<Long> recordTimes = subLog.timeIndices().iterator();
        Long timeIndex = -1L;
        boolean break_flag = false;
        while (recordTimes.hasNext() && !break_flag) {
            timeIndex = recordTimes.next();
            if (this.evaluate(subLog, x1, timeIndex) == true)
                break_flag = true;
        }
        
        return (break_flag == true ? timeIndex : -1L);
    }

    public TSG getComplementary() {
        Function<Collection<LogEntry>, Boolean> f_evaluate = x -> this.evaluate(x);
        Supplier<Collection<String>> f_involved_variables = this::getInvolvedVariables;
        TSG self = this;

        return new TSG() {
            @Override
            public Collection<String> getInvolvedVariables() {
                return f_involved_variables.get();
            }

            @Override
            public boolean evaluate(Collection<LogEntry> logEntries) {
                return !f_evaluate.apply(logEntries);
            }

            /*
            @Override
            public NavigableMap<Long, Boolean> evaluationMap(
                    IndexedTSLog indexedTSLog, 
                    Long x1, 
                    Long x2) {
                NavigableMap<Long, Boolean> selfEvaluationMap = 
                        self.evaluationMap(indexedTSLog, x1, x2);
                selfEvaluationMap.replaceAll((key, value) -> !value);
                return selfEvaluationMap;
            }
             */
        };
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Protected things
     */
    
    /**
     * Use this method if u want to set class configurations before an
     * evaluation (usually for optimizing the evaluation).
     * Return true if there are some changes in the tsg internal configuration, 
     * false otherwise
     * @param L 
     */
    protected boolean preEvaluationArrangements(IndexedTSLog L) {
        if (L != getLogInUse()) {
            setLogInUse(L);
            return true;
        }
        else
            return false;
    }

    protected IndexedTSLog getLogInUse() {
        return logInUse;
    }

    protected void setLogInUse(IndexedTSLog lastLogUsed) {
        this.logInUse = lastLogUsed;
    }
    
    /**
     * Private things
     */
    private String description = "";
    private IndexedTSLog logInUse;
}
