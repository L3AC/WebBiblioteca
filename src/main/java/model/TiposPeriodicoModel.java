package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class TiposPeriodicoModel extends Conexion {

    // === REGISTRAR TIPO PERIODICO ===
    public boolean registrarTipoPeriodico(JSONObject data) {
        String nombre = (String) data.get("nombre_tipo_periodico");
        if (existeNombreTipoPeriodico(nombre)) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO TiposPeriodico (nombre_tipo_periodico)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.INFO, "Tipo de periódico registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al registrar tipo de periódico: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE TIPO PERIODICO EXISTE ===
    private boolean existeNombreTipoPeriodico(String nombre) {
        String sql = "SELECT 1 FROM TiposPeriodico WHERE nombre_tipo_periodico = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de tipo de periódico: " + nombre, e);
            return false;
        }
    }

    // === OBTENER TIPO PERIODICO POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idTipoPeriodico) {
        String sql = """
            SELECT id_tipo_periodico, nombre_tipo_periodico
            FROM TiposPeriodico
            WHERE id_tipo_periodico = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTipoPeriodico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoPeriodico = new JSONObject();
                    tipoPeriodico.put("id_tipo_periodico", rs.getInt("id_tipo_periodico"));
                    tipoPeriodico.put("nombre_tipo_periodico", rs.getString("nombre_tipo_periodico"));
                    return tipoPeriodico;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de periódico por ID: " + idTipoPeriodico, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS TIPOS PERIODICO (como lista de JSON) ===
    public List<JSONObject> listarTiposPeriodico() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_tipo_periodico, nombre_tipo_periodico
            FROM TiposPeriodico
            ORDER BY id_tipo_periodico
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject tipoPeriodico = new JSONObject();
                tipoPeriodico.put("id_tipo_periodico", rs.getInt("id_tipo_periodico"));
                tipoPeriodico.put("nombre_tipo_periodico", rs.getString("nombre_tipo_periodico"));

                lista.add(tipoPeriodico);
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al listar tipos de periódico", e);
        }
        return lista;
    }

    // === ACTUALIZAR TIPO PERIODICO ===
    public boolean actualizarTipoPeriodico(JSONObject data) {
        int idTipoPeriodico = ((Long) data.get("id_tipo_periodico")).intValue();
        String nombre = (String) data.get("nombre_tipo_periodico");

        if (existeNombreTipoPeriodico(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_tipo_periodico").equals((long) idTipoPeriodico)) {
                Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE TiposPeriodico
            SET nombre_tipo_periodico = ?
            WHERE id_tipo_periodico = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idTipoPeriodico);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al actualizar tipo de periódico ID: " + idTipoPeriodico, e);
            return false;
        }
    }

    // === ELIMINAR TIPO PERIODICO ===
    public boolean eliminarTipoPeriodico(int idTipoPeriodico) {
        String sql = "DELETE FROM TiposPeriodico WHERE id_tipo_periodico = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTipoPeriodico);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al eliminar tipo de periódico con ID: " + idTipoPeriodico, e);
            return false;
        }
    }

    // === OBTENER TIPO PERIODICO POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_tipo_periodico, nombre_tipo_periodico
            FROM TiposPeriodico
            WHERE nombre_tipo_periodico = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoPeriodico = new JSONObject();
                    tipoPeriodico.put("id_tipo_periodico", rs.getInt("id_tipo_periodico"));
                    tipoPeriodico.put("nombre_tipo_periodico", rs.getString("nombre_tipo_periodico"));
                    return tipoPeriodico;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposPeriodicoModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de periódico por nombre: " + nombre, e);
        }
        return null;
    }
}