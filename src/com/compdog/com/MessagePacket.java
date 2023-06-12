package com.compdog.com;

import com.compdog.util.BufferUtils;
import com.compdog.util.StringSerializer;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class MessagePacket extends SystemPacket {
    private String author;
    private int authorId;
    private String message;

    public MessagePacket(Instant timestamp, Duration netTime, Duration queueTime) {
        super(SystemPacket.SP_MESSAGE, timestamp, netTime, queueTime);
    }

    public MessagePacket(Instant timestamp, String author, int authorId, String message) {
        super(SystemPacket.SP_MESSAGE, timestamp);
        setAuthor(author);
        setAuthorId(authorId);
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
        int index = 0;
        this.message = StringSerializer.FromBytes(index, bytes);
        index += StringSerializer.GetSerializedLength(this.message);
        this.author = StringSerializer.FromBytes(index, bytes);
        index += StringSerializer.GetSerializedLength(this.author);
        this.authorId = bytes.getInt(index);

        return true;
    }

    @Override
    public ByteBuffer toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(
                StringSerializer.GetSerializedLength(this.message) +
                        StringSerializer.GetSerializedLength(this.author) +
                        4
        );

        int index = 0;
        BufferUtils.putBytes(buffer, index, StringSerializer.ToBytes(this.message));
        index += StringSerializer.GetSerializedLength(this.message);
        BufferUtils.putBytes(buffer, index, StringSerializer.ToBytes(this.author));
        index += StringSerializer.GetSerializedLength(this.author);
        buffer.putInt(index, this.authorId);

        return buffer;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }
}
