package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class AutoresModel extends Conexion {

    // === REGISTRAR AUTOR ===
    public boolean registrarAutor(JSONObject data) {
        String nombre = (String) data.get("nombre_autor");
        if (existeNombreAutor(nombre)) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO Autores (nombre_autor)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(AutoresModel.class.getName()).log(Level.INFO, "Autor registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al registrar autor: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE AUTOR EXISTE ===
    private boolean existeNombreAutor(String nombre) {
        String sql = "SELECT 1 FROM Autores WHERE nombre_autor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de autor: " + nombre, e);
            return false;
        }
    }

    // === OBTENER AUTOR POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idAutor) {
        String sql = """
            SELECT id_autor, nombre_autor
            FROM Autores
            WHERE id_autor = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAutor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject autor = new JSONObject();
                    autor.put("id_autor", rs.getInt("id_autor"));
                    autor.put("nombre_autor", rs.getString("nombre_autor"));
                    return autor;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al obtener autor por ID: " + idAutor, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS AUTORES (como lista de JSON) ===
    public List<JSONObject> listarAutores() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_autor, nombre_autor
            FROM Autores
            ORDER BY id_autor
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject autor = new JSONObject();
                autor.put("id_autor", rs.getInt("id_autor"));
                autor.put("nombre_autor", rs.getString("nombre_autor"));

                lista.add(autor);
            }

        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al listar autores", e);
        }
        return lista;
    }

    // === ACTUALIZAR AUTOR ===
    public boolean actualizarAutor(JSONObject data) {
        int idAutor = ((Long) data.get("id_autor")).intValue();
        String nombre = (String) data.get("nombre_autor");

        if (existeNombreAutor(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_autor").equals((long) idAutor)) {
                Logger.getLogger(AutoresModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE Autores
            SET nombre_autor = ?
            WHERE id_autor = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idAutor);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al actualizar autor ID: " + idAutor, e);
            return false;
        }
    }

    // === ELIMINAR AUTOR ===
    public boolean eliminarAutor(int idAutor) {
        String sql = "DELETE FROM Autores WHERE id_autor = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAutor);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al eliminar autor con ID: " + idAutor, e);
            return false;
        }
    }

    // === OBTENER AUTOR POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_autor, nombre_autor
            FROM Autores
            WHERE nombre_autor = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject autor = new JSONObject();
                    autor.put("id_autor", rs.getInt("id_autor"));
                    autor.put("nombre_autor", rs.getString("nombre_autor"));
                    return autor;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(AutoresModel.class.getName()).log(Level.SEVERE, "Error al obtener autor por nombre: " + nombre, e);
        }
        return null;
    }
}