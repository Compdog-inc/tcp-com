package com.compdog.util;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.function.Function;

public class EventSource<T extends EventListener> {
    private final List<T> listeners = new ArrayList<>();

    public void addEventListener(T listener){
        listeners.add(listener);
    }

    public void removeEventListener(T listener){
        listeners.remove(listener);
    }

    public void invoke(Function<T, Boolean> executor){
        for(T l : listeners){
            if(executor.apply(l)) break;
        }
    }
}
