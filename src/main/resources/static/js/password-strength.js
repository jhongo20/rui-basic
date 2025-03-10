// Función para evaluar la fortaleza de una contraseña
function evaluatePasswordStrength(password) {
    if (!password) {
        return 0;
    }
    
    let score = 0;
    
    // Longitud
    if (password.length >= 8) {
        score += 25;
    } else {
        return Math.min(5 * password.length, 25); // 5 puntos por cada carácter hasta 25
    }
    
    // Verificar si tiene letras mayúsculas
    if (/[A-Z]/.test(password)) {
        score += 25;
    }
    
    // Verificar si tiene letras minúsculas
    if (/[a-z]/.test(password)) {
        score += 25;
    }
    
    // Verificar si tiene números
    if (/[0-9]/.test(password)) {
        score += 15;
    }
    
    // Verificar si tiene caracteres especiales
    if (/[^A-Za-z0-9]/.test(password)) {
        score += 10;
    }
    
    return Math.min(score, 100);
}

// Actualizar la barra de progreso y el texto de fortaleza
function updatePasswordStrength(password) {
    const score = evaluatePasswordStrength(password);
    const strengthBar = document.getElementById('passwordStrength');
    const strengthText = document.getElementById('strengthText');
    
    strengthBar.style.width = score + '%';
    strengthText.textContent = score + '%';
    
    // Establecer color basado en la puntuación
    if (score < 30) {
        strengthBar.className = 'progress-bar bg-danger';
    } else if (score < 70) {
        strengthBar.className = 'progress-bar bg-warning';
    } else {
        strengthBar.className = 'progress-bar bg-success';
    }
    
    // Enviar el valor de seguridad al campo oculto
    document.getElementById('securityScore').value = score;
}

// Función para mostrar/ocultar contraseña
function togglePasswordVisibility(inputId, toggleButtonId) {
    const input = document.getElementById(inputId);
    const button = document.getElementById(toggleButtonId);
    
    button.addEventListener('click', function() {
        if (input.type === 'password') {
            input.type = 'text';
            button.querySelector('i').className = 'bi bi-eye-slash';
        } else {
            input.type = 'password';
            button.querySelector('i').className = 'bi bi-eye';
        }
    });
}

// Inicializar todo cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', function() {
    // Agregar event listener para la contraseña
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            updatePasswordStrength(this.value);
        });
        
        // Configurar los botones para mostrar/ocultar contraseña
        togglePasswordVisibility('password', 'togglePassword');
        togglePasswordVisibility('confirmPassword', 'toggleConfirmPassword');
    }
    
    // Validar que las contraseñas coinciden
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const form = document.querySelector('form');
    
    if (form && confirmPasswordInput) {
        form.addEventListener('submit', function(event) {
            if (passwordInput.value !== confirmPasswordInput.value) {
                alert('Las contraseñas no coinciden');
                event.preventDefault();
            }
            
            // Verificar seguridad de la contraseña
            const score = evaluatePasswordStrength(passwordInput.value);
            if (score < 70) {
                if (!confirm('La seguridad de su contraseña es baja. Se recomienda una contraseña más segura. ¿Desea continuar de todos modos?')) {
                    event.preventDefault();
                }
            }
        });
    }
});