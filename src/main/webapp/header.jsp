<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<link href="${contextPath}/assets/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
<link href="${contextPath}/assets/css/alertify.core.css" rel="stylesheet" type="text/css"/>
<link href="${contextPath}/assets/css/alertify.default.css" rel="stylesheet" type="text/css"/>
<link href="${contextPath}/assets/css/estilos.css" rel="stylesheet" type="text/css"/>

<script src="${contextPath}/assets/js/jquery-1.12.0.min.js" type="text/javascript"></script>
<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
<script src="${contextPath}/assets/js/alertify.js" type="text/javascript"></script>
<script src="${contextPath}/assets/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="${contextPath}/assets/js/dataTables.bootstrap.min.js" type="text/javascript"></script>

<nav class="navbar navbar-custom navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Navegación</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            
            <a class="navbar-brand text-center" href="${contextPath}/index.jsp">
                <img src="${contextPath}/assets/img/udb.png" alt="UDB Logo">
                <span>UNIVERSIDAD<br>DON BOSCO</span>
            </a>
        </div>

        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                
                <c:if test="${empty sessionScope.usuario}">
                    <li><a href="${contextPath}/index.jsp">Inicio</a></li>
                    <li><a href="${contextPath}/login.jsp">Login</a></li>
                </c:if>

                <c:if test="${not empty sessionScope.usuario}">
                    <li><a href="${contextPath}/index.jsp">Inicio</a></li>
                    
                    <c:if test="${sessionScope.usuario.rol.nombre_rol ne 'Administrador'}">
                        <li><a href="${contextPath}/reservas.jsp">Reservas</a></li>
                        <li><a href="${contextPath}/prestamos.jsp">Préstamos</a></li>
                    </c:if>

                    <c:if test="${sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown">Catálogos <span class="caret"></span></a>
                            <ul class="dropdown-menu">
                                <li><a href="${contextPath}/generos.do?op=listar">Géneros</a></li>
                                <li><a href="${contextPath}/autores.do?op=listar">Autores</a></li>
                                <li><a href="${contextPath}/editoriales.do?op=listar">Editoriales</a></li>
                                <li><a href="${contextPath}/tiposdocumento.do?op=listar">Tipos Doc</a></li>
                            </ul>
                        </li>
                        <li><a href="${contextPath}/ejemplares.do?op=listar">Ejemplares</a></li>
                        <li><a href="${contextPath}/usuarios.do?op=listar">Usuarios</a></li>
                    </c:if>

                    <li><a href="${contextPath}/logout">LogOut</a></li>
                </c:if>
            </ul>
        </div>
    </div>
</nav>