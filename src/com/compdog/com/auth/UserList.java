package com.compdog.com.auth;

import com.compdog.util.BufferUtils;
import com.compdog.util.Logger;
import com.compdog.util.StringSerializer;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserList {
    private static final Logger logger = Logger.getLogger("UserList");

    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;

    static class Header{
        private final int majorVersion;
        private final int minorVersion;
        private final int entryCount;

        Header(int majorVersion, int minorVersion, int entryCount) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.entryCount = entryCount;
        }


        public int getMajorVersion() {
            return majorVersion;
        }

        public int getMinorVersion() {
            return minorVersion;
        }

        public int getEntryCount() {
            return entryCount;
        }
    }

    private final List<UserEntry> users;

    private UserList(@NotNull UserEntry[] users){
        this.users = new ArrayList<>(Arrays.asList(users));
    }

    public void AddUser(@NotNull UserEntry user){
        users.add(user);
    }

    @Nullable
    public UserEntry FindUser(@NotNull String username, boolean ignoreCase) {
        UserEntry entry = null;
        for (UserEntry user :
                users) {
            if (
                    (ignoreCase && user.getUsername().equalsIgnoreCase(username)) ||
                            (!ignoreCase && user.getUsername().equals(username))
            )
                entry = user;
        }

        return entry;
    }

    public boolean RemoveUser(@NotNull String username, boolean ignoreCase){
        UserEntry user = FindUser(username, ignoreCase);
        if(user == null)
            return false;
        users.remove(user);
        return true;
    }

    public static UserList Create(){
        return new UserList(new UserEntry[0]);
    }

    @NotNull
    public static UserList LoadOrCreate() {
        if (new File("./users.bin").exists()) {
            UserList list = Load();
            if (list == null)
                return Create();
            else
                return list;
        } else {
            return Create();
        }
    }

    // File format:
    // HEADER: [type][version][entry_count]
    // ENTRY: [username_length][username][password_length][password][salt_length][salt]
    @Nullable
    public static UserList Load(){
        try {
            File file = new File("./users.bin");
            FileInputStream stream = new FileInputStream(file);
            UserEntry[] users = null;
            if(stream.read() == 0xF3){
                byte[] db = new byte[(int) (file.length()-1)];
                int sz = stream.read(db);
                if(sz == db.length) {
                    ByteBuffer buf = ByteBuffer.wrap(db);
                    Header header = new Header(buf.getInt(0), buf.getInt(4), buf.getInt(8));
                    logger.log(Logger.Level.INFO, "Version: "+header.getMajorVersion()+"."+header.getMinorVersion());
                    if(header.getMajorVersion() == MAJOR_VERSION) {
                        users = new UserEntry[header.getEntryCount()];
                        int offset = 12;
                        for (int i = 0; i < header.getEntryCount(); i++) {
                            String name = StringSerializer.FromBytes(offset, buf);
                            offset += StringSerializer.GetSerializedLength(name);
                            int passwordLength = buf.getInt(offset);
                            offset+=4;
                            byte[] password = new byte[passwordLength];
                            BufferUtils.getBytes(buf, offset, password);
                            offset += passwordLength;
                            int saltLength = buf.getInt(offset);
                            offset+=4;
                            byte[] salt = new byte[saltLength];
                            BufferUtils.getBytes(buf, offset, salt);
                            offset += saltLength;
                            users[i] = new UserEntry(name, password, salt);
                        }
                    } else {
                        logger.log(Logger.Level.WARNING, "Format not supported!");
                    }
                }
            }
            stream.close();
            if(users != null){
                return new UserList(users);
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void Save() {
        try {
            File file = new File("./users.bin");
            FileOutputStream stream = new FileOutputStream(file);

            stream.write(0xF3); // type id
            Header header = new Header(MAJOR_VERSION, MINOR_VERSION, users.size());
            ByteBuffer buf = ByteBuffer.allocate(4+4+4);
            buf.putInt(0, header.getMajorVersion());
            buf.putInt(4, header.getMinorVersion());
            buf.putInt(8, header.getEntryCount());
            stream.write(buf.array());

            for (int i = 0; i < header.getEntryCount(); i++) {
                UserEntry user = users.get(i);
                buf = ByteBuffer.allocate(
                        StringSerializer.GetSerializedLength(user.getUsername()) +
                                4 + user.getPassword().length +
                                4 + user.getSalt().length
                );
                BufferUtils.putBytes(buf, 0, StringSerializer.ToBytes(user.getUsername()));
                int index = StringSerializer.GetSerializedLength(user.getUsername());
                buf.putInt(index, user.getPassword().length);
                index+=4;
                BufferUtils.putBytes(buf, index, user.getPassword());
                index += user.getPassword().length;
                buf.putInt(index, user.getSalt().length);
                index+=4;
                BufferUtils.putBytes(buf, index, user.getSalt());
                stream.write(buf.array());
            }

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
