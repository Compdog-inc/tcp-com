package com.compdog.com.gui.components;

import com.compdog.com.gui.components.event.ChatTextBoxSubmitEventListener;
import com.compdog.util.EventSource;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ChatTextBox extends JEditorPane {
    private final EventSource<ChatTextBoxSubmitEventListener> chatTextBoxSubmitEventListenerEventSource;

    public ChatTextBox() {
        super("text/rtf", "");
        chatTextBoxSubmitEventListenerEventSource = new EventSource<>();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    getChatTextBoxSubmitEventListenerEventSource().invoke(ChatTextBoxSubmitEventListener::onSubmit);
                }
            }
        });
    }

    public EventSource<ChatTextBoxSubmitEventListener> getChatTextBoxSubmitEventListenerEventSource() {
        return chatTextBoxSubmitEventListenerEventSource;
    }
}
