/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.time_series_log;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author victor
 */
public class LogEntry {

    /**
     * *
     * Static things
     */
    public static NavigableMap<Long, LogEntry> sortLogEntries(
            Collection<LogEntry> logEntries) {
        return logEntries.stream()
                .collect(
                        Collectors.toMap(
                                (LogEntry le) -> le.getTs(),
                                (LogEntry le) -> le,
                                (v1, v2) -> {
                                    throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                                },
                                TreeMap::new
                        )
                );
    }

    protected Long ts;
    protected String varname;
    protected Object record;

    public LogEntry(Long ts, Object record) {
        this.ts = ts;
        this.record = record;
    }

    public LogEntry(Long ts, String varname, Object record) {
        this.ts = ts;
        this.varname = varname;
        this.record = record;
    }

    public Long getTs() {
        return ts;
    }

    public String getVarname() {
        return varname;
    }

    public Object getRecord() {
        return record;
    }

    public Double getRecordAsDouble() {
        return (Double) record;
    }

    public Integer getRecordAsInteger() {
        /*
        Double aux = getRecordAsDouble();
        if (aux != null) 
            return aux.intValue();
        else
            return null;
         */
        return (Integer) record;
    }

    public Boolean getRecordAsBoolean() {
        return (Boolean) record;
    }

    public String getRecordAsString() {
        return String.valueOf(record);
    }
}
