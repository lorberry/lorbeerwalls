package de.laurel.lorbeerwalls.utils;

import de.laurel.lorbeerwalls.structs.Vector3;
import java.awt.geom.Point2D;

/**
 * utility class for mathematical calculations
 * @author lorberry+chatgpt
 */
public class MathUtils {

    /**
     * converts 3d world coordinates to 2d screen coordinates
     * @param worldPos the 3d world position
     * @param viewMatrix the game's view matrix
     * @param screenWidth width of the screen
     * @param screenHeight height of the screen
     * @return the 2d screen position or null
     */
    public static Point2D.Float worldToScreen(Vector3 worldPos, float[] viewMatrix, int screenWidth, int screenHeight) {
        float screenW = (viewMatrix[12] * worldPos.x) + (viewMatrix[13] * worldPos.y) + (viewMatrix[14] * worldPos.z) + viewMatrix[15];
        if (screenW < 0.1f) return null;

        float screenX = (viewMatrix[0] * worldPos.x) + (viewMatrix[1] * worldPos.y) + (viewMatrix[2] * worldPos.z) + viewMatrix[3];
        float screenY = (viewMatrix[4] * worldPos.x) + (viewMatrix[5] * worldPos.y) + (viewMatrix[6] * worldPos.z) + viewMatrix[7];

        float invertedW = 1.0f / screenW;
        screenX *= invertedW;
        screenY *= invertedW;

        float x = (screenWidth / 2.0f) + (screenX * screenWidth) / 2.0f;
        float y = (screenHeight / 2.0f) - (screenY * screenHeight) / 2.0f;

        return new Point2D.Float(x, y);
    }
}
