package com.falkknudsen.jaywalk.contracts;

public interface IDrawableContainer {
    boolean add(IDrawable renderable);
    long size();
}
