package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepairTask {
    private long id;
    private long defectId;
    private String description;
    private String plannedDate;

    public RepairTask() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getDefectId() { return defectId; }
    public void setDefectId(long defectId) { this.defectId = defectId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPlannedDate() { return plannedDate; }
    public void setPlannedDate(String plannedDate) { this.plannedDate = plannedDate; }
}
