<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="header.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Login - Biblioteca UDB</title>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="col-md-4 col-md-offset-4">
            <div class="card-login">
                <div class="text-center mb-4">
                    <h3 style="font-weight: bold;">Iniciar Sesión</h3>
                    <img src="${contextPath}/assets/img/udb.png" height="60" style="margin-bottom: 15px;">
                </div>

                <form id="loginForm" method="POST">
                    <div class="form-group">
                        <label for="correo">Correo Electrónico</label>
                        <input type="email" class="form-control" id="correo" name="correo" required placeholder="ejemplo@udb.edu.sv">
                    </div>
                    <div class="form-group">
                        <label for="contrasena">Contraseña</label>
                        <input type="password" class="form-control" id="contrasena" name="contrasena" required>
                    </div>
                    <button type="submit" class="btn btn-primary btn-block" style="background-color: #1a428a; border:none;">Ingresar</button>
                </form>

                <div class="text-center" style="margin-top: 15px;">
                    <a href="${contextPath}/registro.jsp">¿No tienes cuenta? Regístrate</a>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // Tu script de login existente se mantiene igual
    document.getElementById('loginForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const correo = document.getElementById('correo').value;
        const contrasena = document.getElementById('contrasena').value;
        const data = { correo: correo, contrasena: contrasena };

        fetch('${contextPath}/usuarios.do?op=login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    window.location.href = '${contextPath}/index.jsp?msg=' + encodeURIComponent('Bienvenido!');
                } else {
                    alertify.error(data.message || 'Error al iniciar sesión.');
                }
            })
            .catch(error => {
                console.error('Fetch error:', error);
                alertify.error('Error de conexión.');
            });
    });
</script>
</body>
</html>