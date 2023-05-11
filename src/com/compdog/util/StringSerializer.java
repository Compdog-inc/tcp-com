package com.compdog.util;

import java.nio.ByteBuffer;

public class StringSerializer {
    public static int GetSerializedLength(String str){
        return 4 + str.length() * 2;
    }

    public static ByteBuffer ToBuffer(String str) {
        ByteBuffer buffer = ByteBuffer.allocate(GetSerializedLength(str));

        buffer.putInt(0, str.length());
        for (int i = 0; i < str.length(); i++) {
            buffer.putChar(i * 2 + 4, str.charAt(i));
        }

        return buffer;
    }

    public static byte[] ToBytes(String str) {
        return ToBuffer(str).array();
    }

    public static String FromBytes(byte[] bytes) {
        return FromBytes(0, ByteBuffer.wrap(bytes));
    }

    public static String FromBytes(int index, ByteBuffer buffer) {
        int length = buffer.getInt(index);

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(buffer.getChar(index + i * 2 + 4));
        }

        return sb.toString();
    }
}
