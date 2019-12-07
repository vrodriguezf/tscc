/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.evaluation;

import org.uam.aida.tscc.APFE.Operation;
import org.uam.aida.tscc.APFE.Operator;
import org.uam.aida.tscc.APFE.ProcedureExecution;
import org.uam.aida.tscc.APFE.TSWFnet.DeadlockOld;
import org.uam.aida.tscc.APFE.TSWFnet.FiringOld;
import org.uam.aida.tscc.business.Transition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author victor
 */
public class EvaluationResultOld {
    private String id;
    private Operation operation;
    private Operator operator;
    private Collection<DeadlockOld> sequentialMismatches;
    private Collection<DeadlockOld> missingActions;
    private Collection<FiringOld> rightActions;
    private Collection<MissingCheck> missingChecks;
    private Collection<Overreaction> overreactions;
    private ArrayList<ArrayList<ProcedureExecution>> forwardExecutionTraces;
    
    /**
     * Retrieve one EvResult from DB
     * @param id
     * @return 
     */
    public static EvaluationResultOld retrieveFromId(String id) {
        return null;
    }
    

    public EvaluationResultOld(Collection<DeadlockOld> sequentialMismatches, Collection<DeadlockOld> missingActions) {
        this.sequentialMismatches = sequentialMismatches;
        this.missingActions = missingActions;
    }

    public EvaluationResultOld() {
        sequentialMismatches = new ArrayList<>();
        missingActions = new ArrayList<>();
        rightActions = new ArrayList<>();
        missingChecks = new ArrayList<>();
        overreactions = new ArrayList<>();
        forwardExecutionTraces = new ArrayList<>();
    }
    
    /**
     * RA + SM + OR
     * @return 
     */
    public List<FiringOld> getPerformedActions() {
        List<FiringOld> result = new ArrayList<>();
        result.addAll(getRightActions());
        result.addAll(getSequentialMismatches()
                .stream()
                .map(DeadlockOld::getLiberatorFiring)
                .collect(Collectors.toList())
        );
        result.addAll(
                getOverreactions()
                .stream()
                .map(Overreaction::getAction)
                .collect(Collectors.toList())
        );
        
        return result;
    }
    
    /**
     * RA + SM + MA
     * @return 
     * @deprecated 
     */
    public List<Transition> getExpectedActions() {
        
        throw new UnsupportedOperationException();
        /*
        List<Transition> result = new ArrayList<>();
        
        result.addAll(getRightActions()
                        .stream()
                        .map(FiringOld::getTransition)
                        .collect(Collectors.toList())
        );
        result.addAll(getSequentialMismatches()
                .stream()
                .map(DeadlockOld::getLockingTransition)
                .collect(Collectors.toList())
        );       
        result.addAll(getMissingActions()
                .stream()
                .map(DeadlockOld::getLockingTransition)
                .collect(Collectors.toList())
        );
        return result;
        */
    }
    
    /**
     * RA + SM
     * @return 
     */
    public List<FiringOld> getExpectedPerformedActions() {
        List<FiringOld> result = new ArrayList<>();
        result.addAll(getRightActions());
        result.addAll(getSequentialMismatches()
                .stream()
                .map(DeadlockOld::getLiberatorFiring)
                .collect(Collectors.toList())
        );
        
        return result;
    }    
    
    /**
     * 
     * @return 
     */
    public List<FiringOld> getBaseFiringTrace() {
        List<FiringOld> result = new ArrayList<>();
        
        getForwardExecutionTraces().get(0).forEach((ProcedureExecution pe) -> {
            result.addAll(pe.getFiringTrace());
        });
        
        return result;
    }
    
    /**
     * GETTERs & SETTERs
     */
    public Collection<DeadlockOld> getSequentialMismatches() {
        return sequentialMismatches;
    }

    public Collection<DeadlockOld> getMissingActions() {
        return missingActions;
    }

    public Collection<MissingCheck> getMissingChecks() {
        return missingChecks;
    }

    public Collection<Overreaction> getOverreactions() {
        return overreactions;
    }

    public Collection<FiringOld> getRightActions() {
        return rightActions;
    }

    public void setRightActions(Collection<FiringOld> performedActions) {
        this.rightActions = performedActions;
    }

    public void setSequentialMismatches(Collection<DeadlockOld> sequentialMismatches) {
        this.sequentialMismatches = sequentialMismatches;
    }

    public void setMissingActions(Collection<DeadlockOld> missingActions) {
        this.missingActions = missingActions;
    }

    public void setMissingChecks(Collection<MissingCheck> missingChecks) {
        this.missingChecks = missingChecks;
    }

    public void setOverreactions(Collection<Overreaction> overreactions) {
        this.overreactions = overreactions;
    }

    public ArrayList<ArrayList<ProcedureExecution>> getForwardExecutionTraces() {
        return forwardExecutionTraces;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
