/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.time_series_log;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 *
 * @author victor
 */
public class SyntheticIndexedLog extends IndexedTSLog {

    /**
     * Add incrementally records in the log, with just one constant variable
     * @param logSize
     */
    public SyntheticIndexedLog(Long logSize, String varName, Double varValue) {
        super("Synthetic", Collections.EMPTY_LIST);
        this.logSize = logSize;
        
        NavigableMap<Long, LogEntry> ts = new TreeMap<>();
        for (int i = 0; i < logSize; i++) {
            ts.put(Long.valueOf((long) i),
                    new LogEntry(Long.valueOf((long) i), varValue));
        }
        this.getIndexedEntries().put(varName, ts);
    }
    
    public SyntheticIndexedLog(Long logSize) {
        this(logSize, "V", 0.0);
    }

    public Long getLogSize() {
        return logSize;
    }
    
    private final Long logSize;
}
