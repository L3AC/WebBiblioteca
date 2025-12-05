<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %> <!-- ? ESTE ES EL HEADER DINÁMICO -->
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Roles</title>
</head>
<body>
    <div class="container ">
        <div class="row pt-5">
            <h3>Lista de Roles</h3>
        </div>
        <div class="row">
            <div class="col-md-12">
                <br/><br/>
                <table class="table table-striped table-bordered table-hover" id="tabla">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Cant. Max. Préstamo</th>
                            <th>Días Préstamo</th>
                            <th>Mora Diaria</th>
                            <th>Operaciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${requestScope.listaRoles}" var="rol">
                            <tr>
                                <td>${rol.id_rol}</td>
                                <td>${rol.nombre_rol}</td>
                                <td>${rol.cant_max_prestamo}</td>
                                <td>${rol.dias_prestamo}</td>
                                <td>${rol.mora_diaria}</td>
                                <td>
                                    <a class="btn btn-primary" href="${contextPath}/roles.do?op=obtener&id=${rol.id_rol}">
                                        <span class="glyphicon glyphicon-edit"></span> Editar
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <script>
        $(document).ready(function(){
           $('#tabla').DataTable();
        });

        <c:if test="${not empty exito}">
            alertify.success('${exito}');
            <c:set var="exito" value="" scope="session" />
        </c:if>
        <c:if test="${not empty fracaso}">
            alertify.error('${fracaso}');
            <c:set var="fracaso" value="" scope="session" />
        </c:if>

        function eliminar(id){
            alertify.confirm("¿Realmente desea eliminar este rol?", function(e){
                if(e){
                    location.href="${contextPath}/roles.do?op=eliminar&id="+ id;
                }
            });
        }
    </script>
</body>
</html>