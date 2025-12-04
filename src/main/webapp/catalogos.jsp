<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="header.jsp" %>

<!DOCTYPE html>
<html lang="es">
<head>
    <title>Catálogo Público - Biblioteca UDB</title>
    <style>
        .search-container {
            background-color: transparent; /* Fondo transparente */
            padding: 10px;
            margin-bottom: 30px;
            border: none;
            box-shadow: none;
        }
        .badge-tipo { padding: 5px 10px; border-radius: 15px; font-size: 12px; font-weight: normal; }
        .badge-Libro { background-color: #e1bee7; color: #4a148c; }
        .badge-Tesis { background-color: #ffccbc; color: #bf360c; }
        .badge-Revistas { background-color: #b2dfdb; color: #004d40; }
        .badge-default { background-color: #cfd8dc; color: #37474f; }

        /* Textos blancos para contrastar con fondo azul */
        h2, h4, .section-title, p { color: white !important; }
        .text-muted { color: #e0e0e0 !important; } /* Reemplazar el gris oscuro por gris muy claro */

        /* Estilos para el contenedor de resultados */
        #resultados-container {
            display: flex;
            flex-wrap: wrap;
            margin: -15px;
        }

        #resultados-container .col-md-4.col-sm-6 {
            display: flex;
            padding: 15px;
        }

        .result-card {
            width: 100%;
            display: flex;
            flex-direction: column;
            min-height: 180px;
            background-color: #fff !important;
            border-radius: 15px !important;
            padding: 15px 25px !important;
            border: 1px solid #eee !important;
            transition: all 0.3s ease;
        }

        .result-card:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            transform: translateY(-2px);
        }

        .result-title {
            font-weight: bold;
            font-size: 16px;
            margin-bottom: 8px;
            flex-grow: 1;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }

        .result-author {
            font-weight: bold;
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
        }

        .result-type {
            margin-top: auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 8px;
        }

        .stock-status {
            font-size: 12px;
            font-weight: bold;
            white-space: nowrap;
        }

        /* Media queries responsivos */
        @media (max-width: 1199px) {
            #resultados-container .col-md-4 {
                flex: 0 0 33.333%;
                max-width: 33.333%;
            }
        }

        @media (max-width: 991px) {
            #resultados-container .col-md-4.col-sm-6 {
                flex: 0 0 50%;
                max-width: 50%;
            }
        }

        @media (max-width: 767px) {
            #resultados-container .col-md-4.col-sm-6 {
                flex: 0 0 100%;
                max-width: 100%;
            }
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
            <div id="resultados-container"></div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function() {
        $('#txtBuscar').on('keypress', function(e) { if(e.which === 13) realizarBusqueda(); });
        $('#btnBuscar').on('click', realizarBusqueda);
        realizarBusqueda(); // Carga inicial
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
                    container.html('<div class="alert alert-info text-center">No se encontraron resultados.</div>');
                    return;
                }
                let html = '';
                data.forEach(function(item) {
                    let badgeClass = 'badge-default';
                    if(item.tipo_documento === 'Libro') badgeClass = 'badge-Libro';
                    else if(item.tipo_documento === 'Tesis') badgeClass = 'badge-Tesis';
                    else if(item.tipo_documento === 'Revistas') badgeClass = 'badge-Revistas';

                    let stockHtml = item.disponibles > 0
                        ? '<span class="stock-status text-success">' + item.disponibles + ' Disponibles</span>'
                        : '<span class="stock-status text-danger">Agotado</span>';

                    html += `
                            <div class="col-md-4 col-sm-6 col-xs-12">
                                <div class="result-card" onclick="verDetalle(\${item.id_ejemplar})" style="cursor:pointer;">
                                    <div class="result-title">\${item.titulo}</div>
                                    <div class="result-author">\${item.nombre_autor}</div>
                                    <div class="result-type">
                                        <span class="badge badge-tipo \${badgeClass}">\${item.tipo_documento}</span>
                                        \${stockHtml}
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

    function verDetalle(id) {
        window.location.href = '${contextPath}/detalleEjemplar.jsp?id=' + id;
    }
</script>
</body>
</html>