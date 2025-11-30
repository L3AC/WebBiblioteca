<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Biblioteca UDB - Inicio</title>
</head>
<body>
<div class="container mt-4">
    <div class="text-center">
        <h1>📚 Bienvenido a la Biblioteca Virtual de la Universidad Don Bosco</h1>
        <p class="lead">Explora nuestro catálogo, gestiona préstamos y más.</p>
    </div>

    <!-- ========== CONTENIDO SEGÚN ROL ========== -->

    <!-- ========== CASO 1: SIN SESIÓN (INVITADO) ========== -->
    <c:if test="${empty sessionScope.usuario}">
        <div class="alert alert-info">
            👋 Por favor, <a href="${contextPath}/login.jsp">inicia sesión</a> o <a href="${contextPath}/registro.jsp">regístrate</a> para acceder a todas las funcionalidades.
        </div>
        <div class="card mt-4">
            <div class="card-body">
                <h5>🔍 Explora nuestro catálogo</h5>
                <p>Puedes navegar por los libros disponibles sin iniciar sesión.</p>
                <a href="${contextPath}/catalogos.jsp" class="btn btn-primary">Ver Catálogos</a>
            </div>
        </div>
    </c:if>

    <!-- ========== CASO 2: USUARIO LOGUEADO (ALUMNO O PROFESOR) ========== -->
    <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol ne 'Administrador'}">
        <div class="alert alert-success">
            👋 Hola, ${sessionScope.usuario.nombre} ${sessionScope.usuario.apellido}!<br>
            Rol: <strong>${sessionScope.usuario.rol.nombre_rol}</strong>
        </div>
        <div class="card mt-4">
            <div class="card-body">
                <h5>📖 Mis Préstamos</h5>
                <p>Revisa los libros que tienes prestados actualmente.</p>
                <a href="${contextPath}/prestamos.jsp" class="btn btn-primary">Ver Préstamos</a>
            </div>
        </div>
        <div class="card mt-4">
            <div class="card-body">
                <h5>📌 Mis Reservas</h5>
                <p>Consulta los libros que has reservado.</p>
                <a href="${contextPath}/reservas.jsp" class="btn btn-primary">Ver Reservas</a>
            </div>
        </div>
    </c:if>

    <!-- ========== CASO 3: ADMINISTRADOR ========== -->
    <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
        <div class="alert alert-success">
            👋 Hola, ${sessionScope.usuario.nombre} ${sessionScope.usuario.apellido}!<br>
            Rol: <strong>${sessionScope.usuario.rol.nombre_rol}</strong>
        </div>
        <div class="card mt-4">
            <div class="card-body">
                <h5>⚙️ Panel de Administración</h5>
                <p>Gestiona catálogos, ejemplares, usuarios y roles.</p>
                <div class="btn-group" role="group">
                    <a href="${contextPath}/catalogos.jsp" class="btn btn-outline-primary">Catálogos</a>
                    <a href="${contextPath}/ejemplares.jsp" class="btn btn-outline-primary">Ejemplares</a>
                    <a href="${contextPath}/usuarios.jsp" class="btn btn-outline-primary">Usuarios</a>
                    <a href="${contextPath}/roles.jsp" class="btn btn-outline-primary">Roles</a>
                </div>
            </div>
        </div>
    </c:if>
</div>
</body>
</html>