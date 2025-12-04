<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
    <head>
        <title>Editar Rol</title>
    </head>
    <body>
        <div class="container">
            <div class="row">
                <h3>Editar Rol</h3>
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
                    <form id="rolForm" role="form">
                        <!-- Elimina el input hidden de op -->
                        <input type="hidden" id="id_rol" value="${rol.id_rol}">
                        <div class="well well-sm">
                            <strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong>
                        </div>
                        <div class="form-group">
                            <label for="nombre_rol">Nombre del Rol</label>
                            <div class="input-group">
                                <input type="text" class="form-control" name="nombre_rol" id="nombre_rol" value="${rol.nombre_rol}" placeholder="Ingresa el nombre del rol" required>
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="cant_max_prestamo">Cantidad Máxima de Préstamo</label>
                            <div class="input-group">
                                <input type="number" class="form-control" name="cant_max_prestamo" id="cant_max_prestamo" value="${rol.cant_max_prestamo}" placeholder="Ingresa la cantidad máxima de préstamos" required min="0">
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="dias_prestamo">Días de Préstamo</label>
                            <div class="input-group">
                                <input type="number" class="form-control" name="dias_prestamo" id="dias_prestamo" value="${rol.dias_prestamo}" placeholder="Ingresa los días de préstamo" required min="1">
                                <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="mora_diaria">Mora Diaria</label>
                            <div class="input-group">
                                <input type="number" step="0.01" class="form-control" name="mora_diaria" id="mora_diaria" value="${rol.mora_diaria}" placeholder="Ingresa la mora diaria" required min="0">
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
            document.getElementById('rolForm').addEventListener('submit', function (e) {
                e.preventDefault();

                console.log('Formulario enviado, iniciando fetch...');

                // Obtener valores directamente de los campos
                const data = {
                    id_rol: parseInt(document.getElementById('id_rol').value),
                    nombre_rol: document.getElementById('nombre_rol').value,
                    cant_max_prestamo: parseInt(document.getElementById('cant_max_prestamo').value),
                    dias_prestamo: parseInt(document.getElementById('dias_prestamo').value),
                    mora_diaria: parseFloat(document.getElementById('mora_diaria').value)
                };

                console.log('Datos enviados:', data);

                fetch('${contextPath}/roles.do?op=modificar', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(data)
                })
                .then(response => {
                    console.log('Respuesta recibida:', response.status);
                    return response.json();
                })
                .then(result => {
                    console.log('JSON recibido:', result);
                    if (result.success) {
                        alertify.success(result.message);
                        window.location.href = '${contextPath}/roles.do?op=listar';
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