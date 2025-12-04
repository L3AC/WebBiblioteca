<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <title>Mis Reservas</title>
</head>
<body>
<div class="container" style="margin-top: 30px;">
    <div class="row">
        <div class="col-md-12">
            <h3><span class="glyphicon glyphicon-bookmark"></span> Gestión de Reservas</h3>
            <hr>
            <div class="table-responsive">
                <table class="table table-custom">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Fecha</th>
                        <th>Ejemplar</th>
                        <th>Código Copia</th>
                        <c:if test="${sessionScope.usuario.rol.nombre_rol eq 'Administrador'}">
                            <th>Usuario</th>
                        </c:if>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody id="bodyReservas"></tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() { cargarReservas(); });

    function cargarReservas() {
        $.ajax({
            url: '${contextPath}/reservas.do?op=listar',
            type: 'GET',
            dataType: 'json',
            success: function(data) {
                let html = '';
                let isAdmin = ${sessionScope.usuario.rol.nombre_rol eq 'Administrador'};

                if(data.length === 0) {
                    html = '<tr><td colspan="6" class="text-center">No hay reservas activas.</td></tr>';
                } else {
                    data.forEach(r => {
                        html += '<tr><td>'+r.id_reserva+'</td><td>'+r.fecha+'</td><td>'+r.titulo+'</td><td><span class="label label-info">'+r.codigo+'</span></td>';
                        if(isAdmin) html += '<td>'+r.usuario+' ('+r.correo+')</td>';
                        html += '<td><button class="btn btn-danger btn-sm" onclick="cancelarReserva('+r.id_reserva+')"><span class="glyphicon glyphicon-remove"></span> Cancelar</button></td></tr>';
                    });
                }
                $('#bodyReservas').html(html);
            }
        });
    }

    function cancelarReserva(id) {
        alertify.confirm("¿Cancelar reserva?", function(e){
            if(e){
                $.ajax({
                    url: '${contextPath}/reservas.do?op=eliminar',
                    type: 'POST',
                    data: { idReserva: id },
                    dataType: 'json',
                    success: function(resp) {
                        if(resp.success) { alertify.success(resp.message); cargarReservas(); }
                        else alertify.error(resp.message);
                    }
                });
            }
        });
    }
</script>
</body>
</html>