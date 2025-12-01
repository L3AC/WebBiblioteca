package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class TDDModel extends Conexion {

    // === REGISTRAR TIPO DOCUMENTO DETALLE ===
    public boolean registrarTipoDocumentoDetalle(JSONObject data) {
        String nombre = (String) data.get("nombre_tipo_detalle");
        if (existeNombreTipoDetalle(nombre)) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO TiposDocumentoDetalle (nombre_tipo_detalle)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(TDDModel.class.getName()).log(Level.INFO, "Tipo de documento detalle registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al registrar tipo de documento detalle: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE TIPO DOCUMENTO DETALLE EXISTE ===
    private boolean existeNombreTipoDetalle(String nombre) {
        String sql = "SELECT 1 FROM TiposDocumentoDetalle WHERE nombre_tipo_detalle = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de tipo de documento detalle: " + nombre, e);
            return false;
        }
    }

    // === OBTENER TIPO DOCUMENTO DETALLE POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idTipoDetalle) {
        String sql = """
            SELECT id_tipo_detalle, nombre_tipo_detalle
            FROM TiposDocumentoDetalle
            WHERE id_tipo_detalle = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTipoDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoDetalle = new JSONObject();
                    tipoDetalle.put("id_tipo_detalle", rs.getInt("id_tipo_detalle"));
                    tipoDetalle.put("nombre_tipo_detalle", rs.getString("nombre_tipo_detalle"));
                    return tipoDetalle;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de documento detalle por ID: " + idTipoDetalle, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS TIPOS DOCUMENTO DETALLE (como lista de JSON) ===
    public List<JSONObject> listarTiposDocumentoDetalle() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_tipo_detalle, nombre_tipo_detalle
            FROM TiposDocumentoDetalle
            ORDER BY id_tipo_detalle
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject tipoDetalle = new JSONObject();
                tipoDetalle.put("id_tipo_detalle", rs.getInt("id_tipo_detalle"));
                tipoDetalle.put("nombre_tipo_detalle", rs.getString("nombre_tipo_detalle"));

                lista.add(tipoDetalle);
            }

        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al listar tipos de documento detalle", e);
        }
        return lista;
    }

    // === ACTUALIZAR TIPO DOCUMENTO DETALLE ===
    public boolean actualizarTipoDocumentoDetalle(JSONObject data) {
        int idTipoDetalle = ((Long) data.get("id_tipo_detalle")).intValue();
        String nombre = (String) data.get("nombre_tipo_detalle");

        if (existeNombreTipoDetalle(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_tipo_detalle").equals((long) idTipoDetalle)) {
                Logger.getLogger(TDDModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE TiposDocumentoDetalle
            SET nombre_tipo_detalle = ?
            WHERE id_tipo_detalle = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idTipoDetalle);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al actualizar tipo de documento detalle ID: " + idTipoDetalle, e);
            return false;
        }
    }

    // === ELIMINAR TIPO DOCUMENTO DETALLE ===
    public boolean eliminarTipoDocumentoDetalle(int idTipoDetalle) {
        String sql = "DELETE FROM TiposDocumentoDetalle WHERE id_tipo_detalle = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTipoDetalle);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al eliminar tipo de documento detalle con ID: " + idTipoDetalle, e);
            return false;
        }
    }

    // === OBTENER TIPO DOCUMENTO DETALLE POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_tipo_detalle, nombre_tipo_detalle
            FROM TiposDocumentoDetalle
            WHERE nombre_tipo_detalle = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoDetalle = new JSONObject();
                    tipoDetalle.put("id_tipo_detalle", rs.getInt("id_tipo_detalle"));
                    tipoDetalle.put("nombre_tipo_detalle", rs.getString("nombre_tipo_detalle"));
                    return tipoDetalle;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TDDModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de documento detalle por nombre: " + nombre, e);
        }
        return null;
    }
}