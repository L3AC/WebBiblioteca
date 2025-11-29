package controllers;

import model.RolesModel;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import model.UsuarioModel;
import model.RolesModel;
import sv.edu.udb.www.utils.Validaciones;

/**
 *
 * @author TuNombre
 */
@WebServlet(name = "UsuariosController", urlPatterns = {"/usuarios.do"})
public class UsuariosController extends HttpServlet {

    ArrayList<String> listaErrores = new ArrayList<>();
    UsuarioModel modelo = new UsuarioModel();
    RolesModel roles = new RolesModel(); // Asumiendo que tienes este modelo

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
            } else if ("login".equals(operacion)) {
                login(request, response);
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
            List<JSONObject> lista = modelo.listarUsuarios();
            request.setAttribute("listaUsuarios", lista);
            request.getRequestDispatcher("/usuarios/listaUsuarios.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Cargar listas para el formulario
            request.setAttribute("listaRoles", roles.listarRoles());
            request.getRequestDispatcher("/usuarios/nuevoUsuario.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
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
                listaErrores.add("No se recibió información para registrar el usuario.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("nombre"))) {
                    listaErrores.add("El nombre es obligatorio.");
                }
                if (Validaciones.isEmpty((String) data.get("apellido"))) {
                    listaErrores.add("El apellido es obligatorio.");
                }
                if (Validaciones.isEmpty((String) data.get("correo"))) {
                    listaErrores.add("El correo es obligatorio.");
                } /*else if (!Validaciones.esCorreoValido((String) data.get("correo"))) {
                    listaErrores.add("El correo no tiene un formato válido.");
                }*/
                if (Validaciones.isEmpty((String) data.get("contrasena"))) {
                    listaErrores.add("La contraseña es obligatoria.");
                }
                if (data.get("id_rol") == null) {
                    listaErrores.add("El rol es obligatorio.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.registrarUsuario(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Usuario registrado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Usuario registrado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo registrar el usuario (posible duplicado).\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject usuario = modelo.obtenerPorId(id);
            if (usuario != null) {
                request.setAttribute("usuario", usuario);
                request.setAttribute("listaRoles", roles.listarRoles());
                request.getRequestDispatcher("/usuarios/editarUsuario.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/error404.jsp");
            }
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
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
                listaErrores.add("No se recibió información para modificar el usuario.");
            } else {
                JSONParser parser = new JSONParser();
                JSONObject data = (JSONObject) parser.parse(jsonString);
                // El id_usuario debe venir en el JSON o en la URL

                // Validaciones
                if (Validaciones.isEmpty((String) data.get("nombre"))) {
                    listaErrores.add("El nombre es obligatorio.");
                }
                if (Validaciones.isEmpty((String) data.get("apellido"))) {
                    listaErrores.add("El apellido es obligatorio.");
                }
                if (Validaciones.isEmpty((String) data.get("correo"))) {
                    listaErrores.add("El correo es obligatorio.");
                } /*else if (!Validaciones.esCorreoValido((String) data.get("correo"))) {
                    listaErrores.add("El correo no tiene un formato válido.");
                }*/
                if (data.get("id_rol") == null) {
                    listaErrores.add("El rol es obligatorio.");
                }

                if (listaErrores.isEmpty()) {
                    boolean ok = modelo.actualizarUsuario(data);
                    if (ok) {
                        request.getSession().setAttribute("exito", "Usuario modificado exitosamente.");
                        out.print("{\"success\": true, \"message\": \"Usuario modificado exitosamente.\"}");
                    } else {
                        out.print("{\"success\": false, \"message\": \"No se pudo modificar el usuario.\"}");
                    }
                } else {
                    out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
                }
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            boolean ok = modelo.eliminarUsuario(id);
            if (ok) {
                request.setAttribute("exito", "Usuario eliminado exitosamente.");
            } else {
                request.setAttribute("fracaso", "No se puede eliminar este usuario.");
            }
            request.getRequestDispatcher("/usuarios.do?op=listar").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void detalles(HttpServletRequest request, HttpServletResponse response) {
        try {
            PrintWriter out = response.getWriter();
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject usuario = modelo.obtenerPorId(id);
            if (usuario != null) {
                out.print(usuario.toJSONString());
            } else {
                out.print("{\"error\": \"Usuario no encontrado\"}");
            }
        } catch (IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Nueva operación: login
    private void login(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");

            if (Validaciones.isEmpty(correo) || Validaciones.isEmpty(contrasena)) {
                out.print("{\"success\": false, \"message\": \"Correo y contraseña son obligatorios.\"}");
                return;
            }

            JSONObject usuario = modelo.login(correo, contrasena);
            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario); // Guardar en sesión
                out.print("{\"success\": true, \"message\": \"Login exitoso.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Credenciales inválidas.\"}");
            }
        } catch (IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }
}