/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.SAVIER_integration.tsg;

import org.uam.aida.tscc.APFE.OPResponse;
import SAVIER_integration.SAVIERConstants;
import SAVIER_integration.SAVIEROperation;
import SAVIER_integration.data_log.STANAG4586.STANAGMessage;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import java.util.logging.Logger;

/**
 *
 * @author victor
 */
public final class SAVIERChecks {
    private static final Logger LOG = Logger.getLogger(SAVIERChecks.class.getName());
    
    /**
     * 
     * @param ar
     * @param time
     * @return 
     */
    public static Boolean isFlightPhaseTakeOffRunActionsBeforeVROT(OPResponse ar, Long time) {
        boolean result = true;
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        // Check selectFPCtrlMode == 53 or 55 or 57
        int selectFPCtrlMode = log
                        .getMessages("3001")
                        .getAtInstant(time)
                        .getIntFromField("selectFPCtrlMode");
        
        if (selectFPCtrlMode != 53 && selectFPCtrlMode != 55 && selectFPCtrlMode != 57)
            return false;
        
        //Check CAS < VROT
        Double CAS = log
                        .getMessages("55001")
                        .getAtInstant(time)
                        .getDoubleFromField("calibratedAirSpeed");
        
        if (CAS < SAVIERConstants.VROT)
            return true;
        else 
            return false;
    }
    
    public static Boolean PDLUPshow0InCoverageRedColor(OPResponse alertResp, Long time) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Boolean result = true;

        result = result && (log
                .getMessages("58501")
                .getAtInstant(time)
                .getIntFromField("gpdlUplinkQoS") == 0);
        
        result = result && (log
                .getMessages("58501")
                .getAtInstant(time)
                .getIntFromField("gpdlDetailStatus") == 8);
        
        return result;
    }
    
    public static Boolean PDLStatusIsFail(OPResponse alertResp, Long time) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Boolean result = true;
        
        //TODO: Esto esta bien!!
        result = result && (log
                .getMessages("58503")
                .getAtInstant(time)
                .getIntFromField("gpdlWarning") == 3);
        
        /*
        result = result && (log
                .getMessages("58503")
                .getAtInstant(time)
                .getIntFromField("gdlms1Warning") == 4);        
        */
        
        
        result = result && (log
                .getMessages("55502")
                .getAtInstant(time)
                .getBooleanFromField("apdlFailCaut") == Boolean.TRUE);
        
        /*
        result = result && (log
                .getMessages("55502")
                .getAtInstant(time)
                .getBitMappedStructFromField("alertReport")
                .getFromLSB(77) == true);
        */
        
        return result;
    }
    
    public static Boolean GOStatusIsNOGO(OPResponse alertResp, Long time) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Boolean result = true;
        
        
        result = result &&(log
                .getMessages("55505")
                .getAtInstant(time)
                .getBooleanFromField("airSegment") == Boolean.FALSE);

/*        
        result = result &&(log
                .getMessages("55505")
                .getAtInstant(time)
                .getBitMappedStructFromField("statusReport")
                .getFromLSB(17)== Boolean.FALSE);        
        */
        
        return result;
    }
    
    public static Boolean flightModeIsTakeOffArmed(OPResponse alertResp, Long time) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Boolean result = true;

        result = result && (log
                .getMessages("3001")
                .getAtInstant(time)
                .getIntFromField("selectFPCtrlMode") == 55);
        
        return result;
    }
    
    /**
     * 
     * @param alertResp
     * @param time
     * @return 
     */
    public static Boolean PDLAirAntennaIsDisplayedInRed(OPResponse alertResp, Long time) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Boolean result = true;
        
        result = result && (log
                .getMessages("53501")
                .getAtInstant(time)
                .getIntFromField("airCommsStatus") == 1);
        
        STANAGMessage aux = log.getMessages("55502").getAtInstant(time);
        
        result = result && (
                aux.getBooleanFromField("adlms1BitCaut") == Boolean.TRUE ||
                aux.getBooleanFromField("adlms1FailCaut") == Boolean.TRUE);
        
        /*
        result = result && (
                aux.getBitMappedStructFromField("alertReport").getFromLSB(70) == true ||
                aux.getBitMappedStructFromField("alertReport").getFromLSB(71) == true 
                );
        */
        
        return result;
    }
    
    /**
     * 
     * @param ar
     * @param time
     * @return 
     */
    public static Boolean prueba(OPResponse ar, Long time) {
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Integer cas = log
                .getMessages("55001")
                .getAtInstant(time)
                .getIntFromField("equivalentAirSpeed");
        
        return cas == 40;
    }
}
