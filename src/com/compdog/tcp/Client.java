package com.compdog.tcp;

import com.compdog.tcp.event.SocketClosedEventListener;
import com.compdog.util.EventSource;
import com.compdog.util.Tuple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private static final System.Logger logger = System.getLogger("Client");

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Thread socketThread;
    private volatile boolean runThread = false;
    private final ConcurrentLinkedQueue<SocketData> inputData;
    private final EventSource<SocketClosedEventListener> socketClosedEventListenerEventSource;

    public Client(){
        inputData = new ConcurrentLinkedQueue<>();
        socketClosedEventListenerEventSource = new EventSource<>();
    }

    public void Connect(String host, int port){
        try{
            socket = new Socket(host, port);
        } catch(IOException e){
            logger.log(System.Logger.Level.ERROR, "Error connecting: "+e.getMessage());
        }

        WrapSocket(socket);
    }

    public void WrapSocket(Socket socket){
        this.socket = socket;

        try {
            output = socket.getOutputStream();
            input = socket.getInputStream();
        } catch(IOException e){
            logger.log(System.Logger.Level.ERROR, "Error getting stream:" + e.getMessage());
        }

        socketThread = new Thread(this::SocketThread);
        socketThread.setName("Socket Thread");
        runThread = true;
        socketThread.start();
    }

    public void Close(){
        try {
            runThread = false;
            socket.shutdownInput();
            socket.shutdownOutput();
            socketThread.join();
            socket.close();
        } catch (IOException e){
            logger.log(System.Logger.Level.ERROR, "Error closing socket: "+e.getMessage());
        } catch (InterruptedException e){
            logger.log(System.Logger.Level.WARNING, "Client thread interrupted: "+e.getMessage());
        }

        inputData.clear();
    }

    public void Send(IPacket packet){
        try {
            output.write(IPacket.getPacketData(packet));
        } catch (IOException e){
            logger.log(System.Logger.Level.ERROR, "Error writing to socket: "+e.getMessage());
            Close();
        }
    }

    private void SocketThread(){
        do {
            try {
                int len = 4+8+4+4;
                byte[] bts = input.readNBytes(len);
                if(bts.length == len) {
                    ByteBuffer header = ByteBuffer.wrap(bts);
                    Instant clientTime = Instant.now();
                    int id = header.getInt(0); // get id
                    long seconds = header.getLong(4);
                    int nano = header.getInt(12);
                    int length = header.getInt(16);
                    byte[] data = input.readNBytes(length);
                    if(data.length == length) {
                        // add data to queue
                        inputData.offer(new SocketData(id, Instant.ofEpochSecond(seconds, nano), clientTime, ByteBuffer.wrap(data)));
                    }
                }
            } catch(IOException e){
                logger.log(System.Logger.Level.ERROR, "Error reading socket: "+e.getMessage());
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
}
