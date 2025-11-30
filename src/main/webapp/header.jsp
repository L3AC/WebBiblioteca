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

<!-- ESTILO PERSONALIZADO PARA COINCIDIR CON TU DISEÑO -->
<style>
    .navbar-custom {
        background-color: #009688; /* Azul turquesa */
        border: none;
        padding: 10px 0;
        min-height: 60px;
    }
    .navbar-custom .navbar-brand {
        color: white !important;
        font-weight: bold;
        font-size: 16px;
        display: flex;
        align-items: center;
        padding: 0 15px;
    }
    .navbar-custom .navbar-brand img {
        width: 50px;
        height: 50px;
        margin-right: 10px;
    }
    .navbar-custom .nav-link {
        color: white !important;
        font-weight: 500;
        padding: 10px 15px;
        font-size: 16px;
    }
    .navbar-custom .nav-link:hover,
    .navbar-custom .dropdown-item:hover {
        color: #ffeb3b !important;
        background-color: rgba(255, 255, 255, 0.1);
    }
    .navbar-custom .dropdown-menu {
        background-color: #fff;
        border: 1px solid #ddd;
        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }
    .navbar-custom .dropdown-menu .dropdown-item {
        color: #333;
        padding: 10px 20px;
    }
    .navbar-custom .navbar-nav {
        margin-left: auto;
    }
    .navbar-custom .navbar-text {
        color: white;
        font-size: 14px;
        margin: 0;
    }
</style>

<!-- Encabezado con menú dinámico -->
<nav class="navbar navbar-custom navbar-fixed-top">
    <div class="container-fluid">
        <!-- Logo y nombre de la universidad -->
        <a class="navbar-brand" href="${contextPath}/index.jsp">
            <img src="${contextPath}/assets/img/logo.png" alt="Logo" class="d-inline-block align-text-top">
            <span>UNIVERSIDAD DON BOSCO</span>
        </a>

        <!-- Botón para móviles -->
        <button class="navbar-toggle collapsed" type="button" data-toggle="collapse" data-target="#navbarNav" aria-expanded="false">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>

        <!-- Menú -->
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="nav navbar-nav">

                <!-- ========== CASO 1: SIN SESIÓN (INVITADO) ========== -->
                <c:if test="${empty sessionScope.usuario}">
                    <li><a href="${contextPath}/index.jsp">Inicio</a></li>
                    <li><a href="${contextPath}/login.jsp">Login</a></li>
                </c:if>

                <!-- ========== CASO 2: USUARIO LOGUEADO (ALUMNO O PROFESOR) ========== -->
                <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol ne 'Administrador'}">
                    <li><a href="${contextPath}/index.jsp">Inicio</a></li>
                    <li><a href="${contextPath}/reservas.jsp">Reservas</a></li>
                    <li><a href="${contextPath}/prestamos.jsp">Préstamos</a></li>
                    <li><a href="${contextPath}/logout" onclick="return confirm('¿Estás seguro de cerrar sesión?');">LogOut</a></li>
                </c:if>

                <!-- ========== CASO 3: ADMINISTRADOR ========== -->
                <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
                    <!-- Catálogos con submenu -->
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                            Catálogos <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a href="${contextPath}/catalogos.jsp?tipo=genero">Género</a></li>
                            <li><a href="${contextPath}/catalogos.jsp?tipo=autores">Autores</a></li>
                            <li><a href="${contextPath}/catalogos.jsp?tipo=editoriales">Editoriales</a></li>
                        </ul>
                    </li>
                    <li><a href="${contextPath}/ejemplares.jsp">Ejemplares</a></li>
                    <!-- Préstamos con submenu -->
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                            Préstamos <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a href="${contextPath}/prestamos.jsp?accion=reservas">Reservas</a></li>
                            <li><a href="${contextPath}/prestamos.jsp?accion=devolucion">Devolución</a></li>
                        </ul>
                    </li>
                    <li><a href="${contextPath}/usuarios.jsp">Usuarios</a></li>
                    <li><a href="${contextPath}/roles.jsp">Roles</a></li>
                    <li><a href="${contextPath}/logout" onclick="return confirm('¿Estás seguro de cerrar sesión?');">LogOut</a></li>
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