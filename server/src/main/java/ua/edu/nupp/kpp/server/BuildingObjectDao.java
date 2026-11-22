package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BuildingObjectDao {

    public List<BuildingObject> findAll() throws SQLException {
        String sql = "SELECT id,code,name,address FROM building_object ORDER BY id";
        List<BuildingObject> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public BuildingObject getById(long id) throws SQLException {
        String sql = "SELECT id,code,name,address FROM building_object WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insert(BuildingObject s) throws SQLException {
        String sql = "INSERT INTO building_object(code,name,address) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, s.getCode());
            p.setString(2, s.getName());
            p.setString(3, s.getAddress());
            p.executeUpdate();
        }
    }

    public void update(BuildingObject s) throws SQLException {
        String sql = "UPDATE building_object SET code=?, name=?, address=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, s.getCode());
            p.setString(2, s.getName());
            p.setString(3, s.getAddress());
            p.setLong(4, s.getId());
            p.executeUpdate();
        }
    }

    public int delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM building_object WHERE id=?")) {
            p.setLong(1, id);
            return p.executeUpdate();
        }
    }

    private BuildingObject map(ResultSet rs) throws SQLException {
        BuildingObject o = new BuildingObject();
        o.setId(rs.getLong("id"));
        o.setCode(rs.getString("code"));
        o.setName(rs.getString("name"));
        o.setAddress(rs.getString("address"));
        return o;
    }
}
