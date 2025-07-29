package de.laurel.lorbeerwalls.structs;

/**
 * represents a 3d vector
 * @author lorberry+chatgpt
 */
public class Vector3 {
    /** x coordinate */
    public float x;
    /** y coordinate */
    public float y;
    /** z coordinate */
    public float z;

    /**
     * creates a new 3d vector
     * @param x x value
     * @param y y value
     * @param z z value
     */
    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
