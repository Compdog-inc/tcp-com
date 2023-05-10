package com.compdog.tcp.event;

import com.compdog.tcp.Client;

import java.util.EventListener;

public interface SocketClosedEventListener extends EventListener {
    public boolean socketClosed(Client client);
}
