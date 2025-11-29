<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!-- CSS y JS -->
<link href="${contextPath}/assets/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
<link href="${contextPath}/assets/css/alertify.core.css" rel="stylesheet" type="text/css"/>
<link href="${contextPath}/assets/css/alertify.default.css" rel="stylesheet" type="text/css"/>
<script src="${contextPath}/assets/js/jquery-1.12.0.min.js" type="text/javascript"></script>
<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
<script src="${contextPath}/assets/js/alertify.js" type="text/javascript"></script>
<script src="${contextPath}/assets/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="${contextPath}/assets/js/dataTables.bootstrap.min.js" type="text/javascript"></script>

<!-- Encabezado con menú dinámico -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <!-- Logo -->
        <a class="navbar-brand" href="${contextPath}/index.jsp">
            <img src="${contextPath}/assets/img/logo.png" alt="Logo" width="40" height="40" class="d-inline-block align-text-top me-2">
            Universidad Don Bosco
        </a>

        <!-- Botón para móviles -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- Menú -->
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">

                <!-- ========== CASO 1: SIN SESIÓN (INVITADO) ========== -->
                <c:if test="${empty sessionScope.usuario}">
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/catalogos.jsp">Catálogos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/login.jsp">Login</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/registro.jsp">Registrarse</a>
                    </li>
                </c:if>

                <!-- ========== CASO 2: USUARIO LOGUEADO (ALUMNO O PROFESOR) ========== -->
                <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol ne 'Administrador'}">
                    <li class="nav-item">
                        <form class="d-flex" style="width: 300px;">
                            <input class="form-control me-2" type="search" placeholder="Buscar libros..." aria-label="Search">
                            <button class="btn btn-outline-light" type="submit">🔍</button>
                        </form>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/reservas.jsp">Reservas</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/prestamos.jsp">Préstamos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/logout" onclick="return confirm('¿Estás seguro de cerrar sesión?');">LogOut</a>
                    </li>
                </c:if>

                <!-- ========== CASO 3: ADMINISTRADOR ========== -->
                <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/catalogos.jsp">Catálogos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/ejemplares.jsp">Ejemplares</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/prestamos.jsp">Préstamos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/usuarios.jsp">Usuarios</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/roles.jsp">Roles</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${contextPath}/logout" onclick="return confirm('¿Estás seguro de cerrar sesión?');">LogOut</a>
                    </li>
                </c:if>

            </ul>
        </div>
    </div>
</nav>

<!-- Script para alertas -->
<script>
    $(document).ready(function() {
        const urlParams = new URLSearchParams(window.location.search);
        const msg = urlParams.get('msg');
        if (msg) {
            alertify.alert("Mensaje", decodeURIComponent(msg), function(){});
        }
    });
</script>