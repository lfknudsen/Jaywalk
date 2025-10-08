package com.falkknudsen.jaywalk;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public interface IDrawable {
    void draw(GraphicsContext gc, Color colour);
}
