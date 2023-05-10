package com.compdog.tcp.event;

import com.compdog.tcp.Client;

import java.util.EventListener;

public interface ClientConnectedEventListener extends EventListener {
    public boolean clientConnected(Client client);
}
