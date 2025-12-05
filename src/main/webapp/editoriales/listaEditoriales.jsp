<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <title>Lista de Editoriales</title>
</head>
<body>
    <div class="container ">
        <div class="row pt-5">
            <h3>Lista de Editoriales</h3>
        </div>
        <div class="row">
            <div class="col-md-12">
                <a type="button" class="btn btn-primary btn-md" href="${contextPath}/editoriales.do?op=nuevo">Nueva Editorial</a>
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
                        <c:forEach items="${requestScope.listaEditoriales}" var="editorial">
                            <tr>
                                <td>${editorial.id_editorial}</td>
                                <td>${editorial.nombre_editorial}</td>
                                <td>
                                    <a class="btn btn-primary" href="${contextPath}/editoriales.do?op=obtener&id=${editorial.id_editorial}">
                                        <span class="glyphicon glyphicon-edit"></span> Editar
                                    </a>
                                    <a class="btn btn-danger" href="javascript:eliminar('${editorial.id_editorial}')">
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
            alertify.confirm("¿Realmente desea eliminar esta editorial?", function(e){
                if(e){
                    location.href="${contextPath}/editoriales.do?op=eliminar&id="+ id;
                }
            });
        }
    </script>
</body>
</html>