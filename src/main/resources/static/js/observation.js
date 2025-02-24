// observation.js
let currentField = '';
let observationModal;

document.addEventListener('DOMContentLoaded', function() {
    observationModal = new bootstrap.Modal(document.getElementById('observationModal'));
});

function createObservation(field, event) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        }
    })
    .then(response => {
        console.log('Create observation status:', response.status);
        if (response.ok) {
            return response.json();
        }
        throw new Error(`Error al crear observación: ${response.status}`);
    })
    .then(data => {
        console.log('Observation created:', data);
        const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
        buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
        
        // Desmarcar el checkbox automáticamente tras crear la observación
        const checkbox = document.querySelector(`.field-checkbox[data-field="${field}"]`);
        if (checkbox) {
            checkbox.checked = false;
        }
        
        showObservationDialog(field);
        showToast('Observación creada correctamente');
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Error al crear la observación', 'error');
    });
}

function removeObservation(field, event) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        }
    })
    .then(response => {
        if (response.ok) {
            const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
            buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
            buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
            buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
            
            // Marcar el checkbox automáticamente tras eliminar la observación
            const checkbox = document.querySelector(`.field-checkbox[data-field="${field}"]`);
            if (checkbox) {
                checkbox.checked = true;
                handleCheckboxChange(checkbox); // Actualizar botones
            }
            
            showToast('Observación eliminada correctamente');
        } else {
            throw new Error('Error al eliminar observación');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Error al eliminar la observación', 'error');
    });
}

function showObservationDialog(field, event) {
    if (event) event.preventDefault();

    currentField = field;
    const intermediaryId = document.getElementById('intermediaryId').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
        .then(response => {
            console.log('Fetch observation status:', response.status);
            if (!response.ok) throw new Error('Error al cargar observación');
            return response.json();
        })
        .then(data => {
            console.log('Observation data:', data);
            document.getElementById('observationText').value = data.observation || '';
            document.getElementById('observationLabel').textContent = 
                `Observación sobre ${getFieldLabel(field)}`;
            observationModal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al cargar la observación', 'error');
        });
}

function saveObservation(event) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;
    const observation = document.getElementById('observationText').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${currentField}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        },
        body: JSON.stringify({ observation: observation })
    })
    .then(response => {
        console.log('Save observation status:', response.status);
        if (response.ok) {
            observationModal.hide();
            showToast('Observación guardada correctamente');
            const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${currentField}"]`);
            buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
            buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
            buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
            
            // Asegurarse de que el checkbox esté desmarcado
            const checkbox = document.querySelector(`.field-checkbox[data-field="${currentField}"]`);
            if (checkbox) {
                checkbox.checked = false;
                handleCheckboxChange(checkbox); // Actualizar botones
            }
        } else {
            return response.text().then(text => { throw new Error(`Error al guardar observación: ${response.status} - ${text}`); });
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Error al guardar la observación', 'error');
        observationModal.show(); // Mantener modal abierto en caso de error
    });
}

function getFieldLabel(field) {
    const labels = {
        'nit': 'NIT',
        'business_name': 'Razón Social',
        'department_id': 'Departamento',
        'city_id': 'Ciudad',
        'address': 'Dirección',
        'email': 'Correo electrónico',
        'phone': 'Teléfono Fijo'
    };
    return labels[field] || field;
}

function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type === 'success' ? 'bg-success' : 'bg-danger'} text-white`;
    toast.innerHTML = `<div class="toast-body">${message}</div>`;
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    setTimeout(() => toast.remove(), 3000);
}