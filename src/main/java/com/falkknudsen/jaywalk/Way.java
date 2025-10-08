package com.falkknudsen.jaywalk;

import com.falkknudsen.jaywalk.contracts.IDrawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

public class Way extends AbstractList<Node> implements IDrawable {
    private float[] coordinates;

    private Way(float[] coordinates) {
        this.coordinates = coordinates;
    }

     Way(List<Node> nodes) {
        coordinates = new float[nodes.size() * 2];
        for (int i = 0; i < nodes.size(); i++) {
            coordinates[i * 2] = Point.projectLon(nodes.get(i).lon());
            coordinates[i * 2 + 1] = Point.projectLat(nodes.get(i).lat());
        }
    }

    @Override
    public Node getFirst() {
        return super.getFirst();
    }

    @Override
    public Node getLast() {
        return super.getLast();
    }

    public static Way create(List<Node> nodes, Map<String, String> tags) {
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("nodes must have at least 2 nodes");
        }
        return new Way(nodes);
    }

    @Override
    public int size() {
        return coordinates.length / 2;
    }

    @Override
    public boolean isEmpty() {
        return coordinates.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Point) return contains((Point) o);
        return false;
    }

    public boolean contains(Point p) {
        for (int i = 0; i < coordinates.length; i += 2) {
            if (p.x() == coordinates[i] && p.y() == coordinates[i + 1])
                return true;
        }
        return false;
    }

    @Override
    public Node get(int index) {
        if (index >= size())
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        return Node.fromXY(coordinates[index * 2], coordinates[index * 2 + 1]);
    }

    @Override
    public Iterator<Node> iterator() {
        return new Iterator<Node>() {
            int nextIndex = 0;

            @Override
            public boolean hasNext() {
                return nextIndex < coordinates.length;
            }

            @Override
            public Node next() {
                return Node.fromXY(coordinates[nextIndex++], coordinates[nextIndex++]);
            }
        };
    }

    @Override
    public Point[] toArray() {
        Point[] points = new Point[size()];
        for (int i = 0; i < coordinates.length; i += 2) {
            points[i] = new Point(coordinates[i], coordinates[i + 1]);
        }
        return points;
    }

    public Point[] toArray(Point[] points) {
        if (points == null) {
            points = new Point[size()];
        }
        if (points.length >= coordinates.length / 2) {
            for (int i = 0; i < coordinates.length; i += 2) {
                points[i] = new Point(coordinates[i], coordinates[i + 1]);
            }
        }
        if (points.length > coordinates.length / 2) {
            points[coordinates.length / 2] = null;
        }
        return points;
    }

    public Node[] toArray(Node[] nodes) {
        if (nodes == null) {
            nodes = new Node[size()];
        }
        if (nodes.length >= coordinates.length / 2) {
            for (int i = 0; i < coordinates.length; i += 2) {
                nodes[i] = Node.fromXY(coordinates[i], coordinates[i + 1]);
            }
        }
        if (nodes.length > coordinates.length / 2) {
            nodes[coordinates.length / 2] = null;
        }
        return nodes;
    }

    public Float[] toArray(Float[] floats) {
        if (floats == null) {
            floats = new Float[size()];
        }
        if (floats.length >= coordinates.length) {
            for (int i = 0; i < coordinates.length; i ++) {
                floats[i] = coordinates[i];
            }
        }
        if (floats.length > coordinates.length) {
            floats[coordinates.length] = null;
        }
        return floats;
    }

    public Double[] toArray(Double[] doubles) {
        if (doubles == null) {
            doubles = new Double[size()];
        }
        if (doubles.length >= coordinates.length) {
            for (int i = 0; i < coordinates.length; i ++) {
                doubles[i] = (double) coordinates[i];
            }
        }
        if (doubles.length > coordinates.length) {
            doubles[coordinates.length] = null;
        }
        return doubles;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        return switch (a) {
            case null -> (T[]) toArray();
            case Node[] nodes -> (T[]) toArray(nodes);
            case Float[] floats -> (T[]) toArray(floats);
            case Double[] doubles -> (T[]) toArray(doubles);
            default -> (T[]) toArray((Point[]) a);
        };
    }

    public boolean addPoint(Point point) {
        var coords = new float[coordinates.length + 2];
        System.arraycopy(coordinates, 0, coords, 0, coordinates.length);
        coords[coordinates.length] = point.x();
        coords[coordinates.length + 1] = point.y();
        coordinates = coords;
        return true;
    }

    @Override
    public boolean add(Node node) {
        var coords = new float[coordinates.length + 2];
        System.arraycopy(coordinates, 0, coords, 0, coordinates.length);
        coords[coordinates.length] = Point.projectLon(node.lon());
        coords[coordinates.length + 1] = Point.projectLat(node.lat());
        coordinates = coords;
        return true;
    }

    @Override
    public void draw(GraphicsContext gc, Color colour) {
        gc.setStroke(colour);
        gc.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length; i += 2) {
            gc.lineTo(coordinates[i], coordinates[i + 1]);
        }
        gc.stroke();
    }
}
