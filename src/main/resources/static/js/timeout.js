// timeout.js - Versión con modal jQuery y cierre automático
document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando control de timeout de sesión (jQuery)');
    
    // Primero, agregar el modal al DOM
    const modalHtml = `
    <div class="modal fade" id="sessionWarningModal" tabindex="-1" role="dialog" aria-labelledby="sessionWarningModalLabel" aria-hidden="true">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="sessionWarningModalLabel">Advertencia de Sesión</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="alert alert-warning">
                        <i class="fas fa-exclamation-triangle mr-2"></i>
                        Tu sesión está a punto de expirar en <span id="countdownTimer" class="font-weight-bold">60</span> segundos por inactividad.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Cerrar</button>
                    <button type="button" class="btn btn-primary" id="extendSessionBtn">
                        <i class="fas fa-sync-alt mr-1"></i> Extender Sesión
                    </button>
                </div>
            </div>
        </div>
    </div>`;

    // Insertar el modal en el cuerpo del documento
    $('body').append(modalHtml);
    
    // Configurar el evento para el botón de extender sesión
    $(document).on('click', '#extendSessionBtn', function() {
        extendSession();
        $('#sessionWarningModal').modal('hide');
    });
    
    // Configuración de tiempos (en milisegundos)
    const sessionTimeout = 10 * 60 * 1000; // 10 minutos 
    const warningTime = 1 * 60 * 1000;     // 1 minuto antes de expirar
    
    let warningTimer;
    let logoutTimer;
    let countdownInterval;
    
    // Función para reiniciar los temporizadores al detectar actividad
    function resetTimers() {
        console.log('Reiniciando temporizadores');
        clearTimeout(warningTimer);
        clearTimeout(logoutTimer);
        clearInterval(countdownInterval);
        
        // Establecer el temporizador de advertencia
        warningTimer = setTimeout(function() {
            showWarning();
        }, sessionTimeout - warningTime);
        
        // Establecer el temporizador de cierre de sesión
        logoutTimer = setTimeout(function() {
            logout();
        }, sessionTimeout);
    }
    
    // Función para mostrar la advertencia
    function showWarning() {
        console.log('Mostrando advertencia de sesión por expirar');
        
        // Mostrar el modal usando jQuery
        $('#sessionWarningModal').modal({
            backdrop: 'static',  // No se cierra al hacer clic fuera
            keyboard: false      // No se cierra con la tecla Escape
        });
        
        // Iniciar la cuenta regresiva
        let secondsLeft = 60;
        $('#countdownTimer').text(secondsLeft);
        
        // Limpiar cualquier intervalo existente
        clearInterval(countdownInterval);
        
        // Crear nuevo intervalo
        countdownInterval = setInterval(function() {
            secondsLeft--;
            $('#countdownTimer').text(secondsLeft);
            
            if (secondsLeft <= 0) {
                clearInterval(countdownInterval);
                $('#sessionWarningModal').modal('hide');
                // Redirigir al logout después de un breve retraso para asegurar que el modal se cierre
                setTimeout(function() {
                    logout();
                }, 500);
            }
        }, 1000);
    }
    
    // Función para extender la sesión
    function extendSession() {
        console.log('Extendiendo sesión');
        
        fetch('/api/session/extend', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (response.ok) {
                console.log('Sesión extendida con éxito');
                resetTimers();
            } else {
                console.error('Error al extender la sesión:', response.status);
                // Si hay error en la extensión, intentamos igual mantener la sesión activa
                resetTimers();
            }
        })
        .catch(error => {
            console.error('Error al extender la sesión:', error);
            // Si hay error en la extensión, intentamos igual mantener la sesión activa
            resetTimers();
        });
    }
    
    // Función para cerrar sesión
    function logout() {
        console.log('Cerrando sesión por timeout');
        window.location.href = '/logout';
    }
    
    // Eventos para detectar actividad del usuario
    const activityEvents = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];
    activityEvents.forEach(function(eventName) {
        document.addEventListener(eventName, function() {
            resetTimers();
        }, true);
    });
    
    // Iniciar los temporizadores cuando carga la página
    resetTimers();
    console.log('Control de timeout de sesión inicializado');
    
    // Para pruebas - descomenta esta línea para probar inmediatamente
    // setTimeout(showWarning, 3000);
});