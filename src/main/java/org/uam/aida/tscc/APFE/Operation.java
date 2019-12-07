/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE;

import org.uam.aida.tscc.APFE.time_series_log.TimeSeriesLog;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.logging.Logger;

/**
 *
 * @author victor
 */
public class Operation {

    private static final Logger LOG = Logger.getLogger(Operation.class.getName());
    
    protected String id;
    protected Long finalTimestamp;
    protected Operator operator;
    protected TimeSeriesLog log;

    public Operation(String id, Operator operator) {
        this.id = id;
        this.operator = operator;
    }

    public Operation(String id, Operator operator, TimeSeriesLog log) {
        this.id = id;
        this.operator = operator;
        this.log = log;
    }

    public String getId() {
        return id;
    }

    public Operator getOperator() {
        return operator;
    }
    
    public TimeSeriesLog getLog() {
        return log;
    }
    
    public Long getStartTime() {
        return this.getLog().getEntries().stream()
                .mapToLong(LogEntry::getTs)
                .min()
                .getAsLong();
    }
    
    public Long getEndTime() {
        return this.getLog().getEntries().stream()
                .mapToLong(LogEntry::getTs)
                .max()
                .getAsLong();
    }
}
