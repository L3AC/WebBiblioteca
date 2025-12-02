package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.AutoresModel;
import model.EditorialesModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import model.EjemplaresModel;
import model.GenerosModel;
import model.TDDModel;
import model.TiposCintaModel;
import model.TiposPeriodicoModel;
import model.TiposRevistaModel;
import utils.Validaciones;

@WebServlet(name = "EjemplaresController", urlPatterns = {"/ejemplares.do"})
public class EjemplaresController extends HttpServlet {

    ArrayList<String> listaErrores = new ArrayList<>();
    EjemplaresModel modelo = new EjemplaresModel();

    AutoresModel autores = new AutoresModel();
    GenerosModel generos = new GenerosModel();
    EditorialesModel editoriales = new EditorialesModel();
    TiposCintaModel tiposCinta = new TiposCintaModel();
    TDDModel tiposDetalle = new TDDModel();
    TiposPeriodicoModel tiposPeriodico = new TiposPeriodicoModel();
    TiposRevistaModel tiposRevista = new TiposRevistaModel();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if (request.getParameter("op") == null) {
                listar(request, response);
                return;
            }

            String operacion = request.getParameter("op");

            if ("listar".equals(operacion)) {
                listar(request, response);
            } else if ("nuevo".equals(operacion)) {
                nuevo(request, response);
            } else if ("insertar".equals(operacion)) {
                insertar(request, response);
            } else if ("obtener".equals(operacion)) {
                obtener(request, response);
            } else if ("modificar".equals(operacion)) {
                modificar(request, response);
            } else if ("eliminar".equals(operacion)) {
                eliminar(request, response);
            } else if ("detalles".equals(operacion)) {
                detalles(request, response);
            } else if ("crearCopias".equals(operacion)) {
                crearCopias(request, response);
            } else {
                request.getRequestDispatcher("/error404.jsp").forward(request, response);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void listar(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Aquí debes usar tu modelo para obtener la lista
            List<JSONObject> lista = modelo.listarEjemplares(); // Asumiendo que tienes este método
            request.setAttribute("listaEjemplares", lista);
            request.getRequestDispatcher("/ejemplares/listaEjemplares.jsp").forward(request, response);
        } catch (Exception ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            // Manejar error
        }
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Cargar listas para el formulario
            request.setAttribute("listaAutores", autores.listarAutores());
            request.setAttribute("listaGeneros", generos.listarGeneros());
            request.setAttribute("listaEditoriales", editoriales.listarEditoriales());
            request.setAttribute("listaTiposCinta", tiposCinta.listarTiposCinta());
            request.setAttribute("listaTiposDetalle", tiposDetalle.listarTiposDocumentoDetalle());
            request.setAttribute("listaTiposPeriodico", tiposPeriodico.listarTiposPeriodico());
            request.setAttribute("listaTiposRevista", tiposRevista.listarTiposRevista());
            request.getRequestDispatcher("/ejemplares/nuevoEjemplar.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        String jsonResponse = null; // Variable para almacenar la respuesta
        boolean success = false; // Bandera para controlar el éxito
        String errorMessage = null; // Variable para almacenar mensajes de error

        try {
            // Leer JSON del cuerpo de la solicitud
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonString = sb.toString();

            if (jsonString.isEmpty()) {
                listaErrores.add("No se recibió información para registrar el ejemplar.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);

                // Validaciones generales
                if (Validaciones.isEmpty((String) data.get("titulo"))) {
                    listaErrores.add("El título es obligatorio.");
                }
                if (data.get("tipo_documento") == null || Validaciones.isEmpty((String) data.get("tipo_documento"))) {
                    listaErrores.add("El tipo de documento es obligatorio.");
                }

                // Validar y convertir todos los posibles campos
                Long idAutor = validarYConvertirLong(data.get("id_autor"), "ID de autor", true);
                Long idEditorial = validarYConvertirLong(data.get("id_editorial"), "ID de editorial", false);
                Long idGenero = validarYConvertirLong(data.get("id_genero"), "ID de género", false);
                Long idTipoDetalle = validarYConvertirLong(data.get("id_tipo_detalle"), "ID de tipo de detalle", false);
                Long idTipoPeriodico = validarYConvertirLong(data.get("id_tipo_periodico"), "ID de tipo de periódico", false);
                Long idTipoRevista = validarYConvertirLong(data.get("id_tipo_revista"), "ID de tipo de revista", false);
                Long idTipoCinta = validarYConvertirLong(data.get("id_tipo_cinta"), "ID de tipo de cinta", false);
                Long cantidadCopias = validarYConvertirLong(data.get("cantidad_copias"), "Cantidad de copias", true);
                Long edicion = validarYConvertirLong(data.get("edicion"), "Edición", false);
                Long volumen = validarYConvertirLong(data.get("volumen"), "Volumen", false);
                Long duracion = validarYConvertirLong(data.get("duracion"), "Duración", false);
                Long anio = validarYConvertirLong(data.get("anio"), "Año", false);
                Long numero = validarYConvertirLong(data.get("numero"), "Número", false);

                // Validar cantidad de copias
                if (cantidadCopias != null && cantidadCopias < 1) {
                    listaErrores.add("La cantidad de copias debe ser un número positivo.");
                }

                if (listaErrores.isEmpty()) {
                    // Usar el método del modelo que recibe parámetros ya validados
                    boolean ok = modelo.registrarEjemplarDesdeJSON(
                            data, idAutor, idEditorial, idGenero, idTipoDetalle, idTipoPeriodico,
                            idTipoRevista, idTipoCinta, cantidadCopias, edicion, volumen, duracion,
                            anio, numero
                    );
                    if (ok) {
                        request.getSession().setAttribute("exito", "Ejemplar registrado exitosamente.");
                        jsonResponse = "{\"success\": true, \"message\": \"Ejemplar registrado exitosamente.\"}";
                        success = true;
                    } else {
                        jsonResponse = "{\"success\": false, \"message\": \"No se pudo registrar el ejemplar.\"}";
                    }
                } else {
                    jsonResponse = "{\"success\": false, \"errors\": " + listaErrores.toString() + "}";
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, "Error al procesar JSON o leer solicitud", ex);
            jsonResponse = "{\"success\": false, \"message\": \"Error al procesar la solicitud: " + ex.getMessage() + "\"}";
            errorMessage = ex.getMessage(); // Opcional, para debugging
        } finally {
            // --- ESTA PARTE ES CRUCIAL ---
            try {
                response.setContentType("application/json"); // Asegura el tipo de contenido
                response.setCharacterEncoding("UTF-8");      // Asegura la codificación
                out = response.getWriter(); // Obtener el writer aquí, al final
                if (jsonResponse != null) {
                    out.print(jsonResponse); // Escribir la respuesta acumulada
                    out.flush(); // Forzar el envío inmediato
                } else {
                    // Caso extremo: no se generó ninguna respuesta
                    out.print("{\"success\": false, \"message\": \"Error interno: no se generó respuesta.\"}");
                }
            } catch (IOException e) {
                Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, "Error al escribir la respuesta JSON", e);
                // No se puede hacer mucho más aquí si no se puede escribir la respuesta
            } finally {
                if (out != null) {
                    out.close(); // Cerrar el writer
                }
            }
        }
    }

    private void modificar(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            listaErrores.clear();

            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonString = sb.toString();

            if (jsonString.isEmpty()) {
                listaErrores.add("No se recibió información para modificar el ejemplar.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);
                int idEjemplar = Integer.parseInt(request.getParameter("id")); // Asumiendo que el ID viene por parámetro

                // Validaciones generales
                if (Validaciones.isEmpty((String) data.get("titulo"))) {
                    listaErrores.add("El título es obligatorio.");
                }
                if (data.get("tipo_documento") == null || Validaciones.isEmpty((String) data.get("tipo_documento"))) {
                    listaErrores.add("El tipo de documento es obligatorio.");
                }

                // Validar y convertir todos los posibles campos
                Long idAutor = validarYConvertirLong(data.get("id_autor"), "ID de autor", true);
                Long idEditorial = validarYConvertirLong(data.get("id_editorial"), "ID de editorial", false);
                Long idGenero = validarYConvertirLong(data.get("id_genero"), "ID de género", false);
                Long idTipoDetalle = validarYConvertirLong(data.get("id_tipo_detalle"), "ID de tipo de detalle", false);
                Long idTipoPeriodico = validarYConvertirLong(data.get("id_tipo_periodico"), "ID de tipo de periódico", false);
                Long idTipoRevista = validarYConvertirLong(data.get("id_tipo_revista"), "ID de tipo de revista", false);
                Long idTipoCinta = validarYConvertirLong(data.get("id_tipo_cinta"), "ID de tipo de cinta", false);
                Long edicion = validarYConvertirLong(data.get("edicion"), "Edición", false);
                Long volumen = validarYConvertirLong(data.get("volumen"), "Volumen", false);
                Long duracion = validarYConvertirLong(data.get("duracion"), "Duración", false);
                Long anio = validarYConvertirLong(data.get("anio"), "Año", false);
                Long numero = validarYConvertirLong(data.get("numero"), "Número", false);

                if (listaErrores.isEmpty()) {
                    // Usar el método del modelo que recibe parámetros ya validados
                    boolean ok = modelo.editarEjemplarDesdeJSON(
                            idEjemplar, data, idAutor, idEditorial, idGenero, idTipoDetalle, idTipoPeriodico,
                            idTipoRevista, idTipoCinta, edicion, volumen, duracion, anio, numero
                    );
                    if (ok) {
                        request.getSession().setAttribute("exito", "Ejemplar modificado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Ejemplar modificado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo modificar el ejemplar.\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException | NumberFormatException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud: " + ex.getMessage() + "\"}");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    // Método auxiliar para validar y convertir Long de forma segura
    private Long validarYConvertirLong(Object obj, String nombreCampo, boolean esObligatorio) {
        if (obj == null) {
            if (esObligatorio) {
                listaErrores.add(nombreCampo + " es obligatorio.");
            }
            return null;
        }

        if (obj instanceof Long) {
            return (Long) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        } else if (obj instanceof String) {
            try {
                Long valor = Long.parseLong((String) obj);
                if (esObligatorio && valor <= 0) {
                    listaErrores.add(nombreCampo + " debe ser un número positivo.");
                }
                return valor;
            } catch (NumberFormatException e) {
                listaErrores.add(nombreCampo + " no válido.");
                return null;
            }
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else {
            listaErrores.add(nombreCampo + " no válido.");
            return null;
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject ejemplar = modelo.obtenerPorId(id);
            if (ejemplar != null) {
                request.setAttribute("ejemplar", ejemplar);
                // Cargar listas para el formulario
                request.setAttribute("listaAutores", autores.listarAutores());
                request.setAttribute("listaGeneros", generos.listarGeneros());
                request.setAttribute("listaEditoriales", editoriales.listarEditoriales());
                request.setAttribute("listaTiposCinta", tiposCinta.listarTiposCinta());
                request.setAttribute("listaTiposDetalle", tiposDetalle.listarTiposDocumentoDetalle());
                request.setAttribute("listaTiposPeriodico", tiposPeriodico.listarTiposPeriodico());
                request.setAttribute("listaTiposRevista", tiposRevista.listarTiposRevista());
                // Cargar también las copias del ejemplar
                request.setAttribute("listaCopias", modelo.obtenerCopiasPorEjemplar(id));
                request.getRequestDispatcher("/ejemplares/editarEjemplar.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/error404.jsp");
            }
        } catch (ServletException | IOException | NumberFormatException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            int idEjemplar = Integer.parseInt(request.getParameter("id"));

            boolean ok = modelo.eliminarEjemplar(idEjemplar);
            if (ok) {
                request.getSession().setAttribute("exito", "Ejemplar eliminado exitosamente.");
                out.print("{\"success\": true, \"message\": \"Ejemplar eliminado exitosamente.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"No se pudo eliminar el ejemplar.\"}");
            }
            request.getRequestDispatcher("/ejemplares.do?op=listar").forward(request, response);
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            if (out != null) {
                out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud: " + ex.getMessage() + "\"}");
            }
        } catch (ServletException ex) {
            System.getLogger(EjemplaresController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void detalles(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
            // int id = Integer.parseInt(request.getParameter("id"));
            // Aquí puedes obtener el ejemplar y retornar JSON
            // JSONObject json = new JSONObject();
            // json.put("id", ejemplar.getId());
            // json.put("titulo", ejemplar.getTitulo());
            // ... más datos
            // out.print(json);
        } catch (IOException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Nueva operación: crear copias adicionales para un ejemplar
    private void crearCopias(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            int idEjemplar = Integer.parseInt(request.getParameter("idEjemplar"));
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));

            if (cantidad < 1) {
                out.print("{\"success\": false, \"message\": \"La cantidad de copias debe ser mayor a 0.\"}");
                return;
            }

            String sql = "SELECT tipo_documento FROM Ejemplares WHERE id_ejemplar = ?";
            java.sql.Connection conn = utils.ConexionBD.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEjemplar);
            java.sql.ResultSet rs = ps.executeQuery();
            String tipoDocumento = null;
            if (rs.next()) {
                tipoDocumento = rs.getString("tipo_documento");
            }
            rs.close();
            ps.close();
            conn.close();

            if (tipoDocumento == null) {
                out.print("{\"success\": false, \"message\": \"No se encontró el ejemplar.\"}");
                return;
            }

            boolean ok = modelo.crearCopiasParaEjemplar(idEjemplar, tipoDocumento, cantidad);
            if (ok) {
                out.print("{\"success\": true, \"message\": \"Copias creadas exitosamente.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"No se pudieron crear las copias.\"}");
            }
        } catch (IOException | SQLException | NumberFormatException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud: " + ex.getMessage() + "\"}");
        }
    }
}
