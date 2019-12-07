/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.SAVIER_integration.tsg;

import org.uam.aida.tscc.APFE.OPResponse;
import SAVIER_integration.SAVIEROperation;
import SAVIER_integration.data_log.STANAG4586.STANAGMessage;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import java.util.logging.Logger;

/**
 *
 * @author victor
 */
public final class SAVIERSupervisions {
    private static final Logger LOG = Logger.getLogger(SAVIERSupervisions.class.getName());
    
    /**
     * 
     * @param alertResp
     * @param startTime
     * @param endTime
     * @return 
     */
    public static Long UAVAbortsTakeOff(
            OPResponse alertResp,
            Long startTime,
            Long endTime) {
        
        Long result;
                
        if (startTime == null) startTime = alertResp.getOPTriggering().getTriggerTs();
        if (endTime == null) endTime = alertResp.getOPTriggering().getEndTime();
        
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();     
        
        result = log
                .getMessages("3001")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> m.getIntFromField("selectFPCtrlMode") == 56)
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        
        return result;
    }
    
    /**
     * 
     * @param alertResp
     * @param startTime
     * @param endTime
     * @return 
     */
    public static Long UAVWithinOmnidirectionalAntenna(OPResponse alertResp,
            Long startTime,
            Long endTime) {
        
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();     
        Long result = null; 
        
        if (startTime == null) startTime = alertResp.getOPTriggering().getTriggerTs();        
        if (endTime == null) endTime = alertResp.getOPTriggering().getEndTime();
        
        return result;
    }
    
}
