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
     * Add incrementally records in the log, with just one variable
     * @param logSize
     */
    public SyntheticIndexedLog(Integer logSize) {
        super("Synthetic", Collections.EMPTY_LIST);
        this.logSize = logSize;
        
        NavigableMap<Long, LogEntry> ts = new TreeMap<>();
        for (int i = 0; i < logSize; i++) {
            ts.put(Long.valueOf((long) i),
                    new LogEntry(Long.valueOf((long) i), 666));
        }
        this.getIndexedEntries().put("V", ts);
    }

    public Integer getLogSize() {
        return logSize;
    }
    
    private final Integer logSize;
}
