package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

public class RepairTaskService {
    private static final Logger LOG = Logger.getLogger(RepairTaskService.class.getName());
    private final RepairTaskDao dao = new RepairTaskDao();
    private final DefectDao defectDao = new DefectDao();

    public static void validate(RepairTask t) {
        if (t.getDefectId() <= 0) {
            throw new IllegalArgumentException("Не вказано дефект");
        }
        if (Validation.blank(t.getDescription())) {
            throw new IllegalArgumentException("Опис завдання не повинен бути порожнім");
        }
        if (!Validation.blank(t.getPlannedDate())) {
            try {
                LocalDate.parse(t.getPlannedDate().trim());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Дата має бути у форматі РРРР-ММ-ДД");
            }
        }
    }

    public List<RepairTask> list() throws SQLException {
        return dao.findAll();
    }

    public void create(RepairTask t) throws SQLException {
        validate(t);
        requireDefect(t.getDefectId());
        dao.insert(t);
        LOG.info("Додано ремонтне завдання для дефекту id=" + t.getDefectId());
    }

    public void update(RepairTask t) throws SQLException {
        validate(t);
        requireDefect(t.getDefectId());
        if (dao.getById(t.getId()) == null) {
            throw new IllegalArgumentException("Ремонтне завдання не знайдено");
        }
        dao.update(t);
        LOG.info("Оновлено ремонтне завдання: id=" + t.getId());
    }

    public void delete(long id) throws SQLException {
        if (dao.delete(id) == 0) {
            throw new IllegalArgumentException("Ремонтне завдання не знайдено");
        }
        LOG.info("Видалено ремонтне завдання: id=" + id);
    }

    private void requireDefect(long defectId) throws SQLException {
        if (defectDao.getById(defectId) == null) {
            throw new IllegalArgumentException("Дефект з ID " + defectId + " не існує");
        }
    }
}
