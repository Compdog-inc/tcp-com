package com.compdog.com;

import com.compdog.tcp.Client;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class PingPacket extends SystemPacket {
    private PingMode mode;
    private Instant initialTimestamp = Instant.MIN;

    public PingPacket(Instant timestamp, Duration netTime, Duration queueTime) {
        super(SystemPacket.SP_PING, timestamp, netTime, queueTime);
    }

    public PingPacket(Instant timestamp) {
        super(SystemPacket.SP_PING, timestamp);
        setMode(PingMode.Initial);
    }

    @Override
    public boolean fromBytes(ByteBuffer bytes) {
        int m = bytes.getInt(0);
        this.mode = m == 0 ? PingMode.Initial : m == 1 ? PingMode.Echo : null;
        this.initialTimestamp = Instant.ofEpochSecond(bytes.getLong(4), bytes.getInt(12));

        return true;
    }

    @Override
    public ByteBuffer toBytes() {
        return ByteBuffer.allocate(4+8+4)
                .putInt(0, mode == PingMode.Initial ? 0 : mode == PingMode.Echo ? 1 : -1)
                .putLong(4, initialTimestamp.getEpochSecond())
                .putInt(12, initialTimestamp.getNano());
    }

    public PingMode getMode() {
        return mode;
    }

    public void setMode(PingMode mode) {
        this.mode = mode;
    }

    public Instant getInitialTimestamp() {
        return initialTimestamp;
    }

    public void setInitialTimestamp(Instant initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }

    public static long handlePing(PingPacket ping, Client client){
        switch (ping.getMode()) {
            case Initial:
                PingPacket echo = new PingPacket(Instant.now());
                echo.setMode(PingMode.Echo);
                echo.setInitialTimestamp(ping.getTimestamp());
                client.Send(echo); // echo back
                break;
            // send + one way delay
            case Echo:
                Instant start = ping.getInitialTimestamp();
                Instant end = ping.getTimestamp().plusNanos(ping.getNetTime().toNanos());
                Duration delay = Duration.between(start, end);
                return delay.toMillis();
        }
        return -1;
    }
}
