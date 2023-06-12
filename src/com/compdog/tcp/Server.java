package com.compdog.tcp;

import com.compdog.tcp.event.ClientConnectedEventListener;
import com.compdog.tcp.event.ClientDisconnectedEventListener;
import com.compdog.tcp.event.SocketClosedEventListener;
import com.compdog.util.EventSource;
import com.compdog.util.Logger;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final Logger logger = Logger.getLogger("Server");

    private ServerSocket server;
    private Thread serverThread;
    private volatile boolean runServer = false;
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private Random random = new Random();

    private final EventSource<ClientConnectedEventListener> clientConnectedEventListenerEventSource;
    private final EventSource<ClientDisconnectedEventListener> clientDisconnectedEventListenerEventSource;

    public Server(){
        clientConnectedEventListenerEventSource = new EventSource<>();
        clientDisconnectedEventListenerEventSource = new EventSource<>();
    }

    public void Listen(int port){
        runServer = true;

        try {
            server = new ServerSocket(port);
            serverThread = new Thread(this::ServerThread);
            serverThread.setName("ServerThread");
            serverThread.start();

            logger.log(Logger.Level.INFO, "Listening: "+ Inet4Address.getLocalHost().getHostAddress()+":"+port);
        } catch (IOException e){
            logger.log(Logger.Level.ERROR, "Error starting server: "+e.getMessage());
        }
    }

    public void Close(){
        for(Client client : clients){
            client.Close();
        }

        runServer = false;
        try {
            server.close();
            serverThread.join();
        } catch (IOException e){
            logger.log(Logger.Level.ERROR, "Error closing server: "+e.getMessage());
        } catch (InterruptedException e){
            logger.log(Logger.Level.WARNING, "Server thread interrupted: "+e.getMessage());
        }
    }

    private final SocketClosedEventListener socketClosedEventListener = new SocketClosedEventListener() {
        @Override
        public boolean socketClosed(Client client) {
            clients.remove(client);
            clientDisconnectedEventListenerEventSource.invoke(l -> l.clientDisconnected(client));
            return false;
        }
    };

    private void ServerThread(){
        while(runServer){
            try {
                Socket socket = server.accept();
                Client client = new Client();
                client.WrapSocket(socket);
                client.getSocketClosedEventListenerEventSource().addEventListener(socketClosedEventListener);
                client.getMetadata().setId(random.nextInt());
                clients.add(client);
                clientConnectedEventListenerEventSource.invoke(l->l.clientConnected(client));
            } catch (IOException e){
                logger.log(Logger.Level.ERROR, "Error waiting for clients: "+e.getMessage());
            }
        }
    }

    public EventSource<ClientConnectedEventListener> getClientConnectedEventListenerEventSource() {
        return clientConnectedEventListenerEventSource;
    }

    public EventSource<ClientDisconnectedEventListener> getClientDisconnectedEventListenerEventSource() {
        return clientDisconnectedEventListenerEventSource;
    }

    public List<Client> getClients(){
        return clients;
    }
}
