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
import model.ReservasModel;
import utils.Validaciones;

@WebServlet(name = "ReservasController", urlPatterns = {"/reservas.do"})
public class ReservasController extends HttpServlet {

    ArrayList<String> listaErrores = new ArrayList<>();
    ReservasModel modelo = new ReservasModel();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if (request.getParameter("op") == null) {
                listar(request, response);
                return;
            }

            String operacion = request.getParameter("op");

            if ("listar".equals(operacion)) {
                listar(request, response);
            } else if ("crear".equals(operacion)) {
                crear(request, response);
            } else if ("eliminar".equals(operacion)) {
                eliminar(request, response);
            } else {
                out.print("{\"success\": false, \"message\": \"Operación no válida.\"}");
            }
        }
    }

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
    }

    private void listar(HttpServletRequest request, HttpServletResponse response) {
        try {
            List<JSONObject> lista = modelo.listarReservas();
            request.setAttribute("listaReservas", lista);
            // Si es AJAX, devolver JSON
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print("[");
            for (int i = 0; i < lista.size(); i++) {
                out.print(lista.get(i).toJSONString());
                if (i < lista.size() - 1) out.print(",");
            }
            out.print("]");
        } catch (IOException ex) {
            Logger.getLogger(ReservasController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void crear(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            listaErrores.clear();

            // Recibir datos por parámetros (no JSON en este caso)
            String idCopiaStr = request.getParameter("idCopia");
            String correoUsuario = request.getParameter("correoUsuario");

            if (Validaciones.isEmpty(idCopiaStr)) {
                listaErrores.add("El ID de la copia es obligatorio.");
            }
            if (Validaciones.isEmpty(correoUsuario)) {
                listaErrores.add("El correo del usuario es obligatorio.");
            }/* else if (!Validaciones.esCorreoValido(correoUsuario)) {
                listaErrores.add("El correo no tiene un formato válido.");
            }*/
            
            if (listaErrores.isEmpty()) {
                int idCopia = Integer.parseInt(idCopiaStr);
                boolean ok = modelo.reservarCopia(idCopia, correoUsuario);
                if (ok) {
                    out.print("{\"success\": true, \"message\": \"Copia reservada exitosamente.\"}");
                } else {
                    out.print("{\"success\": false, \"message\": \"No se pudo reservar la copia (posible duplicado o no disponible).\"}");
                }
            } else {
                out.print("{\"success\": false, \"errors\": " + listaErrores.toString() + "}");
            }
        } catch (IOException ex) {
            Logger.getLogger(ReservasController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            String idReservaStr = request.getParameter("idReserva");

            if (Validaciones.isEmpty(idReservaStr)) {
                out.print("{\"success\": false, \"message\": \"El ID de la reserva es obligatorio.\"}");
                return;
            }

            int idReserva = Integer.parseInt(idReservaStr);
            boolean ok = modelo.cancelarReserva(idReserva);
            if (ok) {
                out.print("{\"success\": true, \"message\": \"Reserva cancelada exitosamente.\"}");
            } else {
                out.print("{\"success\": false, \"message\": \"No se pudo cancelar la reserva.\"}");
            }
        } catch (IOException ex) {
            Logger.getLogger(ReservasController.class.getName()).log(Level.SEVERE, null, ex);
            out.print("{\"success\": false, \"message\": \"Error al procesar la solicitud.\"}");
        }
    }
}