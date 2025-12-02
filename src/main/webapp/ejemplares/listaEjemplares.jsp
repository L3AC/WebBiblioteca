<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Lista de Ejemplares</title>
         <%@ include file='/cabecera.jsp' %>
    </head>
    <body>
        <jsp:include page="/menu.jsp"/>
        <div class="container">
            <div class="row">
                <h3>Lista de Ejemplares</h3>
            </div>
            <div class="row">
                <div class="col-md-12">
                    <a type="button" class="btn btn-primary btn-md" href="${contextPath}/ejemplares.do?op=nuevo"> Nuevo Ejemplar</a>
                <br><br>
                <table class="table table-striped table-bordered table-hover" id="tabla">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Título</th>
                            <th>Autor</th>
                            <th>Tipo de Documento</th>
                            <th>Ubicación</th>
                            <th>Operaciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        
                        <c:forEach items="${requestScope.listaEjemplares}" var="ejemplar">
                         <tr>
                                <td>${ejemplar.id_ejemplar}</td>
                                <td>${ejemplar.titulo}</td>
                                <td>${ejemplar.nombre_autor}</td>
                                <td>${ejemplar.tipo_documento}</td>
                                <td>${ejemplar.ubicacion}</td>
                                <td>
                                    <a class="btn btn-primary" href="${contextPath}/ejemplares.do?op=obtener&id=${ejemplar.id_ejemplar}"><span class="glyphicon glyphicon-edit"></span> Editar</a>
                                    <a  class="btn btn-danger" href="javascript:eliminar('${ejemplar.id_ejemplar}')"><span class="glyphicon glyphicon-trash"></span> Eliminar</a>
                                </td>
                            </tr>
                    </c:forEach>
                           
                   
                    </tbody>
                </table>
                </div>
                
            </div>                    
        </div> 
        <script>
            $(document).ready(function(){
               $('#tabla').DataTable(); 
            });
                       <c:if test="${not empty exito}">
                           alertify.success('${exito}');
                          <c:set var="exito" value="" scope="session" />
                       </c:if>
                           <c:if test="${not empty fracaso}">
                           alertify.error('${fracaso}');
                           <c:set var="fracaso" value="" scope="session" />
                       </c:if>
         function eliminar(id){
           alertify.confirm("¿Realmente desea eliminar este ejemplar?", function(e){
              if(e){
                  location.href="ejemplares.do?op=eliminar&id="+ id;
              } 
           });
  }
        </script>
    </body>
</html>