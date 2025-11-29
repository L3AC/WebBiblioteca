/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sv.edu.udb.www.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sv.edu.udb.www.beans.Autor;
import sv.edu.udb.www.beans.Genero;
import sv.edu.udb.www.model.GenerosModel;
import sv.edu.udb.www.utils.Validaciones;

/**
 *
 * @author Emerson Torres
 */
@WebServlet(name = "GenerosController", urlPatterns = {"/generos.do"})
public class GenerosController extends HttpServlet {
    
    GenerosModel modelo = new GenerosModel();
    ArrayList<String> listaErrores = new ArrayList<>();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if(request.getParameter("op")==null){
            listar(request, response);
            return;
            }
            String operacion = request.getParameter("op");
   
            switch (operacion) {
                case "listar":
                    listar(request, response);
                    break;
                case "nuevo":
                    request.getRequestDispatcher("/generos/nuevoGenero.jsp").forward(request, response);
                    break;
                case "insertar":
                    insertar(request, response);
                    break;
                case "obtener":
                    obtener(request, response);
                    break;
                case "modificar":
                    modificar(request, response);
                    break;
                case "eliminar":
                    eliminar(request, response);
                    break;
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void listar(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setAttribute("listaGeneros", modelo.listarGeneros());
            request.getRequestDispatcher("/generos/listaGeneros.jsp").forward(request, response);
        } catch (ServletException | IOException | SQLException ex) {
            Logger.getLogger(GenerosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void insertar(HttpServletRequest request, HttpServletResponse response) {
      try {
            listaErrores.clear();
            Genero miGenero = new Genero();
            miGenero.setNombreGenero(request.getParameter("nombre"));
            miGenero.setDescripcion(request.getParameter("descripcion"));
           

            if (Validaciones.isEmpty(miGenero.getNombreGenero())) {
                listaErrores.add("El nombre del genero es obligatorio");
            }

            if (listaErrores.size() > 0) {
                request.setAttribute("listaErrores", listaErrores);
                request.setAttribute("autor", miGenero);
                request.getRequestDispatcher("generos.do?op=nuevo").forward(request, response);
            } else {
                if (modelo.insertarGeneros(miGenero) > 0) {
                    request.getSession().setAttribute("exito", "genero registrado exitosamente");
                    response.sendRedirect(request.getContextPath() + "/generos.do?op=listar");
                } else {
                    request.getSession().setAttribute("fracaso", "El genero no ha sido ingresado");
                    response.sendRedirect(request.getContextPath() + "/generos.do?op=listar");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AutoresController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(AutoresController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServletException ex) {
            Logger.getLogger(AutoresController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void obtener(HttpServletRequest request, HttpServletResponse response) {
        try {
            String id = request.getParameter("id");
            Genero miGenero = modelo.obtenerGenero(id);
            if (miGenero != null) {
                request.setAttribute("genero", miGenero);
                request.getRequestDispatcher("/generos/editarGeneros.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/error404.jsp");
            }
        } catch (SQLException | ServletException | IOException ex) {
            Logger.getLogger(GenerosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void modificar(HttpServletRequest request, HttpServletResponse response) {
        try {
            listaErrores.clear();
            Genero miGenero = new Genero();
            miGenero.setNombreGenero(request.getParameter("nombre"));
            miGenero.setDescripcion(request.getParameter("descripcion"));
            miGenero.setIdGenero(Integer.parseInt(request.getParameter("id2")));

            if (Validaciones.isEmpty(miGenero.getNombreGenero())) {
                listaErrores.add("El nombre del genero es obligatorio");
            }

            if (listaErrores.size() > 0) {
                request.setAttribute("listaErrores", listaErrores);
                request.setAttribute("autor", miGenero);
                request.getRequestDispatcher("generos.do?op=nuevo").forward(request, response);
            } else {
                if (modelo.modificarGenero(miGenero) > 0) {
                    request.getSession().setAttribute("exito", "genero registrado exitosamente");
                    response.sendRedirect(request.getContextPath() + "/generos.do?op=listar");
                } else {
                    request.getSession().setAttribute("fracaso", "El genero no ha sido ingresado");
                    response.sendRedirect(request.getContextPath() + "/generos.do?op=listar");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GenerosController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GenerosController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServletException ex) {
            Logger.getLogger(GenerosController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) {
       try {
            String id = request.getParameter("id");
            if (modelo.eliminarGenero(id) > 0) {
                request.setAttribute("exito", "genero eliminado exitosamente");
                
            } else {
                request.setAttribute("fracaso", "No se puede eliminar este genero");
            }
            request.getRequestDispatcher("/generos.do?op=listar").forward(request, response);
        } catch (SQLException | ServletException | IOException ex) {
            Logger.getLogger(GenerosController.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }

}
