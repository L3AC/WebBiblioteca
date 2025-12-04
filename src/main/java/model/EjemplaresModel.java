package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class EjemplaresModel extends Conexion {

    public boolean registrarEjemplarDesdeJSON(JSONObject data) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // --- 1. Extraer datos generales del ejemplar ---
            String titulo = (String) data.get("titulo");
            Long idAutorLong = obtenerLong(data, "id_autor"); // Usar obtenerLong para consistencia
            Integer idAutor = idAutorLong != null ? idAutorLong.intValue() : null;
            String ubicacion = (String) data.get("ubicacion");
            String tipoDocumento = (String) data.get("tipo_documento");
            Long cantCopiasLong = obtenerLong(data, "cantidad_copias");
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
                    System.err.println("Error al hacer rollback en registrarEjemplarDesdeJSON(JSONObject): " + ex.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            System.err.println("Error al registrar ejemplar desde JSON (JSONObject): " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al registrar ejemplar desde JSON", e);
            return false;
        } finally {
            // Manejar el cierre de la conexión con más cuidado
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error al restaurar auto-commit en registrarEjemplarDesdeJSON(JSONObject): " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al restaurar auto-commit", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión en registrarEjemplarDesdeJSON(JSONObject): " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                    // IMPORTANTE: No se devuelve false aquí
                }
            }
        }
    }

    public boolean editarEjemplarDesdeJSON(
            int idEjemplar,
            JSONObject data,
            Long idAutor,
            Long idEditorial, // Para Libros
            Long idGenero,
            Long idTipoDetalle,
            Long idTipoPeriodico,
            Long idTipoRevista,
            Long idTipoCinta,
            Long edicion,
            Long volumen,
            Long duracion, // En minutos, se convierte a TIME
            Long anio,
            Long numero,
            String isbn,
            String idioma,
            String escala,
            String tipoMapa,
            String gradoAcademico,
            String facultad,
            String duracionString, // Formato HH:MM:SS
            Date fechaPublicacion
    // Quitar idEditorialDiccionario
    ) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // --- 1. Actualizar datos generales del ejemplar ---
            String titulo = (String) data.get("titulo");
            String ubicacion = (String) data.get("ubicacion");
            String tipoDocumento = (String) data.get("tipo_documento");

            String sqlEjemplar = "UPDATE Ejemplares SET titulo = ?, id_autor = ?, ubicacion = ?, tipo_documento = ? WHERE id_ejemplar = ?";
            try (PreparedStatement psEjemplar = conn.prepareStatement(sqlEjemplar)) {
                psEjemplar.setString(1, titulo);
                psEjemplar.setLong(2, idAutor); // Ya validado como Long
                psEjemplar.setString(3, ubicacion);
                psEjemplar.setString(4, tipoDocumento);
                psEjemplar.setInt(5, idEjemplar);
                int filas = psEjemplar.executeUpdate();

                if (filas == 0) {
                    throw new SQLException("No se encontró el ejemplar con ID: " + idEjemplar);
                }
            }

            // --- 2. Actualizar datos específicos según tipo de documento ---
            actualizarSubtipoDesdeJSON(
                    conn,
                    tipoDocumento,
                    idEjemplar,
                    idEditorial, idGenero, idTipoDetalle, idTipoPeriodico, idTipoRevista, idTipoCinta,
                    edicion, volumen, duracion, anio, numero, isbn, idioma, escala, tipoMapa, gradoAcademico,
                    facultad, duracionString, fechaPublicacion
            );

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback en editarEjemplarDesdeJSON: " + ex.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            System.err.println("Error al editar ejemplar desde JSON: " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al editar ejemplar desde JSON", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error al restaurar auto-commit: " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al restaurar auto-commit", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }

    private void actualizarSubtipoDesdeJSON(
            Connection conn,
            String tipoDocumento,
            int idEjemplar,
            Long idEditorial, // Solo para Libros
            Long idGenero,
            Long idTipoDetalle,
            Long idTipoPeriodico,
            Long idTipoRevista,
            Long idTipoCinta,
            Long edicion,
            Long volumen,
            Long duracion, // En minutos
            Long anio,
            Long numero,
            String isbn,
            String idioma,
            String escala,
            String tipoMapa,
            String gradoAcademico,
            String facultad,
            String duracionString, // HH:MM:SS
            Date fechaPublicacion
    // Quitar idEditorialDiccionario
    ) throws SQLException {

        // Convertir duracion de Long (minutos) a java.sql.Time si es necesario
        Time duracionTime = null;
        if (duracion != null) {
            try {
                int minutos = duracion.intValue();
                int horas = minutos / 60;
                int minutosResto = minutos % 60;
                duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
            } catch (IllegalArgumentException e) {
                System.err.println("Error al convertir duración Long a Time: " + duracion + " minutos. Error: " + e.getMessage());
            }
        }

        // Convertir duracionString (HH:MM:SS) a java.sql.Time si es necesario
        Time duracionTimeString = null;
        if (duracionString != null && !duracionString.trim().isEmpty()) {
            try {
                duracionTimeString = Time.valueOf(duracionString);
            } catch (IllegalArgumentException e) {
                System.err.println("Error al convertir duración String a Time: " + duracionString + ". Error: " + e.getMessage());
            }
        }

        if ("Libro".equals(tipoDocumento)) {
            String sql = "UPDATE Libros SET isbn = ?, id_editorial = ?, id_genero = ?, edicion = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, isbn);
                ps.setObject(2, idEditorial); // <-- Este es el idEditorial original
                ps.setObject(3, idGenero);
                ps.setObject(4, edicion);
                ps.setInt(5, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Libros (id_ejemplar, isbn, id_editorial, id_genero, edicion) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, isbn);
                        psIns.setObject(3, idEditorial); // <-- Este es el idEditorial original
                        psIns.setObject(4, idGenero);
                        psIns.setObject(5, edicion);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Diccionario".equals(tipoDocumento)) {
            // Diccionario NO tiene id_editorial
            String sql = "UPDATE Diccionarios SET isbn = ?, idioma = ?, volumen = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, isbn);
                ps.setString(2, idioma); // <-- Quitar idEditorial
                ps.setObject(3, volumen);
                ps.setInt(4, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Diccionarios (id_ejemplar, isbn, idioma, volumen) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, isbn);
                        psIns.setString(3, idioma); // <-- Quitar idEditorial
                        psIns.setObject(4, volumen);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Mapas".equals(tipoDocumento)) {
            String sql = "UPDATE Mapas SET escala = ?, tipo_mapa = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, escala);
                ps.setString(2, tipoMapa);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Mapas (id_ejemplar, escala, tipo_mapa) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, escala);
                        psIns.setString(3, tipoMapa);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Tesis".equals(tipoDocumento)) {
            String sql = "UPDATE Tesis SET grado_academico = ?, facultad = ?, anio = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, gradoAcademico);
                ps.setString(2, facultad);
                ps.setObject(3, anio);
                ps.setInt(4, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Tesis (id_ejemplar, grado_academico, facultad, anio) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, gradoAcademico);
                        psIns.setString(3, facultad);
                        psIns.setObject(4, anio);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("DVD".equals(tipoDocumento)) {
            String sql = "UPDATE DVDs SET duracion = ?, id_genero = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTime(1, duracionTimeString);
                ps.setObject(2, idGenero);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO DVDs (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setTime(2, duracionTimeString);
                        psIns.setObject(3, idGenero);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("VHS".equals(tipoDocumento)) {
            String sql = "UPDATE VHS SET duracion = ?, id_genero = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTime(1, duracionTimeString);
                ps.setObject(2, idGenero);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO VHS (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setTime(2, duracionTimeString);
                        psIns.setObject(3, idGenero);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("CD".equals(tipoDocumento)) {
            String sql = "UPDATE CDs SET duracion = ?, id_genero = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTime(1, duracionTimeString);
                ps.setObject(2, idGenero);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO CDs (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setTime(2, duracionTimeString);
                        psIns.setObject(3, idGenero);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Cassettes".equals(tipoDocumento)) {
            String sql = "UPDATE Cassettes SET duracion = ?, id_tipo_cinta = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTime(1, duracionTimeString);
                ps.setObject(2, idTipoCinta);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Cassettes (id_ejemplar, duracion, id_tipo_cinta) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setTime(2, duracionTimeString);
                        psIns.setObject(3, idTipoCinta);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Documento".equals(tipoDocumento)) {
            String sql = "UPDATE Documentos SET id_tipo_detalle = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, idTipoDetalle);
                ps.setInt(2, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Documentos (id_ejemplar, id_tipo_detalle) VALUES (?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setObject(2, idTipoDetalle);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Periodicos".equals(tipoDocumento)) {
            String sql = "UPDATE Periodicos SET fecha_publicacion = ?, id_tipo_periodico = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, fechaPublicacion);
                ps.setObject(2, idTipoPeriodico);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Periodicos (id_ejemplar, fecha_publicacion, id_tipo_periodico) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setDate(2, fechaPublicacion);
                        psIns.setObject(3, idTipoPeriodico);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Revistas".equals(tipoDocumento)) {
            String sql = "UPDATE Revistas SET fecha_publicacion = ?, id_tipo_revista = ?, id_genero = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, fechaPublicacion);
                ps.setObject(2, idTipoRevista);
                ps.setObject(3, idGenero);
                ps.setInt(4, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Revistas (id_ejemplar, fecha_publicacion, id_tipo_revista, id_genero) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setDate(2, fechaPublicacion);
                        psIns.setObject(3, idTipoRevista);
                        psIns.setObject(4, idGenero);
                        psIns.executeUpdate();
                    }
                }
            }
        }
    }
// Nuevo método para actualizar datos específicos en la subtabla correcta desde JSONObject

    private void actualizarSubtipoDesdeJSON(Connection conn, String tipoDocumento, int idEjemplar, JSONObject data) throws SQLException {
        if ("Libro".equals(tipoDocumento)) {
            Long idEditorialLong = obtenerLong(data, "id_editorial");
            Long idGeneroLong = obtenerLong(data, "id_genero");
            Long edicionLong = obtenerLong(data, "edicion");
            String sql = "UPDATE Libros SET isbn = ?, id_editorial = ?, id_genero = ?, edicion = ? WHERE id_ejemplar = ?";
            String isbn = (String) data.get("isbn"); // Obtener el ISBN del JSON
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, isbn);
                ps.setObject(2, idEditorialLong);
                ps.setObject(3, idGeneroLong);
                ps.setObject(4, edicionLong);
                ps.setInt(5, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    // Si no existe un registro, lo insertamos
                    String insertSql = "INSERT INTO Libros (id_ejemplar, isbn, id_editorial, id_genero, edicion) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, isbn);
                        psIns.setObject(3, idEditorialLong);
                        psIns.setObject(4, idGeneroLong);
                        psIns.setObject(5, edicionLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Diccionario".equals(tipoDocumento)) {
            Long idEditorialLong = obtenerLong(data, "id_editorial");
            String idioma = (String) data.get("idioma");
            Long volumenLong = obtenerLong(data, "volumen");
            String sql = "UPDATE Diccionarios SET isbn = ?, id_editorial = ?, idioma = ?, volumen = ? WHERE id_ejemplar = ?";
            String isbn = (String) data.get("isbn");
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, isbn);
                ps.setObject(2, idEditorialLong);
                ps.setString(3, idioma);
                ps.setObject(4, volumenLong);
                ps.setInt(5, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Diccionarios (id_ejemplar, isbn, id_editorial, idioma, volumen) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, isbn);
                        psIns.setObject(3, idEditorialLong);
                        psIns.setString(4, idioma);
                        psIns.setObject(5, volumenLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Mapas".equals(tipoDocumento)) {
            String escala = (String) data.get("escala");
            String tipoMapa = (String) data.get("tipo_mapa");
            String sql = "UPDATE Mapas SET escala = ?, tipo_mapa = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, escala);
                ps.setString(2, tipoMapa);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Mapas (id_ejemplar, escala, tipo_mapa) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, escala);
                        psIns.setString(3, tipoMapa);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Tesis".equals(tipoDocumento)) {
            String grado_academico = (String) data.get("grado_academico");
            String facultad = (String) data.get("facultad");
            Long anioLong = obtenerLong(data, "anio");
            String sql = "UPDATE Tesis SET grado_academico = ?, facultad = ?, anio = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, grado_academico);
                ps.setString(2, facultad);
                ps.setObject(3, anioLong);
                ps.setInt(4, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Tesis (id_ejemplar, grado_academico, facultad, anio) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setString(2, grado_academico);
                        psIns.setString(3, facultad);
                        psIns.setObject(4, anioLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("DVD".equals(tipoDocumento) || "VHS".equals(tipoDocumento) || "CD".equals(tipoDocumento)) {
            Long duracionLong = obtenerLong(data, "duracion");
            Time duracionTime = null;
            if (duracionLong != null) {
                try {
                    int minutos = duracionLong.intValue();
                    int horas = minutos / 60;
                    int minutosResto = minutos % 60;
                    duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al convertir duración Long a Time para " + tipoDocumento + " desde JSONObject: " + duracionLong + " minutos. Error: " + e.getMessage());
                    // Puedes manejar el error como consideres, por ahora se inserta como NULL
                }
            }
            Long idGeneroLong = obtenerLong(data, "id_genero");
            String tabla = "DVD".equals(tipoDocumento) ? "DVDs" : "VHS".equals(tipoDocumento) ? "VHS" : "CDs";
            String sql = "UPDATE " + tabla + " SET duracion = ?, id_genero = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTime(1, duracionTime);
                ps.setObject(2, idGeneroLong);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO " + tabla + " (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setTime(2, duracionTime);
                        psIns.setObject(3, idGeneroLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Cassettes".equals(tipoDocumento)) {
            Long duracionLong = obtenerLong(data, "duracion");
            Time duracionTime = null;
            if (duracionLong != null) {
                try {
                    int minutos = duracionLong.intValue();
                    int horas = minutos / 60;
                    int minutosResto = minutos % 60;
                    duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al convertir duración Long a Time para Cassettes desde JSONObject: " + duracionLong + " minutos. Error: " + e.getMessage());
                    // Puedes manejar el error como consideres, por ahora se inserta como NULL
                }
            }
            Long idTipoCintaLong = obtenerLong(data, "id_tipo_cinta");
            String sql = "UPDATE Cassettes SET duracion = ?, id_tipo_cinta = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTime(1, duracionTime);
                ps.setObject(2, idTipoCintaLong);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Cassettes (id_ejemplar, duracion, id_tipo_cinta) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setTime(2, duracionTime);
                        psIns.setObject(3, idTipoCintaLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Documento".equals(tipoDocumento)) {
            Long idTipoDetalleLong = obtenerLong(data, "id_tipo_detalle");
            String sql = "UPDATE Documentos SET id_tipo_detalle = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, idTipoDetalleLong);
                ps.setInt(2, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Documentos (id_ejemplar, id_tipo_detalle) VALUES (?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setObject(2, idTipoDetalleLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Periodicos".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            Long idTipoPeriodicoLong = obtenerLong(data, "id_tipo_periodico");
            String sql = "UPDATE Periodicos SET fecha_publicacion = ?, id_tipo_periodico = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, fechaPub);
                ps.setObject(2, idTipoPeriodicoLong);
                ps.setInt(3, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Periodicos (id_ejemplar, fecha_publicacion, id_tipo_periodico) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setDate(2, fechaPub);
                        psIns.setObject(3, idTipoPeriodicoLong);
                        psIns.executeUpdate();
                    }
                }
            }
        } else if ("Revistas".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            Long idTipoRevistaLong = obtenerLong(data, "id_tipo_revista");
            Long idGeneroLong = obtenerLong(data, "id_genero");
            String sql = "UPDATE Revistas SET fecha_publicacion = ?, id_tipo_revista = ?, id_genero = ? WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, fechaPub);
                ps.setObject(2, idTipoRevistaLong);
                ps.setObject(3, idGeneroLong);
                ps.setInt(4, idEjemplar);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    String insertSql = "INSERT INTO Revistas (id_ejemplar, fecha_publicacion, id_tipo_revista, id_genero) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setInt(1, idEjemplar);
                        psIns.setDate(2, fechaPub);
                        psIns.setObject(3, idTipoRevistaLong);
                        psIns.setObject(4, idGeneroLong);
                        psIns.executeUpdate();
                    }
                }
            }
        }
    }

    public boolean eliminarEjemplar(int idEjemplar) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // La eliminación en cascada está definida en la base de datos con ON DELETE CASCADE
            String sql = "DELETE FROM Ejemplares WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                int filas = ps.executeUpdate();

                if (filas == 0) {
                    throw new SQLException("No se encontró el ejemplar con ID: " + idEjemplar);
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback en eliminarEjemplar: " + ex.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            System.err.println("Error al eliminar ejemplar: " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al eliminar ejemplar", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }
     public boolean eliminarCopia(int idCopia) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String sql = "DELETE FROM Copias WHERE id_copia = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idCopia);
                int filas = ps.executeUpdate();

                if (filas == 0) {
                    throw new SQLException("No se encontró el ejemplar con ID: " + idCopia);
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback en eliminarCopia: " + ex.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            System.err.println("Error al eliminar ejemplar: " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al eliminar ejemplar", e);
            return false;
        }
    }

// Método para editar un ejemplar existente (actualizado para manejar subtablas correctamente)
    public boolean editarEjemplarDesdeJSON(int idEjemplar, JSONObject data) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // --- 1. Actualizar datos generales del ejemplar ---
            String titulo = (String) data.get("titulo");
            Long idAutorLong = obtenerLong(data, "id_autor");
            Integer idAutor = idAutorLong != null ? idAutorLong.intValue() : null;
            String ubicacion = (String) data.get("ubicacion");
            String tipoDocumento = (String) data.get("tipo_documento");

            String sqlEjemplar = "UPDATE Ejemplares SET titulo = ?, id_autor = ?, ubicacion = ?, tipo_documento = ? WHERE id_ejemplar = ?";
            try (PreparedStatement psEjemplar = conn.prepareStatement(sqlEjemplar)) {
                psEjemplar.setString(1, titulo);
                psEjemplar.setObject(2, idAutor);
                psEjemplar.setString(3, ubicacion);
                psEjemplar.setString(4, tipoDocumento);
                psEjemplar.setInt(5, idEjemplar);
                int filas = psEjemplar.executeUpdate();

                if (filas == 0) {
                    throw new SQLException("No se encontró el ejemplar con ID: " + idEjemplar);
                }
            }

            // --- 2. Actualizar datos específicos según tipo de documento ---
            // Dado que asumimos que el tipo_documento NO cambia, simplemente actualizamos el registro existente
            actualizarSubtipoDesdeJSON(conn, tipoDocumento, idEjemplar, data);

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback en editarEjemplarDesdeJSON(JSONObject): " + ex.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            System.err.println("Error al editar ejemplar desde JSON (JSONObject): " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al editar ejemplar desde JSON", e);
            return false;
        } finally {
            // Manejar el cierre de la conexión con más cuidado
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error al restaurar auto-commit en editarEjemplarDesdeJSON(JSONObject): " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al restaurar auto-commit", e);
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión en editarEjemplarDesdeJSON(JSONObject): " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                    // IMPORTANTE: No se devuelve false aquí
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
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al crear copias adicionales", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }

    // === LISTAR TODOS LOS EJEMPLARES (como lista de JSON) ===
    public List<JSONObject> listarEjemplares() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT e.id_ejemplar, e.titulo, e.tipo_documento, e.ubicacion, a.nombre_autor
            FROM Ejemplares e
            LEFT JOIN Autores a ON e.id_autor = a.id_autor
            ORDER BY e.id_ejemplar
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject ejemplar = new JSONObject();
                ejemplar.put("id_ejemplar", rs.getInt("id_ejemplar"));
                ejemplar.put("titulo", rs.getString("titulo"));
                ejemplar.put("tipo_documento", rs.getString("tipo_documento"));
                ejemplar.put("ubicacion", rs.getString("ubicacion"));
                ejemplar.put("nombre_autor", rs.getString("nombre_autor")); // Puede ser null si no hay autor

                lista.add(ejemplar);
            }

        } catch (SQLException e) {
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al listar ejemplares", e);
        }
        return lista;
    }

    // ------------------ Helpers ------------------
    public boolean registrarEjemplarDesdeJSON(
            JSONObject data,
            Long idAutor,
            Long idEditorial,
            Long idGenero,
            Long idTipoDetalle,
            Long idTipoPeriodico,
            Long idTipoRevista,
            Long idTipoCinta,
            Long cantidadCopias,
            Long edicion,
            Long volumen,
            Long duracion,
            Long anio,
            Long numero
    ) {
        Connection conn = null;
        try {
            conn = utils.ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Insertar en Ejemplares
            String sqlEjemplar = "INSERT INTO Ejemplares (titulo, id_autor, ubicacion, tipo_documento) VALUES (?, ?, ?, ?)";
            int idEjemplar;
            try (PreparedStatement psEjemplar = conn.prepareStatement(sqlEjemplar, Statement.RETURN_GENERATED_KEYS)) {
                psEjemplar.setString(1, (String) data.get("titulo"));
                psEjemplar.setLong(2, idAutor);
                psEjemplar.setString(3, (String) data.get("ubicacion"));
                psEjemplar.setString(4, (String) data.get("tipo_documento"));
                psEjemplar.executeUpdate();

                try (ResultSet rs = psEjemplar.getGeneratedKeys()) {
                    if (rs.next()) {
                        idEjemplar = rs.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del ejemplar recién insertado");
                    }
                }
            }

            // Insertar en la tabla específica según el tipo de documento
            insertarSubtipoDesdeJSON(
                    conn,
                    (String) data.get("tipo_documento"),
                    idEjemplar,
                    data,
                    idEditorial,
                    idGenero,
                    idTipoDetalle,
                    idTipoPeriodico,
                    idTipoRevista,
                    idTipoCinta,
                    edicion,
                    volumen,
                    duracion, // <--- PASAR EL VALOR 'duracion' AQUÍ
                    anio,
                    numero
            );

            // Crear las copias según la cantidad especificada
            crearCopias(conn, idEjemplar, (String) data.get("tipo_documento"), cantidadCopias.intValue());

            conn.commit(); // Confirmar transacción
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir transacción en caso de error
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                    // Loggear este error rollback, pero no detener el flujo principal
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al hacer rollback después de una excepción", ex);
                }
            }
            System.err.println("Error al registrar ejemplar: " + e.getMessage());
            e.printStackTrace(); // Muy útil para debugging
            return false;
        } finally {
            // Manejar el cierre de la conexión con más cuidado
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar auto-commit
                } catch (SQLException e) {
                    System.err.println("Error al restaurar auto-commit: " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al restaurar auto-commit", e);
                    // No se detiene la ejecución, solo se loggea
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión: " + e.getMessage());
                    Logger.getLogger(EjemplaresModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                    // No se detiene la ejecución, solo se loggea
                    // IMPORTANTE: No se devuelve false aquí, porque la operación principal (commit) ya se hizo.
                }
            }
        }
    }

    // Actualiza tu método insertarSubtipoDesdeJSON para recibir todos los parámetros
    // Agrega esta versión del método (antes de los otros métodos auxiliares)
    private void insertarSubtipoDesdeJSON(Connection conn, String tipoDocumento, int idEjemplar, JSONObject data) throws SQLException {
        if ("Libro".equals(tipoDocumento)) {
            String isbn = (String) data.get("isbn");
            Long idEditorialLong = obtenerLong(data, "id_editorial");
            Long idGeneroLong = obtenerLong(data, "id_genero");
            Long edicionLong = obtenerLong(data, "edicion");
            String sql = "INSERT INTO Libros (id_ejemplar, isbn, id_editorial, id_genero, edicion) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, isbn);
                // Usar setLong o setObject directamente con el Long, no convertir a int
                ps.setObject(3, idEditorialLong); // Puede ser null, setObject lo maneja
                ps.setObject(4, idGeneroLong);
                ps.setObject(5, edicionLong);
                ps.executeUpdate();
            }
        } else if ("Diccionario".equals(tipoDocumento)) {
            String isbn = (String) data.get("isbn");
            String idioma = (String) data.get("idioma");
            Long idEditorialLong = obtenerLong(data, "id_editorial");
            Long volumenLong = obtenerLong(data, "volumen");
            String sql = "INSERT INTO Diccionarios (id_ejemplar, isbn, id_editorial, idioma, volumen) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, isbn);
                ps.setObject(3, idEditorialLong);
                ps.setString(4, idioma);
                ps.setObject(5, volumenLong);
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
            String institucion = (String) data.get("institucion");
            String director = (String) data.get("director");
            Long anioLong = obtenerLong(data, "anio");
            String sql = "INSERT INTO Tesis (id_ejemplar, institucion, director, anio) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, institucion);
                ps.setString(3, director);
                ps.setObject(4, anioLong); // Puede ser null
                ps.executeUpdate();
            }
        } else if ("DVD".equals(tipoDocumento) || "VHS".equals(tipoDocumento) || "CD".equals(tipoDocumento)) {
            // <--- CORREGIDO: OBTENER 'duracion' DEL JSONOBJECT Y CONVERTIRLO A TIME AQUÍ
            Long duracionLong = obtenerLong(data, "duracion"); // Obtiene el número de minutos como Long
            Time duracionTime = null;
            if (duracionLong != null) { // Verificar si el Long es null
                try {
                    int minutos = duracionLong.intValue(); // Convertir Long a int para cálculos
                    int horas = minutos / 60;
                    int minutosResto = minutos % 60;
                    // Formatear a HH:mm:ss para Time.valueOf
                    duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al convertir duración Long a Time para " + tipoDocumento + " desde JSONObject: " + duracionLong + " minutos. Error: " + e.getMessage());
                    // Opcional: lanzar una SQLException si la duración es inválida
                    // throw new SQLException("Duración inválida: " + duracionLong, e);
                    // Por ahora, se inserta como NULL si no se puede convertir
                }
            }

            Long idGeneroLong = obtenerLong(data, "id_genero");
            String tabla = "DVD".equals(tipoDocumento) ? "DVDs" : "VHS".equals(tipoDocumento) ? "VHS" : "CDs";
            String sql = "INSERT INTO " + tabla + " (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setTime(2, duracionTime); // Usar el Time calculado
                ps.setObject(3, idGeneroLong); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Cassettes".equals(tipoDocumento)) {
            // <--- CORREGIDO: OBTENER 'duracion' DEL JSONOBJECT Y CONVERTIRLO A TIME AQUÍ
            Long duracionLong = obtenerLong(data, "duracion"); // Obtiene el número de minutos como Long
            Time duracionTime = null;
            if (duracionLong != null) { // Verificar si el Long es null
                try {
                    int minutos = duracionLong.intValue(); // Convertir Long a int para cálculos
                    int horas = minutos / 60;
                    int minutosResto = minutos % 60;
                    // Formatear a HH:mm:ss para Time.valueOf
                    duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al convertir duración Long a Time para Cassettes desde JSONObject: " + duracionLong + " minutos. Error: " + e.getMessage());
                    // Opcional: lanzar una SQLException si la duración es inválida
                    // throw new SQLException("Duración inválida: " + duracionLong, e);
                    // Por ahora, se inserta como NULL si no se puede convertir
                }
            }

            Long idTipoCintaLong = obtenerLong(data, "id_tipo_cinta");
            String sql = "INSERT INTO Cassettes (id_ejemplar, duracion, id_tipo_cinta) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setTime(2, duracionTime); // Usar el Time calculado
                ps.setObject(3, idTipoCintaLong); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Documento".equals(tipoDocumento)) {
            Long idTipoDetalleLong = obtenerLong(data, "id_tipo_detalle");
            String sql = "INSERT INTO Documentos (id_ejemplar, id_tipo_detalle) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setObject(2, idTipoDetalleLong); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Periodicos".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            Long idTipoPeriodicoLong = obtenerLong(data, "id_tipo_periodico");
            String sql = "INSERT INTO Periodicos (id_ejemplar, fecha_publicacion, id_tipo_periodico) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setDate(2, fechaPub);
                ps.setObject(3, idTipoPeriodicoLong); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Revistas".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            Long idTipoRevistaLong = obtenerLong(data, "id_tipo_revista");
            Long idGeneroLong = obtenerLong(data, "id_genero");
            String sql = "INSERT INTO Revistas (id_ejemplar, fecha_publicacion, id_tipo_revista, id_genero) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setDate(2, fechaPub);
                ps.setObject(3, idTipoRevistaLong); // Usar el Long directamente
                ps.setObject(4, idGeneroLong); // Usar el Long directamente
                ps.executeUpdate();
            }
        }
    }

    // Método auxiliar para obtener Long de forma segura
    private Long obtenerLong(JSONObject data, String key) {
        Object obj = data.get(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return null;
    }
    // Agrega esta versión del método (después de la versión de 4 parámetros)

    // Agrega esta versión del método (después de la versión de 4 parámetros)
    // Agrega esta versión del método (después de la versión de 4 parámetros)
    private void insertarSubtipoDesdeJSON(
            Connection conn,
            String tipoDocumento,
            int idEjemplar,
            JSONObject data, // Aún se puede usar para otros campos si es necesario
            Long idEditorial,
            Long idGenero,
            Long idTipoDetalle,
            Long idTipoPeriodico,
            Long idTipoRevista,
            Long idTipoCinta,
            Long edicion,
            Long volumen,
            Long duracion, // <--- El valor LONG ya validado
            Long anio,
            Long numero
    ) throws SQLException {
        if ("Libro".equals(tipoDocumento)) {
            String isbn = (String) data.get("isbn");
            String sql = "INSERT INTO Libros (id_ejemplar, isbn, id_editorial, id_genero, edicion) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, isbn);
                // Usar setLong o setObject directamente con el Long, no convertir a int
                ps.setObject(3, idEditorial); // Puede ser null, setObject lo maneja
                ps.setObject(4, idGenero);
                ps.setObject(5, edicion);
                ps.executeUpdate();
            }
        } else if ("Diccionario".equals(tipoDocumento)) {
            String isbn = (String) data.get("isbn");
            String idioma = (String) data.get("idioma");
            String sql = "INSERT INTO Diccionarios (id_ejemplar, isbn, id_editorial, idioma, volumen) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, isbn);
                ps.setObject(3, idEditorial);
                ps.setString(4, idioma);
                ps.setObject(5, volumen);
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
            String institucion = (String) data.get("institucion");
            String director = (String) data.get("director");
            String sql = "INSERT INTO Tesis (id_ejemplar, institucion, director, anio) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setString(2, institucion);
                ps.setString(3, director);
                ps.setObject(4, anio); // Puede ser null
                ps.executeUpdate();
            }
        } else if ("DVD".equals(tipoDocumento) || "VHS".equals(tipoDocumento) || "CD".equals(tipoDocumento)) {
            // <--- CORREGIDO: USAR EL PARÁMETRO 'duracion' LONG Y CONVERTIRLO A TIME AQUÍ
            Time duracionTime = null;
            if (duracion != null) { // Verificar si el Long es null
                try {
                    int minutos = duracion.intValue(); // Convertir Long a int para cálculos
                    int horas = minutos / 60;
                    int minutosResto = minutos % 60;
                    // Formatear a HH:mm:ss para Time.valueOf
                    duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al convertir duración Long a Time para " + tipoDocumento + ": " + duracion + " minutos. Error: " + e.getMessage());
                    // Opcional: lanzar una SQLException si la duración es inválida
                    // throw new SQLException("Duración inválida: " + duracion, e);
                    // Por ahora, se inserta como NULL si no se puede convertir
                }
            }

            String tabla = "DVD".equals(tipoDocumento) ? "DVDs" : "VHS".equals(tipoDocumento) ? "VHS" : "CDs";
            String sql = "INSERT INTO " + tabla + " (id_ejemplar, duracion, id_genero) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setTime(2, duracionTime); // Usar el Time calculado
                ps.setObject(3, idGenero); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Cassettes".equals(tipoDocumento)) {
            // <--- CORREGIDO: USAR EL PARÁMETRO 'duracion' LONG Y CONVERTIRLO A TIME AQUÍ
            Time duracionTime = null;
            if (duracion != null) { // Verificar si el Long es null
                try {
                    int minutos = duracion.intValue(); // Convertir Long a int para cálculos
                    int horas = minutos / 60;
                    int minutosResto = minutos % 60;
                    // Formatear a HH:mm:ss para Time.valueOf
                    duracionTime = Time.valueOf(String.format("%02d:%02d:00", horas, minutosResto));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error al convertir duración Long a Time para Cassettes: " + duracion + " minutos. Error: " + e.getMessage());
                    // Opcional: lanzar una SQLException si la duración es inválida
                    // throw new SQLException("Duración inválida: " + duracion, e);
                    // Por ahora, se inserta como NULL si no se puede convertir
                }
            }

            String sql = "INSERT INTO Cassettes (id_ejemplar, duracion, id_tipo_cinta) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setTime(2, duracionTime); // Usar el Time calculado
                ps.setObject(3, idTipoCinta); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Documento".equals(tipoDocumento)) {
            String sql = "INSERT INTO Documentos (id_ejemplar, id_tipo_detalle) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setObject(2, idTipoDetalle); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Periodicos".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            String sql = "INSERT INTO Periodicos (id_ejemplar, fecha_publicacion, id_tipo_periodico) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setDate(2, fechaPub);
                ps.setObject(3, idTipoPeriodico); // Usar el Long directamente
                ps.executeUpdate();
            }
        } else if ("Revistas".equals(tipoDocumento)) {
            String fechaPubStr = (String) data.get("fecha_publicacion");
            Date fechaPub = fechaPubStr != null ? Date.valueOf(fechaPubStr) : null;
            String sql = "INSERT INTO Revistas (id_ejemplar, fecha_publicacion, id_tipo_revista, id_genero) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.setDate(2, fechaPub);
                ps.setObject(3, idTipoRevista); // Usar el Long directamente
                ps.setObject(4, idGenero); // Usar el Long directamente
                ps.executeUpdate();
            }
        }
    }

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

    // Eliminar registro de tabla específica
    private void eliminarSubtipo(Connection conn, String tipoDocumento, int idEjemplar) throws SQLException {
        String tabla = null;
        if ("Libro".equals(tipoDocumento)) {
            tabla = "Libros";
        } else if ("Diccionario".equals(tipoDocumento)) {
            tabla = "Diccionarios";
        } else if ("Mapas".equals(tipoDocumento)) {
            tabla = "Mapas";
        } else if ("Tesis".equals(tipoDocumento)) {
            tabla = "Tesis";
        } else if ("DVD".equals(tipoDocumento)) {
            tabla = "DVDs";
        } else if ("VHS".equals(tipoDocumento)) {
            tabla = "VHS";
        } else if ("Cassettes".equals(tipoDocumento)) {
            tabla = "Cassettes";
        } else if ("CD".equals(tipoDocumento)) {
            tabla = "CDs";
        } else if ("Documento".equals(tipoDocumento)) {
            tabla = "Documentos";
        } else if ("Periodicos".equals(tipoDocumento)) {
            tabla = "Periodicos";
        } else if ("Revistas".equals(tipoDocumento)) {
            tabla = "Revistas";
        }

        if (tabla != null) {
            String sql = "DELETE FROM " + tabla + " WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                ps.executeUpdate();
            }
        }
    }

    private String obtenerPrefijo(String tipoDocumento) {
        if ("Libro".equals(tipoDocumento)) {
            return "LIB";
        } else if ("Revistas".equals(tipoDocumento)) {
            return "REV";
        } else if ("CD".equals(tipoDocumento)) {
            return "CDA";
        } else if ("DVD".equals(tipoDocumento)) {
            return "DVD";
        } else if ("Diccionario".equals(tipoDocumento)) {
            return "DIC";
        } else if ("Mapas".equals(tipoDocumento)) {
            return "MAP";
        } else if ("Tesis".equals(tipoDocumento)) {
            return "TES";
        } else if ("VHS".equals(tipoDocumento)) {
            return "VHS";
        } else if ("Cassettes".equals(tipoDocumento)) {
            return "CAS";
        } else if ("Documento".equals(tipoDocumento)) {
            return "DOC";
        } else if ("Periodicos".equals(tipoDocumento)) {
            return "PER";
        } else {
            return "UNK";
        }
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

    // === OBTENER COPIAS POR EJEMPLAR ===
    public List<JSONObject> obtenerCopiasPorEjemplar(int idEjemplar) {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
        SELECT id_copia, codigo_unico, estado
        FROM Copias
        WHERE id_ejemplar = ?
        ORDER BY id_copia
        """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEjemplar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject copia = new JSONObject();
                    copia.put("id_copia", rs.getInt("id_copia"));
                    copia.put("codigo_unico", rs.getString("codigo_unico"));
                    copia.put("estado", rs.getString("estado"));

                    lista.add(copia);
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al obtener copias del ejemplar", e);
        }
        return lista;
    }

    public JSONObject obtenerPorId(int idEjemplar) {
        String sql = """
    SELECT e.id_ejemplar, e.titulo, e.id_autor, e.ubicacion, e.tipo_documento, a.nombre_autor
    FROM Ejemplares e
    LEFT JOIN Autores a ON e.id_autor = a.id_autor
    WHERE e.id_ejemplar = ?
    """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEjemplar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject ejemplar = new JSONObject();
                    ejemplar.put("id_ejemplar", rs.getInt("id_ejemplar"));
                    ejemplar.put("titulo", rs.getString("titulo"));
                    ejemplar.put("id_autor", rs.getObject("id_autor"));
                    ejemplar.put("ubicacion", rs.getString("ubicacion"));
                    String tipoDoc = rs.getString("tipo_documento");
                    ejemplar.put("tipo_documento", tipoDoc);
                    ejemplar.put("nombre_autor", rs.getString("nombre_autor"));

                    try {
                        JSONObject datosEspecificos = obtenerDatosEspecificosPorId(conn, idEjemplar, tipoDoc);
                        if (datosEspecificos != null) {
                            ejemplar.putAll(datosEspecificos);
                        }
                    } catch (Exception ex) {
                        // Solo imprimimos el error en consola, pero permitimos que continúe
                        System.err.println("Advertencia: No se pudieron cargar detalles específicos para ID " + idEjemplar + ": " + ex.getMessage());
                    }
                    // ---------------------

                    return ejemplar;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error al obtener ejemplar por ID: " + idEjemplar, e);
        }
        return null;
    }

     private JSONObject obtenerDatosEspecificosPorId(Connection conn, int idEjemplar, String tipoDocumento) throws SQLException {
        JSONObject datos = new JSONObject();

        if ("Libro".equals(tipoDocumento)) {
            String sql = "SELECT l.isbn, l.id_editorial, e.nombre_editorial, l.id_genero, g.nombre_genero, l.edicion " +
                    "FROM Libros l " +
                    "LEFT JOIN Editoriales e ON l.id_editorial = e.id_editorial " +
                    "LEFT JOIN Generos g ON l.id_genero = g.id_genero " +
                    "WHERE l.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        datos.put("isbn", rs.getString("isbn"));
                        datos.put("id_editorial", rs.getObject("id_editorial"));
                        datos.put("nombre_editorial", rs.getString("nombre_editorial"));
                        datos.put("id_genero", rs.getObject("id_genero"));
                        datos.put("nombre_genero", rs.getString("nombre_genero"));
                        datos.put("edicion", rs.getObject("edicion"));
                    }
                }
            }
        } else if ("Diccionario".equals(tipoDocumento)) {
            String sql = "SELECT d.isbn, d.id_editorial, e.nombre_editorial, d.idioma, d.volumen " +
                    "FROM Diccionarios d " +
                    "LEFT JOIN Editoriales e ON d.id_editorial = e.id_editorial " +
                    "WHERE d.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        datos.put("isbn", rs.getString("isbn"));
                        datos.put("id_editorial", rs.getObject("id_editorial"));
                        datos.put("nombre_editorial", rs.getString("nombre_editorial"));
                        datos.put("idioma", rs.getString("idioma"));
                        datos.put("volumen", rs.getObject("volumen"));
                    }
                }
            }
        } else if ("DVD".equals(tipoDocumento) || "VHS".equals(tipoDocumento) || "CD".equals(tipoDocumento)) {
            String tabla = "DVD".equals(tipoDocumento) ? "DVDs" : "VHS".equals(tipoDocumento) ? "VHS" : "CDs";
            String sql = "SELECT m.duracion, m.id_genero, g.nombre_genero " +
                    "FROM " + tabla + " m " +
                    "LEFT JOIN Generos g ON m.id_genero = g.id_genero " +
                    "WHERE m.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // IMPORTANTE: Convertir Time a String
                        Time duracion = rs.getTime("duracion");
                        datos.put("duracion", duracion != null ? duracion.toString() : "");
                        datos.put("id_genero", rs.getObject("id_genero"));
                        datos.put("nombre_genero", rs.getString("nombre_genero"));
                    }
                }
            }
        } else if ("Mapas".equals(tipoDocumento)) {
            String sql = "SELECT escala, tipo_mapa FROM Mapas WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        datos.put("escala", rs.getString("escala"));
                        datos.put("tipo_mapa", rs.getString("tipo_mapa"));
                    }
                }
            }
        } else if ("Tesis".equals(tipoDocumento)) {
            String sql = "SELECT grado_academico, facultad FROM Tesis WHERE id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        datos.put("facultad", rs.getString("facultad"));
                        datos.put("grado_academico", rs.getString("grado_academico"));
                    }
                }
            }
        }else if ("Revistas".equals(tipoDocumento)) {
            // Modificado para traer nombre_tipo_revista
            String sql = "SELECT r.fecha_publicacion, tr.nombre_tipo_revista, g.nombre_genero " +
                    "FROM Revistas r " +
                    "LEFT JOIN TiposRevista tr ON r.id_tipo_revista = tr.id_tipo_revista " +
                    "LEFT JOIN Generos g ON r.id_genero = g.id_genero " +
                    "WHERE r.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // IMPORTANTE: Convertir Date a String
                        java.util.Date fecha = rs.getDate("fecha_publicacion");
                        datos.put("fecha_publicacion", fecha != null ? fecha.toString() : "");
                        datos.put("nombre_tipo_revista", rs.getString("nombre_tipo_revista")); // Guardamos el nombre
                        datos.put("nombre_genero", rs.getString("nombre_genero"));
                    }
                }
            }
        } else if ("Periodicos".equals(tipoDocumento)) {
            // Modificado para traer nombre_tipo_periodico
            String sql = "SELECT p.fecha_publicacion, tp.nombre_tipo_periodico " +
                    "FROM Periodicos p " +
                    "LEFT JOIN TiposPeriodico tp ON p.id_tipo_periodico = tp.id_tipo_periodico " +
                    "WHERE p.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // IMPORTANTE: Convertir Date a String
                        java.util.Date fecha = rs.getDate("fecha_publicacion");
                        datos.put("fecha_publicacion", fecha != null ? fecha.toString() : "");
                        datos.put("nombre_tipo_periodico", rs.getString("nombre_tipo_periodico")); // Guardamos el nombre
                    }
                }
            }
        } else if ("Cassettes".equals(tipoDocumento)) {
            // Modificado para traer nombre_tipo_cinta
            String sql = "SELECT c.duracion, tc.nombre_tipo_cinta " +
                    "FROM Cassettes c " +
                    "LEFT JOIN TiposCinta tc ON c.id_tipo_cinta = tc.id_tipo_cinta " +
                    "WHERE c.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // IMPORTANTE: Convertir Time a String
                        Time duracion = rs.getTime("duracion");
                        datos.put("duracion", duracion != null ? duracion.toString() : "");
                        datos.put("nombre_tipo_cinta", rs.getString("nombre_tipo_cinta")); // Guardamos el nombre
                    }
                }
            }
        } else if ("Documento".equals(tipoDocumento)) {
            // Modificado para traer nombre_tipo_detalle
            String sql = "SELECT tdd.nombre_tipo_detalle " +
                    "FROM Documentos d " +
                    "LEFT JOIN TiposDocumentoDetalle tdd ON d.id_tipo_detalle = tdd.id_tipo_detalle " +
                    "WHERE d.id_ejemplar = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEjemplar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        datos.put("nombre_tipo_detalle", rs.getString("nombre_tipo_detalle")); // Guardamos el nombre
                    }
                }
            }
        }

        return datos.isEmpty() ? null : datos;
    }
 
    public List<JSONObject> buscarEjemplares(String criterio) {
        List<JSONObject> lista = new ArrayList<>();
        // Esta consulta busca en título, autor o tipo, y cuenta las copias disponibles
        String sql = "SELECT e.id_ejemplar, e.titulo, e.tipo_documento, e.ubicacion, a.nombre_autor, "
                + "(SELECT COUNT(*) FROM Copias c WHERE c.id_ejemplar = e.id_ejemplar AND c.estado = 'Disponible') as disponibles "
                + "FROM Ejemplares e "
                + "LEFT JOIN Autores a ON e.id_autor = a.id_autor "
                + "WHERE e.titulo LIKE ? OR a.nombre_autor LIKE ? OR e.tipo_documento LIKE ? "
                + "ORDER BY e.titulo LIMIT 50";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String busqueda = "%" + criterio + "%";
            ps.setString(1, busqueda);
            ps.setString(2, busqueda);
            ps.setString(3, busqueda);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject ejemplar = new JSONObject();
                    ejemplar.put("id_ejemplar", rs.getInt("id_ejemplar"));
                    ejemplar.put("titulo", rs.getString("titulo"));
                    ejemplar.put("tipo_documento", rs.getString("tipo_documento"));
                    ejemplar.put("ubicacion", rs.getString("ubicacion"));
                    ejemplar.put("nombre_autor", rs.getString("nombre_autor") != null ? rs.getString("nombre_autor") : "Anónimo");
                    ejemplar.put("disponibles", rs.getInt("disponibles"));

                    lista.add(ejemplar);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(EjemplaresModel.class.getName()).log(Level.SEVERE, "Error en búsqueda", e);
        }
        return lista;
    }
}
