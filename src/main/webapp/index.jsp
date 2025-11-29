<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Biblioteca UDB - Inicio</title>
</head>
<body>
<div class="container mt-4">
    <h1>📚 Bienvenido a la Biblioteca Virtual de la Universidad Don Bosco</h1>
    <p>Explora nuestro catálogo, gestiona préstamos y más.</p>

    <c:if test="${empty sessionScope.usuario}">
        <div class="alert alert-info">
            👋 Por favor, inicia sesión para acceder a todas las funcionalidades.
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.usuario}">
        <div class="alert alert-success">
            👋 Hola, ${sessionScope.usuario.nombre} ${sessionScope.usuario.apellido}!
            <br> Rol: ${sessionScope.usuario.rol.nombre_rol}
        </div>
    </c:if>
</div>
</body>
</html>