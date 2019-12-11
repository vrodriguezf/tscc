/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.SAVIER_integration.tsg;

import org.uam.aida.tscc.APFE.OPResponse;
import SAVIER_integration.SAVIEROperation;
import SAVIER_integration.data_log.STANAG4586.STANAG4586Log;
import org.uam.aida.tscc.APFE.utils.Triple;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author victor
 */
public class SAVIERInstantConditions {

    private static final Logger LOG = Logger.getLogger(SAVIERInstantConditions.class.getName());
    
    private static boolean intCondition(OPResponse ar,
            long time,
            String msgType,
            String fieldName,
            String operationType,
            Integer value) {
        
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Integer logValue = log
                .getMessages(msgType)
                .getAtInstant(time)
                .getIntFromField(fieldName);
        
        if (logValue == null) return false; //null-safe
        
        boolean result;
        switch (operationType) {
            case "=": result = logValue.equals(value);break;
            case "!=": result = !logValue.equals(value);break;
            case "<": result = logValue < value;break;
            case ">": result = logValue > value;break;
            default: return false;
        }
        return result;
    }
    
    private static boolean doubleCondition(OPResponse ar,
            long time,
            String msgType,
            String fieldName,
            String operationType,
            Double value) {
        
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Double logValue = log
                .getMessages(msgType)
                .getAtInstant(time)
                .getDoubleFromField(fieldName);
        
        if (logValue == null) return false; //null-safe
        
        boolean result;
        switch (operationType) {
            case "=": result = logValue.equals(value);break;
            case "!=": result = !logValue.equals(value);break;
            case "<": result = logValue < value;break;
            case ">": result = logValue > value;break;
            default: return false;
        }
        return result;
    }
    
    private static boolean booleanCondition(OPResponse ar,
            long time,
            String msgType,
            String fieldName,
            String operationType,
            Boolean value) {
        
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Boolean logValue = log
                .getMessages(msgType)
                .getAtInstant(time)
                .getBooleanFromField(fieldName);
        
        if (logValue == null) {
            LOG.log(Level.WARNING,"No value found for " + msgType + "-" + fieldName + "at time " + time);
            return false;
        }
        return (logValue.equals(value));
    }
    
    
    /**
     * 
     * @param alertResp
     * @param startTime
     * @param endTime
     * @param params
     * @return 
     */
    public static Boolean verifyConditions(
            OPResponse alertResp,
            Long time,
            Map<String,Object> params
    ) {
        //TODO: Adaptar para que pueda haber varias puertas logicas
        
        //Primer nivel: Params -> (MEssageType)
        List<Boolean> conditionResults = new ArrayList<Boolean>();
        params.forEach((String msgType, Object msgConditions_) -> {
            List<Object> asList = (List<Object>) msgConditions_;
            asList.forEach((Object condition_) -> {
                Triple<String,String,Object> condition = (Triple<String,String,Object>) condition_;
                Boolean conditionResult = null;
                if (condition.third instanceof Integer) {
                    conditionResult = intCondition(alertResp, 
                            time,
                            msgType, 
                            condition.first, 
                            condition.second,
                            (Integer) condition.third
                    );
                } else if (condition.third instanceof Double){
                    conditionResult = doubleCondition(alertResp, 
                            time,
                            msgType, 
                            condition.first, 
                            condition.second,
                            (Double) condition.third
                    );                
                } else if(condition.third instanceof Boolean) {
                    conditionResult = booleanCondition(alertResp,
                            time, 
                            msgType, 
                            condition.first, 
                            condition.second,
                            (Boolean) condition.third
                    );
                } else {
                    LOG.log(Level.SEVERE,"NO SE RECONOCE ESTE TIPO DE DATO!");
                }
                
                conditionResults.add(conditionResult);
            });
        });
        
        // Every condition must be fulfilled, and we return the maximum timestamp
        boolean allConditions = conditionResults
                .stream()
                .allMatch(x -> x.equals(Boolean.TRUE));
        
        if (allConditions) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 
     * @param ar
     * @param time
     * @param msgType
     * @param fieldName
     * @param value
     * @return 
     */
    public static boolean intEquality(OPResponse ar,
            long time,
            String msgType,
            String fieldName,
            Integer value) {
        
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Integer logValue = log
                .getMessages(msgType)
                .getAtInstant(time)
                .getIntFromField(fieldName);
        
        return (logValue.equals(value));
    }
}
