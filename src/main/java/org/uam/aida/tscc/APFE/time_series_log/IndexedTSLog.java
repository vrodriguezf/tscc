/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.time_series_log;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 *
 * @author victor
 */
public class IndexedTSLog extends TimeSeriesLog {

    public static IndexedTSLog loadFromCSV(
            String filePath,
            String timestampColname,
            String variableColname,
            String valueColname
    ) throws IOException {

        return new IndexedTSLog(
                filePath,
                loadEntriesFromCSV(filePath,
                        timestampColname,
                        variableColname,
                        valueColname
                )
        );
    }

    public IndexedTSLog(String id, Collection<LogEntry> entries) {
        super(id, entries);

        //Create the map
        //TODO : Hacer mas eficiente con un collectors?
        indexedEntries = new HashMap<>();
        for (LogEntry le : entries) {
            NavigableMap<Long, LogEntry> ts = indexedEntries.getOrDefault(
                    le.getVarname(),
                    new TreeMap<>());
            ts.put(le.getTs(), le);
            indexedEntries.put(le.getVarname(), ts);
        }
    }
    
    /**
     * NOTE: THIS CONSTRUCTOR DOES NOT CREATE THE LIST OF ENTRES IN THE PARENT
     * CLASS. (DANGEROUS)
     * @param indexedEntries
     */
    public IndexedTSLog(Map<String, NavigableMap<Long, LogEntry>> indexedEntries) {
        this.indexedEntries = indexedEntries;
    }

    @Override
    protected Optional<LogEntry> closestPastLogEntry(String varname, Long ts) {
        return  Optional.ofNullable(indexedEntries.get(varname))
                .map((NavigableMap<Long, LogEntry> singleTS) -> {
                    Map.Entry<Long, LogEntry> low = singleTS.floorEntry(ts);
                    return low.getValue();
                });
    }
    
    /**
     * 
     * @param x1
     * @param x2
     * @return 
     */
    public IndexedTSLog subLog(Long x1, Long x2) {
        return new IndexedTSLog(
                this.getIndexedEntries().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().subMap(x1, true, x2, true)))
        );
    }
    
    public IndexedTSLog subLog(Collection<String> involvedVariables) {
        
        Map<String, NavigableMap<Long, LogEntry>> auxMap = this.getIndexedEntries();
        
        return new IndexedTSLog(
                involvedVariables.stream()
                .filter(auxMap::containsKey)
                .collect(Collectors.toMap(Function.identity(), auxMap::get))
        );
    }
    
    public IndexedTSLog subLog(
            Collection<String> involvedVariables, 
            Long x1, 
            Long x2) {
        
        Map<String, NavigableMap<Long, LogEntry>> auxMap = this.getIndexedEntries();
        
        return new IndexedTSLog(
                involvedVariables.stream()
                .filter(auxMap::containsKey)
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> auxMap.get(key).subMap(x1, true, x2, true)
                ))
        );
    }
    
    public List<Long> timeIndices() {
        return (this.getIndexedEntries().values().stream()
                .flatMap(x -> x.keySet().stream())
                .distinct()
                .collect(Collectors.toList()));
    }

    public Map<String, NavigableMap<Long, LogEntry>> getIndexedEntries() {
        return indexedEntries;
    }

    public void setIndexedEntries(Map<String, NavigableMap<Long, LogEntry>> indexedEntries) {
        this.indexedEntries = indexedEntries;
    }

    /**
     * Attributes
     */
    private Map<String, NavigableMap<Long, LogEntry>> indexedEntries;
}
