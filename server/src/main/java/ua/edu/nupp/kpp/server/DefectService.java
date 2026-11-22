package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class DefectService {
    private static final Logger LOG = Logger.getLogger(DefectService.class.getName());
    private final DefectDao dao = new DefectDao();
    private final InspectionDao inspectionDao = new InspectionDao();

    /** Дозволені переходи: ЗАФІКСОВАНО → ПЕРЕВІРЕНО → РЕМОНТ_ПРИЗНАЧЕНО → ЗАКРИТО. */
    public static boolean isTransitionAllowed(DefectState from, DefectState to) {
        return (from == DefectState.RECORDED && to == DefectState.VERIFIED)
                || (from == DefectState.VERIFIED && to == DefectState.REPAIR_ASSIGNED)
                || (from == DefectState.REPAIR_ASSIGNED && to == DefectState.CLOSED);
    }

    public List<Defect> list() throws SQLException {
        return dao.findAll();
    }

    /** Створює дефект на основі обстеження; F1 та I розраховуються на сервері. */
    public void create(long inspectionId) throws SQLException {
        Defect d = new Defect();
        d.setState(DefectState.RECORDED);
        fill(d, requireInspection(inspectionId));
        dao.insert(d);
        LOG.info("Створено дефект за обстеженням id=" + inspectionId);
    }

    /** Перераховує фактори й індекс (за потреби — для іншого обстеження). Стан не змінюється. */
    public void update(long id, long inspectionId) throws SQLException {
        Defect d = dao.getById(id);
        if (d == null) {
            throw new IllegalArgumentException("Дефект не знайдено");
        }
        fill(d, requireInspection(inspectionId));
        dao.update(d);
        LOG.info("Перераховано дефект: id=" + id);
    }

    public void changeState(long id, DefectState to) throws SQLException {
        Defect d = dao.getById(id);
        if (d == null) {
            throw new IllegalArgumentException("Дефект не знайдено");
        }
        if (!isTransitionAllowed(d.getState(), to)) {
            throw new IllegalArgumentException(
                    "Перехід " + d.getState() + " → " + to + " заборонений");
        }
        dao.updateState(id, to);
        LOG.info("Змінено стан дефекту id=" + id + ": " + d.getState() + " → " + to);
    }

    public void delete(long id) throws SQLException {
        if (dao.delete(id) == 0) {
            throw new IllegalArgumentException("Дефект не знайдено");
        }
        LOG.info("Видалено дефект: id=" + id);
    }

    private Inspection requireInspection(long inspectionId) throws SQLException {
        Inspection m = inspectionDao.getById(inspectionId);
        if (m == null) {
            throw new IllegalArgumentException("Обстеження з ID " + inspectionId + " не існує");
        }
        return m;
    }

    private void fill(Defect d, Inspection m) {
        d.setInspectionId(m.getId());
        d.setF1(RiskCalculator.f1(m.getMeasuredValue(), m.getAllowedValue()));
        d.setF2(m.getScaleScore());
        d.setF3(m.getSafetyScore());
        d.setRiskIndex(RiskCalculator.risk(d.getF1(), d.getF2(), d.getF3()));
    }
}
