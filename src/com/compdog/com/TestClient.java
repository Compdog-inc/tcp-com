package com.compdog.com;

import com.compdog.tcp.Client;
import com.compdog.tcp.ClientLevel;
import com.compdog.tcp.SocketData;

import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        Client client = new Client();
        client.Connect("172.27.13.63", 6868);

        new Thread(()->{
            while(client.isRunning()){
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }

                client.Send(new PingPacket(Instant.now())); // get ping
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        client.getMetadata().setUsername(username);
        client.Send(new AuthPacket(Instant.now(), username, password));

        long currentPing = 0;

        while(client.isRunning()) {
            SocketData data = client.GetData().getSecond();
            if (data == null)
                continue;
            switch (data.getId()) {
                case SystemPacket.SP_MESSAGE: {
                    MessagePacket packet = new MessagePacket(
                            data.getTimestamp(), // server time
                            Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                            Duration.between(data.getClientTimestamp(), Instant.now())); // process delay
                    packet.fromBytes(data.getData());
                    System.out.println("["+packet.getAuthor()+"]: "+packet.getMessage());
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
                case SystemPacket.SP_PROMOTE: {
                    PromotionPacket packet = new PromotionPacket(data.getTimestamp(), // server time
                            Duration.between(data.getTimestamp(), data.getClientTimestamp()), // send delay
                            Duration.between(data.getClientTimestamp(), Instant.now())); // process delay
                    packet.fromBytes(data.getData());
                    client.setLevel(packet.getLevel());
                    System.out.println("Promoted to " + client.getLevel());
                    if(client.getLevel() == ClientLevel.Dead){
                        client.Close();
                    } else if(client.getLevel() == ClientLevel.Authorized){
                        new Thread(()->{
                            while(client.isRunning()){
                                String line = scanner.nextLine();
                                client.Send(new MessagePacket(Instant.now(), client.getMetadata().getUsername(), -1, line));
                            }
                        }).start();
                    }
                }
                break;
            }
        }
        client.Close();
    }
}
