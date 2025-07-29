package de.laurel.lorbeerwalls.ui;

import de.laurel.lorbeerwalls.sdk.EnemyData;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * transparent window for drawing wallhack visuals
 * @author lorberry+chatgpt
 */
public class WallhackOverlay extends JWindow {

    /** list of enemies to draw */
    private List<EnemyData> enemyDataList = Collections.emptyList();

    /**
     * creates a fullscreen transparent overlay
     */
    public WallhackOverlay() {
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        setBounds(gd.getDefaultConfiguration().getBounds());
    }

    /**
     * updates enemy data and repaints
     * @param newData the new list of enemies
     */
    public void updateEnemyData(List<EnemyData> newData) {
        this.enemyDataList = newData;
        repaint();
    }

    /**
     * paints the enemies on screen
     * @param g the graphics context
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));

        for (EnemyData enemy : enemyDataList) {
            if (enemy.screenPos() != null) {
                String info = "enemy (HP: " + enemy.health() + ")";
                g2d.drawString(info, enemy.screenPos().x, enemy.screenPos().y);
            }
        }
    }
}
