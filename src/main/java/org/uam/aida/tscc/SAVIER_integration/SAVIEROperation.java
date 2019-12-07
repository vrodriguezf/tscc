/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration;

import org.uam.aida.tscc.APFE.Operation;
import SAVIER_integration.data_log.STANAG4586.STANAGMessage;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import SAVIER_integration.data_log.STANAG4586.STANAGMessageList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.uam.aida.tscc.APFE.utils.Pair;

/**
 *
 * @author victor
 */
public class SAVIEROperation extends Operation {

    private static final Logger LOG = Logger.getLogger(SAVIEROperation.class.getName());

    //@deprecated
    //private STANAG4586Log log;

    public SAVIEROperation(String id, SAVIEROperator operator) {
        super(id, operator);
    }

    public SAVIEROperation(String id, STANAG4586Log log, SAVIEROperator operator) {
        super(id, operator, log);
        //this.log = log;
    }

    @Override
    public STANAG4586Log getLog() {
        return (STANAG4586Log) log;
    }

    @Override
    public Long getStartTime() {
        //Check the first timestamp of the logs
        return this.getLog().getMessagesMap().values()
                .stream()
                .mapToLong((STANAGMessageList messages) -> {
                    return messages
                            .stream()
                            .mapToLong(STANAGMessage::getTs)
                            .min()
                            .orElse(0L);
                })
                .min()
                .getAsLong();
    }

    @Override
    public Long getEndTime() {
        //Check the last timestamp of the logs
        return this.getLog().getMessagesMap().values()
                .stream()
                .mapToLong((messages) -> {
                    return messages
                            .stream()
                            .mapToLong(STANAGMessage::getTs)
                            .max()
                            .orElse(0L);
                })
                .max()
                .getAsLong();
    }

    /**
     *
     * @param alertField
     * @return
     */
    public List<Pair<Long, Long>> getAlertIntervals(String alertId) {

        List<Pair<Long, Long>> result = new ArrayList<>();
        Long opFinalTs = getEndTime();
        Long alertStartTime;
        Long alertEndTime;
        Long searchFrom = 0L;

        //Alertas normales (se activan en el mensaje 55502)
        if (!alertId.equals(SAVIERConstants.ENGINE_BAY_OVERHEATING_ALERT_ID)) {
            do {
                alertStartTime = this.getLog()
                        .getMessages("55502")
                        .getAtInterval(searchFrom, opFinalTs)
                        .stream()
                        .filter(m -> m.getBooleanFromField(alertId) == Boolean.TRUE)
                        .findFirst()
                        .map(STANAGMessage::getTs)
                        .orElse(null);

                if (alertStartTime != null) {
                    alertEndTime = this.getLog()
                            .getMessages("55502")
                            .getAtInterval(alertStartTime, opFinalTs)
                            .stream()
                            .filter(m -> m.getBooleanFromField(alertId) == Boolean.FALSE)
                            .findFirst()
                            .map(STANAGMessage::getTs)
                            .orElse(opFinalTs);

                    result.add(new Pair<Long, Long>(alertStartTime, alertEndTime));

                    //Avanzamos la cota inferior de busqueda
                    searchFrom = alertEndTime;
                }
            } while (alertStartTime != null);
        } else {
            //ENGINE_BAY_OVERHEATING
            do {
                alertStartTime = this.getLog()
                        .getMessages("55254")
                        .getAtInterval(searchFrom, opFinalTs)
                        .stream()
                        .filter(m -> m.getIntFromField("Engine_Bay_Temperature") == 0)
                        .findFirst()
                        .map(STANAGMessage::getTs)
                        .orElse(null);

                if (alertStartTime != null) {
                    alertEndTime = this.getLog()
                            .getMessages("55254")
                            .getAtInterval(alertStartTime, opFinalTs)
                            .stream()
                            .filter(m -> m.getIntFromField("Engine_Bay_Temperature") == 3)
                            .findFirst()
                            .map(STANAGMessage::getTs)
                            .orElse(opFinalTs);

                    result.add(new Pair<Long, Long>(alertStartTime, alertEndTime));

                    //Avanzamos la cota inferior de busqueda
                    searchFrom = alertEndTime;
                }
            } while (alertStartTime != null);
        }

        return result;
    }
}
