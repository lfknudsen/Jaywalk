package com.falkknudsen.jaywalk;

import com.falkknudsen.jaywalk.contracts.IDrawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;

/** Rectangle structure.<br>
 Origin/min fields/functions are guaranteed to be <em>smaller</em>
 or equal to end/max fields/functions. */
public class Rectangle implements Serializable, IDrawable {
    private float originX = 0f,
            originY = 0f,
            endX    = 0f,
            endY    = 0f;

    public float minLat() { return originY; }
    public float minLon() { return originX; }
    public float maxLat() { return endY; }
    public float maxLon() { return endX; }

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.originY = Math.min(minY, maxY);
        this.originX = Math.min(minX, maxX);
        this.endY    = Math.max(minY, maxY);
        this.endX    = Math.max(minX, maxX);
    }

    public Rectangle() {}
    public Rectangle(float originX, float originY, float endX, float endY) {
        setBounds(originX, originY, endX, endY);
    }

    @Override
    public String toString() {
        return toString(originX, originY, endX, endY);
    }

    public static String toString(float minX, float minY, float maxX, float maxY) {
        return "Rectangle (x, y): (" + minX + ", " + minY + "), (" + maxX + ", " + maxY + ")";
    }

    public static String toStringGeo(float minX, float minY, float maxX, float maxY) {
        return "Rectangle (lat, lon): (" + minY + ", " + minX + "), (" + maxY + ", " + maxX + ")";
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Rectangle rect) {
            return originX == rect.originX
                    && originY == rect.originY
                    && endX == rect.endX
                    && endY == rect.endY;
        }
        return false;
    }

    @Override public int hashCode() {
        int bits = 7;
        bits = 15 * bits + Float.floatToIntBits(originX);
        bits = 15 * bits + Float.floatToIntBits(originY);
        bits = 15 * bits + Float.floatToIntBits(endX);
        bits = 15 * bits + Float.floatToIntBits(endY);
        return bits ^ (bits >> 16);
    }

    public void draw(GraphicsContext gc, Color colour) {
        draw(gc, colour, originX, originY, endX, endY);
    }

    public void draw(GraphicsContext gc) {
        draw(gc, Color.BLACK, originX, originY, endX, endY);
    }

    public static void draw(GraphicsContext gc,
                            Point origin, Point end) {
        draw(gc, Color.BLACK, origin.x(), origin.y(), end.x(), end.y());
    }

    public static void draw(GraphicsContext gc, Color colour,
                            Point origin, Point end) {
        draw(gc, colour, origin.x(), origin.y(), end.x(), end.y());
    }

    public static void draw(GraphicsContext gc, Color colour, float[] coord) {
        draw(gc, colour, coord[0], coord[1], coord[2], coord[3]);
    }

    public static void draw(GraphicsContext gc, Color colour, Rectangle bb) {
        draw(gc, colour, bb.minLon(), bb.minLat(), bb.maxLon(), bb.maxLat());
    }

    public static void draw(GraphicsContext gc, Color colour,
                            float minX, float minY, float maxX, float maxY) {
        gc.setStroke(colour);
        gc.moveTo(minX, minY);
        gc.lineTo(minX, maxY);
        gc.lineTo(maxX, maxY);
        gc.lineTo(maxX, minY);
        gc.lineTo(minX, minY);
        gc.stroke();
    }
}
