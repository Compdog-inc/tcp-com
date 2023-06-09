package com.compdog.tcp;

import com.compdog.util.BufferUtils;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public interface IPacket {
    /**
     * The id of the packet
     */
    int getId();

    /**
     * The sender timestamp (from tcp data)
     */
    Instant getTimestamp();

    /**
     * Duration between send and receive time
     */
    Duration getNetTime();

    /**
     * Duration between receive time and processing using GetData
     */
    Duration getQueueTime();

    /**
     * Set packet data from rae bytes
     */
    boolean fromBytes(ByteBuffer bytes);

    /**
     * Convert packet data to raw bytes
     */
    ByteBuffer toBytes();

    static byte[] getPacketData(IPacket packet) {
        byte[] data = packet.toBytes().array();
        ByteBuffer buf = ByteBuffer.allocate(4 + 8 + 4 + 4 + data.length) // int id, long time seconds, int time nano, int data length, byte[] data
                .putInt(0, packet.getId())
                .putLong(4, packet.getTimestamp().getEpochSecond())
                .putInt(12, packet.getTimestamp().getNano())
                .putInt(16, data.length);
        BufferUtils.putBytes(buf, 20, data);
        return buf.array();
    }
}
