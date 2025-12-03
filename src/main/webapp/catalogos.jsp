<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <title>Catálogo Público - Biblioteca UDB</title>
    <style>
        /* Estilos específicos para esta vista basados en tu imagen */
        .search-container {
            background-color: #f8f9fa;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05);
        }
        .badge-tipo {
            padding: 5px 10px;
            border-radius: 15px;
            font-size: 12px;
            font-weight: normal;
        }
        .badge-Libro { background-color: #e1bee7; color: #4a148c; } /* Lila */
        .badge-Tesis { background-color: #ffccbc; color: #bf360c; } /* Naranja suave */
        .badge-Revistas { background-color: #b2dfdb; color: #004d40; } /* Verde azulado */
        .badge-default { background-color: #cfd8dc; color: #37474f; }

        .disponibles-tag {
            font-size: 12px;
            color: #28a745;
            font-weight: bold;
            float: right;
        }
        .nodisponibles-tag {
            font-size: 12px;
            color: #dc3545;
            font-weight: bold;
            float: right;
        }
    </style>
</head>
<body>

<div class="container">
    <div class="row">
        <div class="col-md-12 text-center">
            <h2 class="section-title" style="margin-top: 20px;">Catálogo Público</h2>
            <p class="text-muted">Busca en nuestra colección de libros, revistas y documentos.</p>
        </div>
    </div>

    <div class="row">
        <div class="col-md-8 col-md-offset-2">
            <div class="search-container text-center">
                <div class="input-group input-group-lg">
                    <input type="text" id="txtBuscar" class="form-control search-input" placeholder="Buscar por título, autor o palabra clave..." aria-describedby="btnBuscar">
                    <span class="input-group-btn">
                        <button class="btn btn-primary" type="button" id="btnBuscar">
                            <span class="glyphicon glyphicon-search"></span>
                        </button>
                    </span>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <h4 style="margin-bottom: 20px; font-weight: bold;">Resultados de Búsqueda</h4>
            <div id="resultados-container">
                <div class="alert alert-info text-center">
                    Escribe algo en el buscador para comenzar.
                </div>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="modalDetalle" tabindex="-1" role="dialog" aria-labelledby="modalDetalleLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header" style="background-color: #1a428a; color: white;">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close" style="color: white;"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modalDetalleLabel">
                    <span class="glyphicon glyphicon-book"></span> Detalles del Ejemplar
                </h4>
            </div>
            <div class="modal-body">
                <div id="loading-detalle" class="text-center" style="display:none;">
                    <img src="${contextPath}/assets/img/loading.gif" height="40"> Cargando...
                </div>

                <div id="contenido-detalle">
                    <h3 id="det-titulo" style="margin-top: 0; color: #1a428a;"></h3>
                    <p class="lead text-muted" id="det-autor"></p>
                    <hr>

                    <div class="row">
                        <div class="col-md-6">
                            <p><strong>Tipo:</strong> <span id="det-tipo" class="badge"></span></p>
                            <p><strong>Ubicación:</strong> <span id="det-ubicacion"></span></p>
                        </div>
                        <div class="col-md-6" id="det-especificos">
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cerrar</button>
                <c:if test="${not empty sessionScope.usuario}">
                    <button type="button" class="btn btn-primary" id="btn-reservar-modal">Reservar</button>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script>
    const usuarioLogueado = ${not empty sessionScope.usuario};

    $(document).ready(function() {

        // Eventos de búsqueda
        $('#txtBuscar').on('keypress', function(e) {
            if(e.which === 13) realizarBusqueda();
        });
        $('#btnBuscar').on('click', realizarBusqueda);

        // --- NUEVO: Evento Doble Clic en las tarjetas ---
        $('#resultados-container').on('dblclick', '.result-card', function() {
            let id = $(this).data('id');
            // AGREGA ESTO: Redirección al nuevo archivo
            window.location.href = '${contextPath}/detalleEjemplar.jsp?id=' + id;
        });

        // Carga inicial
        realizarBusqueda();
    });

    function realizarBusqueda() {
        const criterio = $('#txtBuscar').val();
        const container = $('#resultados-container');

        container.html('<div class="text-center text-muted">Buscando...</div>');

        $.ajax({
            url: '${contextPath}/ejemplares.do?op=buscar',
            type: 'GET',
            data: { criterio: criterio },
            dataType: 'json',
            success: function(data) {
                container.empty();
                if (data.length === 0) {
                    container.html('<div class="alert alert-warning text-center">No se encontraron resultados.</div>');
                    return;
                }

                let html = '';
                data.forEach(function(item) {
                    // Clases para badges
                    let badgeClass = 'badge-default';
                    if(item.tipo_documento === 'Libro') badgeClass = 'badge-Libro'; // Asegúrate de tener CSS para esto o usa clases default

                    // Botón según login
                    let botonAccion = usuarioLogueado && item.disponibles > 0
                        ? `<button class="btn btn-primary btn-sm pull-right btn-reservar" data-id="\${item.id_ejemplar}">Reservar</button>`
                        : '';

                    let stockInfo = item.disponibles > 0
                        ? `<span class="text-success"><strong>\${item.disponibles}</strong> Disponibles</span>`
                        : `<span class="text-danger">Agotado</span>`;

                    // IMPORTANTE: Agregamos data-id="\${item.id_ejemplar}" al div principal
                    // y agregamos un title para indicar al usuario que puede hacer doble clic
                    html += `
                        <div class="result-card" data-id="\${item.id_ejemplar}" title="Doble clic para ver detalles">
                            <div class="row">
                                <div class="col-md-9 col-sm-8">
                                    <div class="result-title">\${item.titulo}</div>
                                    <div class="result-author">\${item.nombre_autor}</div>
                                    <div style="margin-top: 5px;">
                                        <span class="label label-info">\${item.tipo_documento}</span>
                                        <small class="text-muted" style="margin-left: 10px;">Ubicación: \${item.ubicacion}</small>
                                    </div>
                                </div>
                                <div class="col-md-3 col-sm-4 text-right" style="border-left: 1px solid #eee;">
                                    \${stockInfo}
                                    <br><br>
                                    \${botonAccion}
                                </div>
                            </div>
                        </div>
                    `;
                });
                container.html(html);
            },
            error: function() {
                container.html('<div class="alert alert-danger">Error al consultar.</div>');
            }
        });
    }

    // --- NUEVO: Función para cargar y mostrar el Modal ---
    function verDetalleModal(id) {
        // Limpiar modal anterior
        $('#det-especificos').empty();
        $('#loading-detalle').show();
        $('#contenido-detalle').hide();
        $('#modalDetalle').modal('show'); // Abrir modal bootstrap

        $.ajax({
            url: '${contextPath}/ejemplares.do?op=detalles',
            type: 'GET',
            data: { id: id },
            dataType: 'json',
            success: function(data) {
                if(data.error) {
                    alert(data.error);
                    return;
                }

                // Llenar datos generales
                $('#det-titulo').text(data.titulo);
                $('#det-autor').text(data.nombre_autor || 'Autor desconocido');
                $('#det-tipo').text(data.tipo_documento);
                $('#det-ubicacion').text(data.ubicacion);

                // Lógica para campos específicos según el tipo
                let htmlEsp = '';
                if(data.isbn) htmlEsp += `<p><strong>ISBN:</strong> \${data.isbn}</p>`;
                if(data.edicion) htmlEsp += `<p><strong>Edición:</strong> \${data.edicion}</p>`;
                if(data.idioma) htmlEsp += `<p><strong>Idioma:</strong> \${data.idioma}</p>`;
                if(data.duracion) htmlEsp += `<p><strong>Duración:</strong> \${data.duracion}</p>`;
                if(data.institucion) htmlEsp += `<p><strong>Institución:</strong> \${data.institucion}</p>`;
                if(data.fecha_publicacion) htmlEsp += `<p><strong>Publicado:</strong> \${data.fecha_publicacion}</p>`;

                $('#det-especificos').html(htmlEsp);

                // Configurar botón reservar del modal
                $('#btn-reservar-modal').off('click').on('click', function() {
                    // Aquí iría la lógica de reservar (la implementaremos luego)
                    alert('Funcionalidad de reserva desde detalle pendiente.');
                });

                // Mostrar contenido
                $('#loading-detalle').hide();
                $('#contenido-detalle').show();
            },
            error: function() {
                alert('Error al cargar los detalles.');
                $('#modalDetalle').modal('hide');
            }
        });
    }

</script>
</body>
</html>