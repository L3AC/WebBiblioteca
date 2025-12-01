package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import model.TDDModel;
import utils.Validaciones;

@WebServlet(name = "TDDController", urlPatterns = {"/tiposdocumentodetalle.do"})
public class TDDController extends HttpServlet {

    ArrayList<String> listaErrores = new ArrayList<>();
    TDDModel modelo = new TDDModel();

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
            List<JSONObject> lista = modelo.listarTiposDocumentoDetalle();
            request.setAttribute("listaTiposDocumentoDetalle", lista);
            request.getRequestDispatcher("/tiposdocumentodetalle/listaTiposDocumentoDetalle.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getRequestDispatcher("/tiposdocumentodetalle/nuevoTipoDocumentoDetalle.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response) {
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
                listaErrores.add("No se recibió información para registrar el tipo de documento detalle.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("nombre_tipo_detalle"))) {
                    listaErrores.add("El nombre del tipo de documento detalle es obligatorio.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.registrarTipoDocumentoDetalle(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Tipo de documento detalle registrado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Tipo de documento detalle registrado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo registrar el tipo de documento detalle (posible duplicado).\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject tipoDetalle = modelo.obtenerPorId(id);
            if (tipoDetalle != null) {
                request.setAttribute("tipoDetalle", tipoDetalle);
                request.getRequestDispatcher("/tiposdocumentodetalle/editarTipoDocumentoDetalle.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/error404.jsp");
            }
        } catch (ServletException | IOException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
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
                listaErrores.add("No se recibió información para modificar el tipo de documento detalle.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("nombre_tipo_detalle"))) {
                    listaErrores.add("El nombre del tipo de documento detalle es obligatorio.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.actualizarTipoDocumentoDetalle(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Tipo de documento detalle modificado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Tipo de documento detalle modificado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo modificar el tipo de documento detalle.\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean ok = modelo.eliminarTipoDocumentoDetalle(id);
            if (ok) {
                request.setAttribute("exito", "Tipo de documento detalle eliminado exitosamente.");
            } else {
                request.setAttribute("fracaso", "No se puede eliminar este tipo de documento detalle.");
            }
            request.getRequestDispatcher("/tiposdocumentodetalle.do?op=listar").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void detalles(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject tipoDetalle = modelo.obtenerPorId(id);
            if (tipoDetalle != null) {
                out.print(tipoDetalle.toJSONString());
            } else {
                out.print("{\"error\": \"Tipo de documento detalle no encontrado\"}");
            }
        } catch (IOException ex) {
            Logger.getLogger(TDDController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}