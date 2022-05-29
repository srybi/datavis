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

    public void setPhi(double phi) {
        this.phi = phi;
    }

    public double getThetaRadians() {
        return Math.toRadians(theta);
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getRePhi() {
        return rePhi;
    }

    public void setRePhi(double rePhi) {
        this.rePhi = rePhi;
    }

    public double getImPhi() {
        return imPhi;
    }

    public void setImPhi(double imPhi) {
        this.imPhi = imPhi;
    }

    public double getReTheta() {
        return reTheta;
    }

    public void setReTheta(double reTheta) {
        this.reTheta = reTheta;
    }

    public double getImTheta() {
        return imTheta;
    }

    public void setImTheta(double imTheta) {
        this.imTheta = imTheta;
    }
}
