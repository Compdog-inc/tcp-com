package com.compdog.com;

import com.compdog.tcp.Client;
import com.compdog.tcp.Server;
import com.compdog.tcp.SocketData;

import java.time.Duration;
import java.time.Instant;

public class Test {
    public static void main(String[] args) {
        System.out.println("Start");
        Server server = new Server();

        server.getClientConnectedEventListenerEventSource().addEventListener(client -> {
            client.Send(new HelloPacket(Instant.now(), "Welcome to the server!"));
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

        while(true){
            for(Client client : server.getClients()){
                SocketData data = client.GetData().getSecond();
                if(data == null)
                    continue;
                switch(data.getId()) {
                    case SystemPacket.SP_HELLO:
                        HelloPacket hello = new HelloPacket(
                                data.getTimestamp(), // server time
                                Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                                Duration.between(data.getClientTimestamp(), Instant.now())); // process delay
                        hello.fromBytes(data.getData());
                        System.out.println("Hello from client! [" + hello.getNetTime().toString() + "/" + hello.getQueueTime().toString() + "]: " + hello.getMessage());
                        break;
                    case SystemPacket.SP_PING:
                        PingPacket packet = new PingPacket(data.getTimestamp(), // server time
                                Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                                Duration.between(data.getClientTimestamp(), Instant.now()));
                        packet.fromBytes(data.getData());
                        PingPacket.handlePing(packet, client);
                        break;
                }
            }
        }

        //server.Close();
    }
}
