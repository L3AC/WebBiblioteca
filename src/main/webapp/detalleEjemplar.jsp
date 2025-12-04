<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <title>Detalle del Ejemplar</title>
    <style>
        .main-container { margin-top: 40px; margin-bottom: 40px; }
        .detail-card { background: #fff; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); overflow: hidden; }
        .detail-card-header { background: #2c3e50; color: white; padding: 20px; text-align: center; }
        .detail-card-header h3 { margin: 0; font-weight: 600; }
        .detail-card-body { padding: 30px; }
        .data-item { margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
        .data-label { font-weight: bold; color: #555; display: block; margin-bottom: 5px; }
        .data-label .glyphicon { margin-right: 8px; color: #4a6b8a; }
        .data-value { font-size: 16px; color: #000; padding-left: 25px; }
        .extra-field { display: none; }
        .copies-container { background: rgba(255,255,255,0.9); padding: 20px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        .table-copies thead th { background-color: #2c3e50; color: white; text-align: center; border: none; }
        .table-copies tbody td { text-align: center; vertical-align: middle; border-color: #ddd; font-weight: 500; }
        .estado-badge { padding: 5px 10px; border-radius: 12px; font-size: 0.9em; }
        .estado-Disponible { background-color: #d4edda; color: #155724; }
        .estado-Prestado, .estado-Reservado { background-color: #f8d7da; color: #721c24; }
        .btn-reservar-action { background-color: #3498db; color: white; border: none; padding: 6px 12px; border-radius: 4px; transition: all 0.3s; }
        .btn-reservar-action:hover { background-color: #2980b9; transform: scale(1.05); }
        @media (max-width: 991px) { .detail-card { margin-bottom: 30px; } }
    </style>
</head>
<body>

<div class="container main-container">
    <div id="status-message" class="alert text-center" style="display: none;"></div>

    <div class="row" id="main-content" style="display: none;">
        <div class="col-md-5">
            <div class="detail-card">
                <div class="detail-card-header">
                    <h3><span class="glyphicon glyphicon-book"></span> Ficha Técnica</h3>
                </div>
                <div class="detail-card-body">
                    <div class="data-item"><span class="data-label"><span class="glyphicon glyphicon-bookmark"></span> Título</span><span class="data-value" id="val-titulo"></span></div>
                    <div class="data-item"><span class="data-label"><span class="glyphicon glyphicon-user"></span> Autor</span><span class="data-value" id="val-autor"></span></div>
                    <div class="data-item"><span class="data-label"><span class="glyphicon glyphicon-tags"></span> Tipo de Documento</span><span class="data-value" id="val-tipo"></span></div>
                    <div class="data-item"><span class="data-label"><span class="glyphicon glyphicon-map-marker"></span> Ubicación Física</span><span class="data-value" id="val-ubicacion"></span></div>

                    <div class="extra-field field-libro">
                        <div class="data-item"><span class="data-label">ISBN</span><span class="data-value" id="val-isbn"></span></div>
                        <div class="data-item"><span class="data-label">Editorial</span><span class="data-value" id="val-editorial"></span></div>
                        <div class="data-item"><span class="data-label">Género</span><span class="data-value" id="val-genero"></span></div>
                        <div class="data-item"><span class="data-label">Edición</span><span class="data-value" id="val-edicion"></span></div>
                    </div>
                    <div class="extra-field field-diccionario">
                        <div class="data-item"><span class="data-label">ISBN</span><span class="data-value" id="val-isbn-dic"></span></div>
                        <div class="data-item"><span class="data-label">Editorial</span><span class="data-value" id="val-editorial-dic"></span></div>
                        <div class="data-item"><span class="data-label">Idioma</span><span class="data-value" id="val-idioma"></span></div>
                        <div class="data-item"><span class="data-label">Volumen</span><span class="data-value" id="val-volumen"></span></div>
                    </div>
                    <div class="extra-field field-mapas">
                        <div class="data-item"><span class="data-label">Escala</span><span class="data-value" id="val-escala"></span></div>
                        <div class="data-item"><span class="data-label">Tipo de Mapa</span><span class="data-value" id="val-tipo-mapa"></span></div>
                    </div>
                    <div class="extra-field field-tesis">
                        <div class="data-item"><span class="data-label">Grado Académico</span><span class="data-value" id="val-grado"></span></div>
                        <div class="data-item"><span class="data-label">Facultad</span><span class="data-value" id="val-facultad"></span></div>
                    </div>
                    <div class="extra-field field-multimedia">
                        <div class="data-item"><span class="data-label">Duración</span><span class="data-value" id="val-duracion"></span></div>
                        <div class="data-item"><span class="data-label">Género</span><span class="data-value" id="val-genero-multi"></span></div>
                    </div>
                    <div class="extra-field field-cassettes">
                        <div class="data-item"><span class="data-label">Duración</span><span class="data-value" id="val-duracion-cas"></span></div>
                        <div class="data-item"><span class="data-label">Tipo de Cinta</span><span class="data-value" id="val-tipo-cinta"></span></div>
                    </div>
                    <div class="extra-field field-documento">
                        <div class="data-item"><span class="data-label">Tipo de Detalle</span><span class="data-value" id="val-tipo-detalle"></span></div>
                    </div>
                    <div class="extra-field field-periodicos">
                        <div class="data-item"><span class="data-label">Fecha Publicación</span><span class="data-value" id="val-fecha-pub"></span></div>
                        <div class="data-item"><span class="data-label">Tipo Periódico</span><span class="data-value" id="val-tipo-periodico"></span></div>
                    </div>
                    <div class="extra-field field-revistas">
                        <div class="data-item"><span class="data-label">Fecha Publicación</span><span class="data-value" id="val-fecha-pub-rev"></span></div>
                        <div class="data-item"><span class="data-label">Tipo Revista</span><span class="data-value" id="val-tipo-revista"></span></div>
                        <div class="data-item"><span class="data-label">Género</span><span class="data-value" id="val-genero-rev"></span></div>
                    </div>

                    <div style="margin-top: 30px;">
                        <a href="catalogos.jsp" class="btn btn-default btn-block"><span class="glyphicon glyphicon-arrow-left"></span> Volver al Catálogo</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-7">
            <div class="copies-container">
                <h3 style="margin-top: 0; color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 15px;">
                    <span class="glyphicon glyphicon-list-alt"></span> Disponibilidad de Copias
                </h3>
                <div class="table-responsive" style="margin-top: 20px;">
                    <table class="table table-hover table-copies">
                        <thead id="tabla-copias-head"></thead>
                        <tbody id="tabla-copias-body"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    const urlParams = new URLSearchParams(window.location.search);
    const idEjemplar = urlParams.get('id');
    const usuarioLogueado = ${not empty sessionScope.usuario};

    $(document).ready(function() {
        if(!idEjemplar) {
            mostrarError("No se ha seleccionado ningún ejemplar.");
            return;
        }
        cargarDatos();
    });

    function mostrarError(msg) {
        $('#main-content').hide();
        $('#status-message').removeClass('alert-info').addClass('alert-danger').html(msg).show();
    }

    function cargarDatos() {
        $.ajax({
            url: '${contextPath}/ejemplares.do?op=detalles',
            type: 'GET',
            data: { id: idEjemplar },
            dataType: 'json',
            success: function(resp) {
                if(resp.error) { mostrarError(resp.error); return; }
                $('#status-message').hide();
                $('#main-content').fadeIn();
                llenarTarjetaDatos(resp.datos);
                construirTablaCopias(resp.copias);
            },
            error: function() { mostrarError("Error crítico de comunicación con el servidor."); }
        });
    }

    function llenarTarjetaDatos(data) {
        $('#val-titulo').text(data.titulo || 'Sin título');
        $('#val-autor').text(data.nombre_autor || 'Sin autor');
        $('#val-tipo').text(data.tipo_documento);
        $('#val-ubicacion').text(data.ubicacion || 'No asignada');

        $('.extra-field').hide();
        const tipo = data.tipo_documento;

        if (tipo === 'Libro') {
            $('#val-isbn').text(data.isbn || '-');
            $('#val-editorial').text(data.nombre_editorial || '-');
            $('#val-genero').text(data.nombre_genero || '-');
            $('#val-edicion').text(data.edicion || '-');
            $('.field-libro').fadeIn();
        }
        else if (tipo === 'Diccionario') {
            $('#val-isbn-dic').text(data.isbn || '-');
            $('#val-editorial-dic').text(data.nombre_editorial || '-');
            $('#val-idioma').text(data.idioma || '-');
            $('#val-volumen').text(data.volumen || '-');
            $('.field-diccionario').fadeIn();
        }
        else if (tipo === 'Mapas') {
            $('#val-escala').text(data.escala || '-');
            $('#val-tipo-mapa').text(data.tipo_mapa || '-');
            $('.field-mapas').fadeIn();
        }
        else if (tipo === 'Tesis') {
            $('#val-grado').text(data.grado_academico || '-');
            $('#val-facultad').text(data.facultad || '-');
            $('.field-tesis').fadeIn();
        }
        else if (['DVD', 'CD', 'VHS'].includes(tipo)) {
            $('#val-duracion').text(data.duracion || '-');
            $('#val-genero-multi').text(data.nombre_genero || '-');
            $('.field-multimedia').fadeIn();
        }
        else if (tipo === 'Cassettes') {
            $('#val-duracion-cas').text(data.duracion || '-');
            $('#val-tipo-cinta').text(data.id_tipo_cinta || '-'); // Idealmente traer nombre
            $('.field-cassettes').fadeIn();
        }
        else if (tipo === 'Documento') {
            $('#val-tipo-detalle').text(data.id_tipo_detalle || '-');
            $('.field-documento').fadeIn();
        }
        else if (tipo === 'Periodicos') {
            $('#val-fecha-pub').text(data.fecha_publicacion || '-');
            $('#val-tipo-periodico').text(data.id_tipo_periodico || '-');
            $('.field-periodicos').fadeIn();
        }
        else if (tipo === 'Revistas') {
            $('#val-fecha-pub-rev').text(data.fecha_publicacion || '-');
            $('#val-tipo-revista').text(data.id_tipo_revista || '-');
            $('#val-genero-rev').text(data.nombre_genero || '-');
            $('.field-revistas').fadeIn();
        }
    }

    function construirTablaCopias(copias) {
        const thead = $('#tabla-copias-head');
        const tbody = $('#tabla-copias-body');
        thead.empty(); tbody.empty();

        let headerHtml = '<tr><th>CÓDIGO</th><th>ESTADO</th>';
        if (usuarioLogueado) headerHtml += '<th>ACCIÓN</th>';
        headerHtml += '</tr>';
        thead.html(headerHtml);

        if (!copias || copias.length === 0) {
            let colSpan = usuarioLogueado ? 3 : 2;
            tbody.html('<tr><td colspan="'+colSpan+'" class="text-muted">No hay copias registradas.</td></tr>');
            return;
        }

        copias.forEach(c => {
            let estadoClass = 'estado-' + c.estado.replace(/\s+/g, '');
            let rowHtml = '<tr><td><strong>'+c.codigo_unico+'</strong></td><td><span class="estado-badge '+estadoClass+'">'+c.estado+'</span></td>';

            if (usuarioLogueado) {
                let btnHtml = (c.estado === 'Disponible')
                    ? '<button class="btn-reservar-action" onclick="iniciarReserva('+c.id_copia+', \''+c.codigo_unico+'\')"><span class="glyphicon glyphicon-check"></span> Reservar</button>'
                    : '<span class="text-muted small">No disponible</span>';
                rowHtml += '<td>'+btnHtml+'</td>';
            }
            rowHtml += '</tr>';
            tbody.append(rowHtml);
        });
    }

    function iniciarReserva(idCopia, codigo) {
        alertify.confirm("¿Deseas reservar la copia " + codigo + "?", function(e){
            if(e){
                $.ajax({
                    url: '${contextPath}/reservas.do?op=crear',
                    type: 'POST',
                    data: { idCopia: idCopia },
                    dataType: 'json',
                    success: function(resp) {
                        if(resp.success) {
                            alertify.success(resp.message);
                            cargarDatos(); // Recargar tabla
                        } else {
                            alertify.error(resp.message);
                        }
                    },
                    error: function() { alertify.error("Error en la solicitud"); }
                });
            }
        });
    }
</script>
</body>
</html>