package de.th.ro.datavis.models;

public class FFSLine {
    public double getPhi() {
        return phi;
    }

    public double getTheta() {
        return theta;
    }

    private double phi;
     private double theta;
     private double rePhi;
     private double imPhi;
     private double reTheta;
     private double imTheta;

    public FFSLine(double phi, double theta, double rePhi, double imPhi, double reTheta, double imTheta) {
        this.phi = phi;
        this.theta = theta;
        this.rePhi = rePhi;
        this.imPhi = imPhi;
        this.reTheta = reTheta;
        this.imTheta = imTheta;
    }

    public double getPhiRadians() {
        return Math.toRadians(phi);
    }

    public double getThetaRadians() {
        return Math.toRadians(theta);
    }

    public double getRePhi() {
        return rePhi;
    }

    public double getImPhi() {
        return imPhi;
    }

    public double getReTheta() {
        return reTheta;
    }

    public double getImTheta() {
        return imTheta;
    }


}
