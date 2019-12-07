/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

/**
 *
 * @author victor
 */
public enum TypeOfLogic {
    ANY_MATCH, ALL_MATCH;

    public TypeOfLogic getComplementary() {
        if (equals(ANY_MATCH)) {
            return ALL_MATCH;
        } else if (equals(ALL_MATCH)) {
            return ANY_MATCH;
        } else {
            return this;
        }
    }
};
