package com.compdog.tcp;

import java.nio.ByteBuffer;
import java.time.Instant;

public class SocketData {
    private int id;
    private Instant timestamp;
    private Instant clientTimestamp;
    private ByteBuffer data;

    public SocketData(int id, Instant timestamp, Instant clientTimestamp, ByteBuffer data){
        this.id = id;
        this.timestamp = timestamp;
        this.clientTimestamp = clientTimestamp;
        this.data = data;
    }

    public ByteBuffer getData() {
        return data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Instant getClientTimestamp() {
        return clientTimestamp;
    }

    public int getId() {
        return id;
    }
}
