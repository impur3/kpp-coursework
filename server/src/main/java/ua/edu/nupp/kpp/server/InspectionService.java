package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class InspectionService {
    private static final Logger LOG = Logger.getLogger(InspectionService.class.getName());
    private final InspectionDao dao = new InspectionDao();
    private final BuildingObjectDao objectDao = new BuildingObjectDao();

    /** Перевірки, які не потребують бази даних. */
    public static void validate(Inspection m) {
        if (m.getObjectId() <= 0) {
            throw new IllegalArgumentException("Не вказано будівельний об’єкт");
        }
        if (Validation.blank(m.getDefectType())) {
            throw new IllegalArgumentException("Тип дефекту не повинен бути порожнім");
        }
        if (Double.isNaN(m.getMeasuredValue()) || m.getMeasuredValue() < 0) {
            throw new IllegalArgumentException("Виміряне значення не може бути від’ємним");
        }
        if (Double.isNaN(m.getAllowedValue()) || m.getAllowedValue() <= 0) {
            throw new IllegalArgumentException("Допустиме значення має бути більшим за нуль");
        }
        if (m.getScaleScore() < 0 || m.getScaleScore() > 100
                || m.getSafetyScore() < 0 || m.getSafetyScore() > 100) {
            throw new IllegalArgumentException("F2 і F3 мають бути в діапазоні 0..100");
        }
    }

    public List<Inspection> list() throws SQLException {
        return dao.findAll();
    }

    public void create(Inspection m) throws SQLException {
        validate(m);
        requireObject(m.getObjectId());
        dao.insert(m);
        LOG.info("Додано обстеження: " + m.getDefectType());
    }

    public void update(Inspection m) throws SQLException {
        validate(m);
        requireObject(m.getObjectId());
        if (dao.getById(m.getId()) == null) {
            throw new IllegalArgumentException("Обстеження не знайдено");
        }
        dao.update(m);
        LOG.info("Оновлено обстеження: id=" + m.getId());
    }

    public void delete(long id) throws SQLException {
        if (dao.delete(id) == 0) {
            throw new IllegalArgumentException("Обстеження не знайдено");
        }
        LOG.info("Видалено обстеження: id=" + id);
    }

    private void requireObject(long objectId) throws SQLException {
        if (objectDao.getById(objectId) == null) {
            throw new IllegalArgumentException("Будівельний об’єкт з ID " + objectId + " не існує");
        }
    }
}
