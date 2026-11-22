package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Inspection {
    private long id;
    private long objectId;
    private String defectType;
    private double measuredValue;
    private double allowedValue;
    private double scaleScore;
    private double safetyScore;

    public Inspection() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getObjectId() { return objectId; }
    public void setObjectId(long objectId) { this.objectId = objectId; }
    public String getDefectType() { return defectType; }
    public void setDefectType(String defectType) { this.defectType = defectType; }
    public double getMeasuredValue() { return measuredValue; }
    public void setMeasuredValue(double measuredValue) { this.measuredValue = measuredValue; }
    public double getAllowedValue() { return allowedValue; }
    public void setAllowedValue(double allowedValue) { this.allowedValue = allowedValue; }
    public double getScaleScore() { return scaleScore; }
    public void setScaleScore(double scaleScore) { this.scaleScore = scaleScore; }
    public double getSafetyScore() { return safetyScore; }
    public void setSafetyScore(double safetyScore) { this.safetyScore = safetyScore; }
}
