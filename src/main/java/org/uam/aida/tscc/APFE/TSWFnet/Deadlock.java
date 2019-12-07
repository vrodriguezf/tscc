/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import org.uam.aida.tscc.business.Place;
import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.Transition;

/**
 *
 * @author victor
 */
public class Deadlock {
    private Transition t;
    private Place p;
    private Token x;

    public Deadlock(Transition t, Place p, Token x) {
        this.t = t;
        this.p = p;
        this.x = x;
    }

    public Transition getT() {
        return t;
    }

    public Place getP() {
        return p;
    }

    public Token getX() {
        return x;
    }

    @Override
    public String toString() {
        return "{p:"+p.getLabel()+ ", t:" + t.getLabel() + ", x:" + x + "}";
    }
    
    
}
