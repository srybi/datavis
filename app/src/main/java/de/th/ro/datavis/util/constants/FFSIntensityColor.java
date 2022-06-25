package de.th.ro.datavis.util.constants;

import com.google.ar.sceneform.rendering.Color;

public enum FFSIntensityColor {
    RED( "red", new Color(1f, 0f, 0f)),
    ORANGE( "orange", new Color(0.9f, 0.47f, 0.23f)),
    YELLOW( "yellow", new Color(1f, 0.95f, 0.02f)),
    GREEN( "green", new Color(0.49f, 1f, 0f)),
    BABYBLUE( "babyBlue", new Color(0.23f, 0.93f, 0.9f)),
    BLUE("blue", new Color(0f, 1f, 0.96f) );

    private final String name;
    private final Color color;

    FFSIntensityColor(final String name, final Color color){
        this.name = name;
        this.color = color;
    }

    public String getName(){
        return this.name;
    }

    public Color getColor() {
        return color;
    }

}
