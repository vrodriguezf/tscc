/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import com.google.common.collect.Range;
import org.uam.aida.tscc.business.InputArc;
import org.uam.aida.tscc.business.OutputArc;
import org.uam.aida.tscc.business.Place;
import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.TokenSet;
import org.uam.aida.tscc.business.Transition;
import org.uam.aida.tscc.APFE.timeseries_guards.TSG;
import org.uam.aida.tscc.APFE.timeseries_guards.TrivialTSG;

/**
 * The time scope of all transitions is [0, 10000] and the tsguard is trivial
 * (always return true)
 * @author victor
 */
public class SyntheticTSWFNet extends TSWFNet {

    public SyntheticTSWFNet(Integer nsteps, TSG tsg) {
        int id_counter = 0;
        
        setId("SyntheticTSWFNet");
        setLabel("Synthetic TSWFNet");
        
        //Initial place
        Place initialPlace = new Place("p_i");
        
        //Add intermediate steps
        Place p = null;
        Transition t = null;
        InputArc ia = null;
        for (int i=0; i<nsteps; i++) 
        {
            //Add action
            if (i == 0) {
                //First step
                p = initialPlace;
            } else {
                //Output arc to the new step
                p = new Place("p_"+(id_counter));
                addOutputArc(new OutputArc(p, t) {
                    public TokenSet execute() {
                        return new TokenSet(currentCase.getObject(), currentCase.getTimestamp());
                    }
                });
            }
            t = new Transition("t_" + id_counter) {
                public boolean evaluate() {
                    return true;
                }

                public TokenSet execute() {
                    return new TokenSet(currentCase = new Token(currentCase.getObject(),currentCase.getTimestamp()));
                }
            };
            t.setLabel("Task " + id_counter);
            t.setTimeScope(Range.closed(0L, 10000L));
            t.setGuardCondition(tsg);
            ia = new InputArc(p, t) {
                public boolean evaluate() {
                    return getTokenSet().size() > 0;
                }

                public TokenSet execute() {
                    return new TokenSet(currentCase = getTokenSet().get(0));
                }
            };
            addPlace(p);
            addTransition(t);
            addInputArc(ia);
            id_counter++;
        }
        
        //End place
        Place end_place = new Place("p_e");
        addPlace(end_place);
        addOutputArc(new OutputArc(end_place, t) {
            public TokenSet execute() {
                return new TokenSet(currentCase.getObject(), currentCase.getTimestamp());
            }
        });
    }
    
    public SyntheticTSWFNet(Integer nsteps) {
        this(nsteps, new TrivialTSG());
    }
}
