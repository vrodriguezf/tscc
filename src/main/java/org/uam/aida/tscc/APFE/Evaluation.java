/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.uam.aida.tscc.APFE.TSWFnet.Deadlock;
import org.uam.aida.tscc.APFE.TSWFnet.TSWFNet;
import org.uam.aida.tscc.APFE.TSWFnet.StepType;
import org.uam.aida.tscc.APFE.TSWFnet.DeadlockOld;
import org.uam.aida.tscc.APFE.TSWFnet.Firing;
import org.uam.aida.tscc.APFE.TSWFnet.FiringOld;
import org.uam.aida.tscc.APFE.TSWFnet.Marking;
import org.uam.aida.tscc.APFE.TSWFnet.MarkingOld;
import org.uam.aida.tscc.APFE.evaluation.EvaluationResultOld;
import org.uam.aida.tscc.APFE.evaluation.Overreaction;
import org.uam.aida.tscc.APFE.utils.Helpers;
import org.uam.aida.tscc.APFE.time_series_log.TimeSeriesLog;
import org.uam.aida.tscc.APFE.evaluation.EvaluationResult;
import org.uam.aida.tscc.APFE.evaluation.FEResult;
import org.uam.aida.tscc.APFE.utils.SetOperations;
import org.uam.aida.tscc.APFE.utils.Triple;
import org.uam.aida.tscc.business.Global;
import org.uam.aida.tscc.business.Place;
import org.uam.aida.tscc.business.Token;
import org.uam.aida.tscc.business.TokenSet;
import org.uam.aida.tscc.business.Transition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.uam.aida.tscc.APFE.evaluation.ConformanceCategory;
import org.uam.aida.tscc.APFE.evaluation.ConformanceFunction;
import org.uam.aida.tscc.APFE.utils.Pair;

/**
 *
 * @author victor
 */
public class Evaluation {

    private static final Logger LOG = Logger.getLogger(Evaluation.class.getName());

    /**
     *
     * @param initialMarking
     * @param alert
     * @param eop
     * @return
     * @throws InterruptedException
     */
    public static Pair<MarkingOld, Long> basicAPFE(
            MarkingOld initialMarking,
            OPTriggering alert,
            TSWFNet eop) throws InterruptedException {
        //Variables declaration;
        EvaluationResultOld evaluationResult = new EvaluationResultOld();
        MarkingOld Mi;
        MarkingOld M, Mprime, MdoublePrime;
        ProcedureExecution procExec;

        //Initial MarkingOld
        Mi = initialMarking;

        //Finish marking
        //Mf = eop.createFinalMarking(responses);
        //Initialize iterative marking
        M = Mi;

        procExec = new ProcedureExecution(eop, M);
        procExec.start();

        // Update iterative marking
        M = eop.getMarking();

        //Time spent
        Long timeSpent = M.entrySet()
                .stream()
                .map(Entry::getValue)
                .mapToLong((TokenSet ts) -> {
                    long max_time
                            = ts.getTokenList()
                                    .stream()
                                    .mapToLong((Object o) -> {
                                        Token t = (Token) o;
                                        return t.getTimestamp();
                                    })
                                    .max()
                                    .getAsLong();
                    return max_time;
                })
                .max()
                .orElse(0L);

        return new Pair<>(M, timeSpent);
    }

    public static Triple<Boolean, Marking, Long> APFE(
            TSWFNet wfnetOP,
            TimeSeriesLog Delta,
            Long x_0) throws Exception {
        //Variables declaration
        Marking M_in, Mf;

        Global.petriNet = wfnetOP;

        //Assign the datalog to the wfnetOP
        wfnetOP.setDataLog(Delta);

        //Set Initial Marking
        M_in = wfnetOP.createInitialMarking(new Long[]{x_0});
        Mf = wfnetOP.executeDynamics(M_in, new ArrayList<>());

        Long PFETime = Mf.values().stream()
                .flatMap(
                        x -> x.stream())
                .mapToLong(Token::getTimestamp)
                .max()
                .orElseThrow(Exception::new);

        if (!Mf.get(wfnetOP.getOutputPlace()).isEmpty()) {
            return new Triple<>(true, Mf, PFETime);
        } else {
            return new Triple<>(false, Mf, PFETime);
        }
    }

    public static Pair<Marking, ConformanceFunction> evaluateUntreatedContext(
            TSWFNet W,
            TimeSeriesLog L,
            Integer n,
            Transition t,
            Marking M
    ) {
        ConformanceFunction c = new ConformanceFunction();
        Marking M_prime = new Marking();
        Map<Place, Token> I = Maps.newHashMap();
        boolean isInputContextValid = true;

        for (Place p : t.getInputPlaces(W)) {
            Long x = M.get(p).stream()
                    .map(Token::getTimestamp)
                    .filter(x_ -> W.isValidContext(t, x_))
                    .findFirst()
                    .orElse(null);

            if (x != null) {
                M_prime.add(p, new Token(x));
                I.put(p, new Token(x));
            } else {
                isInputContextValid = false;
                Token max_token = M.get(p).stream()
                        .max((k1, k2) -> {
                            return (int) (k1.getTimestamp() - k2.getTimestamp());
                        })
                        .orElseThrow(() -> new RuntimeException("not expected value"));

                Token reversedToken = new Token(max_token.getTimestamp() - Global.R);
                M_prime.add(p, reversedToken);
                I.put(p, reversedToken);
            }
        }

        Long x_o;
        if (isInputContextValid) {
            x_o =   I.values().stream()
                    .mapToLong(Token::getTimestamp)
                    .map(x -> W.timeOfFirstFulfillment(t, x))
                    .max()
                    .getAsLong();
            c.put(
                    new Pair<>(t, x_o),
                    ConformanceCategory.MATCH
            );
        } else if (W.isValidContext(t, M_prime)) {
            x_o = M.subMarking(Sets.newHashSet(t.getInputPlaces(W))).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .mapToLong(Token::getTimestamp)
                    .max()
                    .getAsLong();

            Long ttm = I.values().stream()
                    .mapToLong(Token::getTimestamp)
                    .map(x -> W.timeOfFirstFulfillment(t, x))
                    .max()
                    .getAsLong();
            c.put(
                    new Pair<>(t, ttm),
                    ConformanceCategory.TIME_MISMATCH
            );
        } else {
            x_o = M.subMarking(Sets.newHashSet(t.getInputPlaces(W))).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .mapToLong(Token::getTimestamp)
                    .max()
                    .getAsLong();
            c.put(
                    new Pair<>(t, x_o),
                    ConformanceCategory.ABSENCE
            );
        }

        Marking M_prime_prime = W.fireTransition(t, M_prime, I, x_o);

        return new Pair<>(M_prime_prime, c);
    }

    /**
     *
     * @param W
     * @param L
     * @param n
     * @param M_0
     * @return
     */
    public static Pair<ConformanceFunction, List<Marking>> partialCCTS(
            TSWFNet W,
            TimeSeriesLog L,
            Integer n,
            Marking M_0) throws Exception {

        Global.petriNet = W;

        //Set L, n as the defaults parameters for W
        W.setDataLog(L);

        List<Marking> mu = new ArrayList<>();
        Marking M = new Marking(M_0);
        ConformanceFunction c = new ConformanceFunction(); //Conformance function

        //Loop variables
        Collection<Transition> tau;

        //loop code
        do {
            List<Firing> phi = new ArrayList<>();
            M = W.executeDynamics(M, phi);

            //Loop through the list of firings returned by the execution of the net
            // dynamics
            for (Firing f : phi) {
                //Add markings
                mu.add(f.getM_in());

                //Assign matches
                c.put(
                        new Pair<>(f.getT(), f.getX_out()),
                        ConformanceCategory.MATCH
                );
            }

            //Add final marking reached by the execution of the dynamics
            mu.add(M);

            tau = W.enabledTransitions(M);
            if (tau.size() != 0) {
                Transition t = Helpers.getRandom(tau).get();
                Pair<Marking, ConformanceFunction> repairResult
                        = evaluateUntreatedContext(W, L, n, t, M);
                M = repairResult.getKey();
                ConformanceFunction c_prime = repairResult.getValue();
                c_prime.forEach(c::put); //Dump cprime into c
            }
        } while (!tau.isEmpty());

        return new Pair<>(c, mu);
    }

    /**
     *
     * @param W
     * @param L
     * @param n
     * @param M_0
     * @return
     * @throws Exception
     */
    public static ConformanceFunction completeCCTS(
            TSWFNet W,
            TimeSeriesLog L,
            Integer n,
            Marking M_0) throws Exception {
        Pair<ConformanceFunction, List<Marking>> partialCCTSResult = partialCCTS(W, L, n, M_0);
        ConformanceFunction c = partialCCTSResult.getKey();
        List<Marking> mu = partialCCTSResult.getValue();

        for (Marking M : mu) {
            //Check for possible untreated invalid contexts
            // The transition is enabled but it does not belong to the current domain of 
            // the conformance function c
            List<Transition> untreatedInvalidContexts
                    = W.enabledTransitions(M).stream()
                            .filter(t -> {
                                return c.keySet().stream()
                                        .allMatch(pair -> !pair.getKey().equals(t));
                            })
                            .collect(Collectors.toList());

            for (Transition t : untreatedInvalidContexts) {
                Pair<Marking, ConformanceFunction> aux
                        = evaluateUntreatedContext(W, L, n, t, M);
                Marking M_prime = aux.getKey();
                ConformanceFunction c_prime = aux.getValue();
                c_prime.forEach(c::put); //Dump cprime into c

                ConformanceFunction c_prime_prime
                        = completeCCTS(W, L, n, M_prime);
                c_prime_prime.forEach(c::put); //Dump cprime into c
            }
        }

        return c;
    }

    /**
     *
     * @param wfnetOP
     * @param TSLog
     * @param x_0
     * @return
     */
    public static FEResult forwardEvaluation(TSWFNet wfnetOP,
            TimeSeriesLog TSLog,
            Long x_0) throws Exception {

        //Variables
        Marking M;

        Global.petriNet = wfnetOP;

        //Assign the datalog to the wfnetOP
        wfnetOP.setDataLog(TSLog);

        //Create initial Marking
        M = wfnetOP.createInitialMarking(new Long[]{x_0});

        //Control variables
        Collection<Deadlock> delta;

        //return variables
        Collection<EvaluationResult> evaluations = new ArrayList<>();
        List<Marking> markingTrace = new ArrayList<Marking>();
        markingTrace.add(M);

        //LOOP
        do {
            List<Firing> firingRecord = new ArrayList<>();
            Marking M_prime = wfnetOP.executeDynamics(
                    M,
                    firingRecord
            );
            markingTrace.add(new Marking(M_prime));
            //Each firing is considered as a matching
            evaluations.addAll(
                    firingRecord.stream()
                            .map(f -> new EvaluationResult(
                            ConformanceCategory.MATCH,
                            f.getT(),
                            f.getX_out()
                    ))
                            .collect(Collectors.toList())
            );

            //Retrieve set of deadlocks (delta) from the current marking
            delta = wfnetOP.getDeadlocks(M_prime);

            if (!delta.isEmpty()) {
                //Classify deadlocks and update M
                for (Deadlock d : delta) {
                    Marking M_prime_prime = new Marking();
                    M_prime_prime.put(d.getP(), new HashSet<Token>(Arrays.asList(new Token(null, x_0))));

                    if (wfnetOP.getDeadlocks(M_prime_prime).isEmpty()) {
                        // New Task Time Mismatch (TTM)
                        evaluations.add(
                                new EvaluationResult(
                                        ConformanceCategory.TIME_MISMATCH,
                                        d.getT(),
                                        d.getX().getTimestamp())
                        );
                        //Update M properly: M(d.p) = M(d.p)\{d.x} U {x_0}
                        M_prime.put(
                                d.getP(),
                                SetOperations.union(
                                        SetOperations.difference(
                                                M_prime.get(d.getP()),
                                                new HashSet<Token>(Arrays.asList(d.getX()))
                                        ),
                                        new HashSet<Token>(Arrays.asList(new Token(null, x_0)))
                                )
                        );
                    } else {
                        // New Task Abscence (TA)
                        evaluations.add(
                                new EvaluationResult(
                                        ConformanceCategory.ABSENCE,
                                        d.getT(),
                                        d.getX().getTimestamp())
                        );

                        //Update M properly (delete the token causing the deadlock)
                        // M(d.p) = M(d.p) \ {d.x}
                        M_prime.put(
                                d.getP(),
                                SetOperations.difference(
                                        M_prime.get(d.getP()),
                                        new HashSet<Token>(Arrays.asList(d.getX()))
                                )
                        );

                        // Update M properly (insertions)
                        // \forall p \in d.t\bullet M(p) = M(p) U {d.x}
                        for (Place p : d.getT().getOutputPlaces()) {
                            M_prime.put(
                                    p,
                                    SetOperations.union(
                                            M_prime.get(p),
                                            new HashSet<Token>(Arrays.asList(d.getX()))
                                    )
                            );
                        }
                    }
                }
            }

            M = M_prime;

        } while (!delta.isEmpty());

        return new FEResult(evaluations, markingTrace);
    }

    /**
     *
     * @param initialMarking
     * @param eop
     * @param alert
     * @return
     * @throws InterruptedException
     */
    public static EvaluationResultOld forwardEvaluation(
            MarkingOld initialMarking,
            OPTriggering alert,
            TSWFNet eop) throws InterruptedException {

        //Variables declaration;
        EvaluationResultOld evaluationResult = new EvaluationResultOld();
        MarkingOld Mi;
        MarkingOld M, Mprime, MdoublePrime;
        ProcedureExecution procExec;
        Collection<DeadlockOld> deadlocks = new ArrayList<DeadlockOld>();
        ArrayList<ProcedureExecution> executions = new ArrayList<>();

        //Initial MarkingOld
        Mi = initialMarking;

        //Initialize iterative marking
        M = Mi;

        //MAIN LOOP UPDATE: Condicion esta mal
        do {
            //Execute procedure with current marking M and add it to the executions queue
            procExec = new ProcedureExecution(eop, M);
            procExec.start();
            //procExec.join();
            executions.add(procExec);

            // Update iterative marking
            M = eop.getMarking();

            //Add the list of performed actions to the evaluation result
            evaluationResult.getRightActions().addAll(
                    procExec.getFiringTrace()
                            .stream()
                            .filter(f -> eop.getStepType(f.getTransition()) == StepType.ACTION)
                            .collect(Collectors.toCollection(ArrayList::new))
            );

            //Retrieve deadlocks in the current marking
            deadlocks = getDeadlocks(eop);

            if (!deadlocks.isEmpty()) {

                // Soft repair - If passed, the blocker transitions will be marked
                // as sequential mismatches
                //TODO: Este marking hay que revisar como se crea. No hay por qué coger
                //los tokens del place final sin más, hay que coger todos los tokens que no esten 
                // metidos en un deadlock
                Mprime = new MarkingOld();
                Mprime.add(eop.getOutputPlace(), new TokenSet(eop.getOutputPlace().getTokens()));
                for (DeadlockOld deadlock : deadlocks) {
                    deadlock.setLiberatorToken(
                            new Token(
                                    deadlock.getLockedToken().getObject(),
                                    alert.getTriggerTs() //Reset TIME!
                            )
                    );
                    Mprime.add(
                            M.getPlaceOf(deadlock.getLockedToken()),
                            deadlock.getLiberatorToken()
                    );
                }

                // Re-execute the procedure evaluation with marking Mprime (Esta no se guarda en el array)
                procExec = new ProcedureExecution(eop, Mprime);
                procExec.start();
                //procExec.join();

                //Classify deadlocks comparing them with the deadlocks after repairing
                Collection<DeadlockOld> deadlocksAfterSoftRepair = getDeadlocks(eop);
                for (DeadlockOld originalDl : deadlocks) {
                    if (deadlocksAfterSoftRepair.stream()
                            .filter(d -> d.equalDistribution(originalDl))
                            .count() == 0) {
                        //El deadlock ha desparecido con el soft repair - Sequential Mismatch (SM)
                        //Buscamos el liberador de este mismatch
                        originalDl.setLiberatorFiring(procExec.findLiberatorFiring(originalDl));
                        evaluationResult.getSequentialMismatches().add(originalDl);
                    } else {
                        //El deadlock no ha desaparecido -- Missing action (MA)
                        evaluationResult.getMissingActions().add(originalDl);
                    }
                }

                //Hard repair (avanzar los tokens que estan en deadlock)
                MdoublePrime = new MarkingOld();
                for (Entry<Place, TokenSet> mEntry : M.entrySet()) {
                    if (mEntry.getKey().equals(eop.getOutputPlace())) {
                        //The output place is not changed
                        MdoublePrime.add(mEntry.getKey(), new TokenSet(mEntry.getValue()));
                    } else {
                        // Pass the tokens of each place to the next place 
                        // (manually firing the associated transition)
                        for (Place p : eop.nextPlaces(mEntry.getKey())) {
                            MdoublePrime.add(p, new TokenSet(mEntry.getValue()));
                        }
                    }
                }

                //Reset iterative marking
                M = MdoublePrime;
            }
        } while (!deadlocks.isEmpty());

        //Optional: Save the execution traces in the EvaluationResultOld object
        evaluationResult.getForwardExecutionTraces().add(executions);

        return evaluationResult;
    }

    public static EvaluationResultOld FBEvaluation(
            MarkingOld initialMarking,
            OPTriggering alert,
            TSWFNet eop) throws InterruptedException {

        MarkingOld Mi;
        ArrayList<MarkingOld> reversedMarkingTrace;
        EvaluationResultOld evResult;
        ArrayList<ProcedureExecution> backwardExecutionTraces;

        //Initial MarkingOld
        Mi = initialMarking;

        /**
         * ANALYZE PERFORMED ACTIONS, MISSING ACTIONS AND SEQUENTIAL MISMATCHES
         */
        // Perform a Forward Evaluation, saving the execution traces
        evResult = forwardEvaluation(Mi, alert, eop);

        //Backward loop in the forward execution traces
        backwardExecutionTraces = new ArrayList<>(evResult.getForwardExecutionTraces().get(0));
        Collections.reverse(backwardExecutionTraces);

        for (ProcedureExecution procExec : backwardExecutionTraces) {
            //Reverse marking trace
            reversedMarkingTrace = new ArrayList<>(procExec.getMarkingTrace());
            Collections.reverse(reversedMarkingTrace);

            //Backward marking trace loop
            for (MarkingOld M : reversedMarkingTrace) {
                //Get all the possible markings which can be arrived from this marking
                // (if there are no or splits places in the marking, the list will be empty)
                Collection<MarkingOld> alternativeMarkings = getNextPossibleMarkings(M, eop);
                for (MarkingOld altM : alternativeMarkings) {
                    //Check if the marking is not present in the correct marking trace
                    if (!reversedMarkingTrace
                            .stream()
                            .anyMatch((MarkingOld auxM) -> auxM.equalsDistribution(altM))) {

                        //Otherwise, re-execute a forward evaluation from this marking and see what happens
                        EvaluationResultOld auxEv = forwardEvaluation(
                                altM,
                                alert,
                                eop);

                        //Overreactions are defined as actions obtained in a 
                        // secondary evaluation process, which are not present in any of the
                        // actions or deadlocks of the main evaluation
                        // TODO revisar esto
                        Collection<Overreaction> overreactions
                                = retrieveOverreactions(evResult, auxEv, null);

                        if (!overreactions.isEmpty()) {
                            evResult.getOverreactions().addAll(overreactions);
                        }

                        //Save the traces of this sub-evaluation
                        evResult.getForwardExecutionTraces().addAll(auxEv.getForwardExecutionTraces());
                    }
                }

                //Extract the OR-splits of the marking
                /*
                Entry<Place,TokenSet> orSplitMarks = 
                        M.entrySet()
                        .stream()
                        .filter(mark -> eop.isORSplit(mark.getKey()))
                        .collect(Collectors.toSet());
                
                //Check if the marking contains OR-split places
                for (Entry<Place,TokenSet> mark : M.entrySet()) {
                    if (eop.isORSplit(mark.getKey())) {
                        //If one place is an or split, put the tokenset in all the 
                        //net paths different than the folloed in the marking trace,
                        // and re-execute the procedure

                        //1. Extract the other net branch (not executed transition)
                        Collection<Transition> notExecutedTransitions = mark.getKey()
                                .getNextTransitions(eop)
                                .stream()
                                .filter((Transition t) -> !procExec.getFiringTrace().contains(t))
                                .collect(Collectors.toCollection(ArrayList::new));

                        for (Transition t : notExecutedTransitions) {
                            //TODO Recursive call to DBEvaluation
                            
                            //NOW: re-execute forward evaluation from the output
                            // of this transition
                            MarkingOld auxM = new MarkingOld();
                            t.getOutputPlaces().stream().forEach((p) -> {
                                auxM.add(p, mark.getValue());
                            });
                            
                            EvaluationResultOld auxEv = forwardEvaluation(
                                    auxM, 
                                    alert, 
                                    eop);
                            
                            //Overreactions are defined as actions obtained in a 
                            // secondary evaluation process, which are not present in any of the
                            // actions or deadlocks of the main evaluation
                            // TODO revisar esto
                            Collection<Overreaction> overreactions =
                                    retrieveOverreactions(evResult, auxEv, t);
                            
                            if (!overreactions.isEmpty()) {
                                evResult.getOverreactions().addAll(overreactions);
                            }
                        }
                    }
                }
                 */
            }
        }

        //Remove duplicate overreaction, caused by similarity 
        //among markings in the marking trace
        //TODO revisar esto
        evResult.setOverreactions(
                evResult.getOverreactions()
                        .stream()
                        .filter(Helpers.distinctByKey((Overreaction o) -> o.getAction().getTransition()))
                        .collect(Collectors.toCollection(ArrayList::new))
        );

        return evResult;
    }

    /**
     * PRIVATE METHODS
     */
    /**
     * Given a marking, extract the possible OR split places and generate
     * markings for every routing combination of those markings
     *
     * @return
     */
    private static Collection<MarkingOld> getNextPossibleMarkings(MarkingOld m, TSWFNet eop) {

        Collection<MarkingOld> result = new ArrayList<>();

        /**
         * OR-SPLIT es solo aquel place que enruta 2 transiciones, que no
         * dependen de nada mas que de ella
         */
        Collection<Place> orPlaces = m
                .entrySet()
                .stream()
                .map(Entry::getKey)
                .filter(p -> eop.isORSplit(p))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Collection<Transition>> branches = orPlaces
                .stream()
                .map(p -> p.getNextTransitions(eop))
                .collect(Collectors.toList());

        Collection<List<Transition>> combinations = Helpers.permutations(branches);

        for (List<Transition> combination : combinations) {
            MarkingOld newM = new MarkingOld();
            for (Transition t : combination) {
                //Move the tokens of the input place to the output places
                Place inputORPlace = orPlaces
                        .stream()
                        .filter(p -> p.getNextTransitions(eop).contains(t))
                        .findFirst()
                        .get();

                t.getOutputPlaces().stream().forEach((p) -> {
                    newM.add(p, new TokenSet(m.get(inputORPlace)));
                });
            }
            //Complete the marking with the normal places (DO not move tokens here)
            m.entrySet()
                    .stream()
                    .filter(e -> !orPlaces.contains(e.getKey()))
                    .forEach(e -> newM.add(e.getKey(), new TokenSet(e.getValue())));

            //Add the new marking to the result
            result.add(newM);
        }

        return result;
    }

    /**
     * Get deadlocks (Only for ACTION transitions)
     *
     * @param eop
     * @return
     */
    private static Collection<DeadlockOld> getDeadlocks(TSWFNet eop) {
        Collection<DeadlockOld> result = new ArrayList<>();

        //UPDATE: Una transicion se considera bloqueante siempre y cuando 
        // tiene todos los tokens de entrada completos (no basta con tener alguno,
        // porque si no las puertas logicas se consideran bloqueantes (y por tanto
        // missing actions, lo cual es un poco raro))
        Stream<Transition> blockerTransitions;
        blockerTransitions = eop.getTransitions()
                .stream()
                //.filter((Transition t) -> t.inputArcsEnabled(0)) //TODO Revisar
                .filter((Transition t) -> {
                    return t.getInputPlaces()
                            .stream()
                            .allMatch(p -> !p.getTokens().isEmpty());
                    //return !t.getTokenSet().isEmpty();
                });
        //.collect(Collectors.toCollection(ArrayList::new));

        blockerTransitions.forEach((Transition t) -> {
            LOG.log(Level.INFO, "Blocker Transition: {0}", t);
            t.getTokenSet().getTokenList().forEach((Object _token) -> {
                result.add(new DeadlockOld(
                        (Token) _token,
                        t
                ));
            });
        });

        return result;
    }

    //Overreactions are defined as actions obtained in a 
    // secondary evaluation process, which are not present in any of the
    // actions or deadlocks of the main evaluation
    private static Collection<Overreaction> retrieveOverreactions(EvaluationResultOld mainEv,
            EvaluationResultOld auxEv,
            Transition missingCheck) {

        ArrayList<Transition> mainTransitions = new ArrayList<>();
        mainTransitions.addAll(mainEv.getRightActions()
                .stream()
                .map(FiringOld::getTransition)
                .collect(Collectors.toCollection(ArrayList::new))
        );
        mainTransitions.addAll(mainEv.getSequentialMismatches()
                .stream()
                .map(DeadlockOld::getLockingTransition)
                .collect(Collectors.toCollection(ArrayList::new))
        );

        ArrayList<Transition> auxTransitions = new ArrayList<>();
        auxTransitions.addAll(auxEv.getRightActions()
                .stream()
                .map(FiringOld::getTransition)
                .collect(Collectors.toCollection(ArrayList::new))
        );
        auxTransitions.addAll(auxEv.getSequentialMismatches()
                .stream()
                .map(DeadlockOld::getLockingTransition)
                .collect(Collectors.toCollection(ArrayList::new))
        );

        ArrayList<Transition> badTransitions
                = auxTransitions
                        .stream()
                        .filter(t -> !mainTransitions.contains(t))
                        .collect(Collectors.toCollection(ArrayList::new));

        if (!badTransitions.isEmpty()) {
            LOG.log(Level.INFO, "Holaaaaa");
        }

        ArrayList<Overreaction> result = new ArrayList<>();
        result.addAll(
                auxEv.getRightActions().stream()
                        .filter((a) -> badTransitions.contains(a.getTransition()))
                        .map((a) -> new Overreaction(a, missingCheck))
                        .collect(Collectors.toCollection(ArrayList::new))
        );
        result.addAll(auxEv.getSequentialMismatches().stream()
                .filter((d) -> badTransitions.contains(d.getLockingTransition()))
                .map((d) -> new Overreaction(new FiringOld(d.getLockedToken(), d.getLockingTransition()), missingCheck))
                .collect(Collectors.toCollection(ArrayList::new))
        );

        //Overreactions coming from sceondary performed actions (TODO)
        /*
        ArrayList<Overreaction> fromPerformedActions = auxEv.getPerformedActions()
                .stream()
                .filter((FiringOld action) -> {
                    return !mainEv
                            .getPerformedActions()
                            .stream()
                            .map(FiringOld::getTransition)
                            .collect(Collectors.toCollection(ArrayList::new))
                            .contains(action.getTransition());
                })
                .map((action) -> new Overreaction(action, missingCheck))
                .collect(Collectors.toCollection(ArrayList::new));
        
        ArrayList<Overreaction> fromSequentialMismatches = auxEv.getSequentialMismatches()
                .stream()
                .filter((DeadlockOld deadlock) -> {
                    return !mainEv
                            .getPerformedActions()
                            .stream()
                            .map(FiringOld::getTransition)
                            .collect(Collectors.toCollection(ArrayList::new))
                            .contains(deadlock.getLockingTransition());
                })
                .map((deadlock) -> new Overreaction(
                        new FiringOld(deadlock.getLockedToken(), deadlock.getLockingTransition()),
                        missingCheck)
                )
                .collect(Collectors.toCollection(ArrayList::new));
        
        result.addAll(fromPerformedActions);
        result.addAll(fromSequentialMismatches);
        
        return result;
         */
        return result;
    }
}
