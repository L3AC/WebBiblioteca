<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Login - Biblioteca UDB</title>
        <link href="${contextPath}/assets/css/bootstrap.min.css" rel="stylesheet">
        <style>
            body {
                background-color: #f8f9fa;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .login-container {
                max-width: 400px;
                margin-top: 10vh;
            }
            .card {
                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                border: none;
            }
            .card-header {
                background-color: #007bff;
                color: white;
                text-align: center;
                padding: 20px;
            }
            .btn-primary {
                background-color: #007bff;
                border-color: #007bff;
            }
            .btn-primary:hover {
                background-color: #0056b3;
                border-color: #0056b3;
            }
            .form-label {
                font-weight: 500;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="row justify-content-center">
                <div class="col-md-6 login-container">
                    <div class="card">
                        <div class="card-header">
                            <h4>🔒 Iniciar Sesión</h4>
                        </div>
                        <div class="card-body">
                            <!-- ✅ Agregar method="POST" y quitar action -->
                            <form id="loginForm" method="POST">
                                <div class="mb-3">
                                    <label for="correo" class="form-label">Correo Electrónico</label>
                                    <input type="email" class="form-control" id="correo" name="correo" required placeholder="ejemplo@udb.edu.sv">
                                </div>
                                <div class="mb-3">
                                    <label for="contrasena" class="form-label">Contraseña</label>
                                    <input type="password" class="form-control" id="contrasena" name="contrasena" required placeholder="••••••••">
                                </div>
                                <button type="submit" class="btn btn-primary w-100">Ingresar</button>
                            </form>
                            <div class="mt-3 text-center">
                                <a href="${contextPath}/registro.jsp">¿No tienes cuenta? Regístrate</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <script>
            document.getElementById('loginForm').addEventListener('submit', function (e) {
                e.preventDefault(); // ✅ Evita que el formulario envíe por GET

                console.log('Formulario enviado, iniciando fetch...');

                const correo = document.getElementById('correo').value;
                const contrasena = document.getElementById('contrasena').value;

                const data = {
                    correo: correo,
                    contrasena: contrasena
                };

                fetch('${contextPath}/usuarios.do?op=login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(data)
                })
                        .then(response => {
                            console.log('Respuesta recibida:', response.status);
                            if (!response.ok) {
                                throw new Error(`HTTP error! status: ${response.status}`);
                            }
                            return response.json();
                        })
                        .then(data => {
                            console.log('JSON recibido:', data);
                            if (data.success) {
                                console.log('Login exitoso, redirigiendo...');
                                window.location.href = '${contextPath}/index.jsp?msg=' + encodeURIComponent('Bienvenido!');
                            } else {
                                console.log('Login fallido:', data.message);
                                alert(data.message || 'Error al iniciar sesión.');
                            }
                        })
                        .catch(error => {
                            console.error('Fetch error:', error);
                            alert('Error de conexión o respuesta inválida: ' + error.message);
                        });
            });
        </script>
    </body>
</html>