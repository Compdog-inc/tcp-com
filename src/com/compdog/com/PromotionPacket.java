package com.compdog.com;

import com.compdog.tcp.ClientLevel;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class PromotionPacket extends SystemPacket {
    private ClientLevel level;

    public PromotionPacket(Instant timestamp, Duration netTime, Duration queueTime) {
        super(SystemPacket.SP_PROMOTE, timestamp, netTime, queueTime);
    }

    public PromotionPacket(Instant timestamp, ClientLevel level){
        super(SystemPacket.SP_PROMOTE, timestamp);
        setLevel(level);
    }

    @Override
    public boolean fromBytes(ByteBuffer bytes) {
        int l = bytes.getInt(0);
        this.level = l == 0 ? ClientLevel.Unauthorized : l == 1 ? ClientLevel.Authorized : null;
        return true;
    }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4);

        buffer.putInt(0, this.level == ClientLevel.Unauthorized ? 0 : this.level == ClientLevel.Authorized ? 1 : -1);

        return buffer;
    }

    public ClientLevel getLevel() {
        return level;
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }
}
