/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.TSWFnet;

import org.uam.aida.tscc.APFE.OPResponse;
import java.util.Map;

/**
 *
 * @author victor
 */
@FunctionalInterface
public interface ActionEvaluator {
    public Long getFirstActionTimestamp(OPResponse alertResp, 
            Long startTime, 
            Long endTime,
            Map<String,Object> params);
}
