package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class GenerosModel extends Conexion {

    // === REGISTRAR GENERO ===
    public boolean registrarGenero(JSONObject data) {
        String nombre = (String) data.get("nombre_genero");
        if (existeNombreGenero(nombre)) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO Generos (nombre_genero)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(GenerosModel.class.getName()).log(Level.INFO, "Género registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al registrar género: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE GENERO EXISTE ===
    private boolean existeNombreGenero(String nombre) {
        String sql = "SELECT 1 FROM Generos WHERE nombre_genero = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de género: " + nombre, e);
            return false;
        }
    }

    // === OBTENER GENERO POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idGenero) {
        String sql = """
            SELECT id_genero, nombre_genero
            FROM Generos
            WHERE id_genero = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idGenero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject genero = new JSONObject();
                    genero.put("id_genero", rs.getInt("id_genero"));
                    genero.put("nombre_genero", rs.getString("nombre_genero"));
                    return genero;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al obtener género por ID: " + idGenero, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS GENEROS (como lista de JSON) ===
    public List<JSONObject> listarGeneros() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_genero, nombre_genero
            FROM Generos
            ORDER BY id_genero
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject genero = new JSONObject();
                genero.put("id_genero", rs.getInt("id_genero"));
                genero.put("nombre_genero", rs.getString("nombre_genero"));

                lista.add(genero);
            }

        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al listar géneros", e);
        }
        return lista;
    }

    // === ACTUALIZAR GENERO ===
    public boolean actualizarGenero(JSONObject data) {
        int idGenero = ((Long) data.get("id_genero")).intValue();
        String nombre = (String) data.get("nombre_genero");

        if (existeNombreGenero(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_genero").equals((long) idGenero)) {
                Logger.getLogger(GenerosModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE Generos
            SET nombre_genero = ?
            WHERE id_genero = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idGenero);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al actualizar género ID: " + idGenero, e);
            return false;
        }
    }

    // === ELIMINAR GENERO ===
    public boolean eliminarGenero(int idGenero) {
        String sql = "DELETE FROM Generos WHERE id_genero = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGenero);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al eliminar género con ID: " + idGenero, e);
            return false;
        }
    }

    // === OBTENER GENERO POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_genero, nombre_genero
            FROM Generos
            WHERE nombre_genero = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject genero = new JSONObject();
                    genero.put("id_genero", rs.getInt("id_genero"));
                    genero.put("nombre_genero", rs.getString("nombre_genero"));
                    return genero;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(GenerosModel.class.getName()).log(Level.SEVERE, "Error al obtener género por nombre: " + nombre, e);
        }
        return null;
    }
}