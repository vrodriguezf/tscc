/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uam.aida.tscc.APFE.timeseries_guards;

import com.moandjiezana.toml.Toml;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author victor
 */
public abstract class ComposedTSG extends TSG {
    
    public ComposedTSG(List<TSG> parts) {
        this.parts = parts;
    }
    
    public ComposedTSG(TSG... parts) {
        this(Arrays.asList(parts));
    }

    public List<TSG> getParts() {
        return parts;
    }
    
    public List<TSG> flatten() {
        return parts.stream()
                .flatMap((TSG x) -> {
                    if (x instanceof ComposedTSG)
                        return ((ComposedTSG) x).flatten().stream();
                    else
                        return Stream.of(x);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void configure(Toml cfg) {
        Optional.ofNullable(cfg.getTables("parts"))
                .filter(x -> x.size() == getParts().size())
                .ifPresent((List<Toml> partsCfg) -> {
                    IntStream.range(0, partsCfg.size())
                            .forEach(i -> {
                                getParts().get(i).configure(partsCfg.get(i));
                            });
                });
    }
    
    /**
     * private things
     */
    protected List<TSG> parts;
}
