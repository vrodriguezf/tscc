/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.SAVIER_integration.tsg;

import org.uam.aida.tscc.APFE.OPResponse;
import SAVIER_integration.SAVIEROperation;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import SAVIER_integration.data_log.STANAG4586.STANAGMessage;

/**
 *
 * @author victor
 */
public class SAVIERQueriesUtils {

    /**
     * Verify, in a given interval [startTime,endTime] whether a given value
     * increases, taking as baseline the startTime-value.
     *
     * @param msgType
     * @param fieldName
     * @param startTime
     * @param endTime
     * @return TimeStamp when the value increases for the first time. null if
     * the value does not increase
     */
    public static Long verifyValueIncreasing(
            OPResponse alertResp,
            String msgType,
            String fieldName,
            long startTime,
            long endTime,
            Double threshold) {
        
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();

        Double baseValue = log
                .getMessages(msgType)
                .getAtInstant(startTime)
                .getDoubleFromField(fieldName);

        Long result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> (m.getDoubleFromField(fieldName) - baseValue) > threshold)
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);

        return result;
    }
}
