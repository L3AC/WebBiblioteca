<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <title>Nueva Editorial</title>
</head>
<body>
    <div class="container">
        <div class="row">
            <h3>Nueva Editorial</h3>
        </div>
        
        <!-- Mensajes de éxito/error -->
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
                <strong>? Errores de validación:</strong>
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
                <form id="editorialForm" role="form" method="POST">
                    <input type="hidden" name="op" value="insertar">
                    <div class="well well-sm">
                        <strong><span class="glyphicon glyphicon-asterisk"></span>Campos requeridos</strong>
                    </div>
                    
                    <div class="form-group">
                        <label for="nombre_editorial">Nombre de la Editorial</label>
                        <div class="input-group">
                            <input type="text" class="form-control" name="nombre_editorial" id="nombre_editorial" 
                                   placeholder="Ingresa el nombre de la editorial" required>
                            <span class="input-group-addon"><span class="glyphicon glyphicon-asterisk"></span></span>
                        </div>
                    </div>
                    
                    <input type="submit" class="btn btn-info" value="Guardar" name="Guardar">
                    <a class="btn btn-danger" href="${contextPath}/editoriales.do?op=listar">Cancelar</a>
                </form>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('editorialForm').addEventListener('submit', function(e) {
            e.preventDefault();

            console.log('Formulario enviado, iniciando fetch...');

            const formData = new FormData(this);
            const data = Object.fromEntries(formData.entries());

            console.log('Datos enviados:', data);

            fetch('${contextPath}/editoriales.do?op=insertar', {
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
                    window.location.href = '${contextPath}/editoriales.do?op=listar';
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