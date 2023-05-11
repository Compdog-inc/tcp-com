package com.compdog.com;

import com.compdog.util.StringSerializer;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class HelloPacket extends SystemPacket {
    private String message;

    public HelloPacket(Instant timestamp, Duration netTime, Duration queueTime) {
        super(SystemPacket.SP_HELLO, timestamp, netTime, queueTime);
    }

    public HelloPacket(Instant timestamp, String message) {
        super(SystemPacket.SP_HELLO, timestamp);
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean fromBytes(ByteBuffer bytes) {
        this.message = StringSerializer.FromBytes(0, bytes);
        return true;
    }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(StringSerializer.GetSerializedLength(this.message));

        buffer.put(0, StringSerializer.ToBytes(this.message));

        return buffer;
    }
}
