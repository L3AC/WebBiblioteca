package controllers;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.PrestamosModel;
import org.json.simple.JSONObject;
import java.util.List;

@WebServlet(name = "PrestamosController", urlPatterns = {"/prestamos.do"})
public class PrestamosController extends HttpServlet {
    PrestamosModel modelo = new PrestamosModel();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        if(session != null && session.getAttribute("usuario") != null){
            JSONObject usuario = (JSONObject) session.getAttribute("usuario");
            int idUsuario = (int) usuario.get("id_usuario");
            JSONObject rol = (JSONObject) usuario.get("rol");
            boolean esAdmin = "Administrador".equals(rol.get("nombre_rol"));

            List<JSONObject> lista = modelo.listarPrestamos(idUsuario, esAdmin);
            out.print(lista.toString());
        } else {
            out.print("[]");
        }
    }
}
