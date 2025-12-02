<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="../header.jsp" %>

<!DOCTYPE html>
<html>
    <head>
        <title>Editar Ejemplar</title>
    </head>
    <body>
        <div class="container">
            <div class="row">
                <h3>Editar Ejemplar</h3>
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
                <!-- Columna izquierda: Formulario -->
                <div class="col-md-6">
                    <form id="ejemplarForm" role="form">
                        <input type="hidden" name="id_ejemplar" id="id_ejemplar" value="${requestScope.ejemplar.id_ejemplar}">

                        <div class="well well-sm">
                            <strong><span class="glyphicon glyphicon-asterisk"></span>Campos generales</strong>
                        </div>

                        <div class="form-group mb-3">
                            <label for="titulo">Título *</label>
                            <input type="text" class="form-control" name="titulo" id="titulo" value="${requestScope.ejemplar.titulo}" required>
                        </div>

                        <div class="form-group mb-3">
                            <label for="id_autor">Autor *</label>
                            <select class="form-control" name="id_autor" id="id_autor" required>
                                <option value="">Selecciona un autor</option>
                                <c:forEach items="${requestScope.listaAutores}" var="autor">
                                    <option value="${autor.id_autor}" ${autor.id_autor eq requestScope.ejemplar.id_autor ? 'selected' : ''}>
                                        ${autor.nombre_autor}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group mb-3">
                            <label for="ubicacion">Ubicación</label>
                            <input type="text" class="form-control" name="ubicacion" id="ubicacion" value="${requestScope.ejemplar.ubicacion}">
                        </div>

                        <div class="form-group mb-3">
                            <label>Tipo de Documento</label>
                            <div class="form-control-plaintext">
                                ${requestScope.ejemplar.tipo_documento}
                            </div>
                            <input type="hidden" name="tipo_documento" value="${requestScope.ejemplar.tipo_documento}">
                        </div>

                        <!-- Campos dinámicos -->
                        <div id="camposEspecificos">
                            <div class="well well-sm mt-3">
                                <strong><span class="glyphicon glyphicon-asterisk"></span>Campos específicos</strong>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-info">Guardar Cambios</button>
                        <a class="btn btn-danger" href="${contextPath}/ejemplares.do?op=listar">Cancelar</a>
                    </form>
                </div>

                <!-- Columna derecha: Tabla de Copias -->
                <div class="col-md-6">
                    <div class="well well-sm">
                        <strong><span class="glyphicon glyphicon-asterisk"></span>Copias del Ejemplar</strong>
                    </div>

                    <div class="form-group mb-3">
                        <label for="cantidad_nuevas_copias">Agregar nuevas copias:</label>
                        <div class="input-group">
                            <input type="number" class="form-control" id="cantidad_nuevas_copias" min="1" value="1">
                            <button class="btn btn-success" id="btnAgregarCopias">Agregar</button>
                        </div>
                    </div>

                    <table class="table table-striped table-bordered table-hover" id="tablaCopias">
                        <thead>
                            <tr>
                                <th>Código</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${requestScope.listaCopias}" var="copia">
                                <tr>
                                    <td>${copia.codigo_unico}</td>
                                    <td>${copia.estado}</td>
                                    <td>
                                        <button class="btn btn-warning btn-sm" onclick="abrirModalReserva('${copia.id_copia}', '${copia.codigo_unico}')">Reservar</button>
                                        <button class="btn btn-danger btn-sm" onclick="eliminarCopia('${copia.id_copia}', '${copia.codigo_unico}')">Eliminar</button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Modal para Reservar Copia -->
        <div class="modal fade" id="modalReservarCopia" tabindex="-1" aria-labelledby="modalReservarCopiaLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modalReservarCopiaLabel">Reservar Copia</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="form-group mb-3">
                            <label for="correo_usuario">Correo del Usuario *</label>
                            <input type="email" class="form-control" id="correo_usuario" placeholder="Ingresa el correo del usuario">
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn btn-primary" id="btnConfirmarReserva">Reservar</button>
                    </div>
                </div>
            </div>
        </div>

        <script>
            $(document).ready(function() {
                // Cargar catálogos y campos específicos
                let catalogos = null;
                $.get('${contextPath}/catalogos.do?op=obtener')
                    .done(function(data) {
                        catalogos = data;
                        // Cargar los campos específicos una vez que los catálogos estén disponibles
                        cargarCamposEspecificos();
                    })
                    .fail(function() {
                        console.error("Error al cargar catálogos");
                        alert("Error al cargar los catálogos. No se podrán mostrar los campos específicos.");
                    });

                function cargarCamposEspecificos() {
                    const tipo = '${requestScope.ejemplar.tipo_documento}';
                    const camposDiv = $('#camposEspecificos');
                    camposDiv.empty(); // Limpiar cualquier contenido previo

                    if (!tipo || !catalogos) {
                        if (!catalogos) {
                            console.warn("No se han cargado los catálogos aún.");
                        }
                        return; // No hacer nada si no hay tipo o catálogos
                    }

                    // ? Serializar el objeto ejemplar a JSON en el servidor
                    const ejemplar = <c:out value="${requestScope.ejemplarJSON}" escapeXml="false" />;

                    let html = '';

                    switch (tipo) {
                        case 'Libro':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="isbn">ISBN</label>
                                    <input type="text" class="form-control" name="isbn" id="isbn" value="${ejemplar.isbn || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_editorial">Editorial</label>
                                    <select class="form-control" name="id_editorial" id="id_editorial">
                                        <option value="">Selecciona una editorial</option>
                            `;
                            if (catalogos.editoriales) {
                                catalogos.editoriales.forEach(function(e) {
                                    const selected = e.id_editorial == ejemplar.id_editorial ? 'selected' : '';
                                    html += `<option value="${e.id_editorial}" ${selected}>${e.nombre_editorial}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_genero">Género</label>
                                    <select class="form-control" name="id_genero" id="id_genero">
                                        <option value="">Selecciona un género</option>
                            `;
                            if (catalogos.generos) {
                                catalogos.generos.forEach(function(g) {
                                    const selected = g.id_genero == ejemplar.id_genero ? 'selected' : '';
                                    html += `<option value="${g.id_genero}" ${selected}>${g.nombre_genero}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                                <div class="form-group mb-3">
                                    <label for="edicion">Edición</label>
                                    <input type="number" class="form-control" name="edicion" id="edicion" value="${ejemplar.edicion || ''}">
                                </div>
                            `;
                            break;
                        case 'Diccionario':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="idioma">Idioma</label>
                                    <input type="text" class="form-control" name="idioma" id="idioma" value="${ejemplar.idioma || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="volumen">Volumen</label>
                                    <input type="number" class="form-control" name="volumen" id="volumen" value="${ejemplar.volumen || ''}">
                                </div>
                            `;
                            break;
                        case 'Mapas':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="escala">Escala</label>
                                    <input type="text" class="form-control" name="escala" id="escala" value="${ejemplar.escala || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="tipo_mapa">Tipo de Mapa</label>
                                    <input type="text" class="form-control" name="tipo_mapa" id="tipo_mapa" value="${ejemplar.tipo_mapa || ''}">
                                </div>
                            `;
                            break;
                        case 'Tesis':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="grado_academico">Grado Académico</label>
                                    <input type="text" class="form-control" name="grado_academico" id="grado_academico" value="${ejemplar.grado_academico || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="facultad">Facultad</label>
                                    <input type="text" class="form-control" name="facultad" id="facultad" value="${ejemplar.facultad || ''}">
                                </div>
                            `;
                            break;
                        case 'DVD':
                        case 'VHS':
                        case 'CD':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="duracion">Duración (HH:MM:SS)</label>
                                    <input type="time" class="form-control" name="duracion" id="duracion" value="${ejemplar.duracion || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_genero">Género</label>
                                    <select class="form-control" name="id_genero" id="id_genero">
                                        <option value="">Selecciona un género</option>
                            `;
                            if (catalogos.generos) {
                                catalogos.generos.forEach(function(g) {
                                    const selected = g.id_genero == ejemplar.id_genero ? 'selected' : '';
                                    html += `<option value="${g.id_genero}" ${selected}>${g.nombre_genero}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                            `;
                            break;
                        case 'Cassettes':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="duracion">Duración (HH:MM:SS)</label>
                                    <input type="time" class="form-control" name="duracion" id="duracion" value="${ejemplar.duracion || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_tipo_cinta">Tipo de Cinta</label>
                                    <select class="form-control" name="id_tipo_cinta" id="id_tipo_cinta">
                                        <option value="">Selecciona un tipo</option>
                            `;
                            if (catalogos.tiposCinta) {
                                catalogos.tiposCinta.forEach(function(t) {
                                    const selected = t.id_tipo_cinta == ejemplar.id_tipo_cinta ? 'selected' : '';
                                    html += `<option value="${t.id_tipo_cinta}" ${selected}>${t.nombre_tipo_cinta}</option>`;
                                });
                            }
                            html += `
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
                            `;
                            if (catalogos.tiposDetalle) {
                                catalogos.tiposDetalle.forEach(function(t) {
                                    const selected = t.id_tipo_detalle == ejemplar.id_tipo_detalle ? 'selected' : '';
                                    html += `<option value="${t.id_tipo_detalle}" ${selected}>${t.nombre_tipo_detalle}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                            `;
                            break;
                        case 'Periodicos':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="fecha_publicacion">Fecha de Publicación</label>
                                    <input type="date" class="form-control" name="fecha_publicacion" id="fecha_publicacion" value="${ejemplar.fecha_publicacion || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_tipo_periodico">Tipo de Periódico</label>
                                    <select class="form-control" name="id_tipo_periodico" id="id_tipo_periodico">
                                        <option value="">Selecciona un tipo</option>
                            `;
                            if (catalogos.tiposPeriodico) {
                                catalogos.tiposPeriodico.forEach(function(t) {
                                    const selected = t.id_tipo_periodico == ejemplar.id_tipo_periodico ? 'selected' : '';
                                    html += `<option value="${t.id_tipo_periodico}" ${selected}>${t.nombre_tipo_periodico}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                            `;
                            break;
                        case 'Revistas':
                            html = `
                                <div class="form-group mb-3">
                                    <label for="fecha_publicacion">Fecha de Publicación</label>
                                    <input type="date" class="form-control" name="fecha_publicacion" id="fecha_publicacion" value="${ejemplar.fecha_publicacion || ''}">
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_tipo_revista">Tipo de Revista</label>
                                    <select class="form-control" name="id_tipo_revista" id="id_tipo_revista">
                                        <option value="">Selecciona un tipo</option>
                            `;
                            if (catalogos.tiposRevista) {
                                catalogos.tiposRevista.forEach(function(t) {
                                    const selected = t.id_tipo_revista == ejemplar.id_tipo_revista ? 'selected' : '';
                                    html += `<option value="${t.id_tipo_revista}" ${selected}>${t.nombre_tipo_revista}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                                <div class="form-group mb-3">
                                    <label for="id_genero">Género</label>
                                    <select class="form-control" name="id_genero" id="id_genero">
                                        <option value="">Selecciona un género</option>
                            `;
                            if (catalogos.generos) {
                                catalogos.generos.forEach(function(g) {
                                    const selected = g.id_genero == ejemplar.id_genero ? 'selected' : '';
                                    html += `<option value="${g.id_genero}" ${selected}>${g.nombre_genero}</option>`;
                                });
                            }
                            html += `
                                    </select>
                                </div>
                            `;
                            break;
                    }

                    if (html) {
                        camposDiv.append(html);
                    }
                }


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

                    $.ajax({
                        url: '${contextPath}/ejemplares.do?op=modificar&id=' + data.id_ejemplar,
                        method: 'POST',
                        contentType: 'application/json',
                         JSON.stringify(data),
                        success: function(response) {
                            if (response.success) {
                                alert(response.message);
                                window.location.reload();
                            } else {
                                alert('Error: ' + (response.message || response.errors));
                            }
                        },
                        error: function() {
                            alert('Error al procesar la solicitud.');
                        }
                    });
                });

                $('#btnAgregarCopias').click(function() {
                    const cantidad = parseInt($('#cantidad_nuevas_copias').val());
                    if (isNaN(cantidad) || cantidad < 1) {
                        alert('Por favor, ingresa una cantidad válida.');
                        return;
                    }

                    $.ajax({
                        url: '${contextPath}/ejemplares.do?op=crearCopias',
                        method: 'POST',
                        data: {
                            idEjemplar: '${requestScope.ejemplar.id_ejemplar}',
                            cantidad: cantidad
                        },
                        success: function(response) {
                            if (response.success) {
                                alert(response.message);
                                window.location.reload();
                            } else {
                                alert('Error: ' + response.message);
                            }
                        },
                        error: function() {
                            alert('Error al procesar la solicitud.');
                        }
                    });
                });
            });

            function eliminarCopia(idCopia, codigo) {
                if (confirm('¿Estás seguro de que deseas eliminar la copia ' + codigo + '?')) {
                    $.ajax({
                        url: '${contextPath}/copias.do?op=eliminar',
                        method: 'POST',
                         {
                            idCopia: idCopia
                        },
                        success: function(response) {
                            if (response.success) {
                                alert(response.message);
                                window.location.reload();
                            } else {
                                alert('Error: ' + response.message);
                            }
                        },
                        error: function() {
                            alert('Error al procesar la solicitud.');
                        }
                    });
                }
            }

            let copiaSeleccionada = null;
            function abrirModalReserva(idCopia, codigo) {
                copiaSeleccionada = idCopia;
                $('#modalReservarCopiaLabel').text('Reservar Copia: ' + codigo);
                $('#correo_usuario').val('');
                $('#modalReservarCopia').modal('show');
            }

            $('#btnConfirmarReserva').click(function() {
                const correo = $('#correo_usuario').val();
                if (!correo) {
                    alert('Por favor, ingresa el correo del usuario.');
                    return;
                }

                $.ajax({
                    url: '${contextPath}/reservas.do?op=crear',
                    method: 'POST',
                     {
                        idCopia: copiaSeleccionada,
                        correoUsuario: correo
                    },
                    success: function(response) {
                        if (response.success) {
                            alert(response.message);
                            $('#modalReservarCopia').modal('hide');
                        } else {
                            alert('Error: ' + response.message);
                        }
                    },
                    error: function() {
                        alert('Error al procesar la solicitud.');
                    }
                });
            });
        </script>
    </body>
</html>