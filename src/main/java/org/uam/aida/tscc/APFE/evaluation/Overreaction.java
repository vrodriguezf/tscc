/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.evaluation;

import org.uam.aida.tscc.APFE.TSWFnet.FiringOld;
import org.uam.aida.tscc.business.Transition;

/**
 *
 * @author victor
 */
public class Overreaction {
    private FiringOld action;
    private Transition missingCheck;

    public Overreaction(FiringOld action, Transition missingCheck) {
        this.action = action;
        this.missingCheck = missingCheck;
    }

    public FiringOld getAction() {
        return action;
    }

    public Transition getMissingCheck() {
        return missingCheck;
    }

    @Override
    public String toString() {
        return "Overreaction{" + 
                "action=" + action.getTransition()+ 
                ", missingCheck=" + missingCheck.getLabel() + 
                '}';
    }
}
