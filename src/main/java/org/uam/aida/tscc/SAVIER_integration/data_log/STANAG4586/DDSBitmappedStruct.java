/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SAVIER_integration.data_log.STANAG4586;

import java.math.BigDecimal;
import java.util.BitSet;

/**
 *
 * @author victor
 */
public class DDSBitmappedStruct {
    
    public static DDSBitmappedStruct fromString(final String s) {
        //Long aux = new BigDecimal(String.valueOf(s)).longValue();
        //return (DDSBitmappedStruct) BitSet.valueOf(new long[] { Long.parseLong(s, 2) });
        //BitSet auxBitSet = BitSet.valueOf(new long[] { aux });
        BitSet auxBitSet = BitSet.valueOf(new long[] { Long.parseLong(s, 2) });
        return new DDSBitmappedStruct(auxBitSet);
    }
    
    private BitSet bitSet;

    public DDSBitmappedStruct() {
        bitSet = new BitSet();
    }

    public DDSBitmappedStruct(BitSet bitSet) {
        this.bitSet = bitSet;
    }
    
    /**
     * Get a bit starting to count from the less signficative bit
     * @param bitIndex
     * @return 
     */
    public boolean getFromMSB(int bitIndex) {
        return bitSet.get(bitSet.length()-1-bitIndex);
    }
    
    public boolean getFromLSB(int bitIndex) {
        return bitSet.get(bitIndex);
    }
    
    @Override
    public String toString() {
        return Long.toString(bitSet.toLongArray()[0], 2);
    }

    public BitSet getBitSet() {
        return bitSet;
    }
}
