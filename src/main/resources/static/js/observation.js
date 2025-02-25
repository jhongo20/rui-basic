// observation.js
// Declaración única de las variables
let currentField = null; // Puede ser null, un string, o vacío
let currentIdoniedadId = null; // Puede ser null o el ID de RuiIdoniedad
let observationModal;

document.addEventListener('DOMContentLoaded', function() {
    observationModal = new bootstrap.Modal(document.getElementById('observationModal'));
    console.log('Modal de observación inicializado, currentField:', currentField, 'currentIdoniedadId:', currentIdoniedadId);
});

function createObservation(field, event, id = null) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;

    if(field === 'work_exp' && id){
        // Observación para experiencia laboral
        currentField = field;
        currentIdoniedadId = id; // Para usar en saveObservation
        
        fetch(`/intermediary/workexp/${id}/observation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => {
            console.log('Create observation status (Work Exp):', response.status, response.url);
            if (response.ok) {
                return response.json();
            }
            throw new Error(`Error al crear observación en experiencia laboral: ${response.status} - ${response.url}`);
        })
        .then(data => {
            console.log('Observation created (Work Exp):', data);
            const buttonsContainer = document.querySelector(`.observation-buttons[data-work-exp-id="${id}"]`);
            if (buttonsContainer) {
                buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
            } else {
                console.error('Contenedor de botones no encontrado para workExpId:', id);
            }
            
            // Desmarcar el checkbox
            const checkbox = document.querySelector(`.work-exp-checkbox[data-work-exp-id="${id}"]`);
            if (checkbox) {
                checkbox.checked = false;
            }
            
            showObservationDialog(field, null, id);
            showToast('Observación creada correctamente (Experiencia Laboral)');
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al crear la observación en Experiencia Laboral', 'error');
        });
    } else if (id) {
        // Observación para Idoneidad Profesional (por registro)
        currentField = null;
        currentIdoniedadId = id;  // CORREGIDO: id en lugar de idoniedadId

        fetch(`/api/idoniedad/${id}/observation`, {  // CORREGIDO: id en lugar de idoniedadId
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => {
            console.log('Create observation status (Idoneidad):', response.status, response.url);
            if (response.ok) {
                return response.json();
            }
            throw new Error(`Error al crear observación en Idoneidad: ${response.status} - ${response.url}`);
        })
        .then(data => {
            console.log('Observation created (Idoneidad):', data);
            const buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${id}"]`);  // CORREGIDO
            if (buttonsContainer) {
                buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
            } else {
                console.error('Contenedor de botones no encontrado para idoniedadId:', id);
            }
            
            // Desmarcar el checkbox automáticamente tras crear la observación
            const checkbox = document.querySelector(`.row-checkbox[data-idoniedad-id="${id}"]`);  // CORREGIDO
            if (checkbox) {
                checkbox.checked = false;
            }
            
            showObservationDialog(null, null, id);
            showToast('Observación creada correctamente (Idoneidad)');
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al crear la observación en Idoneidad', 'error');
        });
    } else if (field) {
        // Observación para Información General (por campo)
        currentField = field;
        currentIdoniedadId = null;

        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => {
            console.log('Create observation status (Intermediary):', response.status, response.url);
            if (response.ok) {
                return response.json();
            }
            throw new Error(`Error al crear observación en Intermediary: ${response.status} - ${response.url}`);
        })
        .then(data => {
            console.log('Observation created (Intermediary):', data);
            const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
            if (buttonsContainer) {
                buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
            } else {
                console.error('Contenedor de botones no encontrado para field:', field);
            }
            
            // Desmarcar el checkbox automáticamente tras crear la observación
            const checkbox = document.querySelector(`.field-checkbox[data-field="${field}"]`);
            if (checkbox) {
                checkbox.checked = false;
            }
            
            showObservationDialog(field);
            showToast('Observación creada correctamente (Intermediary)');
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al crear la observación en Intermediary', 'error');
        });
    } else {
        console.error('No se proporcionó field ni idoniedadId para crear observación');
        showToast('Error: No se especificó el campo o registro para la observación', 'error');
    }
}

function removeObservation(field, event, id = null) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;

    if (field === 'work_exp' && id) {
        // Observación para experiencia laboral
        currentField = field;
        currentIdoniedadId = id;
        
        fetch(`/intermediary/workexp/${id}/observation`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => {
            console.log('Remove observation status (Work Exp):', response.status, response.url);
            if (response.ok) {
                const buttonsContainer = document.querySelector(`.observation-buttons[data-work-exp-id="${id}"]`);
                if (buttonsContainer) {
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                } else {
                    console.error('Contenedor de botones no encontrado para workExpId:', id);
                }
                
                // Marcar el checkbox
                const checkbox = document.querySelector(`.work-exp-checkbox[data-work-exp-id="${id}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                    handleCheckboxChange(checkbox);
                }
                
                showToast('Observación eliminada correctamente (Experiencia Laboral)');
            } else {
                throw new Error('Error al eliminar observación en Experiencia Laboral');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al eliminar la observación en Experiencia Laboral', 'error');
        });
    } else if (id) {
        // Observación para Idoneidad Profesional (por registro)
        currentField = null;
        currentIdoniedadId = id;  // CORREGIDO

        fetch(`/api/idoniedad/${id}/observation`, {  // CORREGIDO
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => {
            console.log('Remove observation status (Idoneidad):', response.status, response.url);
            if (response.ok) {
                const buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${id}"]`);  // CORREGIDO
                if (buttonsContainer) {
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                } else {
                    console.error('Contenedor de botones no encontrado para idoniedadId:', id);
                }
                
                // Marcar el checkbox automáticamente tras eliminar la observación
                const checkbox = document.querySelector(`.row-checkbox[data-idoniedad-id="${id}"]`);  // CORREGIDO
                if (checkbox) {
                    checkbox.checked = true;
                    handleCheckboxChange(checkbox); // Actualizar botones
                }
                
                showToast('Observación eliminada correctamente (Idoneidad)');
            } else {
                throw new Error('Error al eliminar observación en Idoneidad');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al eliminar la observación en Idoneidad', 'error');
        });
    } else if (field) {
        // Observación para Información General (por campo)
        currentField = field;
        currentIdoniedadId = null;

        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            }
        })
        .then(response => {
            console.log('Remove observation status (Intermediary):', response.status, response.url);
            if (response.ok) {
                const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
                if (buttonsContainer) {
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                } else {
                    console.error('Contenedor de botones no encontrado para field:', field);
                }
                
                // Marcar el checkbox automáticamente tras eliminar la observación
                const checkbox = document.querySelector(`.field-checkbox[data-field="${field}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                    handleCheckboxChange(checkbox); // Actualizar botones
                }
                
                showToast('Observación eliminada correctamente (Intermediary)');
            } else {
                throw new Error('Error al eliminar observación en Intermediary');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al eliminar la observación en Intermediary', 'error');
        });
    } else {
        console.error('No se proporcionó field ni idoniedadId para eliminar observación');
        showToast('Error: No se especificó el campo o registro para eliminar la observación', 'error');
    }
}

function showObservationDialog(field, event, id = null) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;
    
    // Almacenar los valores para usar en saveObservation
    if (field) document.getElementById('currentField').value = field;
    if (id) document.getElementById('currentIdoniedadId').value = id;

    if (field === 'work_exp' && id) {
        // Observación para experiencia laboral
        currentField = field;
        currentIdoniedadId = id;

        fetch(`/intermediary/workexp/${id}/observation`)
            .then(response => {
                console.log('Fetch observation status (Work Exp):', response.status, response.url);
                if (!response.ok) {
                    if (response.status === 404) {
                        // Si no hay observación, mostrar campo vacío
                        document.getElementById('observationText').value = '';
                        document.getElementById('observationLabel').textContent = 
                            `Observación sobre la experiencia laboral (ID: ${id})`;
                        observationModal.show();
                        return null;
                    }
                    throw new Error('Error al cargar observación en Experiencia Laboral');
                }
                return response.json();
            })
            .then(data => {
                if (data) {
                    console.log('Observation data (Work Exp):', data);
                    document.getElementById('observationText').value = data.observation || '';
                    document.getElementById('observationLabel').textContent = 
                        `Observación sobre la experiencia laboral (ID: ${id})`;
                }
                observationModal.show();
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Error al cargar la observación en Experiencia Laboral', 'error');
            });
        } else if (id) {
            // Observación para Idoneidad Profesional (por registro)
            currentField = null;
            currentIdoniedadId = id;  // CORREGIDO
    
            fetch(`/api/idoniedad/${id}/observation`)  // CORREGIDO
                .then(response => {
                    console.log('Fetch observation status (Idoneidad):', response.status, response.url);
                    if (!response.ok) {
                        if (response.status === 404) {
                            // Si no hay observación, inicializamos vacía
                            document.getElementById('observationText').value = '';
                            document.getElementById('observationLabel').textContent = 
                                `Observación sobre el registro de idoneidad (ID: ${id})`;
                            observationModal.show();
                            return null;
                        }
                        throw new Error('Error al cargar observación en Idoneidad');
                    }
                    return response.json();
                })
                .then(data => {
                    if (data) {
                        console.log('Observation data (Idoneidad):', data);
                        document.getElementById('observationText').value = data.observation || '';
                        document.getElementById('observationLabel').textContent = 
                            `Observación sobre el registro de idoneidad (ID: ${id})`;
                    }
                    observationModal.show();
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast('Error al cargar la observación en Idoneidad', 'error');
                });
        } else if (field) {
        // Observación para Información General (por campo)
        currentField = field;
        currentIdoniedadId = null;

        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
            .then(response => {
                console.log('Fetch observation status (Intermediary):', response.status, response.url);
                if (!response.ok) throw new Error('Error al cargar observación en Intermediary');
                return response.json();
            })
            .then(data => {
                console.log('Observation data (Intermediary):', data);
                document.getElementById('observationText').value = data.observation || '';
                document.getElementById('observationLabel').textContent = 
                    `Observación sobre ${getFieldLabel(field)}`;
                observationModal.show();
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('Error al cargar la observación en Intermediary', 'error');
            });
    } else {
        console.error('No se proporcionó field ni idoniedadId para mostrar observación');
        showToast('Error: No se especificó el campo o registro para la observación', 'error');
    }
}

function saveObservation(event) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;
    const observation = document.getElementById('observationText').value;

    // Recuperar los valores almacenados en el modal
    const field = document.getElementById('currentField').value;
    const idoniedadId = document.getElementById('currentIdoniedadId').value;

    console.log("saveObservation called with field:", field, "idoniedadId:", idoniedadId);

    if (field === 'work_exp' && idoniedadId) {
        // Guardar observación para experiencia laboral
        fetch(`/intermediary/workexp/${idoniedadId}/observation`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            },
            body: JSON.stringify({ observation: observation })
        })
        .then(response => {
            console.log('Save observation status (Work Exp):', response.status, response.url);
            if (response.ok) {
                observationModal.hide();
                showToast('Observación guardada correctamente (Experiencia Laboral)');
                const buttonsContainer = document.querySelector(`.observation-buttons[data-work-exp-id="${idoniedadId}"]`);
                if (buttonsContainer) {
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                } else {
                    console.error('Contenedor de botones no encontrado para workExpId:', idoniedadId);
                }
                
                // Asegurarse de que el checkbox esté desmarcado
                const checkbox = document.querySelector(`.work-exp-checkbox[data-work-exp-id="${idoniedadId}"]`);
                if (checkbox) {
                    checkbox.checked = false;
                }
            } else {
                return response.text().then(text => { throw new Error(`Error al guardar observación en Experiencia Laboral: ${response.status} - ${text}`); });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al guardar la observación en Experiencia Laboral', 'error');
            observationModal.show(); // Mantener modal abierto en caso de error
        });
    } else if (idoniedadId) {
        // Guardar observación para Idoneidad Profesional
        fetch(`/api/idoniedad/${idoniedadId}/observation`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            },
            body: JSON.stringify({ observation: observation })
        })
        .then(response => {
            console.log('Save observation status (Idoneidad):', response.status, response.url);
            if (response.ok) {
                observationModal.hide();
                showToast('Observación guardada correctamente (Idoneidad)');
                const buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${idoniedadId}"]`);
                if (buttonsContainer) {
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                } else {
                    console.error('Contenedor de botones no encontrado para idoniedadId:', idoniedadId);
                }
                
                // Asegurarse de que el checkbox esté desmarcado
                const checkbox = document.querySelector(`.row-checkbox[data-idoniedad-id="${idoniedadId}"]`);
                if (checkbox) {
                    checkbox.checked = false;
                }
            } else {
                return response.text().then(text => { throw new Error(`Error al guardar observación en Idoneidad: ${response.status} - ${text}`); });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al guardar la observación en Idoneidad', 'error');
        });
    } else if (field) {
        // Guardar observación para Información General
        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
            },
            body: JSON.stringify({ observation: observation })
        })
        .then(response => {
            console.log('Save observation status (Intermediary):', response.status, response.url);
            if (response.ok) {
                observationModal.hide();
                showToast('Observación guardada correctamente (Intermediary)');
                const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
                if (buttonsContainer) {
                    buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                    buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                    buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                } else {
                    console.error('Contenedor de botones no encontrado para field:', field);
                }
                
                // Asegurarse de que el checkbox esté desmarcado
                const checkbox = document.querySelector(`.field-checkbox[data-field="${field}"]`);
                if (checkbox) {
                    checkbox.checked = false;
                    handleCheckboxChange(checkbox); // Actualizar botones
                }
            } else {
                return response.text().then(text => { throw new Error(`Error al guardar observación en Intermediary: ${response.status} - ${text}`); });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('Error al guardar la observación en Intermediary', 'error');
            observationModal.show(); // Mantener modal abierto en caso de error
        });
    } else {
        console.error('No se especificó el campo o registro para guardar la observación');
        showToast('Error: No se especificó el campo o registro para guardar la observación', 'error');
    }
}

function getFieldLabel(field) {
    if (field === 'work_exp') return 'Experiencia Laboral';
    if (!field) return 'Registro de Idoneidad'; // Para Idoneidad Profesional (registro general)
    
    const labels = {
        // Campos originales
        'nit': 'NIT',
        'business_name': 'Razón Social',
        'department_id': 'Departamento',
        'city_id': 'Ciudad',
        'address': 'Dirección',
        'email': 'Correo electrónico',
        'phone': 'Teléfono Fijo',
        'document_type': 'Tipo de Identificación',
        'document_number': 'No. Identificación',
        'first_name': 'Primer Nombre',
        'second_name': 'Segundo Nombre',
        'first_surname': 'Primer Apellido',
        'second_surname': 'Segundo Apellido',
        'cellphone': 'Teléfono Móvil',
        
        // Nuevos campos de infraestructura humana
        'infra_document_type': 'Tipo de Documento (Infraestructura)',
        'infra_document_number': 'Número de Documento (Infraestructura)',
        'infra_first_name': 'Primer Nombre (Infraestructura)',
        'infra_second_name': 'Segundo Nombre (Infraestructura)',
        'infra_first_surname': 'Primer Apellido (Infraestructura)',
        'infra_second_surname': 'Segundo Apellido (Infraestructura)',

        // Campos de infraestructura operativa
        'operativa_camara': 'Soporte matrícula de cámara de comercio',
        'operativa_software': 'Soporte de certificación de Software o Base de Datos',
        'operativa_equipos': 'Soporte de certificación de equipos tecnológicos',
        'operativa_phone1': 'Línea telefónica 1',
        'operativa_phone2': 'Línea telefónica 2',
        'operativa_phone3': 'Línea telefónica 3',
        'operativa_phone_fax': 'Número del Fax',
        'operativa_email': 'Correo Electrónico (Operativa)',
        'operativa_address': 'Dirección de oficina de atención al ciudadano',

        // Campo de firma digitalizada
        'firma_digitalizada': 'Firma Digitalizada'
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