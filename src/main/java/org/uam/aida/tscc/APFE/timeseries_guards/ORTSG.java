/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;

/**
 *
 * @author victor
 */
public class ORTSG extends ComposedTSG {
    
    public ORTSG(List<TSG> parts) {
        super(parts);
    }
    
    public ORTSG(TSG... parts) {
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
                .anyMatch(gc -> gc.evaluate(logEntries));
    }

    @Override
    public boolean evaluate(IndexedTSLog L, Long x1, Long x2) {
        return this.getParts().stream()
                .anyMatch(gc -> gc.evaluate(L, x1, x2));
    }

    @Override
    public boolean evaluate(IndexedTSLog L) {
        return this.getParts().stream()
                .anyMatch(gc -> gc.evaluate(L));
    }
}
