/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import java.util.Optional;

/**
 *
 * @author victor
 */
public class VariableDecreasingTSG extends MonotonicTSG {

    public VariableDecreasingTSG(
            boolean strict, 
            Double epsilon, 
            LifecycleStage eventType, 
            Long minFulfillmentDuration, 
            Double maxUnfulfillmentPercentage, 
            Long granularity, 
            String varname) {
        super(epsilon, eventType, minFulfillmentDuration, maxUnfulfillmentPercentage, granularity, varname);
        this.strict = strict;
    }

    public VariableDecreasingTSG(String varname, LifecycleStage eventType) {
        super(varname, eventType);
        this.strict = false;
    }

    public VariableDecreasingTSG(String varname) {
        super(varname);
        this.strict = false;
    }

    @Override
    public void configure(Toml cfg) {
        super.configure(cfg);
        Optional.ofNullable(cfg.getBoolean("strict")).ifPresent(x -> setStrict(x));
    }

    @Override
    public TSG getComplementary() {
        return new VariableIncreasingTSG(
                !this.isStrict(),
                this.getEpsilon(),
                this.getEventType(),
                this.getMinFulfillmentDuration(),
                this.getMaxUnfulfillmentPercentage(),
                this.getGranularity(),
                this.getVarname());
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    protected boolean evaluateConsecutiveRecords(Object r1, Object r2) {
        //Cast both records to numbers
        Double r1_ = (Double) r1;
        Double r2_ = (Double) r2;

        return (this.isStrict()
                ? (r1_ - r2_) > -1*(this.getEpsilon())
                : (r1_ - r2_) >= -1*(this.getEpsilon()));
    }

    /**
     * Private things
     */
    private boolean strict;
}
