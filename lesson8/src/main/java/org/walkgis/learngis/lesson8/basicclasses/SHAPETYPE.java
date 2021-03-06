package org.walkgis.learngis.lesson8.basicclasses;

public enum SHAPETYPE {
    point(1),
    polyline(3),
    polygon(5);
    private int value;

    SHAPETYPE(int value) {
        this.value = value;
    }

    public static SHAPETYPE getByValue(Integer value) {
        for (SHAPETYPE sexEnum : SHAPETYPE.values()) {
            if (sexEnum.value == value)
                return sexEnum;
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
};
