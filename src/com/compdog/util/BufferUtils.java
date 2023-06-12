package com.compdog.util;

import java.nio.ByteBuffer;

public class BufferUtils {
    public static void putBytes(ByteBuffer buffer, int index, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            buffer.put(index + i, bytes[i]);
        }
    }

    public static void getBytes(ByteBuffer buffer, int index, byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buffer.get(index + i);
        }
    }
}
