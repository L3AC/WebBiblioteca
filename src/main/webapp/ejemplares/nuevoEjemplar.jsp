<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
<head>
    <title>Nuevo Ejemplar</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</head>
<body>
    <div class="container">
        <div class="row">
            <h3>Registrar Nuevo Ejemplar</h3>
        </div>

        <!-- ? Mensajes de éxito/error -->
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
                        </select>
                    </div>

                    <!-- Campos dinámicos -->
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
        $(document).ready(function() {
            // ? Cargar catálogos al inicio
            let catalogos = null;
            $.get('${contextPath}/catalogos.do?op=obtener', function(data) {
                catalogos = data;
            });

            $('#tipo_documento').change(function() {
                const tipo = $(this).val();
                const camposDiv = $('#camposEspecificos');
                camposDiv.empty().hide();

                if (!tipo || !catalogos) return;

                let html = '';

                switch(tipo) {
                    case 'Libro':
                        html = `
                            <div class="form-group mb-3">
                                <label for="isbn">ISBN</label>
                                <input type="text" class="form-control" name="isbn" id="isbn" placeholder="Ingresa el ISBN">
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_editorial">Editorial</label>
                                <select class="form-control" name="id_editorial" id="id_editorial">
                                    <option value="">Selecciona una editorial</option>
                                    ${catalogos.editoriales.map(e => `<option value="${e.id_editorial}">${e.nombre_editorial}</option>`).join('')}
                                </select>
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_genero">Género</label>
                                <select class="form-control" name="id_genero" id="id_genero">
                                    <option value="">Selecciona un género</option>
                                    ${catalogos.generos.map(g => `<option value="${g.id_genero}">${g.nombre_genero}</option>`).join('')}
                                </select>
                            </div>
                            <div class="form-group mb-3">
                                <label for="edicion">Edición</label>
                                <input type="number" class="form-control" name="edicion" id="edicion" placeholder="Ingresa la edición">
                            </div>
                        `;
                        break;
                    case 'Diccionario':
                        html = `
                            <div class="form-group mb-3">
                                <label for="idioma">Idioma</label>
                                <input type="text" class="form-control" name="idioma" id="idioma" placeholder="Ingresa el idioma">
                            </div>
                            <div class="form-group mb-3">
                                <label for="volumen">Volumen</label>
                                <input type="number" class="form-control" name="volumen" id="volumen" placeholder="Ingresa el volumen">
                            </div>
                        `;
                        break;
                    case 'Mapas':
                        html = `
                            <div class="form-group mb-3">
                                <label for="escala">Escala</label>
                                <input type="text" class="form-control" name="escala" id="escala" placeholder="Ingresa la escala">
                            </div>
                            <div class="form-group mb-3">
                                <label for="tipo_mapa">Tipo de Mapa</label>
                                <input type="text" class="form-control" name="tipo_mapa" id="tipo_mapa" placeholder="Ingresa el tipo de mapa">
                            </div>
                        `;
                        break;
                    case 'Tesis':
                        html = `
                            <div class="form-group mb-3">
                                <label for="grado_academico">Grado Académico</label>
                                <input type="text" class="form-control" name="grado_academico" id="grado_academico" placeholder="Ingresa el grado académico">
                            </div>
                            <div class="form-group mb-3">
                                <label for="facultad">Facultad</label>
                                <input type="text" class="form-control" name="facultad" id="facultad" placeholder="Ingresa la facultad">
                            </div>
                        `;
                        break;
                    case 'DVD':
                    case 'VHS':
                    case 'CD':
                        html = `
                            <div class="form-group mb-3">
                                <label for="duracion">Duración (HH:MM:SS)</label>
                                <input type="time" class="form-control" name="duracion" id="duracion">
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_genero">Género</label>
                                <select class="form-control" name="id_genero" id="id_genero">
                                    <option value="">Selecciona un género</option>
                                    ${catalogos.generos.map(g => `<option value="${g.id_genero}">${g.nombre_genero}</option>`).join('')}
                                </select>
                            </div>
                        `;
                        break;
                    case 'Cassettes':
                        html = `
                            <div class="form-group mb-3">
                                <label for="duracion">Duración (HH:MM:SS)</label>
                                <input type="time" class="form-control" name="duracion" id="duracion">
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_tipo_cinta">Tipo de Cinta</label>
                                <select class="form-control" name="id_tipo_cinta" id="id_tipo_cinta">
                                    <option value="">Selecciona un tipo</option>
                                    ${catalogos.tiposCinta.map(t => `<option value="${t.id_tipo_cinta}">${t.nombre_tipo_cinta}</option>`).join('')}
                                </select>
                            </div>
                        `;
                        break;
                    case 'Documento':
                        html = `
                            <div class="form-group mb-3">
                                <label for="id_tipo_detalle">Tipo de Documento Detalle</label>
                                <select class="form-control" name="id_tipo_detalle" id="id_tipo_detalle">
                                    <option value="">Selecciona un tipo</option>
                                    ${catalogos.tiposDetalle.map(t => `<option value="${t.id_tipo_detalle}">${t.nombre_tipo_detalle}</option>`).join('')}
                                </select>
                            </div>
                        `;
                        break;
                    case 'Periodicos':
                        html = `
                            <div class="form-group mb-3">
                                <label for="fecha_publicacion">Fecha de Publicación</label>
                                <input type="date" class="form-control" name="fecha_publicacion" id="fecha_publicacion">
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_tipo_periodico">Tipo de Periódico</label>
                                <select class="form-control" name="id_tipo_periodico" id="id_tipo_periodico">
                                    <option value="">Selecciona un tipo</option>
                                    ${catalogos.tiposPeriodico.map(t => `<option value="${t.id_tipo_periodico}">${t.nombre_tipo_periodico}</option>`).join('')}
                                </select>
                            </div>
                        `;
                        break;
                    case 'Revistas':
                        html = `
                            <div class="form-group mb-3">
                                <label for="fecha_publicacion">Fecha de Publicación</label>
                                <input type="date" class="form-control" name="fecha_publicacion" id="fecha_publicacion">
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_tipo_revista">Tipo de Revista</label>
                                <select class="form-control" name="id_tipo_revista" id="id_tipo_revista">
                                    <option value="">Selecciona un tipo</option>
                                    ${catalogos.tiposRevista.map(t => `<option value="${t.id_tipo_revista}">${t.nombre_tipo_revista}</option>`).join('')}
                                </select>
                            </div>
                            <div class="form-group mb-3">
                                <label for="id_genero">Género</label>
                                <select class="form-control" name="id_genero" id="id_genero">
                                    <option value="">Selecciona un género</option>
                                    ${catalogos.generos.map(g => `<option value="${g.id_genero}">${g.nombre_genero}</option>`).join('')}
                                </select>
                            </div>
                        `;
                        break;
                }

                if (html) {
                    camposDiv.html(html).show();
                }
            });

            $('#ejemplarForm').submit(function(e) {
                e.preventDefault();

                const formData = $(this).serializeArray();
                const data = {};
                formData.forEach(function(item) {
                    data[item.name] = item.value;
                });

                // Convertir números
                if (data.id_autor) data.id_autor = parseInt(data.id_autor);
                if (data.id_editorial) data.id_editorial = parseInt(data.id_editorial);
                if (data.id_genero) data.id_genero = parseInt(data.id_genero);
                if (data.id_tipo_cinta) data.id_tipo_cinta = parseInt(data.id_tipo_cinta);
                if (data.id_tipo_detalle) data.id_tipo_detalle = parseInt(data.id_tipo_detalle);
                if (data.id_tipo_periodico) data.id_tipo_periodico = parseInt(data.id_tipo_periodico);
                if (data.id_tipo_revista) data.id_tipo_revista = parseInt(data.id_tipo_revista);
                if (data.edicion) data.edicion = parseInt(data.edicion);
                if (data.volumen) data.volumen = parseInt(data.volumen);
                if (data.cantidad_copias) data.cantidad_copias = parseInt(data.cantidad_copias);

                $.ajax({
                    url: '${contextPath}/ejemplares.do?op=insertar',
                    method: 'POST',
                    contentType: 'application/json',
                     JSON.stringify(data),
                    success: function(response) {
                        if (response.success) {
                            alert(response.message);
                            window.location.href = '${contextPath}/ejemplares.do?op=listar';
                        } else {
                            alert('Error: ' + (response.message || response.errors));
                        }
                    },
                    error: function() {
                        alert('Error al procesar la solicitud.');
                    }
                });
            });
        });
    </script>
</body>
</html>