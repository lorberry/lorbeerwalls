package de.laurel.lorbeerwalls.ui;

import de.laurel.lorbeerwalls.hacks.WallhackWorker;
import de.laurel.lorbeerwalls.sdk.EnemyData;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WallhackOverlay extends JWindow {

    private List<EnemyData> enemyDataList = Collections.emptyList();

    public WallhackOverlay() {
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        setBounds(gd.getDefaultConfiguration().getBounds());
    }

    public void updateEnemyData(List<EnemyData> newData) {
        this.enemyDataList = newData;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Color.RED);

        for (EnemyData enemy : enemyDataList) {
            Map<Integer, Point2D.Float> bones = enemy.boneScreenPositions();
            for (int[] connection : WallhackWorker.BONE_CONNECTIONS) {
                Point2D.Float bone1 = bones.get(connection[0]);
                Point2D.Float bone2 = bones.get(connection[1]);

                if (bone1 != null && bone2 != null) {
                    g2d.drawLine((int) bone1.x, (int) bone1.y, (int) bone2.x, (int) bone2.y);
                }
            }
        }
    }
}
