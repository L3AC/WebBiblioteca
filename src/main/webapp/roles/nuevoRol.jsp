<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %> <!-- ? ESTE ES EL HEADER DINÁMICO -->

<!DOCTYPE html>
<html>
<head>
    <title>Nuevo Rol</title>
</head>
<body>
    <div class="container">
        <div class="row">
            <h3>Nuevo Rol</h3>
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
                <form id="rolForm" role="form" action="${contextPath}/roles.do" method="POST">
                    <input type="hidden" name="op" value="insertar">
                    <div class="well well-sm">
                        <strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong>
                    </div>
                    <div class="form-group">
                        <label for="nombre_rol">Nombre del Rol</label>
                        <div class="input-group">
                            <input type="text" class="form-control" name="nombre_rol" id="nombre_rol" placeholder="Ingresa el nombre del rol" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="cant_max_prestamo">Cantidad Máxima de Préstamo</label>
                        <div class="input-group">
                            <input type="number" class="form-control" name="cant_max_prestamo" id="cant_max_prestamo" placeholder="Ingresa la cantidad máxima de préstamos" required min="0">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="dias_prestamo">Días de Préstamo</label>
                        <div class="input-group">
                            <input type="number" class="form-control" name="dias_prestamo" id="dias_prestamo" placeholder="Ingresa los días de préstamo" required min="1">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="mora_diaria">Mora Diaria</label>
                        <div class="input-group">
                            <input type="number" step="0.01" class="form-control" name="mora_diaria" id="mora_diaria" placeholder="Ingresa la mora diaria" required min="0">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    <input type="submit" class="btn btn-info" value="Guardar" name="Guardar">
                    <a class="btn btn-danger" href="${contextPath}/roles.do?op=listar">Cancelar</a>
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