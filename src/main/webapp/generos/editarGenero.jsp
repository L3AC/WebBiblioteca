<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <title>Editar Género</title>
</head>
<body>
    <div class="container">
        <div class="row">
            <h3>Editar Género</h3>
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
                
                <form id="generoForm" role="form" method="POST">
                    <input type="hidden" name="op" value="modificar">
                    <input type="hidden" name="id_genero" value="${genero.id_genero}">
                    <div class="well well-sm">
                        <strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong>
                    </div>
                    
                    <div class="form-group">
                        <label for="nombre_genero">Nombre del Género</label>
                        <div class="input-group">
                            <input type="text" class="form-control" name="nombre_genero" id="nombre_genero" 
                                   value="${genero.nombre_genero}" placeholder="Ingresa el nombre del género" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    
                    <input type="submit" class="btn btn-info" value="Guardar" name="Guardar">
                    <a class="btn btn-danger" href="${contextPath}/generos.do?op=listar">Cancelar</a>
                </form>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('generoForm').addEventListener('submit', function (e) {
            e.preventDefault();

            console.log('Formulario enviado, iniciando fetch...');

            const formData = new FormData(this);
            const data = Object.fromEntries(formData.entries());

            // Convertir id_genero a número si es necesario
            if (data.id_genero) {
                data.id_genero = parseInt(data.id_genero, 10);
            }

            console.log('Datos enviados:', data);

            fetch('${contextPath}/generos.do?op=modificar', {
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
                    window.location.href = '${contextPath}/generos.do?op=listar';
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