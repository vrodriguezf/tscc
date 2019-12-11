/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

/**
 *
 * @author victor
 */
public class GreaterThanTSG extends NumericalComparisonTSG {

    public GreaterThanTSG(
            Double value, 
            boolean strict, 
            Double epsilon, 
            LifecycleStage eventType, 
            Long minFulfillmentDuration, 
            Double maxUnfulfillmentPercentage, 
            Long granularity, 
            String varname) {
        super(value, strict, epsilon, eventType, minFulfillmentDuration, maxUnfulfillmentPercentage, granularity, varname);
    }

    public GreaterThanTSG(String varname, Double value, boolean strict) {
        super(value, strict, varname);
    }

    public GreaterThanTSG(String varname, Double value, boolean strict, LifecycleStage stage) {
        super(value, strict, varname, stage);
    }
    
    @Override
    public boolean isComparisonFulfilled(Double timeSeriesRecord, Double value) {
        
        return (this.isStrict()
                ? (timeSeriesRecord - value) > (-1 * this.getEpsilon())
                : (timeSeriesRecord - value) >= (-1 * this.getEpsilon()));
    }
}
