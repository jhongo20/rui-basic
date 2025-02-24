// checkboxes.js
function handleCheckboxChange(checkbox) {
    const field = checkbox.dataset.field;
    const isChecked = checkbox.checked;
    const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
    
    if (!isChecked) {
        // Si el checkbox está desmarcado, verificar si hay observación
        const intermediaryId = document.getElementById('intermediaryId').value;
        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
            .then(response => response.json())
            .then(data => {
                if (data && data.iconClose) {
                    // Si hay observación, mostrar botones de eliminar y editar
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                } else {
                    // Si no hay observación, mostrar solo el botón de agregar
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                }
            })
            .catch(error => console.error('Error al verificar observación:', error));
    } else {
        // Si el checkbox está marcado, ocultar todos los botones
        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
        buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
    }
}

// Inicialización al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    const checkboxes = document.querySelectorAll('.field-checkbox');
    checkboxes.forEach(checkbox => {
        const field = checkbox.dataset.field;
        const intermediaryId = document.getElementById('intermediaryId').value;
        
        // Consultar si hay observación para este campo
        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
            .then(response => response.json())
            .then(data => {
                if (data && data.iconClose) {
                    // Si hay observación, desmarcar el checkbox y mostrar botones de eliminar y editar
                    checkbox.checked = false;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                } else {
                    // Si no hay observación, marcar el checkbox y ocultar botones
                    checkbox.checked = true;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                }
            })
            .catch(error => console.error('Error al inicializar observaciones:', error));
    });
});