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
public class VariableConstantTSG extends MonotonicTSG {

    public VariableConstantTSG(Double epsilon, LifecycleStage eventType, Long minFulfillmentDuration, Double maxUnfulfillmentPercentage, Long granularity, String varname) {
        super(epsilon, eventType, minFulfillmentDuration, maxUnfulfillmentPercentage, granularity, varname);
    }

    public VariableConstantTSG(String varname, LifecycleStage eventType) {
        super(varname, eventType);
    }

    public VariableConstantTSG(String varname) {
        super(varname);
    }
    

    @Override
    protected boolean evaluateConsecutiveRecords(Object r1, Object r2) {
        //Cast both records to numbers
        Double r1_ = (Double) r1;
        Double r2_ = (Double) r2;
        
        if (this.getEpsilon().equals(0.0)) {
            return (Math.abs(r1_ - r2_) == 0.0);
        } else {
            return (Math.abs(r1_ - r2_) < this.getEpsilon());
        }
    }
}
