/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration.data_log.STANAG4586;

import org.uam.aida.tscc.APFE.time_series_log.LogEntry;
import java.util.BitSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author victor
 */
public class STANAGMessage extends LogEntry {

    private static final Logger LOG = Logger.getLogger(STANAGMessage.class.getName());
    
    private Map<String,Object> fieldsMap;
    
    public STANAGMessage(Long ts, Map<String,Object> fieldsMap) {
        super(ts, fieldsMap);
        this.fieldsMap = fieldsMap;
    }

    public Map<String, Object> getFieldsMap() {
        return fieldsMap;
    }
    
    public Object getField(String fieldName) {
        return this.fieldsMap.get(fieldName);
    }
    
    public Integer getIntFromField(String fieldName) {
        //Todos los field vienen en Double
        Double aux = getDoubleFromField(fieldName);
        if (aux != null) return aux.intValue();
                else return null;
    }
    
    public Double getDoubleFromField(String fieldName) {
        return (Double) this.fieldsMap.get(fieldName);
    }
    
    public Number getNumberFromField(String fieldName) {
        return (Number) this.fieldsMap.get(fieldName);
    }
    
    public Boolean getBooleanFromField(String fieldName) {
        return (Boolean) this.fieldsMap.get(fieldName);
    }
    
    public String getStringFromField(String fieldName) {
        return String.valueOf(this.fieldsMap.get(fieldName));
    }
    
    public DDSBitmappedStruct getBitMappedStructFromField(String fieldName) {
        String aux = getStringFromField(fieldName);
        System.out.println(aux);
        System.out.println(aux.substring(1, aux.length()-1));
        
        //Le quitamos el [] al array
        return DDSBitmappedStruct.fromString(aux.substring(1, aux.length()-1));
    }
}
