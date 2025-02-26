// Al final de tu página HTML, antes de cerrar el body 

document.addEventListener('DOMContentLoaded', function() {
    // Obtener el formulario de marcar como revisado
    const revisadoForms = document.querySelectorAll('form[action*="/intermediary/status/marcar-revisado/"]');
    
    if (revisadoForms.length > 0) {
        revisadoForms.forEach(form => {
            form.addEventListener('submit', function(e) {
                // Prevenir el envío del formulario
                e.preventDefault();
                
                const formAction = this.action;
                const intermediaryId = formAction.split('/').pop();
                
                // Realizar verificación de observaciones
                fetch(`/intermediary/check-observaciones/${intermediaryId}`)
                    .then(response => response.json())
                    .then(data => {
                        if (data.observacionesPendientes) {
                            // Actualizar el mensaje del modal con la cantidad exacta
                            document.getElementById('observationsMessage').textContent = 
                                `Existen ${data.cantidadObservaciones} observaciones no revisadas, ¿está seguro que desea marcar como revisado el registro?`;
                            
                            // Configurar el botón de confirmación
                            document.getElementById('confirmMarkAsReviewed').onclick = function() {
                                // Usar un formulario oculto para enviar la solicitud
                                const hiddenForm = document.getElementById('hiddenReviewForm');
                                hiddenForm.action = formAction;
                                hiddenForm.submit();
                                
                                // Cerrar el modal
                                const modal = bootstrap.Modal.getInstance(document.getElementById('observationsConfirmModal'));
                                modal.hide();
                            };
                            
                            // Mostrar el modal
                            const modal = new bootstrap.Modal(document.getElementById('observationsConfirmModal'));
                            modal.show();
                        } else {
                            // Si no hay observaciones, enviar el formulario directamente
                            form.submit();
                        }
                    })
                    .catch(error => {
                        console.error('Error al verificar observaciones:', error);
                        // En caso de error, no enviar el formulario automáticamente
                        alert('Ocurrió un error al verificar las observaciones. Por favor, inténtelo de nuevo.');
                    });
            });
        });
    }
});