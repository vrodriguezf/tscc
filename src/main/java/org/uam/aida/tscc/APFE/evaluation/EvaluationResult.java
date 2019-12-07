/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.evaluation;

import org.uam.aida.tscc.business.Transition;

/**
 *
 * @author victor
 */
public class EvaluationResult {
    public EvaluationResult(ConformanceCategory type, Transition task, Long timestamp) {
        this.type = type;
        this.task = task;
        this.timeOfFirstFullfilment = timestamp;
    }

    public ConformanceCategory getType() {
        return type;
    }

    public Transition getTask() {
        return task;
    }

    public Long getTimeOfFirstFulfillment() {
        return timeOfFirstFullfilment;
    }
    
    
    /**
     * Attributes
     */
    private ConformanceCategory type;
    private Transition task;
    private Long timeOfFirstFullfilment;
}
