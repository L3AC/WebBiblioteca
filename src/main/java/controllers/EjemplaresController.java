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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import model.EjemplaresModel;
/*import sv.edu.udb.www.model.AutoresModel;
import sv.edu.udb.www.model.GenerosModel;
import model.EditorialesModel;*/
import utils.Validaciones;

@WebServlet(name = "EjemplaresController", urlPatterns = {"/ejemplares.do"})
public class EjemplaresController extends HttpServlet {

    ArrayList<String> listaErrores = new ArrayList<>();
    EjemplaresModel modelo = new EjemplaresModel();
    /*AutoresModel autores = new AutoresModel();
    GenerosModel generos = new GenerosModel();
    EditorialesModel editoriales = new EditorialesModel();*/

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
            // Aquí puedes usar una vista JSP que muestre la lista de ejemplares
            request.getRequestDispatcher("/ejemplares/listaEjemplares.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response) {
        /*try {
            // Cargar listas para el formulario
            request.setAttribute("listaAutores", autores.listarAutores());
            request.setAttribute("listaGeneros", generos.listarGeneros());
            request.setAttribute("listaEditoriales", editoriales.listarEditoriales());
            request.getRequestDispatcher("/ejemplares/nuevoEjemplar.jsp").forward(request, response);
        } catch (SQLException | ServletException | IOException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            listaErrores.clear();

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

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("titulo"))) {
                    listaErrores.add("El título es obligatorio.");
                }
                if (data.get("tipo_documento") == null || Validaciones.isEmpty((String) data.get("tipo_documento"))) {
                    listaErrores.add("El tipo de documento es obligatorio.");
                }
                Long cantCopias = (Long) data.get("cantidad_copias");
                if (cantCopias == null || cantCopias < 1) {
                    listaErrores.add("La cantidad de copias debe ser un número positivo.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.registrarEjemplarDesdeJSON(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Ejemplar registrado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Ejemplar registrado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo registrar el ejemplar.\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            // Aquí puedes cargar el ejemplar y mostrarlo en una vista de edición
            // No implementado en detalle, pero puedes usar un método del modelo para obtener los datos
            request.getRequestDispatcher("/ejemplares/editarEjemplar.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
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
                int idEjemplar = Integer.parseInt(request.getParameter("id"));

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("titulo"))) {
                    listaErrores.add("El título es obligatorio.");
                }
                if (data.get("tipo_documento") == null || Validaciones.isEmpty((String) data.get("tipo_documento"))) {
                    listaErrores.add("El tipo de documento es obligatorio.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.editarEjemplarDesdeJSON(idEjemplar, data);
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
        } catch (IOException | ParseException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
        // Eliminar no implementado aquí, pero puedes hacerlo en el modelo
        // Ejemplo:
        // modelo.eliminarEjemplar(id);
        try {
            request.getRequestDispatcher("/ejemplares.do?op=listar").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (IOException | SQLException ex) {
            Logger.getLogger(EjemplaresController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }
}