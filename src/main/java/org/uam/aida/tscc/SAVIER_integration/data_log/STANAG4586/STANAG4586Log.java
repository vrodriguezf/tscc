/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration.data_log.STANAG4586;

import org.uam.aida.tscc.APFE.time_series_log.TimeSeriesLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author victor
 */
public class STANAG4586Log extends TimeSeriesLog {

    private static final Logger LOG = Logger.getLogger(STANAG4586Log.class.getName());
    
    // Mapa con los logs de cada mensaje
    private Map<String,STANAGMessageList> messages;

    public STANAG4586Log(String id) {
        super(id);
        messages = new HashMap<>();
    }

    public STANAG4586Log(String id, Map<String, STANAGMessageList> messages) {
        super(id);
        this.messages = messages;
    }

    public Map<String, STANAGMessageList> getMessagesMap() {
        return messages;
    }
    
    public STANAGMessageList getMessages(String messageId) {
        STANAGMessageList result = this.messages.get(messageId);
        return ((result != null) ? result : new STANAGMessageList());
    }
}
