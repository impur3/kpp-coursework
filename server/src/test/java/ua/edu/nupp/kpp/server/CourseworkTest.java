package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseworkTest {

    @Test
    void f1WithoutExceedanceIsZero() {
        assertEquals(0.0, RiskCalculator.f1(20, 25), 0.0001);
    }

    @Test
    void f1ForDemoDataIs52() {
        assertEquals(52.0, RiskCalculator.f1(38, 25), 0.0001);
    }

    @Test
    void riskForDemoDataIs593() {
        assertEquals(59.3, RiskCalculator.risk(52, 60, 70), 0.0001);
    }

    @Test
    void riskRejectsFactorAbove100() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.risk(101, 50, 50));
    }

    @Test
    void recordedToVerifiedAllowed() {
        assertTrue(DefectService.isTransitionAllowed(DefectState.RECORDED, DefectState.VERIFIED));
    }

    @Test
    void verifiedToRepairAssignedAllowed() {
        assertTrue(DefectService.isTransitionAllowed(DefectState.VERIFIED, DefectState.REPAIR_ASSIGNED));
    }

    @Test
    void repairAssignedToClosedAllowed() {
        assertTrue(DefectService.isTransitionAllowed(DefectState.REPAIR_ASSIGNED, DefectState.CLOSED));
    }

    @Test
    void recordedToClosedRejected() {
        assertFalse(DefectService.isTransitionAllowed(DefectState.RECORDED, DefectState.CLOSED));
    }

    @Test
    void buildingObjectValidationWorks() {
        BuildingObject o = new BuildingObject();
        o.setCode("");
        o.setName("Житловий будинок");
        o.setAddress("м. Полтава");
        assertThrows(IllegalArgumentException.class, () -> BuildingObjectService.validate(o));
    }

    @Test
    void inspectionValidationWorks() {
        Inspection m = new Inspection();
        m.setObjectId(1);
        m.setDefectType("Тріщина");
        m.setMeasuredValue(1);
        m.setAllowedValue(1);
        m.setScaleScore(120);
        m.setSafetyScore(50);
        assertThrows(IllegalArgumentException.class, () -> InspectionService.validate(m));
    }
}
