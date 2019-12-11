/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.config;

import com.moandjiezana.toml.Toml;

/**
 * 
 * @author victor
 */
public interface Configurable<T> {
    public void configure(T cfg);
}
