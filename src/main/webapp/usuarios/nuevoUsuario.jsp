<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Nuevo Usuario</title>
    <%@ include file='/cabecera.jsp' %>
</head>
<body>
    <jsp:include page="/menu.jsp"/>
    <div class="container">
        <div class="row">
            <h3>Nuevo Usuario</h3>
        </div>
        <div class="row">
            <div class="col-md-7">
                <c:if test="${not empty listaErrores}">
                    <div class="alert alert-danger">
                        <ul>
                            <c:forEach var="error" items="${requestScope.listaErrores}">
                                <li>${error}</li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
                <form id="usuarioForm" role="form" action="${contextPath}/usuarios.do" method="POST">
                    <input type="hidden" name="op" value="insertar">
                    <div class="well well-sm">
                        <strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong>
                    </div>
                    <div class="form-group">
                        <label for="nombre">Nombre</label>
                        <div class="input-group">
                            <input type="text" class="form-control" name="nombre" id="nombre" placeholder="Ingresa el nombre" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="apellido">Apellido</label>
                        <div class="input-group">
                            <input type="text" class="form-control" name="apellido" id="apellido" placeholder="Ingresa el apellido" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="correo">Correo</label>
                        <div class="input-group">
                            <input type="email" class="form-control" name="correo" id="correo" placeholder="Ingresa el correo" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="contrasena">Contraseña</label>
                        <div class="input-group">
                            <input type="password" class="form-control" name="contrasena" id="contrasena" placeholder="Ingresa la contraseña" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="id_rol">Rol</label>
                        <select class="form-control" name="id_rol" id="id_rol" required>
                            <option value="">Selecciona un rol</option>
                            <c:forEach items="${requestScope.listaRoles}" var="rol">
                                <option value="${rol.id_rol}">${rol.nombre_rol}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <input type="submit" class="btn btn-info" value="Guardar" name="Guardar">
                    <a class="btn btn-danger" href="${contextPath}/usuarios.do?op=listar">Cancelar</a>
                </form>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('usuarioForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const formData = new FormData(this);
            const data = Object.fromEntries(formData.entries());

            fetch('${contextPath}/usuarios.do?op=insertar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alertify.success(result.message);
                    window.location.href = '${contextPath}/usuarios.do?op=listar';
                } else {
                    if (result.errors) {
                        alertify.error(result.errors.join('<br>'));
                    } else {
                        alertify.error(result.message);
                    }
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alertify.error('Error de conexión.');
            });
        });
    </script>
</body>
</html>