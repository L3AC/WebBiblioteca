<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <title>Editar Autor</title>
</head>
<body>
    <div class="container">
        <div class="row">
            <h3>Editar Autor</h3>
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
                
                <form id="autorForm" role="form" method="POST">
                    <input type="hidden" name="op" value="modificar">
                    <input type="hidden" name="id_autor" value="${autor.id_autor}">
                    <div class="well well-sm">
                        <strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong>
                    </div>
                    
                    <div class="form-group">
                        <label for="nombre_autor">Nombre del Autor</label>
                        <div class="input-group">
                            <input type="text" class="form-control" name="nombre_autor" id="nombre_autor" 
                                   value="${autor.nombre_autor}" placeholder="Ingresa el nombre del autor" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    
                    <input type="submit" class="btn btn-info" value="Guardar" name="Guardar">
                    <a class="btn btn-danger" href="${contextPath}/autores.do?op=listar">Cancelar</a>
                </form>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('autorForm').addEventListener('submit', function (e) {
            e.preventDefault();

            console.log('Formulario enviado, iniciando fetch...');

            const formData = new FormData(this);
            const data = Object.fromEntries(formData.entries());

            // Convertir id_autor a número si es necesario
            if (data.id_autor) {
                data.id_autor = parseInt(data.id_autor, 10);
            }

            console.log('Datos enviados:', data);

            fetch('${contextPath}/autores.do?op=modificar', {
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
                    window.location.href = '${contextPath}/autores.do?op=listar';
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