package com.compdog.com;

import com.compdog.com.auth.Authenticator;
import com.compdog.com.auth.UserList;
import com.compdog.tcp.Client;
import com.compdog.tcp.ClientLevel;
import com.compdog.tcp.Server;
import com.compdog.tcp.SocketData;
import com.compdog.util.Logger;

import java.time.Duration;
import java.time.Instant;

public class Test {
    private static final Logger logger = Logger.getLogger("Server Entry");

    private static UserList currentUserList;

    enum ArgType{
        None,
        Add,
        Remove,
        Check
    }

    private static boolean isPort(String str){
        if(str.startsWith("-"))
            return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: [port] - Start server\n-a [username] [password] - Add user\n-r [username] - Remove user\n-c [username] [password] - Authenticate (exit code - bitmask: 0-OK, 1-FAIL)\nNote: only returns first 32 authentications in exit code");
            return;
        }

        logger.log(Logger.Level.INFO, "Loading user list");
        currentUserList = UserList.LoadOrCreate();

        int exitCode = 0;

        if (isPort(args[0])) {
            logger.log(Logger.Level.INFO, "Creating server instance");

            Server server = new Server();
            long currentPing = 0;

            server.getClientConnectedEventListenerEventSource().addEventListener(client -> {
                // Client ready to authenticate
                client.Promote(ClientLevel.Unauthorized);
                logger.log(Logger.Level.INFO, "Client connected [" + client.getMetadata().getId() + ":" + client.getMetadata().getUsername() + "]");
                return false;
            });

            server.Listen(6868);

            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (Client client : server.getClients()) {
                        Pinger.SinglePing(client);
                    }
                }
            }).start();

            while (true) {
                for (Client client : server.getClients()) {
                    if (client.getLevel() == ClientLevel.Dead) {
                        // dead clients shouldn't exist
                        logger.log(Logger.Level.INFO, "Dead client [" + client.getMetadata().getId() + ":" + client.getMetadata().getUsername() + "]");
                        client.Close();
                    }

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

                            // broadcast message to clients
                            MessagePacket msg = new MessagePacket(Instant.now(), packet.getAuthor(), client.getMetadata().getId(), packet.getMessage());
                            for (Client c : server.getClients()) {
                                c.Send(msg);
                            }
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
                                logger.log(Logger.Level.INFO, "Client authorized [" + client.getMetadata().getId() + ":" + client.getMetadata().getUsername() + "]");
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
                            logger.log(Logger.Level.WARNING, "[" + client.getMetadata().getId() + ":" + client.getMetadata().getUsername() + "]" + " tried to promote server to " + packet.getLevel().toString());
                        }
                        break;
                    }
                }
            }

            //server.Close();
        } else {
            // user utils
            ArgType arg = ArgType.None;
            int argDepth = 0;
            String username="";
            String password="";
            int authCount = 0;
            for (String s : args) {
                if (arg == ArgType.None) {
                    if (s.startsWith("-")) {
                        switch (s.substring(1)) {
                            case "a":
                                arg = ArgType.Add;
                                break;
                            case "r":
                                arg = ArgType.Remove;
                                break;
                            case "c":
                                arg = ArgType.Check;
                                break;
                            default:
                                arg = ArgType.None;
                                break;
                        }
                        argDepth = 0;
                    }
                } else {
                    argDepth++;
                    switch (arg) {
                        case Add:
                            if (argDepth == 1)
                                username = s;
                            else if (argDepth == 2) {
                                password = s;
                                logger.log(Logger.Level.INFO, "Adding user " + username);
                                getCurrentUserList().AddUser(Authenticator.CreateUser(username, password));
                                arg = ArgType.None;
                                argDepth = 0;
                            }
                            break;
                        case Check:
                            if (argDepth == 1)
                                username = s;
                            else if (argDepth == 2) {
                                password = s;
                                logger.log(Logger.Level.INFO, "Authenticating user " + username);
                                boolean success = Authenticator.Authenticate(username, password);
                                if (success)
                                    logger.log(Logger.Level.INFO, "Success!");
                                else
                                    logger.log(Logger.Level.INFO, "Auth failed");
                                if (success) {
                                    exitCode &= ~(1 << authCount++);
                                } else {
                                    exitCode |= (1 << authCount++);
                                }
                                arg = ArgType.None;
                                argDepth = 0;
                            }
                            break;
                        case Remove:
                            if (argDepth == 1) {
                                username = s;
                                logger.log(Logger.Level.INFO, "Removing user " + username);
                                boolean exists = getCurrentUserList().RemoveUser(username, false);
                                if (!exists) {
                                    logger.log(Logger.Level.ERROR, "User " + username + " does not exist!");
                                }
                                arg = ArgType.None;
                                argDepth = 0;
                            }
                            break;
                    }
                }
            }

            logger.log(Logger.Level.INFO, "Saving changes to user list");
            getCurrentUserList().Save();
        }

        System.exit(exitCode);
    }

    public static UserList getCurrentUserList() {
        return currentUserList;
    }
}
