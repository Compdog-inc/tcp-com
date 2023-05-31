package com.compdog.com;

import com.compdog.tcp.Client;

import java.time.Instant;

public class Pinger {
    public static void SinglePing(Client client){
        client.Send(new PingPacket(Instant.now())); // get ping
    }

    public static void StartPing(Client client, long interval){
        new Thread(()->{
            while(client.isRunning()){
                try {
                    Thread.sleep(interval);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }

                if(!client.isRunning()) return;
                SinglePing(client);
            }
        }).start();
    }
}
