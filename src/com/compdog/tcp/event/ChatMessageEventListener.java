package com.compdog.tcp.event;

import com.compdog.com.MessagePacket;

import java.util.EventListener;

public interface ChatMessageEventListener extends EventListener {
    public boolean messageReceived(MessagePacket packet);
}
