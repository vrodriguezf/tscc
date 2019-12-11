/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.evaluation;

import org.uam.aida.tscc.business.Transition;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author victor
 */
public class MissingCheck {
    private Transition check;
    private Collection<Transition> overreactions;

    public MissingCheck(Transition check, Collection<Transition> overreactions) {
        this.check = check;
        this.overreactions = overreactions;
    }

    public MissingCheck(Transition check) {
        this(check, new ArrayList<>());
    }

    public Transition getCheck() {
        return check;
    }

    public Collection<Transition> getOverreactions() {
        return overreactions;
    }
}
