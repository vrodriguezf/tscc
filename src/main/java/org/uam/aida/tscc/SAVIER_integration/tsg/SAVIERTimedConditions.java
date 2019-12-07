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
import org.uam.aida.tscc.APFE.utils.Triple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * El Object de param es un List<Triple> que contiene campo,valor, operacion
 * Sigue la interfaz ActionEvaluator
 * TODO Añadir inequalities
 * @author victor
 */
public class SAVIERTimedConditions {

    private static final Logger LOG = Logger.getLogger(SAVIERTimedConditions.class.getName());
    
    private static Long intCondition(
            OPResponse ar,
            long startTime,
            long endTime,
            String msgType,
            String fieldName,
            String operationType,
            Integer value
    ) {
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Long result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> m.getIntFromField(fieldName)!= null && m.getIntFromField(fieldName).equals(value))
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        
        return result;
    }
    
    private static Long booleanCondition(
            OPResponse ar,
            long startTime,
            long endTime,
            String msgType,
            String fieldName,
            String operationType,
            Boolean value
    ) {
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Long result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> m.getBooleanFromField(fieldName).equals(value))
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        
        return result;
    }
    
    private static Long doubleCondition(
            OPResponse ar,
            long startTime,
            long endTime,
            String msgType,
            String fieldName,
            String operationType,
            Double value
    ) {
        STANAG4586Log log = ((SAVIEROperation) ar.getOperation()).getLog();
        
        Long result = null;
        
        if ("=".equals(operationType))
        {
            result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> m.getDoubleFromField(fieldName).equals(value))
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        }
        else if ("!=".equals(operationType))
        {
            result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> !m.getDoubleFromField(fieldName).equals(value))
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        }
        else if ("<".equals(operationType))
        {
            result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> (m.getDoubleFromField(fieldName) < value))
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        }
        else if (">".equals(operationType))
        {
            result = log
                .getMessages(msgType)
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> (m.getDoubleFromField(fieldName) > value))
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);
        }
        
        return result;
    }
    
    /**
     * Interfaz ActionEvaluator
     * @param alertResp
     * @param startTime
     * @param endTime
     * @param params
     * @return 
     */
    public static Long verifyConditions(
            OPResponse alertResp,
            Long startTime,
            Long endTime,
            Map<String,Object> params
    ) {
        //TODO: Adaptar para que pueda haber varias puertas logicas
        
        //Primer nivel: Params -> (MEssageType)
        List<Long> conditionResults = new ArrayList<Long>();
        params.forEach((String msgType, Object msgConditions_) -> {
            List<Object> asList = (List<Object>) msgConditions_;
            asList.forEach((Object condition_) -> {
                Triple<String,String,Object> condition = (Triple<String,String,Object>) condition_;
                Long conditionResult = null;
                if (condition.third instanceof Integer) {
                    conditionResult = intCondition(alertResp, 
                            startTime, 
                            endTime, 
                            msgType, 
                            condition.first, 
                            condition.second,
                            (Integer) condition.third
                    );
                } else if (condition.third instanceof Double){
                    conditionResult = doubleCondition(alertResp, 
                            startTime, 
                            endTime, 
                            msgType, 
                            condition.first, 
                            condition.second,
                            (Double) condition.third
                    );                
                } else if(condition.third instanceof Boolean) {
                    conditionResult = booleanCondition(alertResp, 
                            startTime, 
                            endTime, 
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
                .allMatch(x -> x != null);
        
        if (allConditions) {
            return Collections.max(conditionResults);
        } else {
            return null;
        }
    }
}
