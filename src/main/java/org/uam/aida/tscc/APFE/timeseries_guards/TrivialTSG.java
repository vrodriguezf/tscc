/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author victor
 */
public class TrivialTSG extends TSG {

    @Override
    public Collection<String> getInvolvedVariables() {
        return Collections.emptySet();
    }
    
    @Override
    public boolean evaluate(Collection<LogEntry> logEntries) {
        return true;
    }
}
