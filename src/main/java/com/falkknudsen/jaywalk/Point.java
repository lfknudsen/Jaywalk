package com.falkknudsen.jaywalk;

import java.util.Objects;

public record Point(float x, float y) {
    public static Point of(Node node) {
        if (node == null) return null;
        return new Point(projectLon(node.lon()), projectLat(node.lat()));
    }

    public float lat() {
        return toLat(y);
    }

    public float lon() {
        return toLon(x);
    }

    /** Projects a longitude value onto the map. */
    public static float projectLon(float lon) { return lon * 0.56f; }
    /** Projects a latitude value onto the map. */
    public static float projectLat(float lat) { return -lat; }
    /** Turns a map-projected x-value back into the original longitude. */
    public static float toLon(float x) { return x / 0.56f; }
    /** Turns a map-projected y-value back into the original latitude. */
    public static float toLat(float y) { return -y; }

    public static Point fromLonLat(double lon, double lat) {
        return new Point(
                projectLon((float) lon),
                projectLat((float) lat));
    }

    public Point subtract(Point pt) {
        return new Point(x - pt.x, y - pt.y);
    }

    public Point add(Point pt) {
        return new Point(x + pt.x, y + pt.y);
    }

    /** Reverse the projection process, creating a {@link Node} based on
     the given x and y values. */
    public static Node inverse(float x, float y) {
        return new Node(toLat(y), toLon(x));
    }

    public Node inverse() {
        return new Node(toLat(y), toLon(x));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof Point(float x1, float y1)) {
            // This is how Java compares floats internally in e.g. Float.equals() and Float.compare().
            return Float.floatToIntBits(this.x) == Float.floatToIntBits(x1) &&
                    Float.floatToIntBits(this.y) == Float.floatToIntBits(y1);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
