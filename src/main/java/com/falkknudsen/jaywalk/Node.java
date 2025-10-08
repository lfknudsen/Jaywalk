package com.falkknudsen.jaywalk;

import com.falkknudsen.jaywalk.util.Maths;

import java.io.Serializable;
import java.util.Objects;

/** Represents a Node as understood in the OpenStreetMap data, i.e. a coordinate
 on the Earth's surface as defined by latitude and longitude. */
public class Node implements Serializable {
    private final float lat;
    private final float lon;

    public Node(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /** Create a new Node by projecting the given {@code lat} and {@code lon}
     onto the map and then back again.<br>
     This is helpful since the transformation can in rare
     cases cause the number to change slightly, and fundamentally we tend to be
     working on {@linkplain Node}s that have been through this transformation. */
    public static Node of(float lat, float lon) {
        return new Node(Point.toLat(Point.projectLat(lat)),
                Point.toLon(Point.projectLon(lon)));
    }

    public static Node fromXY(float x, float y) {
        return new Node(Point.toLat(y), Point.toLon(x));
    }

    public Node(Node n) {
        this.lat = Point.toLat(Point.projectLat(n.lat()));
        this.lon = Point.toLon(Point.projectLon(n.lon()));
    }

    public static Node invert(float x, float y) {
        return new Node(Point.toLat(y), Point.toLon(x));
    }

    /** Constructs this {@linkplain Node} based on the given {@linkplain Point}.
     Equivalent to {@link Point#inverse()}. */
    public Node(Point pt) {
        this.lat = pt.lat();
        this.lon = pt.lon();
    }

    /** Creates a new {@linkplain Point} based on this {@linkplain Node}.
     Equivalent to {@link Point#fromLonLat}. */
    public Point project() {
        return Point.fromLonLat(lat, lon);
    }

    public Node(double lat, double lon) {
        this((float) lat, (float) lon);
    }

    @Override
    public String toString() {
        return "(" + lat() + "°, " + lon() + "°)";
    }

    public float lat() {
        return lat;
    }

    public float lon() {
        return lon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof Node asNode) {
            // This is how Java compares floats internally in Float.equals() and Float.compare().
            return Float.floatToIntBits(this.lat) == Float.floatToIntBits(asNode.lat) &&
                    Float.floatToIntBits(this.lon) == Float.floatToIntBits(asNode.lon);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    public float distTo(Point point){
        return (float) Maths.distance(this, point.inverse());
    }
}