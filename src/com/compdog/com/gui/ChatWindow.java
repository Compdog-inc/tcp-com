package com.compdog.com.gui;

import com.compdog.com.AuthPacket;
import com.compdog.com.MessagePacket;
import com.compdog.com.gui.components.ChatMessageComponent;
import com.compdog.com.gui.components.LoadingIndicator;
import com.compdog.tcp.Client;
import com.compdog.tcp.ClientLevel;
import com.compdog.tcp.event.ChatMessageEventListener;
import com.compdog.tcp.event.ClientPromotedEventListener;
import com.compdog.util.EventSource;
import com.compdog.util.Task;
import sun.awt.AWTAccessor;
import sun.awt.windows.WComponentPeer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.time.Instant;
import java.util.prefs.Preferences;

public class ChatWindow extends JFrame {
    private final Client client;
    private final String host;
    private final int port;

    private final EventSource<ClientPromotedEventListener> clientPromotedEventListenerEventSource;
    private final EventSource<ChatMessageEventListener> chatMessageEventListenerEventSource;

    private final Preferences prefs = Preferences.userNodeForPackage(ChatWindow.class);
    private final String AUTOLOGIN_PREF_NAME = "auto_login";
    private final String USERNAME_PREF_NAME = "username";
    private final String PASSWORD_PREF_NAME = "password";

    private JPanel connectingPanel;
    private JPanel loginPanel;
    private JPanel chatPanel;

    private LoadingIndicator loadingInd;
    private Component loadingBox;
    private Component loadingBox2;
    private JPanel loadingCont;
    private JLabel loadingText;
    private JButton connectRetryBtn;

    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JCheckBox loginAuto;

    private JPanel chatMsgsPanel;
    private JEditorPane chatInput;

    private boolean clientConnected;

    public EventSource<ClientPromotedEventListener> getClientPromotedEventListenerEventSource() {
        return clientPromotedEventListenerEventSource;
    }

    public EventSource<ChatMessageEventListener> getChatMessageEventListenerEventSource() {
        return chatMessageEventListenerEventSource;
    }

    private enum UIPanel {
        Connecting,
        Login,
        Chat
    }

    public ChatWindow(Client client, String host, int port){
        this.client = client;
        this.host = host;
        this.port = port;

        clientPromotedEventListenerEventSource = new EventSource<>();
        chatMessageEventListenerEventSource = new EventSource<>();
        clientPromotedEventListenerEventSource.addEventListener((e)->{
            System.out.println("Promoted to " + client.getLevel());
            if(client.getLevel() == ClientLevel.Dead) {
                showPanel(UIPanel.Connecting);
                setTitle("TCP Chat - Connecting");
                Task.Start(this::InitializeClient);
            } else if(client.getLevel() == ClientLevel.Authorized){
                showPanel(UIPanel.Chat);
                setTitle("TCP Chat - ["+host+":"+port+"]");
            }
            return true;
        });

        chatMessageEventListenerEventSource.addEventListener((e)->{
            System.out.println("["+e.getAuthor()+"]: "+e.getMessage());
            chatMsgsPanel.add(new ChatMessageComponent(e.getMessage()));
            chatMsgsPanel.revalidate();
            return true;
        });

        setName("TCP Chat");
        setTitle("TCP Chat");
        setSize(800, 500);
        setMinimumSize(new Dimension(620,440));
        setLocationByPlatform(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setDefaultLookAndFeelDecorated(true);
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                AWTAccessor.ComponentAccessor acc = AWTAccessor.getComponentAccessor();
                WComponentPeer peer = acc.getPeer(e.getWindow());
                long hwnd = peer.getHWnd(); // finally!!!
                //DWMApi.DwmSetWindowAttribute(hwnd, DwmWindowAttribute.UseImmersiveDarkMode, 1, 1);

                Task.Start(() -> InitializeClient());
            }

            @Override
            public void windowClosing(WindowEvent e) {
                Task.Start(client::Close);
                loginPanel.setVisible(false);
                connectingPanel.setVisible(false);
                chatPanel.setVisible(false);
                clientConnected = true;
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        InitializeComponents();
    }

    private void InitializeComponents(){
        JPanel pagerPanel = new JPanel(new PageLayout());
        pagerPanel.setBackground(new Color(47, 48, 51));

        connectingPanel = new JPanel(new GridBagLayout());
        loginPanel = new JPanel(new GridBagLayout());
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        connectingPanel.setBackground(pagerPanel.getBackground());
        loginPanel.setBackground(pagerPanel.getBackground());
        chatPanel.setBackground(pagerPanel.getBackground());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        loadingCont = new JPanel();
        loadingCont.setBackground(pagerPanel.getBackground());
        loadingCont.setLayout(new BoxLayout(loadingCont, BoxLayout.Y_AXIS));
        connectingPanel.add(loadingCont, gbc);

        loadingInd = new LoadingIndicator();
        loadingInd.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingCont.add(loadingInd);
        loadingCont.add(loadingBox = Box.createVerticalStrut(20));
        loadingText = new JLabel();
        loadingText.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        loadingText.setForeground(Color.white);
        loadingCont.add(loadingText);
        loadingText.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingCont.add(loadingBox2 = Box.createVerticalStrut(20));
        loadingBox2.setVisible(false);
        connectRetryBtn = new JButton("Retry");
        connectRetryBtn.setBackground(new Color(74, 74, 74));
        connectRetryBtn.setForeground(Color.WHITE);
        connectRetryBtn.setFocusPainted(false);
        connectRetryBtn.setBorderPainted(false);
        connectRetryBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        connectRetryBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingCont.add(connectRetryBtn);
        connectRetryBtn.setVisible(false);
        connectRetryBtn.addActionListener((e)-> Task.Start(this::InitializeClient));

        JPanel loginCont = new JPanel();
        loginCont.setBackground(pagerPanel.getBackground());
        loginCont.setLayout(new BoxLayout(loginCont, BoxLayout.Y_AXIS));
        loginPanel.add(loginCont, gbc);

        JPanel usernameCont = new JPanel();
        usernameCont.setBackground(pagerPanel.getBackground());
        usernameCont.setLayout(new BoxLayout(usernameCont, BoxLayout.X_AXIS));
        JLabel usernameLab = new JLabel("Username:");
        usernameLab.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        usernameLab.setAlignmentY(Component.CENTER_ALIGNMENT);
        usernameLab.setForeground(Color.white);
        usernameCont.add(usernameLab);
        usernameCont.add(Box.createHorizontalStrut(10));
        usernameInput = new JTextField();
        usernameInput.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        usernameInput.setAlignmentY(Component.CENTER_ALIGNMENT);
        usernameInput.setBackground(new Color(74, 74, 74));
        usernameInput.setForeground(Color.WHITE);
        usernameInput.setPreferredSize(new Dimension(200,usernameInput.getPreferredSize().height));
        usernameCont.add(usernameInput);
        usernameCont.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCont.add(usernameCont);

        loginCont.add(Box.createVerticalStrut(20));

        JPanel passwordCont = new JPanel();
        passwordCont.setBackground(pagerPanel.getBackground());
        passwordCont.setLayout(new BoxLayout(passwordCont, BoxLayout.X_AXIS));
        JLabel passwordLab = new JLabel("Password:");
        passwordLab.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        passwordLab.setForeground(Color.white);
        passwordLab.setAlignmentY(Component.CENTER_ALIGNMENT);
        passwordCont.add(passwordLab);
        passwordCont.add(Box.createHorizontalStrut(10));
        passwordInput = new JPasswordField();
        passwordInput.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        passwordInput.setPreferredSize(new Dimension(200,passwordInput.getPreferredSize().height));
        passwordInput.setAlignmentY(Component.CENTER_ALIGNMENT);
        passwordInput.setBackground(new Color(74, 74, 74));
        passwordInput.setForeground(Color.WHITE);
        passwordCont.add(passwordInput);
        passwordCont.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCont.add(passwordCont);

        loginCont.add(Box.createVerticalStrut(20));

        loginAuto = new JCheckBox("Remember me", prefs.getBoolean(AUTOLOGIN_PREF_NAME, false));
        loginAuto.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginAuto.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        loginAuto.setBackground(pagerPanel.getBackground());
        loginAuto.setForeground(Color.WHITE);
        loginCont.add(loginAuto);

        loginCont.add(Box.createVerticalStrut(20));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(74, 74, 74));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener((e)->{
            if(loginAuto.isSelected()){
                prefs.putBoolean(AUTOLOGIN_PREF_NAME, true);
                prefs.put(USERNAME_PREF_NAME, usernameInput.getText());
                prefs.put(PASSWORD_PREF_NAME, new String(passwordInput.getPassword()));
            }
            Task.Start(this::Authenticate);
        });
        loginCont.add(loginBtn);

        chatMsgsPanel = new JPanel();
        chatMsgsPanel.setBackground(pagerPanel.getBackground());
        chatMsgsPanel.setLayout(new BoxLayout(chatMsgsPanel, BoxLayout.Y_AXIS));
        chatPanel.add(new JScrollPane(chatMsgsPanel), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new GridBagLayout());
        bottomBar.setBackground(pagerPanel.getBackground());

        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;
        cons.insets.left = 10;
        cons.insets.bottom = 10;
        cons.insets.top = 10;

        chatInput = new JEditorPane("text/rtf", "");
        chatInput.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        chatInput.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        chatInput.setBackground(new Color(74, 74, 74));
        chatInput.setForeground(Color.WHITE);
        JScrollPane inputScroll = new JScrollPane(chatInput);
        inputScroll.setPreferredSize(new Dimension(10,100));
        bottomBar.add(inputScroll, cons);

        GridBagConstraints cons2 = new GridBagConstraints();
        cons2.anchor = GridBagConstraints.SOUTH;
        cons2.insets.left = 10;
        cons2.insets.bottom = 10;
        cons2.insets.right = 10;
        cons2.insets.top = 10;

        JButton sendBtn = new JButton("Send");
        sendBtn.setBackground(new Color(74, 74, 74));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorderPainted(false);
        sendBtn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sendBtn.addActionListener((e)->Task.Start(this::SendMessage));
        bottomBar.add(sendBtn, cons2);

        chatPanel.add(bottomBar, BorderLayout.SOUTH);

        showPanel(UIPanel.Connecting);

        pagerPanel.add(connectingPanel);
        pagerPanel.add(loginPanel);
        pagerPanel.add(chatPanel);
        add(pagerPanel);
    }

    private void showPanel(UIPanel p){
        switch (p) {
            case Connecting -> {
                connectingPanel.setVisible(true);
                loginPanel.setVisible(false);
                chatPanel.setVisible(false);
            }
            case Login -> {
                loginPanel.setVisible(true);
                connectingPanel.setVisible(false);
                chatPanel.setVisible(false);
            }
            case Chat -> {
                chatPanel.setVisible(true);
                connectingPanel.setVisible(false);
                loginPanel.setVisible(false);
            }
        }
    }

    public boolean isClientConnected(){
        return clientConnected;
    }

    private void InitializeClient(){
        loadingText.setText("Connecting to server");
        loadingInd.setVisible(true);
        loadingBox.setVisible(true);
        loadingBox2.setVisible(false);
        connectRetryBtn.setVisible(false);
        loadingText.setForeground(Color.white);
        loadingCont.revalidate();

        setTitle("TCP Chat - Connecting");

        boolean ok = client.Connect(host, port);
        if(ok) {
            clientConnected = true;
            loadingText.setText("Connected");

            setTitle("TCP Chat - Login [" + host + ":" + port + "]");

            boolean autoLogin = prefs.getBoolean(AUTOLOGIN_PREF_NAME, false);
            if (autoLogin) {
                Authenticate(prefs.get(USERNAME_PREF_NAME, ""), prefs.get(PASSWORD_PREF_NAME, ""));
            } else {
                showPanel(UIPanel.Login);
            }
        }
        else {
            loadingInd.setVisible(false);
            loadingBox.setVisible(false);
            loadingBox2.setVisible(true);
            connectRetryBtn.setVisible(true);
            loadingText.setForeground(new Color(240, 62, 62));
            loadingText.setText("Couldn't connect to " + host);
            loadingCont.revalidate();
        }
    }

    private void Authenticate(){
        Authenticate(usernameInput.getText(), new String(passwordInput.getPassword()));
    }

    private void Authenticate(String username, String password) {
        client.getMetadata().setUsername(username);
        client.Send(new AuthPacket(Instant.now(), username, password));
    }

    private void SendMessage() {
        RTFEditorKit rtf = (RTFEditorKit)chatInput.getEditorKitForContentType("text/rtf");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            rtf.write(os, chatInput.getDocument(), 0, chatInput.getDocument().getLength());
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }
        client.Send(new MessagePacket(Instant.now(), client.getMetadata().getUsername(), -1, os.toString()));
    }
}
