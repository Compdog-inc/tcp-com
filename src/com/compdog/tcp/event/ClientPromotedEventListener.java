package com.compdog.tcp.event;

import com.compdog.tcp.Client;

import java.util.EventListener;

public interface ClientPromotedEventListener extends EventListener {
    public boolean clientPromoted(Client client);
}
