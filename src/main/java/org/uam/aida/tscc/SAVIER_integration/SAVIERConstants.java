/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration;

/**
 *
 * @author victor
 */
public class SAVIERConstants {
    public static final int LOG_PERIOD = 1; //ms
    public static final double VROT = 65; //knots
    
    //Before Take Off
    public static final String BEFORE_TAKE_OFF_ID = "BEFORE_TAKE_OFF";
    
    //Normal Take Off
    public static final String NORMAL_TAKE_OFF_ID = "NORMAL_TAKE_OFF";
    
    //APDL FAIL
    public static final String APDL_FAIL_ALERT_ID = "apdlFailCaut";
    
    //ENGINE BAY OVERHEATING (EBO)
    public static final String ENGINE_BAY_OVERHEATING_ALERT_ID = "ENGINE_BAY_OVERHEATING";
    public static final Long EBO_EXTRA_RESPONSE_TIME = 90000L; //ms
}
