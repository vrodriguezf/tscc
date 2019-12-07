/* Copyright Guillem Catala. www.guillemcatala.com/petrinetsim. Licensed http://creativecommons.org/licenses/by-nc-sa/3.0/ */
package org.uam.aida.tscc.business;

import org.uam.aida.tscc.APFE.TSWFnet.Marking;
import org.uam.aida.tscc.APFE.TSWFnet.MarkingOld;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Guillem
 */
public class PetriNet extends NetObject {

    /** Contains all places of the net. */
    private ArrayList<Place> places = new ArrayList();
    /** Contains all transitions of the net. */
    private ArrayList<Transition> transitions = new ArrayList();
    /** Contains all input arcs of the net. */
    private ArrayList<InputArc> inputArcs = new ArrayList();
    /** Contains all outputarcs of the net. */
    private ArrayList<OutputArc> outputArcs = new ArrayList();
    /** Contains all net objects of the net. */
    private HashMap netElements = new HashMap();
    /** String that represents the classes and libraries that will be imported in the autogenerated class. */
    private String importText = "";
    /** String that represents the methods and attributes that will be created in the autogenerated class. */
    private String declarationText = "";
    /** String that represents the classes and libraries that will be implemented by the autogenerated class. */
    private String implementText = "";

    /** PetriNet constructor */
    public PetriNet() {
        this.label = "Untitled" + this.id;
        this.id = "n" + this.id;
    }

    /** Checks whether the Petri Net has enabled transitions */
    public boolean isDead() {
        boolean isDead = true;
        Iterator i = getTransitions().iterator();
        while (isDead && i.hasNext()) {
            Transition transition = (Transition) i.next();
            isDead = !(transition.enabled(0));
        }
        System.out.println("Dead = " + isDead);
        return isDead;
    }

    /** Adds a place to this net */
    public void addPlace(Place place) {
        this.places.add(place);
        this.netElements.put(place.getId(), place);
    }

    /** Adds a transition to this net */
    public void addTransition(Transition transition) {
        this.transitions.add(transition);
        this.netElements.put(transition.getId(), transition);
    }

    /** Adds an input arc to this net */
    public void addInputArc(InputArc inputArc) {
        this.inputArcs.add(inputArc);
        this.netElements.put(inputArc.getId(), inputArc);
    }

    /** Adds an output arc to this net */
    public void addOutputArc(OutputArc outputArc) {
        this.outputArcs.add(outputArc);
        this.netElements.put(outputArc.getId(), outputArc);
    }

    /** Removes a place from this net */
    public void removePlace(Place place) {
        this.removeInputArcs(place.getId());
        this.removeOutputArcs(place.getId());
        this.places.remove(place);
        this.netElements.remove(place.getId());
    }

    /** Removes a transition from this net */
    public void removeTransition(Transition transition) {
        this.removeInputArcs(transition.getId());
        this.removeOutputArcs(transition.getId());
        this.transitions.remove(transition);
        this.netElements.remove(transition.getId());
    }

    /** Removes an input arc from this net */
    public void removeInputArc(InputArc inputArc) {
        this.inputArcs.remove(inputArc);
        this.netElements.remove(inputArc.getId());
    }

    /** Removes an output arc from this net */
    public void removeOutputArc(OutputArc outputArc) {
        this.outputArcs.remove(outputArc);
        this.netElements.remove(outputArc.getId());
    }

    /** Removes all input arcs that connects to this netObject id*/
    public void removeInputArcs(String id) {
        Iterator it = getInputArcs().iterator();
        while (it.hasNext()) {
            InputArc inputArc = (InputArc) it.next();
            if (id.equals(inputArc.getPlace().getId())) {
                it.remove();
            }
        }
    }

    /** Removes all output arcs that connects to this netObject id*/
    public synchronized void removeOutputArcs(String id) {
        Iterator it = getOutputArcs().iterator();
        while (it.hasNext()) {
            OutputArc outputArc = (OutputArc) it.next();
            if (id.equals(outputArc.getPlace().getId())) {
                it.remove();
            }
        }
    }
    
    /**
     * 
     * @param time
     * @param verifyGuards
     * @return
     * @deprecated
     */
    public Collection<Transition> enabledTransitions(long time, boolean verifyGuards) {
        Iterator it = this.getTransitions().iterator();
        Collection<Transition> enabledTransitions = new ArrayList<>();
        while (it.hasNext()) {
            Transition transition = (Transition) it.next();
            if (transition.enabled(time,verifyGuards)) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }
    
    /**
     * Retrieve the enabled transitions in marking M 
     * WITHOUT CHECKING THE TRANSITION GUARD (if any)
     * @param M Marking
     * @return 
     */
    public Collection<Transition> enabledTransitions(Marking M) {
        return 
                this.getTransitions().stream()
                .filter(t -> t.enabled(this, M))
                .collect(Collectors.toSet());
    }
    
    /**
     * 
     * @return current marking
     */
    public MarkingOld getMarking() {
        MarkingOld result = new MarkingOld();
        
        for (Place p : getPlaces()) {
            if (!p.getTokens().isEmpty()) {
                result.add(
                        p,
                        new TokenSet(p.getTokens())
                );                
            }          
        } 
        
        return result;
    }
    
    /**
     * 
     * @return current marking
     */
    public Marking getMarking_() {
        Marking result = new Marking();
        
        for (Place p : getPlaces()) {
            if (!p.getTokens_().isEmpty()) {
                result.add(
                        p,
                        p.getTokens_()
                );                
            }          
        }
        
        return result;
    }
    
    /**
     * Obtain the transitions fired in a marking trace for this petri net
     * @param markingTrace
     * @return 
     */
    public Collection<Transition> getTransitionTrace(Collection<MarkingOld> markingTrace) {
        return null;
    }
    
    /**
     * Establish a token distribution in this petrinet
     * @param m 
     * @deprecated 
     */
    public void setMarking(MarkingOld m) {
        //1. Clean current petriNet tokens
        for (Place p : getPlaces()) p.setTokens(new TokenSet());
        
        //For each place in the marking expression, add the corresponding tokenSet
        for (Entry<Place,TokenSet> entry : m.entrySet()) {
            Place p = entry.getKey();
            TokenSet t = entry.getValue();
            
            p.addTokens(t);
        }
    }
    
    /**
     * TODO: Refactor without TokenSet
     * @param M 
     */
    public void setMarking(Marking M) {
        //1. Clean current petriNet tokens
        for (Place p : getPlaces()) p.setTokens(new TokenSet());
        
        //For each place in the marking expression, add the corresponding set of tokens
        for (Entry<Place,Set<Token>> entry : M.entrySet()) {
            Place p = entry.getKey();
            Set<Token> kappa = entry.getValue();
            
            p.addTokens(kappa);
        }
    }

    /**
     * @return the places
     */
    public ArrayList<Place> getPlaces() {
        return places;
    }

    /**
     * @param places the places to set
     */
    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    /**
     * @return the transitions
     */
    public ArrayList<Transition> getTransitions() {
        return transitions;
    }

    /**
     * @param transitions the transitions to set
     */
    public void setTransitions(ArrayList<Transition> transitions) {
        this.transitions = transitions;
    }

    /**
     * @return the inputArcs
     */
    public ArrayList<InputArc> getInputArcs() {
        return inputArcs;
    }

    /**
     * @param inputArcs the inputArcs to set
     */
    public void setInputArcs(ArrayList<InputArc> inputArcs) {
        this.inputArcs = inputArcs;
    }

    /**
     * @return the outputArcs
     */
    public ArrayList<OutputArc> getOutputArcs() {
        return outputArcs;
    }

    /**
     * @param outputArcs the outputArcs to set
     */
    public void setOutputArcs(ArrayList<OutputArc> outputArcs) {
        this.outputArcs = outputArcs;
    }

    /**
     * @return the netElements
     */
    public HashMap getNetElements() {
        return this.netElements;
    }

    /**
     * @return the netElements
     */
    public NetObject getNetElement(String id) {
        return (NetObject) this.netElements.get(id);
    }

    /**
     * @return the declarationText
     */
    public String getDeclarationText() {
        return declarationText;
    }

    /**
     * @param declarationText the declarationText to set
     */
    public void setDeclarationText(String declarationText) {
        this.declarationText = declarationText;
    }

    /**
     * @return the importText
     */
    public String getImportText() {
        return importText;
    }

    /**
     * @param importText the importText to set
     */
    public void setImportText(String importText) {
        this.importText = importText;
    }

    /**
     * @return the implementText
     */
    public String getImplementText() {
        return implementText;
    }

    /**
     * @param implementText the implementText to set
     */
    public void setImplementText(String implementText) {
        this.implementText = implementText;
    }
}
