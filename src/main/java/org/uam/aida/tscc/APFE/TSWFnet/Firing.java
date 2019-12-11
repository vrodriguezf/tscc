/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import java.util.Map;
import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.Transition;
import java.util.Set;
import org.uam.aida.tscc.business.Place;

/**
 *
 * @author victor
 */
public class Firing {
    private Transition t;
    private Marking M_in;
    private Map<Place, Token> I;
    private Long x_out;
    
    /**
     * Constructor
     */
    public Firing(Transition t, Marking M_in, Map<Place, Token> I, Long x_out) {
        this.t = t;
        this.M_in = M_in;
        this.I = I;
        this.x_out = x_out;
    }

    public Transition getT() {
        return t;
    }

    public Marking getM_in() {
        return M_in;
    }

    public Map<Place, Token> getI() {
        return I;
    }

    public Long getX_out() {
        return x_out;
    }

    @Override
    public String toString() {
        return "Firing{" + "t=" + t + ", M_in=" + M_in + ", I=" + I + ", x_out=" + x_out + '}';
    }
}
