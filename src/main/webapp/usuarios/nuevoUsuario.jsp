
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %> <!-- ? ESTE ES EL HEADER DINÁMICO -->

<!DOCTYPE html>
<html>
<head>
    <title>Nuevo Usuario</title>
</head>
<body>
    <div class="container">
        <div class="row">
            <h3>Nuevo Usuario</h3>
        </div>
        
        <!-- ? Mensajes de éxito/error -->
        <c:if test="${not empty exito}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <strong>? Éxito:</strong> ${exito}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty fracaso}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>? Error:</strong> ${fracaso}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty listaErrores}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <strong>?? Errores de validación:</strong>
                <ul class="mb-0">
                    <c:forEach var="error" items="${requestScope.listaErrores}">
                        <li>${error}</li>
                    </c:forEach>
                </ul>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="row">
            <div class="col-md-7">
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
        // ? Opcional: Auto-cerrar alertas después de 5 segundos
        setTimeout(function() {
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(function(alert) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            });
        }, 5000);
    </script>
</body>
</html>