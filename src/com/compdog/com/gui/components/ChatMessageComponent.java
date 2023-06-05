package com.compdog.com.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ChatMessageComponent extends JPanel {
    private final JEditorPane pane;
    private final JTextArea authorField;

    public ChatMessageComponent(String author, String text){
        setLayout(new GridBagLayout());
        setBackground(Color.red);
        setMinimumSize(new Dimension(100,100));
        setPreferredSize(getMinimumSize());
        setMaximumSize(new Dimension(300,200));

        authorField = new JTextArea(author);
        authorField.setEditable(false);
        authorField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        authorField.setBackground(new Color(74, 74, 74));
        authorField.setForeground(Color.WHITE);

        GridBagConstraints cons2 = new GridBagConstraints();
        cons2.fill = GridBagConstraints.HORIZONTAL;
        cons2.weightx=1;
        cons2.gridx=0;
        cons2.gridy=0;
        add(authorField, cons2);

        pane = new JEditorPane("text/rtf", "");
        pane.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        pane.setBackground(new Color(74, 74, 74));
        pane.setForeground(Color.WHITE);
        pane.setEditable(false);
        try {
            ((RTFEditorKit)pane.getEditorKit()).read(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), pane.getDocument(), 0);
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx=1;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=1;
        add(pane, cons);
    }
}
