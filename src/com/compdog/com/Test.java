package com.compdog.com;

import com.compdog.com.auth.Authenticator;
import com.compdog.tcp.Client;
import com.compdog.tcp.ClientLevel;
import com.compdog.tcp.Server;
import com.compdog.tcp.SocketData;

import java.time.Duration;
import java.time.Instant;

public class Test {
    public static void main(String[] args) {
        System.out.println("Start");
        Server server = new Server();
        long currentPing = 0;

        server.getClientConnectedEventListenerEventSource().addEventListener(client -> {
            client.Send(new HelloPacket(Instant.now(),
                    "Welcome to the server, " +
                            client.GetSocketInstance().getInetAddress().toString()
            ));
            return false;
        });

        server.Listen(6868);

        new Thread(()->{
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(Client client : server.getClients()){
                    client.Send(new PingPacket(Instant.now())); // send ping
                }
            }
        }).start();

        while(true) {
            for (Client client : server.getClients()) {
                if(client.getLevel() == ClientLevel.Dead){
                    // dead clients shouldn't exist
                    client.Close();
                }

                SocketData data = client.GetData().getSecond();
                if (data == null)
                    continue;
                switch (data.getId()) {
                    case SystemPacket.SP_HELLO: {
                        HelloPacket hello = new HelloPacket(
                                data.getTimestamp(), // server time
                                Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                                Duration.between(data.getClientTimestamp(), Instant.now())); // process delay
                        hello.fromBytes(data.getData());
                        System.out.println("Hello from client! [" + hello.getNetTime().toString() + "/" + hello.getQueueTime().toString() + "]: " + hello.getMessage());
                    }
                    break;
                    case SystemPacket.SP_PING: {
                        PingPacket packet = new PingPacket(data.getTimestamp(), // server time
                                Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                                Duration.between(data.getClientTimestamp(), Instant.now()));
                        packet.fromBytes(data.getData());
                        long ping = PingPacket.handlePing(packet, client);
                        if (ping >= 0) { // received echo
                            currentPing = ping;
                        }
                    }
                    break;
                    case SystemPacket.SP_AUTH: {
                        AuthPacket packet = new AuthPacket(data.getTimestamp(), // server time
                                Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                                Duration.between(data.getClientTimestamp(), Instant.now()));
                        packet.fromBytes(data.getData());
                        if (Authenticator.Authenticate(packet.getUsername(), packet.getPassword())) {
                            client.getMetadata().setUsername(packet.getUsername());
                            client.Promote(ClientLevel.Authorized);
                        } else {
                            client.Promote(ClientLevel.Dead);
                        }
                    }
                    break;
                    case SystemPacket.SP_PROMOTE: {
                        PromotionPacket packet = new PromotionPacket(data.getTimestamp(), // server time
                                Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                                Duration.between(data.getClientTimestamp(), Instant.now()));
                        packet.fromBytes(data.getData());
                        System.out.println("[" + client.getMetadata().getUsername() + "] tried to promote server to " + packet.getLevel().toString());
                    }
                    break;
                }
            }
        }

        //server.Close();
    }
}