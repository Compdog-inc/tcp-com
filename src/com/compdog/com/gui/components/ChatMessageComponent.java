package com.compdog.com.gui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatMessageComponent extends JPanel {
    private final JEditorPane pane;

    public ChatMessageComponent(String text){
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(50,50,50,50));
        setBackground(Color.red);
        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(BOTTOM_ALIGNMENT);
        setMinimumSize(new Dimension(100,50));
        setPreferredSize(getMinimumSize());
        setMaximumSize(new Dimension(300,100));
        pane = new JEditorPane("text/rtf", text);
        pane.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        pane.setBackground(new Color(74, 74, 74));
        pane.setForeground(Color.WHITE);
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx=1;
        cons.weighty=1;
        cons.gridx=0;
        cons.gridy=0;
        add(pane, cons);
    }
}
