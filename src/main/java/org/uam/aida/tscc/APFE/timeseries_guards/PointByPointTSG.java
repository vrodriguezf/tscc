/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import java.util.Map;
import java.util.NavigableMap;
import org.uam.aida.tscc.APFE.time_series_log.IndexedTSLog;
import org.uam.aida.tscc.APFE.time_series_log.LogEntry;

/**
 *
 * @author victor
 */
public abstract class PointByPointTSG extends CountinuousConditionTSG {

    //CONSTRUCTORS
    public PointByPointTSG(
            LifecycleStage eventType, 
            Long minFulfillmentDuration, 
            Double maxUnfulfillmentPercentage, 
            Long granularity, 
            String varname) {
        super(eventType, minFulfillmentDuration, maxUnfulfillmentPercentage, granularity, varname);
    }

    public PointByPointTSG(String varname, LifecycleStage stage) {
        super(varname, stage);
    }

    public PointByPointTSG(String varname) {
        super(varname);
    }
    
    /**
     * ABSTRACT
     * @param sortedLogEntries
     * @return 
     */
    public abstract NavigableMap<Long, Boolean> pointByPointEvaluation(
        NavigableMap<Long, LogEntry> sortedLogEntries);
    

    @Override
    public boolean evaluateCountinuousCondition(NavigableMap<Long, LogEntry> timeSeries) {
        //Check extreme cases
        if (isExtremeCase(timeSeries)) {
            return false;
        }
        
        NavigableMap<Long, Boolean> pointByPointEvResult = 
                pointByPointEvaluation(timeSeries);
        
        //Check if the condition is fulfilled for a sufficient interval of time, 
        // according to the minimum state duratin of this guard (minFulfillmentDuration)
        // and the maximumNonFulfilmentPercentage
        Long firstFulfillmentTimeIndex = null;
        Long unfulfillmentAccumulator = 0L;
        
        for (Map.Entry<Long, Boolean> entry : pointByPointEvResult.entrySet()) {
            if (entry.getValue() == true) {
                if (firstFulfillmentTimeIndex != null) {
                    if ((entry.getKey() - firstFulfillmentTimeIndex) >= 
                            this.getMinFulfillmentDuration())
                        return true;
                } else {
                    firstFulfillmentTimeIndex = entry.getKey();
                }
            } else {
                // This point does not fulfill the condition. Add the interval 
                //between the precvious point and this one to the unfulfillment
                //accumulator
                if (!entry.getKey().equals(timeSeries.firstKey())) {
                    unfulfillmentAccumulator += (entry.getKey() - timeSeries.lowerKey(entry.getKey()));
                
                    //Check if the unfulfillment accumulator has exceeded the maximum
                    //allowed percentage of unfulfillment of this TSG
                    if (((double) unfulfillmentAccumulator/getMinFulfillmentDuration()) > 
                            getMaxUnfulfillmentPercentage()) {
                        //Reset accumulation variables
                        firstFulfillmentTimeIndex = null;
                        unfulfillmentAccumulator = 0L;
                    }
                }
            }
        }
        
        return false;
    }
}
