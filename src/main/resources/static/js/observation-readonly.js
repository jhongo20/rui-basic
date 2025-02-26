// observation-view-only.js
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar todos los botones de vista
    initViewObservationButtons();
});

function viewObservation(field, event, id = null) {
    if (event) event.preventDefault();

    const intermediaryId = document.getElementById('intermediaryId').value;
    let url;
    let title;
    
    if (field === 'work_exp' && id) {
        url = `/intermediary/workexp/${id}/observation`;
        title = 'Experiencia Laboral';
    } else if (id) {
        url = `/api/idoniedad/${id}/observation`;
        title = 'Registro de Idoneidad';
    } else if (field) {
        url = `/api/intermediary/${intermediaryId}/observation/${field}`;
        title = getFieldLabel(field);
    } else {
        console.error('No se proporcionó field ni id');
        return;
    }
    
    fetch(url)
        .then(response => {
            if (!response.ok) {
                if (response.status === 404) {
                    return { observation: null };
                }
                throw new Error('Error al cargar observación');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.observation) {
                document.getElementById('viewObservationText').value = data.observation;
                document.getElementById('viewObservationTitle').textContent = `Observación sobre ${title}`;
                const viewModal = new bootstrap.Modal(document.getElementById('viewObservationModal'));
                viewModal.show();
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

function initViewObservationButtons() {
    const intermediaryId = document.getElementById('intermediaryId').value;
    
    // Inicializar botones de campos generales
    document.querySelectorAll('.observation-view-only[data-field]').forEach(container => {
        const field = container.dataset.field;
        const viewButton = container.querySelector('.view-observation');
        
        if (field) {
            fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
                .then(response => {
                    if (!response.ok) {
                        if (response.status === 404) {
                            return { observation: null };
                        }
                        throw new Error('Error al verificar observación');
                    }
                    return response.json();
                })
                .then(data => {
                    // Solo mostrar el botón si hay una observación
                    if (data && data.observation) {
                        viewButton.style.display = 'inline-block';
                    } else {
                        viewButton.style.display = 'none';
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        }
    });
    
    // Inicializar botones de idoneidad profesional
    document.querySelectorAll('.observation-view-only[data-idoniedad-id]').forEach(container => {
        const id = container.dataset.idoniedadId;
        const viewButton = container.querySelector('.view-observation');
        
        if (id) {
            fetch(`/api/idoniedad/${id}/observation`)
                .then(response => {
                    if (!response.ok) {
                        if (response.status === 404) {
                            return { observation: null };
                        }
                        throw new Error('Error al verificar observación');
                    }
                    return response.json();
                })
                .then(data => {
                    // Solo mostrar el botón si hay una observación
                    if (data && data.observation) {
                        viewButton.style.display = 'inline-block';
                    } else {
                        viewButton.style.display = 'none';
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        }
    });
    
    // Inicializar botones de experiencia laboral
    document.querySelectorAll('.observation-view-only[data-work-exp-id]').forEach(container => {
        const id = container.dataset.workExpId;
        const viewButton = container.querySelector('.view-observation');
        
        if (id) {
            fetch(`/intermediary/workexp/${id}/observation`)
                .then(response => {
                    if (!response.ok) {
                        if (response.status === 404) {
                            return { observation: null };
                        }
                        throw new Error('Error al verificar observación');
                    }
                    return response.json();
                })
                .then(data => {
                    // Solo mostrar el botón si hay una observación
                    if (data && data.observation) {
                        viewButton.style.display = 'inline-block';
                    } else {
                        viewButton.style.display = 'none';
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        }
    });
}

function getFieldLabel(field) {
    if (field === 'work_exp') return 'Experiencia Laboral';
    if (!field) return 'Registro de Idoneidad';
    
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
        
        // Campos de infraestructura humana
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