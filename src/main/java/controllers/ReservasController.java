package controllers;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.ReservasModel;
import org.json.simple.JSONObject;
import java.util.List;

@WebServlet(name = "ReservasController", urlPatterns = {"/reservas.do"})
public class ReservasController extends HttpServlet {
    ReservasModel modelo = new ReservasModel();

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String op = request.getParameter("op");
        HttpSession session = request.getSession(false);
        JSONObject usuario = (session != null) ? (JSONObject) session.getAttribute("usuario") : null;

        if(usuario == null){
            out.print("{\"success\": false, \"message\": \"Sesión expirada\"}");
            return;
        }

        int idUsuario = (int) usuario.get("id_usuario");
        JSONObject rol = (JSONObject) usuario.get("rol");
        boolean esAdmin = "Administrador".equals(rol.get("nombre_rol"));

        if("listar".equals(op)){
            List<JSONObject> lista = modelo.listarReservas(idUsuario, esAdmin);
            out.print(lista.toString());
        } else if("crear".equals(op)){
            int idCopia = Integer.parseInt(request.getParameter("idCopia"));
            if(modelo.reservarCopia(idCopia, idUsuario)){
                out.print("{\"success\": true, \"message\": \"Reserva realizada con éxito\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Error: Copia no disponible\"}");
            }
        } else if("aceptar".equals(op)){
            int idReserva = Integer.parseInt(request.getParameter("idReserva"));
            if(modelo.aceptarReserva(idReserva)){
                out.print("{\"success\": true, \"message\": \"Préstamo registrado correctamente\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Error al registrar préstamo\"}");
            }
        } else if("eliminar".equals(op)){
            int idReserva = Integer.parseInt(request.getParameter("idReserva"));
            if(modelo.cancelarReserva(idReserva)){
                out.print("{\"success\": true, \"message\": \"Reserva cancelada\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"Error al cancelar\"}");
            }
        }
    }
}