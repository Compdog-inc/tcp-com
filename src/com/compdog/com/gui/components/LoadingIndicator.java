package com.compdog.com.gui.components;

import com.compdog.util.MathUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class LoadingIndicator extends JComponent {

    private Timer animTmr;
    private long prevTime;

    private int arcStart = 0;
    private int arcAngle = 0;

    public LoadingIndicator() {
        super();
        setPreferredSize(new Dimension(100, 100));
        setMaximumSize(new Dimension(100,100));
        setMinimumSize(new Dimension(100,100));
        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {
                animTmr.start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                animTmr.stop();
            }
        });

        animTmr = new Timer(16, e -> {
            long time = System.nanoTime();
            float delta = (float)((time - prevTime) / 1000) / 1000000.0f;
            if(delta > 0.3f)
                delta = 0.3f;
            prevTime = time;
            animate(delta);
            repaint();
        });
        animTmr.start();
    }

    private float animTime = 0.0f;

    private void animate(float delta)
    {
        animTime += delta;
        if(animTime > 1.5f)
            animTime = 0;
        float nT = animTime / 1.5f;

        float arcPos = MathUtils.Lerp(0,360,nT);
        float arcLength = MathUtils.Lerp(0,180,MathUtils.Sin(nT * MathUtils.PI));
        arcStart = (int)(arcPos - arcLength/2);
        arcAngle = (int)(arcLength);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.white);
        g2d.setStroke(new BasicStroke(10.0f));
        int w = getWidth();
        int h = getHeight();
        int s = Math.min(w,h);
        int ox = (w - s)/2;
        int oy = (h-s)/2;
        g2d.drawArc(5+ox, 5+oy, s-10, s-10, arcStart, arcAngle);
    }
}
