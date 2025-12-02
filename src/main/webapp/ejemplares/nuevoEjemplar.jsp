<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
    <head>
        <title>Nuevo Ejemplar</title>
    </head>
    <body>
        <div class="container">
            <div class="row">
                <h3>Registrar Nuevo Ejemplar</h3>
            </div>

            <c:if test="${not empty exito}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <strong>? Éxito:</strong> ${exito}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            <c:if test="${not empty fracaso}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <strong>? Error:</strong> ${fracaso}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <div class="row">
                <div class="col-md-10">
                    <form id="ejemplarForm" role="form">
                        <div class="well well-sm">
                            <strong><span class="glyphicon glyphicon-asterisk"></span>Campos generales</strong>
                        </div>

                        <div class="form-group mb-3">
                            <label for="titulo">Título *</label>
                            <input type="text" class="form-control" name="titulo" id="titulo" placeholder="Ingresa el título" required>
                        </div>

                        <div class="form-group mb-3">
                            <label for="id_autor">Autor *</label>
                            <select class="form-control" name="id_autor" id="id_autor" required>
                                <option value="">Selecciona un autor</option>
                                <c:forEach items="${requestScope.listaAutores}" var="autor">
                                    <option value="${autor.id_autor}">${autor.nombre_autor}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group mb-3">
                            <label for="ubicacion">Ubicación</label>
                            <input type="text" class="form-control" name="ubicacion" id="ubicacion" placeholder="Ingresa la ubicación">
                        </div>

                        <div class="form-group mb-3">
                            <label for="tipo_documento">Tipo de Documento *</label>
                            <select class="form-control" name="tipo_documento" id="tipo_documento" required>
                                <option value="">Cargando catálogos...</option>
                            </select>
                        </div>

                        <div id="camposEspecificos" style="display: none;">
                            <div class="well well-sm mt-3">
                                <strong><span class="glyphicon glyphicon-asterisk"></span>Campos específicos</strong>
                            </div>
                        </div>

                        <div class="form-group mb-3">
                            <label for="cantidad_copias">Cantidad de Copias *</label>
                            <input type="number" class="form-control" name="cantidad_copias" id="cantidad_copias" placeholder="Ingresa la cantidad de copias" min="1" required>
                        </div>

                        <button type="submit" class="btn btn-info">Guardar</button>
                        <a class="btn btn-danger" href="${contextPath}/ejemplares.do?op=listar">Cancelar</a>
                    </form>
                </div>
            </div>
        </div>

        <script>
            $(document).ready(function () {
                let catalogos = null;
                let catalogosCargados = false;

                // Mostrar mensaje de carga inicial
                $('#tipo_documento').empty().append('<option value="">Cargando catálogos...</option>').prop('disabled', true);

                // Opciones originales para el select de tipo de documento
                const opcionesTipoDocumento = `
                    <option value="">Selecciona un tipo</option>
                    <option value="Libro">Libro</option>
                    <option value="Diccionario">Diccionario</option>
                    <option value="Mapas">Mapas</option>
                    <option value="Tesis">Tesis</option>
                    <option value="DVD">DVD</option>
                    <option value="VHS">VHS</option>
                    <option value="Cassettes">Cassettes</option>
                    <option value="CD">CD</option>
                    <option value="Documento">Documento</option>
                    <option value="Periodicos">Periódicos</option>
                    <option value="Revistas">Revistas</option>
                `;

                // Cargar catálogos al inicio
                $.get('${contextPath}/catalogos.do?op=obtener')
                        .done(function (data) {
                            console.log("Catálogos recibidos:", data);
                            catalogos = data;
                            catalogosCargados = true;

                            // Restaurar las opciones originales del select y habilitarlo
                            $('#tipo_documento').empty().append(opcionesTipoDocumento).prop('disabled', false);
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.error("Error al cargar catálogos:", textStatus, errorThrown);
                            console.log("Respuesta del servidor:", jqXHR.responseText);

                            $('#tipo_documento').empty().append('<option value="">Error al cargar catálogos</option>').prop('disabled', true);
                            alert("Error al cargar los catálogos. La página no funcionará correctamente. Consulta la consola para más detalles.");
                        });

                // Definir el evento change con manejo de asincronía
                // Definir el evento change con manejo de asincronía
                $('#tipo_documento').on('change', function () {
                    const tipo = $(this).val();
                    const camposDiv = $('#camposEspecificos');

                    console.log("Tipo de documento seleccionado:", tipo);
                    console.log("Catálogos cargados:", catalogosCargados);
                    console.log("Objeto catalogos:", catalogos);

                    // Limpiar y ocultar campos específicos
                    camposDiv.empty().hide();

                    if (!catalogosCargados || !catalogos) {
                        console.error("Catálogos no disponibles");
                        return;
                    }

                    if (!tipo) {
                        return;
                    }

                    let html = '';

                    // Asegurar que las propiedades del catálogo existen antes de usarlas
                    const editoriales = catalogos.editoriales || [];
                    const generos = catalogos.generos || [];
                    const tiposPeriodico = catalogos.tiposPeriodico || [];
                    const tiposRevista = catalogos.tiposRevista || [];
                    const tiposCinta = catalogos.tiposCinta || [];
                    const tiposDetalle = catalogos.tiposDetalle || [];

                    switch (tipo) {
                        case 'Libro':
                            html = '<div class="form-group mb-3">' +
                                    '<label for="isbn">ISBN</label>' +
                                    '<input type="text" class="form-control" name="isbn" id="isbn" placeholder="Ingresa el ISBN">' +
                                    '</div>';

                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_editorial">Editorial</label>' +
                                    '<select class="form-control" name="id_editorial" id="id_editorial">' +
                                    '<option value="">Selecciona una editorial</option>';

                            if (editoriales && editoriales.length > 0) {
                                editoriales.forEach(function (e) {
                                    html += '<option value="' + e.id_editorial + '">' + e.nombre_editorial + '</option>';
                                });
                            }

                            html += '</select></div>';

                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_genero">Género</label>' +
                                    '<select class="form-control" name="id_genero" id="id_genero">' +
                                    '<option value="">Selecciona un género</option>';

                            if (generos && generos.length > 0) {
                                generos.forEach(function (g) {
                                    html += '<option value="' + g.id_genero + '">' + g.nombre_genero + '</option>';
                                });
                            }

                            html += '</select></div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="edicion">Edición</label>' +
                                    '<input type="number" class="form-control" name="edicion" id="edicion" placeholder="Ingresa la edición">' +
                                    '</div>';
                            break;

                        case 'Diccionario':
                            html = '<div class="form-group mb-3">' +
                                    '<label for="isbn">ISBN</label>' +
                                    '<input type="text" class="form-control" name="isbn" id="isbn" placeholder="Ingresa el ISBN">' +
                                    '</div>';

                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_editorial">Editorial</label>' +
                                    '<select class="form-control" name="id_editorial" id="id_editorial">' +
                                    '<option value="">Selecciona una editorial</option>';

                            if (editoriales && editoriales.length > 0) {
                                editoriales.forEach(function (e) {
                                    html += '<option value="' + e.id_editorial + '">' + e.nombre_editorial + '</option>';
                                });
                            }

                            html += '</select></div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="idioma">Idioma</label>' +
                                    '<input type="text" class="form-control" name="idioma" id="idioma" placeholder="Ingresa el idioma">' +
                                    '</div>';
                            break;

                        case 'Mapas':
                            html = '<div class="form-group mb-3">' +
                                    '<label for="escala">Escala</label>' +
                                    '<input type="text" class="form-control" name="escala" id="escala" placeholder="Ingresa la escala">' +
                                    '</div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="tipo_mapa">Tipo de Mapa</label>' +
                                    '<input type="text" class="form-control" name="tipo_mapa" id="tipo_mapa" placeholder="Ingresa el tipo de mapa">' +
                                    '</div>';
                            break;

                        case 'Tesis':
                            html = '<div class="form-group mb-3">' +
                                    '<label for="institucion">Institución</label>' +
                                    '<input type="text" class="form-control" name="institucion" id="institucion" placeholder="Ingresa la institución">' +
                                    '</div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="director">Director</label>' +
                                    '<input type="text" class="form-control" name="director" id="director" placeholder="Ingresa el director">' +
                                    '</div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="anio">Año</label>' +
                                    '<input type="number" class="form-control" name="anio" id="anio" placeholder="Ingresa el año">' +
                                    '</div>';
                            break;

                        case 'DVD':
                        case 'VHS':
                        case 'Cassettes':
                        case 'CD':
                            html = '<div class="form-group mb-3">' +
                                    '<label for="duracion">Duración (minutos)</label>' +
                                    '<input type="number" class="form-control" name="duracion" id="duracion" placeholder="Ingresa la duración">' +
                                    '</div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="formato">Formato</label>' +
                                    '<input type="text" class="form-control" name="formato" id="formato" placeholder="Ingresa el formato">' +
                                    '</div>';

                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_tipo_cinta">Tipo de Cinta</label>' +
                                    '<select class="form-control" name="id_tipo_cinta" id="id_tipo_cinta">' +
                                    '<option value="">Selecciona un tipo</option>';

                            if (tiposCinta && tiposCinta.length > 0) {
                                tiposCinta.forEach(function (tc) {
                                    html += '<option value="' + tc.id_tipo_cinta + '">' + tc.nombre_tipo_cinta + '</option>';
                                });
                            }

                            html += '</select></div>';
                            break;

                        case 'Documento':
                            html = '<div class="form-group mb-3">' +
                                    '<label for="fecha_documento">Fecha del Documento</label>' +
                                    '<input type="date" class="form-control" name="fecha_documento" id="fecha_documento">' +
                                    '</div>';

                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_tipo_detalle">Tipo de Documento</label>' +
                                    '<select class="form-control" name="id_tipo_detalle" id="id_tipo_detalle">' +
                                    '<option value="">Selecciona un tipo</option>';

                            if (tiposDetalle && tiposDetalle.length > 0) {
                                tiposDetalle.forEach(function (td) {
                                    html += '<option value="' + td.id_tipo_detalle + '">' + td.nombre_tipo_detalle + '</option>';
                                });
                            }

                            html += '</select></div>';
                            break;

                        case 'Periodicos':
                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_tipo_periodico">Tipo de Periódico</label>' +
                                    '<select class="form-control" name="id_tipo_periodico" id="id_tipo_periodico">' +
                                    '<option value="">Selecciona un tipo</option>';

                            if (tiposPeriodico && tiposPeriodico.length > 0) {
                                tiposPeriodico.forEach(function (tp) {
                                    html += '<option value="' + tp.id_tipo_periodico + '">' + tp.nombre_tipo_periodico + '</option>';
                                });
                            }

                            html += '</select></div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="fecha_publicacion">Fecha de Publicación</label>' +
                                    '<input type="date" class="form-control" name="fecha_publicacion" id="fecha_publicacion">' +
                                    '</div>';
                            break;

                        case 'Revistas':
                            html += '<div class="form-group mb-3">' +
                                    '<label for="id_tipo_revista">Tipo de Revista</label>' +
                                    '<select class="form-control" name="id_tipo_revista" id="id_tipo_revista">' +
                                    '<option value="">Selecciona un tipo</option>';

                            if (tiposRevista && tiposRevista.length > 0) {
                                tiposRevista.forEach(function (tr) {
                                    html += '<option value="' + tr.id_tipo_revista + '">' + tr.nombre_tipo_revista + '</option>';
                                });
                            }

                            html += '</select></div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="volumen">Volumen</label>' +
                                    '<input type="number" class="form-control" name="volumen" id="volumen" placeholder="Ingresa el volumen">' +
                                    '</div>' +
                                    '<div class="form-group mb-3">' +
                                    '<label for="numero">Número</label>' +
                                    '<input type="number" class="form-control" name="numero" id="numero" placeholder="Ingresa el número">' +
                                    '</div>';
                            break;
                    }

                    if (html) {
                        camposDiv.html(html).show();
                    }
                });

                // Evento submit
                $('#ejemplarForm').submit(function (e) {
                    e.preventDefault();

                    // Validar que los catálogos estén cargados
                    if (!catalogosCargados || !catalogos) {
                        alert("Los catálogos no están disponibles. Recarga la página.");
                        console.error("Catálogos no disponibles para el submit");
                        return; // Detener el proceso
                    }

                    const formData = $(this).serializeArray();
                    const data = {};
                    formData.forEach(function (item) {
                        data[item.name] = item.value;
                    });

                    // Convertir números - ahora con validación adicional
                    if (data.id_autor && !isNaN(data.id_autor))
                        data.id_autor = parseInt(data.id_autor);
                    if (data.id_editorial && !isNaN(data.id_editorial))
                        data.id_editorial = parseInt(data.id_editorial);
                    if (data.id_genero && !isNaN(data.id_genero))
                        data.id_genero = parseInt(data.id_genero);
                    if (data.id_tipo_cinta && !isNaN(data.id_tipo_cinta))
                        data.id_tipo_cinta = parseInt(data.id_tipo_cinta);
                    if (data.id_tipo_detalle && !isNaN(data.id_tipo_detalle))
                        data.id_tipo_detalle = parseInt(data.id_tipo_detalle);
                    if (data.id_tipo_periodico && !isNaN(data.id_tipo_periodico))
                        data.id_tipo_periodico = parseInt(data.id_tipo_periodico);
                    if (data.id_tipo_revista && !isNaN(data.id_tipo_revista))
                        data.id_tipo_revista = parseInt(data.id_tipo_revista);
                    if (data.edicion && !isNaN(data.edicion))
                        data.edicion = parseInt(data.edicion);
                    if (data.volumen && !isNaN(data.volumen))
                        data.volumen = parseInt(data.volumen);
                    if (data.numero && !isNaN(data.numero))
                        data.numero = parseInt(data.numero);
                    if (data.anio && !isNaN(data.anio))
                        data.anio = parseInt(data.anio);
                    if (data.duracion && !isNaN(data.duracion))
                        data.duracion = parseInt(data.duracion);
                    if (data.cantidad_copias && !isNaN(data.cantidad_copias))
                        data.cantidad_copias = parseInt(data.cantidad_copias);

                    $.ajax({
                        url: '${contextPath}/ejemplares.do?op=insertar',
                        method: 'POST',
                        contentType: 'application/json',
                        data: JSON.stringify(data),
                        success: function (response) {
                            // CORREGIDO: Usar 'response.success' en lugar de 'response.exito'
                            if (response.success) { // <-- AQUÍ ESTÁ EL CAMBIO
                                //alert(response.message);
                                window.location.href = '${contextPath}/ejemplares.do?op=listar';
                            } else {
                                // Asegúrate de que response.errors sea un string o un array para join
                                let errorMsg = response.message || (Array.isArray(response.errors) ? response.errors.join(', ') : response.errors);
                                alert('Ejemplar error ' + errorMsg);
                            }
                        },
                        error: function (xhr, status, error) {
                            console.error("Error AJAX:", error);
                            console.log("XHR:", xhr);
                            alert('Error al procesar la solicitud: ' + error);
                        }
                    });
                });
            });
        </script>
    </body>
</html>