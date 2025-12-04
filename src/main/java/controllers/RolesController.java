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
import model.RolesModel;
import utils.Validaciones;

/**
 *
 * @author TuNombre
 */
@WebServlet(name = "RolesController", urlPatterns = {"/roles.do"})
public class RolesController extends HttpServlet {

    ArrayList<String> listaErrores = new ArrayList<>();
    RolesModel modelo = new RolesModel();

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
            List<JSONObject> lista = modelo.listarRoles();
            request.setAttribute("listaRoles", lista);
            request.getRequestDispatcher("/roles/listaRoles.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getRequestDispatcher("/roles/nuevoRol.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
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
                listaErrores.add("No se recibió información para registrar el rol.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("nombre_rol"))) {
                    listaErrores.add("El nombre del rol es obligatorio.");
                }
                Long cantMaxPrestamo = (Long) data.get("cant_max_prestamo");
                if (cantMaxPrestamo == null || cantMaxPrestamo < 0) {
                    listaErrores.add("La cantidad máxima de préstamos debe ser un número positivo.");
                }
                Long diasPrestamo = (Long) data.get("dias_prestamo");
                if (diasPrestamo == null || diasPrestamo < 0) {
                    listaErrores.add("Los días de préstamo deben ser un número positivo.");
                }
                Double moraDiaria = (Double) data.get("mora_diaria");
                if (moraDiaria == null || moraDiaria < 0) {
                    listaErrores.add("La mora diaria debe ser un número positivo.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.registrarRol(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Rol registrado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Rol registrado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo registrar el rol (posible duplicado).\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject rol = modelo.obtenerPorId(id);
            if (rol != null) {
                request.setAttribute("rol", rol);
                request.getRequestDispatcher("/roles/editarRol.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/error404.jsp");
            }
        } catch (ServletException | IOException ex) {
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
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

            System.out.println("JSON recibido: " + jsonString);

            if (jsonString.isEmpty()) {
                listaErrores.add("No se recibió información para modificar el rol.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);

                System.out.println("Datos parseados: " + data.toJSONString());
                System.out.println("ID Rol: " + data.get("id_rol"));
                System.out.println("Nombre Rol: " + data.get("nombre_rol"));

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("nombre_rol"))) {
                    listaErrores.add("El nombre del rol es obligatorio.");
                }
                Long cantMaxPrestamo = (Long) data.get("cant_max_prestamo");
                if (cantMaxPrestamo == null || cantMaxPrestamo < 0) {
                    listaErrores.add("La cantidad máxima de préstamos debe ser un número positivo.");
                }
                Long diasPrestamo = (Long) data.get("dias_prestamo");
                if (diasPrestamo == null || diasPrestamo < 0) {
                    listaErrores.add("Los días de préstamo deben ser un número positivo.");
                }
                Double moraDiaria = (Double) data.get("mora_diaria");
                if (moraDiaria == null || moraDiaria < 0) {
                    listaErrores.add("La mora diaria debe ser un número positivo.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.actualizarRol(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Rol modificado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Rol modificado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo modificar el rol.\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            System.out.println("Error al procesar la solicitud: " + ex.getMessage());
            ex.printStackTrace();
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud: " + ex.getMessage() + "\"}");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean ok = modelo.eliminarRol(id);
            if (ok) {
                request.setAttribute("exito", "Rol eliminado exitosamente.");
            } else {
                request.setAttribute("fracaso", "No se puede eliminar este rol.");
            }
            request.getRequestDispatcher("/roles.do?op=listar").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void detalles(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject rol = modelo.obtenerPorId(id);
            if (rol != null) {
                out.print(rol.toJSONString());
            } else {
                out.print("{\"error\": \"Rol no encontrado\"}");
            }
        } catch (IOException ex) {
            Logger.getLogger(RolesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
