package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class EditorialesModel extends Conexion {

    // === REGISTRAR EDITORIAL ===
    public boolean registrarEditorial(JSONObject data) {
        String nombre = (String) data.get("nombre_editorial");
        if (existeNombreEditorial(nombre)) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO Editoriales (nombre_editorial)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(EditorialesModel.class.getName()).log(Level.INFO, "Editorial registrada: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al registrar editorial: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE EDITORIAL EXISTE ===
    private boolean existeNombreEditorial(String nombre) {
        String sql = "SELECT 1 FROM Editoriales WHERE nombre_editorial = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de editorial: " + nombre, e);
            return false;
        }
    }

    // === OBTENER EDITORIAL POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idEditorial) {
        String sql = """
            SELECT id_editorial, nombre_editorial
            FROM Editoriales
            WHERE id_editorial = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEditorial);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject editorial = new JSONObject();
                    editorial.put("id_editorial", rs.getInt("id_editorial"));
                    editorial.put("nombre_editorial", rs.getString("nombre_editorial"));
                    return editorial;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al obtener editorial por ID: " + idEditorial, e);
        }
        return null;
    }

    // === LISTAR TODAS LAS EDITORIALES (como lista de JSON) ===
    public List<JSONObject> listarEditoriales() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_editorial, nombre_editorial
            FROM Editoriales
            ORDER BY id_editorial
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject editorial = new JSONObject();
                editorial.put("id_editorial", rs.getInt("id_editorial"));
                editorial.put("nombre_editorial", rs.getString("nombre_editorial"));

                lista.add(editorial);
            }

        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al listar editoriales", e);
        }
        return lista;
    }

    // === ACTUALIZAR EDITORIAL ===
    public boolean actualizarEditorial(JSONObject data) {
        int idEditorial = ((Long) data.get("id_editorial")).intValue();
        String nombre = (String) data.get("nombre_editorial");

        if (existeNombreEditorial(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_editorial").equals((long) idEditorial)) {
                Logger.getLogger(EditorialesModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE Editoriales
            SET nombre_editorial = ?
            WHERE id_editorial = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idEditorial);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al actualizar editorial ID: " + idEditorial, e);
            return false;
        }
    }

    // === ELIMINAR EDITORIAL ===
    public boolean eliminarEditorial(int idEditorial) {
        String sql = "DELETE FROM Editoriales WHERE id_editorial = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEditorial);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al eliminar editorial con ID: " + idEditorial, e);
            return false;
        }
    }

    // === OBTENER EDITORIAL POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_editorial, nombre_editorial
            FROM Editoriales
            WHERE nombre_editorial = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject editorial = new JSONObject();
                    editorial.put("id_editorial", rs.getInt("id_editorial"));
                    editorial.put("nombre_editorial", rs.getString("nombre_editorial"));
                    return editorial;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(EditorialesModel.class.getName()).log(Level.SEVERE, "Error al obtener editorial por nombre: " + nombre, e);
        }
        return null;
    }
}