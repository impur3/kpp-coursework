package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InspectionDao {

    private static final String COLS =
            "id,object_id,defect_type,measured_value,allowed_value,scale_score,safety_score";

    public List<Inspection> findAll() throws SQLException {
        String sql = "SELECT " + COLS + " FROM inspection ORDER BY id";
        List<Inspection> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Inspection getById(long id) throws SQLException {
        String sql = "SELECT " + COLS + " FROM inspection WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Inspection m) throws SQLException {
        String sql = "INSERT INTO inspection(object_id,defect_type,measured_value,allowed_value,"
                + "scale_score,safety_score) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, m.getObjectId());
            p.setString(2, m.getDefectType());
            p.setDouble(3, m.getMeasuredValue());
            p.setDouble(4, m.getAllowedValue());
            p.setDouble(5, m.getScaleScore());
            p.setDouble(6, m.getSafetyScore());
            p.executeUpdate();
        }
    }

    public void update(Inspection m) throws SQLException {
        String sql = "UPDATE inspection SET object_id=?, defect_type=?, measured_value=?, "
                + "allowed_value=?, scale_score=?, safety_score=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, m.getObjectId());
            p.setString(2, m.getDefectType());
            p.setDouble(3, m.getMeasuredValue());
            p.setDouble(4, m.getAllowedValue());
            p.setDouble(5, m.getScaleScore());
            p.setDouble(6, m.getSafetyScore());
            p.setLong(7, m.getId());
            p.executeUpdate();
        }
    }

    public int delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM inspection WHERE id=?")) {
            p.setLong(1, id);
            return p.executeUpdate();
        }
    }

    private Inspection map(ResultSet rs) throws SQLException {
        Inspection m = new Inspection();
        m.setId(rs.getLong("id"));
        m.setObjectId(rs.getLong("object_id"));
        m.setDefectType(rs.getString("defect_type"));
        m.setMeasuredValue(rs.getDouble("measured_value"));
        m.setAllowedValue(rs.getDouble("allowed_value"));
        m.setScaleScore(rs.getDouble("scale_score"));
        m.setSafetyScore(rs.getDouble("safety_score"));
        return m;
    }
}
