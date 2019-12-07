/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration.data_log.STANAG4586;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 *
 * @author victor
 */
public class STANAGMessageList extends ArrayList<STANAGMessage> {

    public STANAGMessageList(int initialCapacity) {
        super(initialCapacity);
    }

    public STANAGMessageList() {
    }

    public STANAGMessageList(Collection<? extends STANAGMessage> c) {
        super(c);
    }
    
    /**
     * Get a element of the log by time stamp. May be the timestamp is not the same, but
     * we tae the closest message
     * @param timeStamp
     * @return 
     */
    public STANAGMessage getAtInstant(Long timeStamp) {        
        //Coge el primero de los mensajes posteriores o igaules a este timestamp
        STANAGMessage result = 
                this
                .stream()
                .filter(m -> m.getTs() >= timeStamp)
                .sorted((STANAGMessage m1, STANAGMessage m2) -> {
                    return (int) (m1.getTs() - m2.getTs());
                })
                .findFirst()
                .orElse(new STANAGMessage(0L, new HashMap<>())); //Null-safe
        
        return result;
    }
    
    /**
     * THe result is returned sorted?? (YES)
     * @param startTime
     * @param endTime
     * @return 
     */
    public STANAGMessageList getAtInterval(Long startTime, Long endTime) {
        STANAGMessageList result = 
                this
                .stream()
                .filter((STANAGMessage o) -> {
                    return (o.getTs() > startTime && o.getTs() <= endTime);
                })
                .sorted((STANAGMessage m1, STANAGMessage m2) -> {
                    return (int) (m1.getTs() - m2.getTs());
                })
                .collect(Collectors.toCollection(STANAGMessageList::new));
        
        return result;
    }
}
