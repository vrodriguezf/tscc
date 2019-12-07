/* Copyright Guillem Catala. www.guillemcatala.com/petrinetsim. Licensed http://creativecommons.org/licenses/by-nc-sa/3.0/ */
package org.uam.aida.tscc.business;

/**
 *
 * @author Guillem
 */
public class Global {

    /** The current PetriNet model */
    public static PetriNet petriNet = new PetriNet();
    /** Application mode*/
    public static int mode = 0;
    /** To enable figure selection*/
    public static final int SELECTMODE = 0;
    /** To add places */
    public static final int PLACEMODE = 1;
    /** To add transitions */
    public static final int TRANSITIONMODE = 2;
    /** To add arcs*/
    public static final int NORMALARCMODE = 3;
    /** When simulation occurs */
    public static final int SIMULATIONMODE = 4;
    
    /** Headless execution */
    public static boolean HEADLESS_EXECUTION = false;
    
    /** Save data **/
    public static boolean DBSAVE = true;
    
    /** Conformance checking parameters **/
    public static Long R = 1000L; //Reversing time
}
