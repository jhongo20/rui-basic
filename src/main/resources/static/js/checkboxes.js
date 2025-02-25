// checkboxes.js
function handleCheckboxChange(checkbox) {
    const isFieldCheckbox = checkbox.classList.contains('field-checkbox');
    const isRowCheckbox = checkbox.classList.contains('row-checkbox');
    const intermediaryId = document.getElementById('intermediaryId').value;
    let field = null;
    let idoniedadId = null;
    let buttonsContainer = null;

    console.log('Checkbox cambiado:', {
        isFieldCheckbox,
        isRowCheckbox,
        dataset: checkbox.dataset
    });

    if (isFieldCheckbox) {
        field = checkbox.dataset.field;
        buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
    } else if (isRowCheckbox) {
        idoniedadId = checkbox.dataset.idoniedadId;
        if (!idoniedadId) {
            console.error('No se encontró data-idoniedad-id en el checkbox:', checkbox);
            return;
        }
        buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${idoniedadId}"]`);
    
    // Agregar log para depuración
    console.log('Checkbox de idoneidad:', {
        idoniedadId,
        buttonsContainer,
        isChecked: checkbox.checked,
        checkbox
    });
        
    } else {
        console.error('Checkbox no reconocido:', checkbox);
        return;
    }

    if (!buttonsContainer) {
        console.error('Contenedor de botones no encontrado para:', { field, idoniedadId });
        return;
    }

    const isChecked = checkbox.checked;

    if (!isChecked) {
        // Si el checkbox está desmarcado, verificar si hay observación
        if (field) {
            // Para Información General (por campo)
            fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
                .then(response => {
                    console.log('Respuesta fetch (Intermediary):', response.status, response.url);
                    if (!response.ok) throw new Error(`Error HTTP: ${response.status} - ${response.url}`);
                    return response.json();
                })
                .then(data => {
                    console.log('Datos de observación (Intermediary):', data);
                    if (data && data.iconClose) {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                    } else {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                    }
                })
                .catch(error => console.error('Error al verificar observación en Intermediary:', error));
        } // checkboxes.js (ajuste en handleCheckboxChange para rowCheckbox)
        // checkboxes.js (ajuste en handleCheckboxChange para rowCheckbox)
else if (idoniedadId) {
    // Para Idoneidad Profesional (por registro)
    fetch(`/api/idoniedad/${idoniedadId}/observation`)
        .then(response => {
            console.log('Respuesta fetch (Idoneidad):', response.status, response.url);
            if (response.status === 404 || !response.ok) {
                // Si no hay observación (404) o hay otro error, asumimos iconClose: false
                buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                return Promise.resolve({ iconClose: false, commentDisabled: true, observation: "No hay observación" });
            }
            return response.json();
        })
        .then(data => {
            console.log('Datos de observación (Idoneidad):', data);
            if (data && data.iconClose) {
                buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
            } else {
                buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
                buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
            }
        })
        .catch(error => {
            console.error('Error al verificar observación en Idoneidad:', error);
            // Si hay un error, mostrar "Agregar" por defecto
            buttonsContainer.querySelector('.btn-add-observation').style.display = 'inline-block';
            buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
            buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
        });
}
    } else {
        // Si el checkbox está marcado, ocultar todos los botones
        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
        buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
    }
}

// Inicialización al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    const fieldCheckboxes = document.querySelectorAll('.field-checkbox');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    const intermediaryId = document.getElementById('intermediaryId').value;

    console.log('Inicializando checkboxes:', { fieldCheckboxes: fieldCheckboxes.length, rowCheckboxes: rowCheckboxes.length });

    // Inicializar checkboxes de Información General
    fieldCheckboxes.forEach(checkbox => {
        const field = checkbox.dataset.field;
        fetch(`/api/intermediary/${intermediaryId}/observation/${field}`)
            .then(response => {
                console.log('Respuesta inicial (Intermediary):', response.status, response.url);
                if (!response.ok) throw new Error(`Error HTTP: ${response.status} - ${response.url}`);
                return response.json();
            })
            .then(data => {
                console.log('Datos iniciales (Intermediary):', data);
                if (data && data.iconClose) {
                    checkbox.checked = false;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
                    if (buttonsContainer) {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                    } else {
                        console.error('Contenedor de botones no encontrado para field:', field);
                    }
                } else {
                    checkbox.checked = true;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-field="${field}"]`);
                    if (buttonsContainer) {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                    }
                }
            })
            .catch(error => console.error('Error al inicializar observaciones en Intermediary:', error));
    });

    // Inicializar checkboxes de Idoneidad Profesional
    rowCheckboxes.forEach(checkbox => {
        const idoniedadId = checkbox.dataset.idoniedadId;
        if (!idoniedadId) {
            console.error('No se encontró data-idoniedad-id en el checkbox:', checkbox);
            return;
        }
        fetch(`/api/idoniedad/${idoniedadId}/observation`)
            .then(response => {
                console.log('Respuesta inicial (Idoneidad):', response.status, response.url);
                if (response.status === 404) {
                    // Si no hay observación (404), asumimos iconClose: false
                    checkbox.checked = true;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${idoniedadId}"]`);
                    if (buttonsContainer) {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                    }
                    return Promise.resolve({ iconClose: false, commentDisabled: true, observation: "" });
                } else if (!response.ok) {
                    throw new Error(`Error HTTP: ${response.status} - ${response.url}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Datos iniciales (Idoneidad):', data);
                if (data && data.iconClose) {
                    checkbox.checked = false;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${idoniedadId}"]`);
                    if (buttonsContainer) {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'inline-block';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'inline-block';
                    } else {
                        console.error('Contenedor de botones no encontrado para idoniedadId:', idoniedadId);
                    }
                } else {
                    checkbox.checked = true;
                    const buttonsContainer = document.querySelector(`.observation-buttons[data-idoniedad-id="${idoniedadId}"]`);
                    if (buttonsContainer) {
                        buttonsContainer.querySelector('.btn-add-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-remove-observation').style.display = 'none';
                        buttonsContainer.querySelector('.btn-show-observation').style.display = 'none';
                    }
                }
            })
            .catch(error => console.error('Error al inicializar observaciones en Idoneidad:', error));
    });
});