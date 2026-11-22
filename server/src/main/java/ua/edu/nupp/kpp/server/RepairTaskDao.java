package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepairTaskDao {

    public List<RepairTask> findAll() throws SQLException {
        String sql = "SELECT id,defect_id,description,planned_date FROM repair_task ORDER BY id";
        List<RepairTask> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public RepairTask getById(long id) throws SQLException {
        String sql = "SELECT id,defect_id,description,planned_date FROM repair_task WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(RepairTask t) throws SQLException {
        String sql = "INSERT INTO repair_task(defect_id,description,planned_date) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, t.getDefectId());
            p.setString(2, t.getDescription());
            p.setString(3, t.getPlannedDate());
            p.executeUpdate();
        }
    }

    public void update(RepairTask t) throws SQLException {
        String sql = "UPDATE repair_task SET defect_id=?, description=?, planned_date=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, t.getDefectId());
            p.setString(2, t.getDescription());
            p.setString(3, t.getPlannedDate());
            p.setLong(4, t.getId());
            p.executeUpdate();
        }
    }

    public int delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM repair_task WHERE id=?")) {
            p.setLong(1, id);
            return p.executeUpdate();
        }
    }

    private RepairTask map(ResultSet rs) throws SQLException {
        RepairTask t = new RepairTask();
        t.setId(rs.getLong("id"));
        t.setDefectId(rs.getLong("defect_id"));
        t.setDescription(rs.getString("description"));
        t.setPlannedDate(rs.getString("planned_date"));
        return t;
    }
}
