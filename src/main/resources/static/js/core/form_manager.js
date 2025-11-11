class FormManager {
    constructor(formElement, options = {}) {
        this.form = formElement;
        this.options = {
            validateRealTime: true,
            confirmUnsavedChanges: true,
            ...options
        };
        
        this.originalData = this.getFormData();
        this.init();
    }

    init() {
        if (this.options.validateRealTime) {
            this.addRealTimeValidation();
        }

        if (this.options.confirmUnsavedChanges) {
            this.trackChanges();
        }

        this.form.addEventListener('submit', (e) => this.handleSubmit(e));
    }

    addRealTimeValidation() {
        const inputs = this.form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => this.clearFieldError(input));
        });
    }

    trackChanges() {
        const inputs = this.form.querySelectorAll('input, textarea, select');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                this.hasUnsavedChanges = true;
            });
        });

        window.addEventListener('beforeunload', (e) => {
            if (this.hasUnsavedChanges) {
                e.preventDefault();
                e.returnValue = '';
            }
        });
    }

    async validateField(input) {
        const validators = this.getFieldValidators(input);
        
        for (const validator of validators) {
            const error = await validator(input);
            if (error) {
                this.showFieldError(input, error);
                return false;
            }
        }
        
        this.clearFieldError(input);
        return true;
    }

    getFieldValidators(input) {
        const validators = [];
        
        if (input.required) {
            validators.push(this.validateRequired);
        }
        
        if (input.type === 'email') {
            validators.push(this.validateEmail);
        }
        
        // Adicione mais validadores conforme necessário
        const customValidator = input.dataset.validator;
        if (customValidator && this[customValidator]) {
            validators.push(this[customValidator].bind(this));
        }

        return validators;
    }

    validateRequired(input) {
        if (!input.value.trim()) {
            return 'Este campo é obrigatório';
        }
        return null;
    }

    validateEmail(input) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (input.value && !emailRegex.test(input.value)) {
            return 'Email inválido';
        }
        return null;
    }

    showFieldError(input, message) {
        this.clearFieldError(input);
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.textContent = message;
        
        input.classList.add('error');
        input.parentNode.appendChild(errorDiv);
    }

    clearFieldError(input) {
        const existingError = input.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
        input.classList.remove('error');
    }

    getFormData() {
        const formData = new FormData(this.form);
        const data = {};
        
        for (const [key, value] of formData.entries()) {
            data[key] = value;
        }
        
        return data;
    }

    setFormData(data) {
        Object.keys(data).forEach(key => {
            const input = this.form.querySelector(`[name="${key}"]`);
            if (input) {
                if (input.type === 'checkbox') {
                    input.checked = Boolean(data[key]);
                } else {
                    input.value = data[key];
                }
            }
        });
    }

    resetForm() {
        this.form.reset();
        this.originalData = this.getFormData();
        this.hasUnsavedChanges = false;
        
        // Limpa todos os erros
        this.form.querySelectorAll('.field-error').forEach(error => error.remove());
        this.form.querySelectorAll('.error').forEach(input => input.classList.remove('error'));
    }

    async handleSubmit(event) {
        event.preventDefault();
        
        const isValid = await this.validateForm();
        if (!isValid) {
            return;
        }

        if (this.options.onSubmit) {
            await this.options.onSubmit(this.getFormData());
        }
    }

    async validateForm() {
        const inputs = this.form.querySelectorAll('input, textarea, select');
        let isValid = true;

        for (const input of inputs) {
            const fieldValid = await this.validateField(input);
            if (!fieldValid) {
                isValid = false;
            }
        }

        return isValid;
    }

    markAsSaved() {
        this.hasUnsavedChanges = false;
        this.originalData = this.getFormData();
    }
}