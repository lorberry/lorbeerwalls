package de.laurel.lorbeerwalls.sdk;

import java.awt.geom.Point2D;
import java.util.Map;

public record EnemyData(Map<Integer, Point2D.Float> boneScreenPositions, int health) {
}
