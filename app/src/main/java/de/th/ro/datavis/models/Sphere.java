package de.th.ro.datavis.models;

import java.math.BigDecimal;

public class Sphere {
    private double x;
    private double y;
    private double z;
    private double intensity;

    public Sphere(double x, double y, double z, double i) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.intensity = i;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }

    public double getIntensity(){
        return intensity;
    }

}
