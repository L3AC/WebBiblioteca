<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %> <!-- ? ESTE ES EL HEADER DINÁMICO -->
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Géneros</title>
</head>
<body>
    <div class="container ">
        <div class="row pt-5">
            <h3>Lista de Géneros</h3>
        </div>
        <div class="row">
            <div class="col-md-12">
                <a type="button" class="btn btn-primary btn-md" href="${contextPath}/generos.do?op=nuevo">Nuevo Género</a>
                <br/><br/>
                <table class="table table-striped table-bordered table-hover" id="tabla">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Operaciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${requestScope.listaGeneros}" var="genero">
                            <tr>
                                <td>${genero.id_genero}</td>
                                <td>${genero.nombre_genero}</td>
                                <td>
                                    <a class="btn btn-primary" href="${contextPath}/generos.do?op=obtener&id=${genero.id_genero}">
                                        <span class="glyphicon glyphicon-edit"></span> Editar
                                    </a>
                                    <a class="btn btn-danger" href="javascript:eliminar('${genero.id_genero}')">
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
            alertify.confirm("¿Realmente desea eliminar este género?", function(e){
                if(e){
                    location.href="${contextPath}/generos.do?op=eliminar&id="+ id;
                }
            });
        }
    </script>
</body>
</html>