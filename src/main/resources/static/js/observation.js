// Funciones para manejar el estado y eventos
let currentField = '';
let currentTab = 'general';
let observationModal;

document.addEventListener('DOMContentLoaded', function() {
    observationModal = new bootstrap.Modal(document.getElementById('observationModal'));
});

// Función para crear observación
function createObservation(field) {
    const intermediaryId = document.getElementById('intermediaryId').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Error al crear observación');
    })
    .then(data => {
        // Actualizar UI
        updateFieldUI(field, true);
        showToast('Observación creada correctamente');
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Error al crear la observación', 'error');
    });
}

// Función para remover observación
function removeObservation(field) {
    const intermediaryId = document.getElementById('intermediaryId').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        }
    })
    .then(response => {
        if (response.ok) {
            // Actualizar UI
            updateFieldUI(field, false);
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

// Función para mostrar diálogo de observación
function showObservationDialog(field) {
    currentField = field;
    const intermediaryId = document.getElementById('intermediaryId').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
        .then(response => response.json())
        .then(data => {
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

// Función para guardar observación
function saveObservation() {
    const intermediaryId = document.getElementById('intermediaryId').value;
    const observation = document.getElementById('observationText').value;

    fetch(`/api/intermediary/${intermediaryId}/observation/${currentField}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify({
            observation: observation
        })
    })
    .then(response => {
        if (response.ok) {
            observationModal.hide();
            showToast('Observación guardada correctamente');
            updateFieldUI(currentField, true);
        } else {
            throw new Error('Error al guardar observación');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('Error al guardar la observación', 'error');
    });
}

// Utilidades
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

function updateFieldUI(field, hasObservation) {
    const container = document.querySelector(`[data-field="${field}"]`);
    if (container) {
        // Actualizar botones
        container.querySelector('.btn-check').style.display = hasObservation ? 'none' : 'inline-block';
        container.querySelector('.btn-close').style.display = hasObservation ? 'inline-block' : 'none';
        container.querySelector('.btn-comment').disabled = !hasObservation;
    }
}

function showToast(message, type = 'success') {
    // Asumiendo que tienes un div para toasts
    const toastContainer = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type === 'success' ? 'bg-success' : 'bg-danger'} text-white`;
    toast.innerHTML = `
        <div class="toast-body">
            ${message}
        </div>
    `;
    toastContainer.appendChild(toast);
    new bootstrap.Toast(toast).show();
}