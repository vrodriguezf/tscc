/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import org.uam.aida.tscc.APFE.OPResponse;
import org.uam.aida.tscc.SAVIER_integration.tsg.SAVIERTimedConditions;
import static org.uam.aida.tscc.APFE.utils.Maps.entriesToMap;
import static org.uam.aida.tscc.APFE.utils.Maps.entry;
import org.uam.aida.tscc.APFE.utils.Triple;
import org.uam.aida.tscc.business.InputArc;
import org.uam.aida.tscc.business.OutputArc;
import org.uam.aida.tscc.business.Place;
import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.TokenSet;
import org.uam.aida.tscc.business.Transition;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

/**
 *
 * @author victor
 */
public class DummyOP extends TSWFNet {

    /**
     * Constants
     */
    public static long MU_ACTIONS = 100;
    
    /**
     * Step conditions
     */
    public static boolean sc_dummy_action(Token token) {
        return (SAVIERTimedConditions.verifyConditions((OPResponse) token.getObject(),
                token.getTimestamp(),
                token.getTimestamp() + MU_ACTIONS,
                Collections.unmodifiableMap(Stream.of(
                        entry("Sheet1", Arrays.asList(new Triple("action", "=", 1)))
                ).collect(entriesToMap()))) != null);
    }
    
    public static boolean sc_true(Token token) {
        return true;
    }
    
    /**
     * Time of Completion
     */
    public static Long toc_dummy_action(OPResponse data,Long xi, Long xf) {
        return SAVIERTimedConditions.verifyConditions(
                data,
                xi,
                xf,
                Collections.unmodifiableMap(Stream.of(
                        entry("Sheet1", Arrays.asList(new Triple("action", "=", 1)))
                ).collect(entriesToMap()))
        );
    }
    
    public static Long toc_fixed(OPResponse data,Long xi, Long xf)
    {
        return 30L;
    }

    public DummyOP(Integer nsteps) {
        int id_counter = 0;
        
        setId("DummyOP");
        setLabel("DUMMY OP");
        
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
                    return (evaluateInputTokens(this, DummyOP::sc_dummy_action));
                }

                public TokenSet execute() {
                    return new TokenSet(currentCase = new Token(
                            currentCase.getObject(),
                            toc_dummy_action((OPResponse) currentCase.getObject(),
                                    currentCase.getTimestamp(),
                                    currentCase.getTimestamp() + MU_ACTIONS
                            )));
                }
            };
            t.setLabel("ACTION:dummy_action");
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
}
