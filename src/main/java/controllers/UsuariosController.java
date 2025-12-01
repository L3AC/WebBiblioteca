package controllers;

import model.RolesModel;
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
import jakarta.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import model.UsuarioModel;
import model.RolesModel;
import utils.Validaciones;

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
            Logger logger = Logger.getLogger(UsuariosController.class.getName());
            List<JSONObject> lista = modelo.listarUsuarios();
            logger.log(Level.INFO, "Usuarios encontrados: {0}", lista.size());

            request.setAttribute("listaUsuarios", lista);
            // ✅ CAMBIA ESTA RUTA SEGÚN DONDE ESTÉ TU ARCHIVO
            request.getRequestDispatcher("/usuarios/listaUsuarios.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void nuevo(HttpServletRequest request, HttpServletResponse response) {
        try {
            // ✅ Cargar listas para el formulario
            request.setAttribute("listaRoles", roles.listarRoles());
            request.getRequestDispatcher("/usuarios/nuevoUsuario.jsp").forward(request, response);
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JSONObject usuario = modelo.obtenerPorId(id);
            if (usuario != null) {
                request.setAttribute("usuario", usuario);
                request.setAttribute("listaRoles", roles.listarRoles()); // ✅ Cargar roles para edición
                request.getRequestDispatcher("/usuarios/editarUsuario.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/error404.jsp");
            }
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        PrintWriter out = null;
        try {
            response.setContentType("text/html;charset=UTF-8"); // ✅ Cambiado a HTML, no JSON
            listaErrores.clear();

            // ✅ Recibir datos como parámetros normales (no JSON)
            String nombre = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String idRolStr = request.getParameter("id_rol");

            // Validaciones
            if (Validaciones.isEmpty(nombre)) {
                listaErrores.add("El nombre es obligatorio.");
            }
            if (Validaciones.isEmpty(apellido)) {
                listaErrores.add("El apellido es obligatorio.");
            }
            if (Validaciones.isEmpty(correo)) {
                listaErrores.add("El correo es obligatorio.");
            }
            if (Validaciones.isEmpty(contrasena)) {
                listaErrores.add("La contraseña es obligatoria.");
            }
            if (idRolStr == null || idRolStr.trim().isEmpty()) {
                listaErrores.add("El rol es obligatorio.");
            }

            if (listaErrores.isEmpty()) {
                // ✅ Crear el JSONObject con los datos recibidos
                JSONObject data = new JSONObject();
                data.put("nombre", nombre);
                data.put("apellido", apellido);
                data.put("correo", correo);
                data.put("contrasena", contrasena);
                data.put("id_rol", Long.parseLong(idRolStr));

                boolean ok = modelo.registrarUsuario(data);
                if (ok) {
                    request.getSession().setAttribute("exito", "Usuario registrado exitosamente.");
                    response.sendRedirect(request.getContextPath() + "/usuarios.do?op=listar"); // ✅ Redirigir a la lista
                } else {
                    request.setAttribute("fracaso", "No se pudo registrar el usuario (posible duplicado).");
                    request.getRequestDispatcher("/usuarios/nuevoUsuario.jsp").forward(request, response); // ✅ Mostrar error en el formulario
                }
            } else {
                request.setAttribute("listaErrores", listaErrores); // ✅ Pasar errores al JSP
                request.getRequestDispatcher("/usuarios/nuevoUsuario.jsp").forward(request, response);
            }
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                request.setAttribute("fracaso", "Error al procesar la solicitud.");
                request.getRequestDispatcher("/usuarios/nuevoUsuario.jsp").forward(request, response);
            } catch (IOException | ServletException e) {
                Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, "Error fatal", e);
            }
        }
    }

    private void modificar(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setContentType("application/json; charset=UTF-8"); // ✅ Asegurar JSON
            out = response.getWriter();
            listaErrores.clear();

            // ✅ Recibir datos como parámetros normales (no JSON)
            String idUsuarioStr = request.getParameter("id_usuario");
            String nombre = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String correo = request.getParameter("correo");
            String contrasena = request.getParameter("contrasena");
            String idRolStr = request.getParameter("id_rol");

            // Validaciones
            if (Validaciones.isEmpty(nombre)) {
                listaErrores.add("El nombre es obligatorio.");
            }
            if (Validaciones.isEmpty(apellido)) {
                listaErrores.add("El apellido es obligatorio.");
            }
            if (Validaciones.isEmpty(correo)) {
                listaErrores.add("El correo es obligatorio.");
            }
            if (idRolStr == null || idRolStr.trim().isEmpty()) {
                listaErrores.add("El rol es obligatorio.");
            }

            if (listaErrores.isEmpty()) {
                // ✅ Crear el JSONObject con los datos recibidos
                JSONObject data = new JSONObject();
                data.put("id_usuario", Long.parseLong(idUsuarioStr));
                data.put("nombre", nombre);
                data.put("apellido", apellido);
                data.put("correo", correo);
                if (contrasena != null && !contrasena.trim().isEmpty()) {
                    data.put("contrasena", contrasena);
                }
                data.put("id_rol", Long.parseLong(idRolStr));

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
        } catch (IOException ex) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, null, ex);
            try {
                out = response.getWriter();
                out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
            } catch (IOException e) {
                Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, "Error fatal", e);
            }
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

    private void login(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setContentType("application/json; charset=UTF-8");
            out = response.getWriter();

            // ✅ Leer el cuerpo como JSON
            StringBuilder sb = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonString = sb.toString();

            if (jsonString.isEmpty()) {
                out.print("{\"success\": false, \"message\": \"No se recibieron datos.\"}");
                return;
            }

            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(jsonString);

            String correo = (String) data.get("correo");
            String contrasena = (String) data.get("contrasena");

            Logger logger = Logger.getLogger(UsuariosController.class.getName());
            logger.log(Level.INFO, "Correo recibido: {0}", correo);
            logger.log(Level.INFO, "Contraseña recibida: {0}", contrasena);

            // ✅ Validación
            if (correo == null || contrasena == null || correo.trim().isEmpty() || contrasena.trim().isEmpty()) {
                out.print("{\"success\": false, \"message\": \"Correo y contraseña son obligatorios.\"}");
                return;
            }

            // ✅ Llamar al modelo
            JSONObject usuario = modelo.login(correo, contrasena);
            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                out.print("{\"success\": true, \"message\": \"Login exitoso.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Credenciales inválidas.\"}");
            }
        } catch (IOException | org.json.simple.parser.ParseException e) {
            Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, "Error al procesar login", e);
            try {
                if (out == null) {
                    response.setContentType("application/json; charset=UTF-8");
                    out = response.getWriter();
                }
                out.print("{\"success\": false, \"message\": \"Error interno del servidor.\"}");
            } catch (IOException ex) {
                Logger.getLogger(UsuariosController.class.getName()).log(Level.SEVERE, "Error fatal", ex);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
