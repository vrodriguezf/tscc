/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.Transition;

/**
 *
 * @author victor
 */
public class DeadlockOld {
   private Token lockedToken; 
   private Transition lockingTransition;
   private Token liberatorToken;
   private FiringOld liberatorFiring;

    public DeadlockOld(Token lockedToken, Transition lockingTransition) {
        this.lockedToken = lockedToken;
        this.lockingTransition = lockingTransition;
    }

    public DeadlockOld(Token lockedToken, Transition lockingTransition, FiringOld liberator) {
        this.lockedToken = lockedToken;
        this.lockingTransition = lockingTransition;
        this.liberatorFiring = liberator;
    }

    public Token getLockedToken() {
        return lockedToken;
    }

    public Transition getLockingTransition() {
        return lockingTransition;
    }

    public FiringOld getLiberatorFiring() {
        return liberatorFiring;
    }

    public void setLiberatorFiring(FiringOld liberator) {
        this.liberatorFiring = liberator;
    }

    public Token getLiberatorToken() {
        return liberatorToken;
    }

    public void setLiberatorToken(Token liberatorToken) {
        this.liberatorToken = liberatorToken;
    }
    
    public boolean equalDistribution(DeadlockOld d) {
        
        if (d.getLockingTransition().equals(lockingTransition) && 
                d.getLockedToken().getObject() == lockedToken.getObject())
            return true;
        
        return false;
    }

    @Override
    public String toString() {
        return "Deadlock{" + 
                "lockedToken=" + lockedToken + 
                ", lockingTransition=" + lockingTransition.getLabel() + 
                '}';
    }
    
    
}
