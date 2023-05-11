package com.compdog.com;

import com.compdog.util.StringSerializer;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class AuthPacket extends SystemPacket {
    private String username;
    private String password;

    public AuthPacket(Instant timestamp, Duration netTime, Duration queueTime) {
        super(SystemPacket.SP_AUTH, timestamp, netTime, queueTime);
    }

    public AuthPacket(Instant timestamp, String username, String password){
        super(SystemPacket.SP_AUTH, timestamp);
        setUsername(username);
        setPassword(password);
    }

    @Override
    public boolean fromBytes(ByteBuffer bytes) {
        this.username = StringSerializer.FromBytes(0, bytes);
        this.password = StringSerializer.FromBytes(StringSerializer.GetSerializedLength(this.username), bytes);
        return true;
    }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(StringSerializer.GetSerializedLength(this.username) + StringSerializer.GetSerializedLength(this.password));

        buffer.put(0, StringSerializer.ToBytes(this.username));
        buffer.put(StringSerializer.GetSerializedLength(this.username), StringSerializer.ToBytes(this.password));

        return buffer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
