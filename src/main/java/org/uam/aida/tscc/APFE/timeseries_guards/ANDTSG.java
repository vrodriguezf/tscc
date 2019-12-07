/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;

/**
 *
 * @author victor
 */
public class ANDTSG extends ComposedTSG {

    public ANDTSG(List<TSG> parts) {
        super(parts);
    }

    public ANDTSG(TSG... parts) {
        super(parts);
    }

    @Override
    public Collection<String> getInvolvedVariables() {
        return this.getParts().stream()
                .flatMap(tsg -> tsg.getInvolvedVariables().stream())
                .distinct()
                .collect(Collectors.toSet());
    }
    

    @Override
    public boolean evaluate(Collection<LogEntry> logEntries) {
        return this.getParts()
                .stream()
                .allMatch(gc -> gc.evaluate(logEntries));
    }

    @Override
    public boolean evaluate(IndexedTSLog L, Long x1, Long x2) {
        return this.getParts().stream()
                .allMatch(gc -> gc.evaluate(L, x1, x2));
    }

    @Override
    public boolean evaluate(IndexedTSLog L) {
        return this.getParts().stream()
                .allMatch(gc -> gc.evaluate(L));
    }
    

    @Override
    public NavigableMap<Long, Boolean> evaluationMap(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        
        List<NavigableMap<Long, Boolean>> evaluationResults = this.getParts().stream()
                .map(tsg -> tsg.evaluationMap(indexedTSLog, x1, x2))
                .collect(Collectors.toList());
        
        //For each TRUE result of one part of the AND guard, examine if the closest
        //past record of the rest of the guards has been also marked as TRUE.
        //In that case the guard is fulfilled at that time.
        //The reference set of indices for the final navigable map is the first
        //one (TODO: This could ocaasionate troubles??)
        NavigableMap<Long, Boolean> reference = evaluationResults.get(0);
        return 
                reference.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                (Map.Entry<Long, Boolean> x) -> x.getKey(), 
                                (Map.Entry<Long, Boolean> x) -> {
                                    if (x.getValue()) {
                                        return evaluationResults.stream()
                                                .allMatch((NavigableMap<Long, Boolean> m) -> {
                                                    return m.floorEntry(x.getKey()).equals(true);
                                                });
                                    } else {
                                        return Boolean.FALSE;
                                    }
                                },
                                (v1,v2) ->{ throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));},
                                TreeMap::new
                                )
                );
    }

    /*
    @Override
    public Long timeOfFirstFulfillment(IndexedTSLog indexedTSLog, Long x1, Long x2) {
        NavigableMap<Long, Boolean> evaluationMap = this.evaluationMap(indexedTSLog, x1, x2);
        return  
                evaluationMap.navigableKeySet()
                .stream()
                .filter(x -> evaluationMap.get(x))
                .findFirst()
                .orElse(-1L);
    }
    */

    /*
    @Override
    public Long timeOfFirstFulfillment(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        
        Long result;
        List<Long> partsTFF = getParts().stream()
                .map(x -> x.timeOfFirstFulfillment(indexedTSLog, x1, x2))
                .collect(Collectors.toList());
        
        
        if (partsTFF.contains(-1L)) {
            //One of them if not fulfilled implies that the AND will never be fulfilled
            result = -1L;
        }
        else {
            Long maxTFF = Collections.max(partsTFF);
            //Check if, at the maximum TFF found, all the parts are evaluated
            //as true. If so, the TFF is maxTFF. Otherwise, start this function
            //again recursively from maxTFF to x2.
            IndexedTSLog L_prime = new IndexedTSLog(
                    indexedTSLog.getIndexedEntries().entrySet().stream()
                    .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().subMap(x1, true, maxTFF, true)
                            )
                    )
            );
            
            if (getParts().stream().allMatch(x -> x.evaluate(L_prime))) {
                result = maxTFF;
            } else {
                //Recursive call
                result = timeOfFirstFulfillment(indexedTSLog, maxTFF, x2);
            }
        }
        
        return result;
    }
*/
    
    
}
