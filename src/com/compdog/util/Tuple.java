package com.compdog.util;

public class Tuple<T0,T1> {
    private T0 first;
    private T1 second;

    public Tuple(T0 first, T1 second){
        this.first = first;
        this.second = second;
    }

    public T0 getFirst() {
        return first;
    }

    public T1 getSecond() {
        return second;
    }
}
