package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefectDao {

    private static final String COLS = "id,inspection_id,state,f1,f2,f3,risk_index";

    public List<Defect> findAll() throws SQLException {
        String sql = "SELECT " + COLS + " FROM defect ORDER BY id";
        List<Defect> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Defect getById(long id) throws SQLException {
        String sql = "SELECT " + COLS + " FROM defect WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(Defect d) throws SQLException {
        String sql = "INSERT INTO defect(inspection_id,state,f1,f2,f3,risk_index) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, d.getInspectionId());
            p.setString(2, d.getState().name());
            p.setDouble(3, d.getF1());
            p.setDouble(4, d.getF2());
            p.setDouble(5, d.getF3());
            p.setDouble(6, d.getRiskIndex());
            p.executeUpdate();
        }
    }

    public void update(Defect d) throws SQLException {
        String sql = "UPDATE defect SET inspection_id=?, state=?, f1=?, f2=?, f3=?, risk_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, d.getInspectionId());
            p.setString(2, d.getState().name());
            p.setDouble(3, d.getF1());
            p.setDouble(4, d.getF2());
            p.setDouble(5, d.getF3());
            p.setDouble(6, d.getRiskIndex());
            p.setLong(7, d.getId());
            p.executeUpdate();
        }
    }

    public void updateState(long id, DefectState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE defect SET state=? WHERE id=?")) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    public int delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM defect WHERE id=?")) {
            p.setLong(1, id);
            return p.executeUpdate();
        }
    }

    private Defect map(ResultSet rs) throws SQLException {
        Defect d = new Defect();
        d.setId(rs.getLong("id"));
        d.setInspectionId(rs.getLong("inspection_id"));
        d.setState(DefectState.valueOf(rs.getString("state")));
        d.setF1(rs.getDouble("f1"));
        d.setF2(rs.getDouble("f2"));
        d.setF3(rs.getDouble("f3"));
        d.setRiskIndex(rs.getDouble("risk_index"));
        return d;
    }
}
