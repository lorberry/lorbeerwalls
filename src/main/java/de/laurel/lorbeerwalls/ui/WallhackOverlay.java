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
    private final MainGUI mainGUI;

    public WallhackOverlay(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
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

        for (EnemyData enemy : enemyDataList) {
            Map<Integer, Point2D.Float> bones = enemy.boneScreenPositions();

            if (mainGUI.isSkeletonEspEnabled()) {
                g2d.setColor(Color.RED);
                for (int[] connection : WallhackWorker.BONE_CONNECTIONS) {
                    Point2D.Float bone1 = bones.get(connection[0]);
                    Point2D.Float bone2 = bones.get(connection[1]);

                    if (bone1 != null && bone2 != null) {
                        g2d.drawLine((int) bone1.x, (int) bone1.y, (int) bone2.x, (int) bone2.y);
                    }
                }
            }

            if (mainGUI.isHealthBarEnabled()) {
                drawHealthBar(g2d, enemy);
            }
        }
    }

    private void drawHealthBar(Graphics2D g2d, EnemyData enemy) {
        Map<Integer, Point2D.Float> bones = enemy.boneScreenPositions();
        Point2D.Float head = bones.get(6);
        Point2D.Float foot = bones.get(27);

        if (head == null || foot == null) return;

        float minX = Float.MAX_VALUE;
        for (Point2D.Float bonePos : bones.values()) {
            if (bonePos.x < minX) {
                minX = bonePos.x;
            }
        }

        float barHeight = Math.abs(head.y - foot.y);
        float barX = minX - 10;
        float barY = head.y;

        int health = enemy.health();
        float healthPercentage = health / 100f;
        float healthBarHeight = barHeight * healthPercentage;

        g2d.setColor(Color.BLACK);
        g2d.fillRect((int)barX - 1, (int)barY - 1, 7, (int)barHeight + 2);

        float red = (1 - healthPercentage) * 255;
        float green = healthPercentage * 255;
        g2d.setColor(new Color((int)red, (int)green, 0));
        g2d.fillRect((int)barX, (int)(barY + (barHeight - healthBarHeight)), 5, (int)healthBarHeight);
    }
}
