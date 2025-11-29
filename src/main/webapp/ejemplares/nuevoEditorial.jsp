<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Nuevo editorial</title>
        <%@ include file='/cabecera.jsp' %>
    </head>
    <body>
        <jsp:include page="/menu.jsp"/>
        <div class="container">
            <div class="row">
                <h3>Nuevo editorial</h3>
            </div>
            <div class="row">
                <div class=" col-md-7">
                   
                    <c:if test="${not empty listaErrores}">
                    <div class="alert alert-danger">
                        <ul>
                            <c:forEach var="errores"  items="${requestScope.listaErrores}">
                                <li>${errores}</li>
                            </c:forEach>
                        </ul>
                    </div>
                    </c:if>
                    <form role="form" action="${contextPath}/editoriales.do" method="POST">
                        <input type="hidden"  id="op" name="op" value="insertar"/>
                        <div class="well well-sm"><strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong></div>
                        <div class="form-group">
                            
                            <label for="codigo">Codigo del editorial:</label>
                            <div class="input-group">
                                <input type="text" class="form-control" name="codigo"  id="codigo" value="${editorial.codigoEditorial}" placeholder="Ingresa el codigo del genero" >
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="nombre">Nombre del editorial:</label>
                            <div class="input-group">
                                <input type="text" class="form-control" name="nombre" id="nombre"  value="${editorial.nombreEditorial}"  placeholder="Ingresa el nombre del editorial" >
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="contacto">Contacto:</label>
                            <div class="input-group">
                                <input type="text" class="form-control" id="contacto"  value="${editorial.contacto}" name="contacto"  placeholder="Ingresa el nombre del contacto">
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="telefono">Telefono:</label>
                            <div class="input-group">
                                <input type="tel" class="form-control" id="telefono"  value="${editorial.telefono}" name="telefono"  placeholder="Ingresa el telefono del contacto" >
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <input type="submit" class="btn btn-info" value="Guardar" name="Guardar">
                        <a class="btn btn-danger" href="${contextPath}/editoriales.do?op=listar">Cancelar</a>
                    </form>
                </div>
            </div>  
        </div>
    </body>
</html>



