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
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author victor
 */
public final class SAVIERActions {

    private static final Logger LOG = Logger.getLogger(SAVIERActions.class.getName());

    
    
    /**
     *
     * @param alertResp
     * @param startTime
     * @param endTime
     * @param waypointNumber
     * @return
     */
    public static Long commandWaypoint(OPResponse alertResp,
            Long startTime,
            Long endTime,
            Map<String, Object> params) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Long result = null;
        int waypointNumber;

        if (startTime == null) {
            startTime = alertResp.getOPTriggering().getTriggerTs();
        }
        if (endTime == null) {
            endTime = alertResp.getOPTriggering().getEndTime();
        }
        if (params.get("waypointNumber") == null) {
            LOG.log(Level.WARNING, "Incorrect params. Exiting...");
            return null;
        } else {
            waypointNumber = (int) params.get("waypointNumber");
        }

        result = log
                .getMessages("2002")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> m.getIntFromField("commandedWaypointNumber") == waypointNumber)
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);

        //Ademas de comprobar que el comando se ha enviado, para que sea efectivo
        //el modo de control seleccionado debe haber cambiado a RESUME-TO-MISSION
        //  a partir de que se ha recibido el command waypoint
        if (result != null) {
            result = vehicleOperatingModeCommand(alertResp,
                    result,
                    endTime,
                    Collections.unmodifiableMap(
                            Stream.of(
                                    new SimpleEntry<>("mode", 33)
                            )
                            .collect(
                                    Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())
                            )
                    )
            );
        }

        return result;
    }

    /**
     *
     * @param alertResp
     * @param startTime
     * @param endTime
     * @param params
     * @return
     */
    public static Long vehicleOperatingModeCommand(OPResponse alertResp,
            Long startTime,
            Long endTime,
            Map<String, Object> params
    ) {

        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Long result = null;
        int commandNumber;

        if (startTime == null) {
            startTime = alertResp.getOPTriggering().getTriggerTs();
        }
        if (endTime == null) {
            endTime = alertResp.getOPTriggering().getEndTime();
        }

        if (params.get("mode") == null) {
            LOG.log(Level.WARNING, "Mode cannot be null. Exiting...");
            return null;
        } else {
            commandNumber = (int) params.get("mode");
        }

        result = log
                .getMessages("2001")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> m.getIntFromField("selectFPCtrlMode") == commandNumber)
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
     * @param params
     * @return
     */
    public static Long changeToGPDLOmnidirectAntenna(OPResponse alertResp,
            Long startTime,
            Long endTime,
            Map<String, Object> params) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Long result = null;

        if (startTime == null) {
            startTime = alertResp.getOPTriggering().getTriggerTs();
        }
        if (endTime == null) {
            endTime = alertResp.getOPTriggering().getEndTime();
        }

        result = log
                .getMessages("52001")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> {
                    return (Objects.equals(m.getBooleanFromField("gpdlOmniDirectionalAntenna"), Boolean.TRUE)
                            && Objects.equals(m.getBooleanFromField("gpdlDirectionalAntenna"), Boolean.FALSE));
                })
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);

        /*        
        result = log
                .getMessages("52001")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter(m -> {
                    return (Objects.equals(m.getBitMappedStructFromField("antennaIdentifier").getFromLSB(0), Boolean.TRUE)
                            && Objects.equals(m.getBitMappedStructFromField("antennaIdentifier").getFromLSB(1), Boolean.FALSE));
                })
                .findFirst()
                .map(DDSMessage::getTs)
                .orElse(null);
         */
        return result;
    }

    /**
     * TODO: Esta condicion esta un poco a medias
     * @param alertResp
     * @param startTime
     * @param endTime
     * @param params
     * @return
     */
    public static Long modifyUAVAltitudeOrSpeed(OPResponse alertResp,
            Long startTime,
            Long endTime,
            Map<String, Object> params) {
        STANAG4586Log log = ((SAVIEROperation) alertResp.getOperation()).getLog();
        Long result = null;

        // Altitude
        Long condition1 = log
                .getMessages("2002")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter((STANAGMessage m) -> {
                    return ((m.getIntFromField("commandedAltitudeType") == 1)
                            && (SAVIERQueriesUtils.verifyValueIncreasing(
                                    alertResp,
                                    "2002",
                                    "commandedAltitude",
                                    startTime,
                                    endTime,
                                    10.0)) != null);
                })
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);

        //Speed - ROC
        Long condition2 = log
                .getMessages("2002")
                .getAtInterval(startTime, endTime)
                .stream()
                .filter((STANAGMessage m) -> {
                    return ((m.getIntFromField("commandedAltitudeType") == 2)
                            && (SAVIERQueriesUtils.verifyValueIncreasing(
                                    alertResp,
                                    "2002",
                                    "commandedVerticalSpeed",
                                    startTime,
                                    endTime,
                                    2.0)) != null);
                })
                .findFirst()
                .map(STANAGMessage::getTs)
                .orElse(null);

        //Speed - GS or CAS
        Long condition3 = SAVIERQueriesUtils
                .verifyValueIncreasing(alertResp,
                        "2002",
                        "commandedSpeed",
                        startTime,
                        endTime,
                        2.0);

        //TODO: Esto devolvería null si los 3 son null?
        if (condition1 == null && condition2 == null && condition3 == null)
            return null;
        else
            return Collections.min(Arrays.asList(
                    (condition1 != null ? condition1 : Long.MAX_VALUE), 
                    (condition2 != null ? condition2 : Long.MAX_VALUE), 
                    (condition3 != null ? condition3 : Long.MAX_VALUE))
            );
    }
}
