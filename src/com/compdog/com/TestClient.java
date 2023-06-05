package com.compdog.com;

import com.compdog.com.gui.ChatWindow;
import com.compdog.tcp.Client;
import com.compdog.tcp.ClientLevel;
import com.compdog.tcp.SocketData;

import javax.swing.*;
import java.time.Duration;
import java.time.Instant;

public class TestClient {
    public static void main(String[] args) {
        String res = JOptionPane.showInputDialog(null, "server:port to connect to:", "Select Server", JOptionPane.PLAIN_MESSAGE);
        if(res == null || res.length() == 0){
            return;
        }

        String[] parts =res.split(":",2);

        Client client = new Client();
        ChatWindow chat = new ChatWindow(client, parts[0], Integer.parseInt(parts[1]));
        chat.setVisible(true);

        long currentPing = 0;

        while(!chat.isClientConnected()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
                    chat.getChatMessageEventListenerEventSource().invoke((e)->e.messageReceived(packet));
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
                    if(client.getLevel() == ClientLevel.Dead){
                        client.Close();
                    }
                    chat.getClientPromotedEventListenerEventSource().invoke((e)->e.clientPromoted(client));
                }
                break;
            }
        }
        client.Close();
    }
}
