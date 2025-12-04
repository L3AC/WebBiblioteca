<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <title>Mis Préstamos</title>
</head>
<body>
<div class="container" style="margin-top: 30px;">
    <div class="row">
        <div class="col-md-12">
            <h3><span class="glyphicon glyphicon-list-alt"></span> Gestión de Préstamos</h3>
            <hr>
            <div class="table-responsive">
                <table class="table table-custom">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Ejemplar</th>
                        <th>Código Copia</th>
                        <c:if test="${sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
                            <th>Usuario</th>
                        </c:if>
                        <th>F. Préstamo</th>
                        <th>F. Devolución</th>
                        <th>Mora</th>
                        <th>Estado</th>
                    </tr>
                    </thead>
                    <tbody id="bodyPrestamos"></tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() { cargarPrestamos(); });

    function cargarPrestamos() {
        $.ajax({
            url: '${contextPath}/prestamos.do?op=listar',
            type: 'GET',
            dataType: 'json',
            success: function(data) {
                let html = '';
                let isAdmin = ${sessionScope.usuario.rol.nombre_rol eq 'Administrador'};

                if(data.length === 0) {
                    let colSpan = isAdmin ? 8 : 7;
                    html = '<tr><td colspan="' + colSpan + '" class="text-center">No hay préstamos registrados.</td></tr>';
                } else {
                    data.forEach(p => {
                        let moraVal = parseFloat(p.mora);
                        let moraHtml = moraVal > 0 ? '<span class="text-danger font-weight-bold">$' + p.mora + '</span>' : '<span class="text-success">$0.00</span>';
                        let fechaDev = p.fecha_fin ? p.fecha_fin : '<span class="label label-warning">Pendiente</span>';
                        
                        // Formateo de estado
                        let estadoClass = 'label-default';
                        if(p.estado === 'Activo') estadoClass = 'label-primary';
                        else if(p.estado === 'Devuelto') estadoClass = 'label-success';
                        else if(p.estado === 'Mora') estadoClass = 'label-danger';
                        
                        let estadoHtml = '<span class="label ' + estadoClass + '">' + p.estado + '</span>';

                        html += '<tr>' +
                                '<td>' + p.id + '</td>' +
                                '<td>' + p.titulo + '</td>' +
                                '<td><span class="label label-info">' + p.codigo + '</span></td>';
                        
                        if(isAdmin) {
                            html += '<td>' + p.usuario + '</td>';
                        }

                        html += '<td>' + p.fecha_inicio + '</td>' +
                                '<td>' + fechaDev + '</td>' +
                                '<td>' + moraHtml + '</td>' +
                                '<td>' + estadoHtml + '</td>' +
                                '</tr>';
                    });
                }
                $('#bodyPrestamos').html(html);
            },
            error: function(err) {
                console.error("Error cargando préstamos", err);
                $('#bodyPrestamos').html('<tr><td colspan="8" class="text-center text-danger">Error al cargar los datos.</td></tr>');
            }
        });
    }
</script>
</body>
</html>