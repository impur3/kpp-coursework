package ua.edu.nupp.kpp.server;

/** Дефект (об'єкт життєвого циклу). */
public class Defect {
    private long id;
    private long inspectionId;
    private DefectState state;
    private double f1;
    private double f2;
    private double f3;
    private double riskIndex;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getInspectionId() { return inspectionId; }
    public void setInspectionId(long inspectionId) { this.inspectionId = inspectionId; }
    public DefectState getState() { return state; }
    public void setState(DefectState state) { this.state = state; }
    public double getF1() { return f1; }
    public void setF1(double f1) { this.f1 = f1; }
    public double getF2() { return f2; }
    public void setF2(double f2) { this.f2 = f2; }
    public double getF3() { return f3; }
    public void setF3(double f3) { this.f3 = f3; }
    public double getRiskIndex() { return riskIndex; }
    public void setRiskIndex(double riskIndex) { this.riskIndex = riskIndex; }
}
