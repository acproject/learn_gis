package org.walkgis.learngis.lesson6.basicclasses;

import javafx.scene.canvas.GraphicsContext;

public abstract class GISSpatial {
    public GISVertex center;
    public GISExtent extent;

    public abstract void draw(GraphicsContext graphicsContext, GISView gisView);
}
