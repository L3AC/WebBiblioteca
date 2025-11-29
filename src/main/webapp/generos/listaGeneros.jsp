<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Lista de generos</title>
       
        <%@ include file='/cabecera.jsp' %>
    </head>
    <body>
        <jsp:include page="/menu.jsp"/>
        <div class="container">
            <div class="row">
                <h3>Lista de generos</h3>
            </div>
            <div class="row">
                <div class="col-md-10">
                    <a type="button" class="btn btn-primary btn-md" href="${contextPath}/generos.do?op=nuevo">Nuevo genero</a>
                
                <br/><br/>
                
                <table class="table table-striped table-bordered table-hover" id="tabla">
                    <thead>
                        <tr>
                            <th>Id del genero</th>
                            <th>Nombre del genero</th>
                            <th>Descripcion</th>
                            <th>Operaciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        
                        <c:forEach items="${requestScope.listaGeneros}" var="generos">
                         <tr>
                                <td>${generos.idGenero}</td>
                                <td>${generos.nombreGenero}</td>
                                <td>${generos.descripcion}</td>
                                <td>
                                    <a class="btn btn-primary" href="${contextPath}/generos.do?op=obtener&id=${generos.idGenero}"><span class="glyphicon glyphicon-edit"></span> Editar</a>
                                    <a  class="btn btn-danger" href="javascript:eliminar('${generos.idGenero}')"><span class="glyphicon glyphicon-trash"></span> Eliminar</a>
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
           alertify.confirm("¿Realmente decea eliminar este genero?", function(e){
              if(e){
                  location.href="generos.do?op=eliminar&id="+ id;
              } 
           });
  }
        </script>
    </body>
</html>


