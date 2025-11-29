package model;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class EjemplarModel extends Conexion {

    public boolean registrarEjemplarDesdeJSON(JSONObject data) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // --- 1. Extraer datos generales del ejemplar ---
            String titulo = (String) data.get("titulo");
            Long idAutorLong = (Long) data.get("id_autor");
            Integer idAutor = idAutorLong != null ? idAutorLong.intValue() : null;
            String ubicacion = (String) data.get("ubicacion");
            String tipoDocumento = (String) data.get("tipo_documento");
            Long cantCopiasLong = (Long) data.get("cantidad_copias");
            int cantidadCopias = cantCopiasLong != null ? cantCopiasLong.intValue() : 0;

            // Insertar en Ejemplares
            String sqlEjemplar = "INSERT INTO Ejemplares (titulo, id_autor, ubicacion, tipo_documento) VALUES (?, ?, ?, ?)";
            PreparedStatement psEjemplar = conn.prepareStatement(sqlEjemplar, Statement.RETURN_GENERATED_KEYS);
            psEjemplar.setString(1, titulo);
            psEjemplar.setObject(2, idAutor);
            psEjemplar.setString(3, ubicacion);
            psEjemplar.setString(4, tipoDocumento);
            psEjemplar.executeUpdate();

            ResultSet rs = psEjemplar.getGeneratedKeys();
            int idEjemplar = -1;
            if (rs.next()) {
                idEjemplar = rs.getInt(1);
            } else {
                throw new SQLException("No se generó el ID del ejemplar.");
            }
            psEjemplar.close();

            // --- 2. Insertar en tabla específica según tipo_documento ---
            insertarSubtipoDesdeJSON(conn, tipoDocumento, idEjemplar, data);

            // --- 3. Crear copias (método separado) ---
            crearCopias(conn, idEjemplar, tipoDocumento, cantidadCopias);

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(EjemplarModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            Logger.getLogger(EjemplarModel.class.getName()).log(Level.SEVERE, "Error al registrar ejemplar desde JSON", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(EjemplarModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }

    // Método para editar un ejemplar existente (sin tocar copias)
    public boolean editarEjemplarDesdeJSON(int idEjemplar, JSONObject data) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // --- 1. Actualizar datos generales del ejemplar ---
            String titulo = (String) data.get("titulo");
            Long idAutorLong = (Long) data.get("id_autor");
            Integer idAutor = idAutorLong != null ? idAutorLong.intValue() : null;
            String ubicacion = (String) data.get("ubicacion");
            String tipoDocumento = (String) data.get("tipo_documento");

            String sqlEjemplar = "UPDATE Ejemplares SET titulo = ?, id_autor = ?, ubicacion = ?, tipo_documento = ? WHERE id_ejemplar = ?";
            PreparedStatement psEjemplar = conn.prepareStatement(sqlEjemplar);
            psEjemplar.setString(1, titulo);
            psEjemplar.setObject(2, idAutor);
            psEjemplar.setString(3, ubicacion);
            psEjemplar.setString(4, tipoDocumento);
            psEjemplar.setInt(5, idEjemplar);
            int filas = psEjemplar.executeUpdate();
            psEjemplar.close();

            if (filas == 0) {
                throw new SQLException("No se encontró el ejemplar con ID: " + idEjemplar);
            }

            // --- 2. Eliminar registro de tipo específico y volver a insertar ---
            eliminarSubtipo(conn, tipoDocumento, idEjemplar);
            insertarSubtipoDesdeJSON(conn, tipoDocumento, idEjemplar, data);

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(EjemplarModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            Logger.getLogger(EjemplarModel.class.getName()).log(Level.SEVERE, "Error al editar ejemplar desde JSON", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(EjemplarModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }

    // ------------------ Métodos públicos reutilizables ------------------
    public boolean crearCopiasParaEjemplar(int idEjemplar, String tipoDocumento, int cantidad) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            crearCopias(conn, idEjemplar, tipoDocumento, cantidad);
            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(EjemplarModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            Logger.getLogger(EjemplarModel.class.getName()).log(Level.SEVERE, "Error al crear copias adicionales", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(EjemplarModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }

    // ------------------ Helpers ------------------

    private void insertarSubtipoDesdeJSON(Connection conn, String tipoDocumento, int idEjemplar, JSONObject data) throws SQLException {
        if ("Libro".equals(tipoDocumento)) {
            String isbn = (String) data.get("isbn");
            Long idEditorialLong = (Long) data.get("id_editorial");
            Integer idEditorial = idEditorialLong != null ? idEditorialLong.intValue() : null;
            Long idGeneroLong = (Long) data.get("id_genero");
            Integer idGenero = idGeneroLong != null ? idGeneroLong.intValue() : null;
            Long edicionLong = (Long) data.get("edicion");
            Integer edicion = edicionLong != null ? edicionLong.intValue() : null;

            String sql = "INSERT INTO Libros (id_ejemplar, isbn, id_editorial, id_genero, edicion) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, isbn);
                ps.setObject(3, idEditorial);
                ps.setObject(4, idGenero);
                ps.setObject(5, edicion);
                ps.executeUpdate();
            }
        } else if ("Diccionario".equals(tipoDocumento)) {
            String idioma = (String) data.get("idioma");
            Long volumenLong = (Long) data.get("volumen");
            Integer volumen = volumenLong != null ? volumenLong.intValue() : null;

            String sql = "INSERT INTO Diccionarios (id_ejemplar, idioma, volumen) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, idioma);
                ps.setObject(3, volumen);
                ps.executeUpdate();
            }
        } else if ("Mapas".equals(tipoDocumento)) {
            String escala = (String) data.get("escala");
            String tipoMapa = (String) data.get("tipo_mapa");

            String sql = "INSERT INTO Mapas (id_ejemplar, escala, tipo_mapa) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, escala);
                ps.setString(3, tipoMapa);
                ps.executeUpdate();
            }
        } else if ("Tesis".equals(tipoDocumento)) {
            String grado = (String) data.get("grado_academico");
            String facultad = (String) data.get("facultad");

            String sql = "INSERT INTO Tesis (id_ejemplar, grado_academico, facultad) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, grado);
                ps.setString(3, facultad);
                ps.executeUpdate();
            }
        } else if ("DVD".equals(tipoDocumento) || "VHS".equals(tipoDocumento) || "CD".equals(tipoDocumento)) {
            String duracionStr = (String) data.get("duracion");
            Time duracion = duracionStr != null ? Time.valueOf(duracionStr) : null;
            Long idGeneroLong = (Long) data.get("id_genero");
            Integer idGenero = idGeneroLong != null ? idGeneroLong.intValue() : null;

            String tabla = "DVD".equals(tipoDocumento) ? "DVDs" : "VHS".equals(tipoDocumento) ? "VHS" : "CDs";
            String sql = "INSERT INTO " + tabla + " (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setTime(2, duracion);
                ps.setObject(3, idGenero);
                ps.executeUpdate();
            }
        } else if ("Cassettes".equals(tipoDocumento)) {
            String duracionStr = (String) data.get("duracion");
            Time duracion = duracionStr != null ? Time.valueOf(duracionStr) : null;
            Long idTipoCintaLong = (Long) data.get("id_tipo_cinta");
            Integer idTipoCinta = idTipoCintaLong != null ? idTipoCintaLong.intValue() : null;

            String sql = "INSERT INTO Cassettes (id_ejemplar, duracion, id_tipo_cinta) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setTime(2, duracion);
                ps.setObject(3, idTipoCinta);
                ps.executeUpdate();
            }
        } else if ("Documento".equals(tipoDocumento)) {
            Long idTipoDetalleLong = (Long) data.get("id_tipo_detalle");
            Integer idTipoDetalle = idTipoDetalleLong != null ? idTipoDetalleLong.intValue() : null;

            String sql = "INSERT INTO Documentos (id_ejemplar, id_tipo_detalle) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setObject(2, idTipoDetalle);
                ps.executeUpdate();
            }
        } else if ("Periodicos".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            Long idTipoPeriodicoLong = (Long) data.get("id_tipo_periodico");
            Integer idTipoPeriodico = idTipoPeriodicoLong != null ? idTipoPeriodicoLong.intValue() : null;

            String sql = "INSERT INTO Periodicos (id_ejemplar, fecha_publicacion, id_tipo_periodico) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setDate(2, fechaPub);
                ps.setObject(3, idTipoPeriodico);
                ps.executeUpdate();
            }
        } else if ("Revistas".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            Long idTipoRevistaLong = (Long) data.get("id_tipo_revista");
            Integer idTipoRevista = idTipoRevistaLong != null ? idTipoRevistaLong.intValue() : null;
            Long idGeneroLong = (Long) data.get("id_genero");
            Integer idGenero = idGeneroLong != null ? idGeneroLong.intValue() : null;

            String sql = "INSERT INTO Revistas (id_ejemplar, fecha_publicacion, id_tipo_revista, id_genero) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setDate(2, fechaPub);
                ps.setObject(3, idTipoRevista);
                ps.setObject(4, idGenero);
                ps.executeUpdate();
            }
        }
    }

    // Eliminar registro de tabla específica
    private void eliminarSubtipo(Connection conn, String tipoDocumento, int idEjemplar) throws SQLException {
        String tabla = null;
        if ("Libro".equals(tipoDocumento)) tabla = "Libros";
        else if ("Diccionario".equals(tipoDocumento)) tabla = "Diccionarios";
        else if ("Mapas".equals(tipoDocumento)) tabla = "Mapas";
        else if ("Tesis".equals(tipoDocumento)) tabla = "Tesis";
        else if ("DVD".equals(tipoDocumento)) tabla = "DVDs";
        else if ("VHS".equals(tipoDocumento)) tabla = "VHS";
        else if ("Cassettes".equals(tipoDocumento)) tabla = "Cassettes";
        else if ("CD".equals(tipoDocumento)) tabla = "CDs";
        else if ("Documento".equals(tipoDocumento)) tabla = "Documentos";
        else if ("Periodicos".equals(tipoDocumento)) tabla = "Periodicos";
        else if ("Revistas".equals(tipoDocumento)) tabla = "Revistas";

        if (tabla != null) {
            String sql = "DELETE FROM " + tabla + " WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.executeUpdate();
            }
        }
    }

    // Método privado reutilizable para crear copias
    private void crearCopias(Connection conn, int idEjemplar, String tipoDocumento, int cantidad) throws SQLException {
        String prefijo = obtenerPrefijo(tipoDocumento);
        for (int i = 1; i <= cantidad; i++) {
            int nextNum = obtenerSiguienteNumeroSecuencial(conn, prefijo);
            String codigoUnico = String.format("%s%05d", prefijo, nextNum);

            String sqlCopia = "INSERT INTO Copias (id_ejemplar, codigo_unico, estado) VALUES (?, ?, 'Disponible')";
            try (PreparedStatement psCopia = conn.prepareStatement(sqlCopia)) {
                psCopia.setInt(1, idEjemplar);
                psCopia.setString(2, codigoUnico);
                psCopia.executeUpdate();
            }
        }
    }

    private String obtenerPrefijo(String tipoDocumento) {
        if ("Libro".equals(tipoDocumento)) return "LIB";
        else if ("Revistas".equals(tipoDocumento)) return "REV";
        else if ("CD".equals(tipoDocumento)) return "CDA";
        else if ("DVD".equals(tipoDocumento)) return "DVD";
        else if ("Diccionario".equals(tipoDocumento)) return "DIC";
        else if ("Mapas".equals(tipoDocumento)) return "MAP";
        else if ("Tesis".equals(tipoDocumento)) return "TES";
        else if ("VHS".equals(tipoDocumento)) return "VHS";
        else if ("Cassettes".equals(tipoDocumento)) return "CAS";
        else if ("Documento".equals(tipoDocumento)) return "DOC";
        else if ("Periodicos".equals(tipoDocumento)) return "PER";
        else return "UNK";
    }

    private int obtenerSiguienteNumeroSecuencial(Connection conn, String prefijo) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Copias WHERE codigo_unico LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefijo + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") + 1;
            }
        }
        return 1;
    }
}