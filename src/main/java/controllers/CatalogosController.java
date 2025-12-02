package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import model.*;
import org.json.simple.JSONObject;

@WebServlet(name = "CatalogosController", urlPatterns = {"/catalogos.do"})
public class CatalogosController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        Map<String, Object> result = new HashMap<>();
        try {
            result.put("autores", new AutoresModel().listarAutores());
            result.put("generos", new GenerosModel().listarGeneros());
            result.put("editoriales", new EditorialesModel().listarEditoriales());
            result.put("tiposCinta", new TiposCintaModel().listarTiposCinta());
            result.put("tiposDetalle", new TDDModel().listarTiposDocumentoDetalle());
            result.put("tiposPeriodico", new TiposPeriodicoModel().listarTiposPeriodico());
            result.put("tiposRevista", new TiposRevistaModel().listarTiposRevista());
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.print(new JSONObject(result).toJSONString());
    }
}