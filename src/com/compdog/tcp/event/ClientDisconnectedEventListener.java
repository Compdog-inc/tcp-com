package com.compdog.tcp.event;

import com.compdog.tcp.Client;

import java.util.EventListener;

public interface ClientDisconnectedEventListener extends EventListener {
    public boolean clientDisconnected(Client client);
}
