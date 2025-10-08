package com.falkknudsen.jaywalk;

import com.falkknudsen.jaywalk.contracts.IDrawable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

public class Relation implements IDrawable {
    public Way[] ways;
    public Relation[] relations;

    private Relation(Way[] ways, Relation[] relations) {
        this.ways = ways;
        this.relations = relations;
    }

    private Relation(List<Way> ways, List<Relation> relations) {
        this.ways = new Way[ways.size()];
        for (int i = 0; i < ways.size(); i++) {
            this.ways[i] = ways.get(i);
        }
        this.relations = new Relation[relations.size()];
        for (int i = 0; i < relations.size(); i++) {
            this.relations[i] = relations.get(i);
        }
    }

    public static Relation create(Way[] ways, Relation[] relations, Map<String, String> tags) {
        return new Relation(ways, relations);
    }

    public static Relation create(List<Way> ways, List<Relation> relations, Map<String, String> tags) {
        return new Relation(ways, relations);
    }

    @Override
    public void draw(GraphicsContext gc, Color colour) {
        for (Way way : ways) {
            way.draw(gc, colour);
        }
        for (Relation relation : relations) {
            relation.draw(gc, colour);
        }
    }
}
