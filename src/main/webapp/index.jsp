<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Biblioteca UDB - Inicio</title>
    <style>
        /* Estilos ligeros inline para complementar Bootstrap 3 sin tocar tu CSS principal */
        .icon-large {
            font-size: 40px;
            margin-bottom: 10px;
            color: #1a428a; /* Azul UDB */
        }
        .welcome-card {
            background-color: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
        }
        /* Ajuste para que tus result-cards funcionen como botones grandes */
        .dashboard-item {
            display: block;
            text-decoration: none !important;
            height: auto;
        }
        .result-card {
            height: 140px;
            text-align: center;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 15px;
        }
        .result-title {
            font-size: 18px;
            font-weight: 600;
            margin: 8px 0 5px 0;
        }
        .result-type {
            font-size: 12px;
            color: #666;
        }
    </style>
</head>
<body>

<div class="container" style="margin-top: 20px;">
    
    <div class="welcome-card text-center">
        <img src="${contextPath}/assets/img/udb.png" alt="Logo UDB" style="height: 80px; margin-bottom: 15px;">
        <h2 style="margin-top: 0; font-weight: bold; color: #333;">Biblioteca Virtual UDB</h2>
        <p class="lead">Acceso a recursos bibliográficos, reservas y préstamos.</p>
        
        <c:if test="${not empty sessionScope.usuario}">
            <hr>
            <h4>Hola, <strong>${sessionScope.usuario.nombre} ${sessionScope.usuario.apellido}</strong></h4>
            <span class="label label-primary" style="font-size: 14px;">${sessionScope.usuario.rol.nombre_rol}</span>
        </c:if>
    </div>

    <c:if test="${not empty param.msg}">
        <div class="alert alert-info alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <strong>Información:</strong> ${param.msg}
        </div>
    </c:if>

    <c:if test="${empty sessionScope.usuario}">
        <div class="row">
            <div class="col-md-6 col-md-offset-3">
                <div class="alert alert-warning text-center">
                    <span class="glyphicon glyphicon-info-sign"></span> 
                    Para acceder a todas las funciones, por favor <a href="${contextPath}/login.jsp" class="alert-link">Inicia Sesión</a>.
                </div>
            </div>
        </div>
        
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <a href="${contextPath}/catalogos.jsp" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-search icon-large"></span>
                        <div class="result-title" style="font-size: 20px;">Catálogo Público</div>
                        <div class="result-type">Explorar ejemplares disponibles</div>
                    </div>
                </a>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol ne 'Administrador'}">
        <div class="row">
            <div class="col-md-4">
                <a href="${contextPath}/catalogos.jsp" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-book icon-large"></span>
                        <div class="result-title">Catálogo</div>
                        <div class="result-type">Buscar y reservar</div>
                    </div>
                </a>
            </div>

            <div class="col-md-4">
                <a href="${contextPath}/prestamos.jsp" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-list-alt icon-large"></span>
                        <div class="result-title">Mis Préstamos</div>
                        <div class="result-type">Historial y activos</div>
                    </div>
                </a>
            </div>

            <div class="col-md-4">
                <a href="${contextPath}/reservas.jsp" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-bookmark icon-large"></span>
                        <div class="result-title">Mis Reservas</div>
                        <div class="result-type">Estado de solicitudes</div>
                    </div>
                </a>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty sessionScope.usuario and sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
        <div class="row">
            <div class="col-md-12">
                <h4 style="color: white; text-shadow: 1px 1px 2px black; margin-bottom: 20px;">
                    <span class="glyphicon glyphicon-cog"></span> Gestión de Biblioteca
                </h4>
            </div>
        </div>

        <div class="row">
            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/libros.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-book icon-large"></span>
                        <div class="result-title">Libros</div>
                        <div class="result-type">Administrar catálogo</div>
                    </div>
                </a>
            </div>

            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/ejemplares.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-duplicate icon-large"></span>
                        <div class="result-title">Ejemplares</div>
                        <div class="result-type">Gestión de copias</div>
                    </div>
                </a>
            </div>

            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/usuarios.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-user icon-large"></span>
                        <div class="result-title">Usuarios</div>
                        <div class="result-type">Administrar cuentas</div>
                    </div>
                </a>
            </div>

            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/roles.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-lock icon-large"></span>
                        <div class="result-title">Roles</div>
                        <div class="result-type">Permisos y accesos</div>
                    </div>
                </a>
            </div>
        </div>
        
        <div class="row" style="margin-top: 15px;">
            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/autores.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-pencil icon-large"></span>
                        <div class="result-title">Autores</div>
                    </div>
                </a>
            </div>
            
            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/editoriales.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-print icon-large"></span>
                        <div class="result-title">Editoriales</div>
                    </div>
                </a>
            </div>

            <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/generos.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-tags icon-large"></span>
                        <div class="result-title">Géneros</div>
                    </div>
                </a>
            </div>
            
             <div class="col-md-3 col-sm-6">
                <a href="${contextPath}/tiposdocumentodetalle.do?op=listar" class="dashboard-item">
                    <div class="result-card">
                        <span class="glyphicon glyphicon-file icon-large"></span>
                        <div class="result-title">Tipos Doc.</div>
                    </div>
                </a>
            </div>
        </div>
    </c:if>
</div>

</body>
</html>