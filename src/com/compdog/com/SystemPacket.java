package com.compdog.com;

import com.compdog.tcp.IPacket;

import java.time.Duration;
import java.time.Instant;

public abstract class SystemPacket implements IPacket {
    public static final int SP_HELLO = 1; // system Hello packet
    public static final int SP_PING = 2; // system Ping packet

    private final int id;
    private final Instant timestamp;
    private final Duration netTime;
    private final Duration queueTime;

    public SystemPacket(int id, Instant timestamp){
        this.id = id;
        this.timestamp = timestamp;
        this.netTime = Duration.ZERO;
        this.queueTime = Duration.ZERO;
    }

    public SystemPacket(int id, Instant timestamp, Duration netTime, Duration queueTime){
        this.id = id;
        this.timestamp = timestamp;
        this.netTime = netTime;
        this.queueTime = queueTime;
    }

    @Override
    public final int getId(){
        return id;
    }

    @Override
    public final Instant getTimestamp(){
        return timestamp;
    }

    @Override
    public final Duration getNetTime(){
        return netTime;
    }

    @Override public final Duration getQueueTime(){
        return queueTime;
    }
}
