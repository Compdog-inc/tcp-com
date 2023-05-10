package com.compdog.com;

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
        int length = bytes.getInt(0);

        StringBuilder sb = new StringBuilder(length);
        for(int i=0;i<length;i++){
            sb.append(bytes.getChar(i*2+4));
        }

        this.message = sb.toString();
        return true;
    }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + this.message.length()*2);

        buffer.putInt(0, this.message.length());
        for (int i = 0; i < this.message.length(); i++) {
            buffer.putChar(i*2 + 4, this.message.charAt(i));
        }

        return buffer;
    }
}
