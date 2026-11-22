package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class BuildingObjectService {
    private static final Logger LOG = Logger.getLogger(BuildingObjectService.class.getName());
    private final BuildingObjectDao dao = new BuildingObjectDao();

    public static void validate(BuildingObject s) {
        if (Validation.blank(s.getCode()) || Validation.blank(s.getName())
                || Validation.blank(s.getAddress())) {
            throw new IllegalArgumentException("Код, назва і адреса не повинні бути порожніми");
        }
    }

    public List<BuildingObject> list() throws SQLException {
        return dao.findAll();
    }

    public void create(BuildingObject s) throws SQLException {
        validate(s);
        dao.insert(s);
        LOG.info("Додано будівельний об’єкт: " + s.getCode());
    }

    public void update(BuildingObject s) throws SQLException {
        validate(s);
        if (dao.getById(s.getId()) == null) {
            throw new IllegalArgumentException("Будівельний об’єкт не знайдено");
        }
        dao.update(s);
        LOG.info("Оновлено будівельний об’єкт: id=" + s.getId());
    }

    public void delete(long id) throws SQLException {
        if (dao.delete(id) == 0) {
            throw new IllegalArgumentException("Будівельний об’єкт не знайдено");
        }
        LOG.info("Видалено будівельний об’єкт: id=" + id);
    }
}
