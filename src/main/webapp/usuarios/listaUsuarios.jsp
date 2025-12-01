<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %> <!-- ? ESTE ES EL HEADER DINÁMICO -->
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Usuarios</title>
</head>
<body>
    <div class="container ">
        <div class="row pt-5">
            <h3>Lista de Usuarios</h3>
        </div>
        <div class="row">
            <div class="col-md-12">
                <a type="button" class="btn btn-primary btn-md" href="${contextPath}/usuarios.do?op=nuevo">Nuevo Usuario</a>
                <br/><br/>
                <table class="table table-striped table-bordered table-hover" id="tabla">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Apellido</th>
                            <th>Correo</th>
                            <th>Rol</th>
                            <th>Operaciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${requestScope.listaUsuarios}" var="usuario">
                            <tr>
                                <td>${usuario.id_usuario}</td>
                                <td>${usuario.nombre}</td>
                                <td>${usuario.apellido}</td>
                                <td>${usuario.correo}</td>
                                <td>${usuario.rol.nombre_rol}</td>
                                <td>
                                    <a class="btn btn-primary" href="${contextPath}/usuarios.do?op=obtener&id=${usuario.id_usuario}">
                                        <span class="glyphicon glyphicon-edit"></span> Editar
                                    </a>
                                    <a class="btn btn-danger" href="javascript:eliminar('${usuario.id_usuario}')">
                                        <span class="glyphicon glyphicon-trash"></span> Eliminar
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
            alertify.confirm("¿Realmente desea eliminar este usuario?", function(e){
                if(e){
                    location.href="${contextPath}/usuarios.do?op=eliminar&id="+ id;
                }
            });
        }
    </script>
</body>
</html>