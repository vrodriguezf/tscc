/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.evaluation;

import org.uam.aida.tscc.APFE.TSWFnet.Marking;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author victor
 */
public class FEResult {
    Collection<EvaluationResult> evaluations;
    private List<Marking> markingTrace;

    public FEResult(Collection<EvaluationResult> evaluations, List<Marking> markingTrace) {
        this.evaluations = evaluations;
        this.markingTrace = markingTrace;
    }

    public Collection<EvaluationResult> getEvaluations() {
        return evaluations;
    }

    public List<Marking> getMarkingTrace() {
        return markingTrace;
    }
    
    
}
