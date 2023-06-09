package com.compdog.tcp;

import com.compdog.com.PromotionPacket;
import com.compdog.tcp.event.SocketClosedEventListener;
import com.compdog.util.EventSource;
import com.compdog.util.Logger;
import com.compdog.util.Tuple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private static final Logger logger = Logger.getLogger("Client");

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Thread socketThread;
    private volatile boolean runThread = false;
    private final ConcurrentLinkedQueue<SocketData> inputData;
    private final EventSource<SocketClosedEventListener> socketClosedEventListenerEventSource;

    private ClientLevel level;
    private ClientMetadata metadata;

    public Client(){
        inputData = new ConcurrentLinkedQueue<>();
        socketClosedEventListenerEventSource = new EventSource<>();
        level = ClientLevel.Unauthorized;
        metadata = new ClientMetadata();
    }

    public boolean isRunning(){
        return runThread;
    }

    public boolean Connect(String host, int port){
        boolean ok = true;
        try{
            socket = new Socket(host, port);
        } catch(IOException e){
            logger.log(Logger.Level.ERROR, "Error connecting: "+e.getMessage());
            ok = false;
        }

        if(ok) {
            WrapSocket(socket);
        }

        return ok;
    }

    public void WrapSocket(Socket socket){
        this.socket = socket;

        try {
            output = socket.getOutputStream();
            input = socket.getInputStream();
        } catch(IOException e){
            logger.log(Logger.Level.ERROR, "Error getting stream:" + e.getMessage());
        }

        socketThread = new Thread(this::SocketThread);
        socketThread.setName("Socket Thread");
        runThread = true;
        socketThread.start();
    }

    public Socket GetSocketInstance(){
        return this.socket;
    }

    public void Close(){
        if(runThread) {
            try {
                runThread = false;
                socket.shutdownInput();
                socket.shutdownOutput();
                socketThread.join();
                socket.close();
            } catch (IOException e) {
                logger.log(Logger.Level.ERROR, "Error closing socket: " + e.getMessage());
            } catch (InterruptedException e) {
                logger.log(Logger.Level.WARNING, "Client thread interrupted: " + e.getMessage());
            }

            inputData.clear();
        }
    }

    public void Send(IPacket packet){
        try {
            output.write(IPacket.getPacketData(packet));
        } catch (IOException e){
            logger.log(Logger.Level.ERROR, "Error writing to socket: "+e.getMessage());
            Close();
        }
    }

    private void SocketThread(){
        do {
            try {
                int len = 4+8+4+4;
                byte[] bts = new byte[len];
                int tmp = input.read(bts);
                if(tmp == len) {
                    ByteBuffer header = ByteBuffer.wrap(bts);
                    Instant clientTime = Instant.now();
                    int id = header.getInt(0); // get id
                    long seconds = header.getLong(4);
                    int nano = header.getInt(12);
                    int length = header.getInt(16);
                    byte[] data = new byte[length];
                    tmp = input.read(data);
                    if(tmp == length) {
                        // add data to queue
                        inputData.offer(new SocketData(id, Instant.ofEpochSecond(seconds, nano), clientTime, ByteBuffer.wrap(data)));
                    }
                }
            } catch(IOException e){
                logger.log(Logger.Level.ERROR, "Error reading socket: "+e.getMessage());
                runThread = false;
            }
        } while(runThread);

        socketClosedEventListenerEventSource.invoke(l->l.socketClosed(this));
    }

    public Tuple<Boolean, SocketData> GetData(){
        SocketData data = inputData.poll();
        if(data == null){
            return new Tuple<>(false, null);
        }

        return new Tuple<>(inputData.size() > 0, data); // more events available
    }

    public EventSource<SocketClosedEventListener> getSocketClosedEventListenerEventSource() {
        return socketClosedEventListenerEventSource;
    }

    public ClientLevel getLevel() {
        return level;
    }

    public void setLevel(ClientLevel level){
        this.level = level;
    }

    public void Promote(ClientLevel level){
        setLevel(level);
        // notify client of promotion
        Send(new PromotionPacket(Instant.now(), this.level));
    }

    public ClientMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ClientMetadata metadata) {
        this.metadata = metadata;
    }
}
