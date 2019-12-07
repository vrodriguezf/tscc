/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;

/**
 * A chain TSG is fulfilled if and only if each of its links is fulfilled
 * after the previous one.
 * @author victor
 */
public class ChainTSG  extends ComposedTSG {

    public ChainTSG(List<TSG> links) {
        super(links);
    }

    public ChainTSG(TSG... parts) {
        super(parts);
    }
    
    

    @Override
    public boolean evaluate(Collection<LogEntry> logEntries) {
        NavigableMap<Long, LogEntry> sortedLogEntries = 
                LogEntry.sortLogEntries(logEntries);
        
        Iterator<TSG> itTSG = getParts().iterator();
        boolean break_flag = false;
        while (itTSG.hasNext() && !break_flag) {
            TSG currentTSG = itTSG.next();
            
            Optional<Long> tff = sortedLogEntries.navigableKeySet().stream()
                    .filter((Long upperKey) -> {
                        return currentTSG.evaluate(
                                sortedLogEntries.headMap(upperKey, true).values()
                        );
                    })
                    .findFirst();
            
            if (tff.isPresent()) {
                sortedLogEntries.tailMap(tff.get(), true);
            } else {
                break_flag = true;
            }
        }
        
        return (break_flag ? false : true);
    }
    
    @Override
    public NavigableMap<Long, Boolean> evaluationMap(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        throw new UnsupportedOperationException(
                "TODO: Implement by calling individual evaluationMap functions"
        );
    }
    
    
    @Override
    public Long timeOfFirstFulfillment(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        Iterator<TSG> itTSG = getParts().iterator();
        
        boolean break_flag = false;
        Long lowerTimeIndex = x1;
        while(itTSG.hasNext() && !break_flag) {
            TSG currentTSG = itTSG.next();
            Long currentTFF = currentTSG.timeOfFirstFulfillment(
                    indexedTSLog, 
                    lowerTimeIndex, 
                    x2
            );
            
            if (currentTFF.equals(-1L))
                break_flag = true;
            else
                lowerTimeIndex = currentTFF;
        }
        
        return (break_flag ? -1L : lowerTimeIndex);
    }
    
    /*
    @Override
    public Long timeOfFirstFulfillment(
            IndexedTSLog indexedTSLog, 
            Long x1, 
            Long x2) {
        
        List<NavigableMap<Long, Boolean>> evMaps = getParts().stream()
                .map(x -> x.evaluationMap(indexedTSLog, x1, x2))
                .collect(Collectors.toList());
        
        Iterator<Entry<Long, Boolean>> referenceEntries = 
                evMaps.get(0).entrySet().stream()
                .filter(x -> x.getValue().equals(Boolean.TRUE))
                .iterator();
        
        boolean exitFlag_1 = false;
        Entry<Long, Boolean> nextReferenceEntry = null;
        while (referenceEntries.hasNext() && !exitFlag_1) {
            boolean exitFlag_2 = false;
            nextReferenceEntry = referenceEntries.next();
            Long nextKeyLoop = nextReferenceEntry.getKey();
            for (int i = 0; i < evMaps.size() && !exitFlag_2; i++) {
                nextKeyLoop = evMaps.get(i).ceilingKey(nextKeyLoop);
                if (!evMaps.get(i).get(nextKeyLoop))
                    exitFlag_2 = true;
            }
            if (!exitFlag_2) exitFlag_1 = true;
        }
        
        if (exitFlag_1)
            return nextReferenceEntry.getKey();
        else 
            return -1L;
    }
*/
    
}
