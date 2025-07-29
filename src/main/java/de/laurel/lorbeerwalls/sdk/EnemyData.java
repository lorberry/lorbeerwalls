package de.laurel.lorbeerwalls.sdk;

import java.awt.geom.Point2D;

/**
 * record representing enemy data on screen
 * @author lorberry+chatgpt
 * @param screenPos enemy position on screen
 * @param health enemy health
 */
public record EnemyData(Point2D.Float screenPos, int health) {
}
