package com.compdog.com.gui;

import java.awt.*;

public class ChatListLayout implements LayoutManager {
    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Component[] components = parent.getComponents();

        int heightCounter = 0;
        int maxWidth = 0;
        for(int i=0;i<components.length;i++) {
            var c = components[components.length-i-1];
            heightCounter += c.getPreferredSize().height+10;
            if(c.getPreferredSize().width>maxWidth)
                maxWidth = c.getPreferredSize().width;
        }

        return new Dimension(maxWidth,heightCounter);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Component[] components = parent.getComponents();

        int heightCounter = 0;
        int maxWidth = 0;
        for(int i=0;i<components.length;i++) {
            var c = components[components.length-i-1];
            heightCounter += c.getPreferredSize().height+10;
            if(c.getPreferredSize().width>maxWidth)
                maxWidth = c.getPreferredSize().width;
        }

        return new Dimension(maxWidth,heightCounter);
    }

    @Override
    public void layoutContainer(Container parent) {
        Component[] components = parent.getComponents();

        int heightCounter = 0;
        for(int i=0;i<components.length;i++) {
            var c = components[components.length-i-1];
            c.setBounds(new Rectangle(0, parent.getBounds().height - heightCounter - c.getPreferredSize().height, parent.getBounds().width, c.getPreferredSize().height));
            heightCounter += c.getPreferredSize().height+10;
        }
    }
}
