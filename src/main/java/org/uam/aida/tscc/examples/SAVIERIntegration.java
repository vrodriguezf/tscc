/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import org.uam.aida.tscc.APFE.OPTriggering;
import org.uam.aida.tscc.APFE.OPResponse;
import SAVIER_integration.SAVIERConstants;
import org.uam.aida.tscc.APFE.Evaluation;
import SAVIER_integration.SAVIEROperation;
import SAVIER_integration.SAVIEROperator;
import org.uam.aida.tscc.APFE.TSWFnet.TSWFNet;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import org.uam.aida.tscc.APFE.evaluation.EvaluationResultOld;
import SAVIER_integration.input.SAVIERInputManager;
import org.uam.aida.tscc.APFE.output.OutputManager;
import org.uam.aida.tscc.business.Global;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import SAVIER_integration.OP_models.APDL_FAIL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uam.aida.tscc.APFE.utils.Pair;
import SAVIER_integration.OP_models.ENGINE_BAY_OVERHEATING;
import SAVIER_integration.OP_models.BEFORE_TAKE_OFF;
import SAVIER_integration.OP_models.NORMAL_TAKE_OFF;


/**
 *
 * @author victor
 */
public class SAVIERIntegration {

    private static final Logger LOG = Logger.getLogger(SAVIERIntegration.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Command-line arguments
        String logFilePath = args[0];
        String operators[] = {"Savierito-1", "Savierito-2", "Savierito-3",
            "Savierito-4", "Savierito-5", "Savierito-6", "Savierito-7", "Savierito-8", "Savierito-9",
            "Savierito-10", "Savierito-11", "Savierito-12", "Savierito-13"};
        //Long alertTriggerTs = new BigDecimal(args[1]).longValue();
        String operationId;
        String operatorId;
        boolean evaluateNormalProcedures;

        Map<String, TSWFNet> OPsMap = new HashMap<>();
        OPsMap.put(SAVIERConstants.BEFORE_TAKE_OFF_ID, new BEFORE_TAKE_OFF());
        OPsMap.put(SAVIERConstants.NORMAL_TAKE_OFF_ID, new NORMAL_TAKE_OFF());
        OPsMap.put(SAVIERConstants.APDL_FAIL_ALERT_ID, new APDL_FAIL());
        OPsMap.put(SAVIERConstants.ENGINE_BAY_OVERHEATING_ALERT_ID, new ENGINE_BAY_OVERHEATING());

        if (args.length == 0) {
            LOG.log(Level.SEVERE, "Argument mismatch. Re-execute the program with the corret arguments");
        }

        if (args.length > 1) {
            operationId = args[1];
        } else {
            operationId = "DemoSAVIER-default";
        }

        if (args.length > 2) {
            operatorId = args[2];
        } else {
            operatorId = operators[(int) Math.floor(Math.random() * operators.length)];
        }
        
        if (args.length > 3) {
            evaluateNormalProcedures = true;
        } else {
            evaluateNormalProcedures = false;
        }

        //Try to load the log file
        STANAG4586Log log = null;
        LOG.log(Level.INFO, "*** READING LOGS ***");
        try {
            //Prueba : cargar un fichero .xlsx de log
            log = SAVIERInputManager.loadLogFile(logFilePath);
        } catch (IOException ex) {
            Logger.getLogger(SAVIERIntegration.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOG.log(Level.INFO, "*** DONE ****");

        //Evaluate the alert response
        Map<OPTriggering, EvaluationResultOld> evaluations = new HashMap<>();

        //Create the operation object (The id is extracted from the log file path)
        SAVIEROperation op = new SAVIEROperation(
                operationId,
                log,
                new SAVIEROperator(operatorId)
        );
        
        Map<String, List<Pair<Long, Long>>> trainingSchedule = new HashMap<>();
        
        //Introduce the normal procedures as "alerts"
        //TODO: Esto no esta bien conceptualmente
        /*
        trainingSchedule.put(
                SAVIERConstants.BEFORE_TAKE_OFF_ID,
                Arrays.asList(new Pair<Long,Long>(
                        op.getStartTime(),
                        op.getEndTime()
                )));
                */
        
        /*
        trainingSchedule.put(
                SAVIERConstants.NORMAL_TAKE_OFF_ID,
                Arrays.asList(new Pair<Long,Long>(
                        op.getStartTime(),
                        op.getEndTime()
                )));
*/

        //CREATE THE TRAINING PLAN
        //Get alerts in this operation and introduce it in the training plan
        //1. Normal procedures
        if (evaluateNormalProcedures)
        {
            trainingSchedule.put(
                SAVIERConstants.BEFORE_TAKE_OFF_ID, 
                Arrays.asList(new Pair<Long,Long>(op.getStartTime(),op.getEndTime())));
        
            trainingSchedule.put(
                SAVIERConstants.NORMAL_TAKE_OFF_ID, 
                Arrays.asList(new Pair<Long,Long>(op.getStartTime(),op.getEndTime())));
        }
        
        //2. Emergency procedures
        trainingSchedule.put(SAVIERConstants.APDL_FAIL_ALERT_ID,    
                                op.getAlertIntervals(SAVIERConstants.APDL_FAIL_ALERT_ID));
        trainingSchedule.put(
                SAVIERConstants.ENGINE_BAY_OVERHEATING_ALERT_ID,
                op.getAlertIntervals(SAVIERConstants.ENGINE_BAY_OVERHEATING_ALERT_ID)
        );

        LOG.log(Level.INFO,
                "-----------------OPERATION EVALUATION [{0}], timeRange=[{1},{2}])---------------",
                new Object[]{op.getId(), op.getStartTime(), op.getEndTime()});

        for (Entry<String, List<Pair<Long, Long>>> alertIntervals : trainingSchedule.entrySet()) {

            //Establish the emergency procedure to use
            TSWFNet eop = OPsMap.get(alertIntervals.getKey());
            Global.petriNet = eop;

            for (Pair<Long, Long> alertInterval : alertIntervals.getValue()) {
                OPTriggering alert = new OPTriggering(
                        alertIntervals.getKey(),
                        alertInterval.getKey(),
                        alertInterval.getValue(), //TODO: Esto se ha peusto porque la alerta no acaba!
                        eop
                );
                OPResponse ar = new OPResponse(alert, op);

                LOG.log(Level.INFO,
                        "-----------------STARTING PROCEDURE FOLLOWING EVALUATION [{0}], timeRange=[{1},{2}])---------------",
                        new Object[]{alert.getId(), alert.getTriggerTs(), alert.getEndTime()});

                EvaluationResultOld eval = null;
                try {
                    //Evaluation
                    eval = Evaluation.FBEvaluation(
                            eop.createInitialMarking(Arrays.asList(ar)),
                            alert,
                            eop);

                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }

                LOG.log(Level.INFO,
                        "-----------------ENDING PROCEDURE FOLLOWING EVALUATION [{0}], timeRange=[{1},{2}])---------------",
                        new Object[]{alert.getId(), alert.getTriggerTs(), alert.getEndTime()});
                LOG.log(Level.INFO, "1- PERFORMED ACTIONS: {0}", eval.getRightActions());

                LOG.log(Level.INFO, "2- MISSING ACTIONS: {0}", eval.getMissingActions());
                LOG.log(Level.INFO, "3- SEQUENTIAL MISMATCHES: {0}", eval.getSequentialMismatches());
                LOG.log(Level.INFO, "4- OVERREACTIONS: {0}", eval.getOverreactions());
                LOG.log(Level.INFO, "--------------------------------------------------");

                evaluations.put(alert, eval);
            }
        }

        //Store data (MongoDB)
        if (Global.DBSAVE) {
            OutputManager.saveEvaluationResults(op, evaluations);
            LOG.log(Level.INFO, "------------- Data was stored in database successfully --------------");
        }
    }
}
