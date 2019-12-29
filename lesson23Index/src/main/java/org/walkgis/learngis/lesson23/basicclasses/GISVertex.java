package org.walkgis.learngis.lesson23.basicclasses;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GISVertex {
    public double x;
    public double y;

    public GISVertex() {
    }

    public GISVertex(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public GISVertex(DataInputStream randomAccessFile) throws IOException {
        this.x = randomAccessFile.readDouble();
        this.y = randomAccessFile.readDouble();
    }

    public GISVertex(GISVertex vertex) {
        copyFrom(vertex);
    }

    public double distance(GISVertex anotherVertex) {
        return Math.sqrt((x - anotherVertex.x) * (x - anotherVertex.x) + (y - anotherVertex.y) * (y - anotherVertex.y));
    }

    public void copyFrom(GISVertex gisVertex) {
        this.x = gisVertex.x;
        this.y = gisVertex.y;
    }

    public void writeVertex(DataOutputStream bw) {
        try {
            bw.writeDouble(x);
            bw.writeDouble(y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSame(GISVertex vertex) {
        return x == vertex.x && y == vertex.y;
    }
}
