class ModalManager {
    constructor() {
        this.modalContainer = document.getElementById('modal-manager-container');
        this.modals = {
            confirm: document.getElementById('confirm-modal'),
            info: document.getElementById('info-modal'),
            success: document.getElementById('success-modal'),
            error: document.getElementById('error-modal'),
            warning: document.getElementById('warning-modal')
        };
        
        this.currentModal = null;
        this.initEventListeners();
    }

    initEventListeners() {
        // Event listeners to confirmation modal
        const confirmCancel = document.getElementById('confirm-modal-cancel');
        const confirmConfirm = document.getElementById('confirm-modal-confirm');
        
        if (confirmCancel) {
            confirmCancel.addEventListener('click', () => this.hide());
        }
        if (confirmConfirm) {
            confirmConfirm.addEventListener('click', () => {
                if (this.onConfirmCallback) {
                    this.onConfirmCallback();
                }
                this.hide();
            });
        }

        // Event listeners to close
        const closeButtons = [
            'info-modal-close',
            'success-modal-close', 
            'error-modal-close',
            'warning-modal-close'
        ];

        closeButtons.forEach(btnId => {
            const button = document.getElementById(btnId);
            if (button) {
                button.addEventListener('click', () => this.hide());
            }
        });

        // Closes modal when clicking backdrop
        if (this.modalContainer) {
            this.modalContainer.addEventListener('click', (e) => {
                if (e.target === this.modalContainer) {
                    this.hide();
                }
            });
        }

        // Closes modal with ESC
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.currentModal) {
                this.hide();
            }
        });
    }

    show(options) {
        const {
            title = '',
            message = '',
            type = 'info',
            closeMessage = 'Fechar',
            confirmMessage = 'Confirmar',
            onConfirm = null,
            onClose = null
        } = options;

        this.hide(); // Closes every opened modal

        const modal = this.modals[type];
        if (!modal) {
            console.error(`Modal type '${type}' not found`);
            return;
        }

        this.currentModal = modal;
        this.onConfirmCallback = onConfirm;
        this.onCloseCallback = onClose;

        this.setupModalContent(modal, type, title, message, closeMessage, confirmMessage);

        // Shows modal
        this.modalContainer.classList.add('active');
        modal.style.display = 'block';

        // Animations
        requestAnimationFrame(() => {
            modal.classList.add('active');
        });

        this.focusAppropriateButton(modal, type);
    }

    setupModalContent(modal, type, title, message, closeMessage, confirmMessage) {
        // Title and message setup
        const titleElement = modal.querySelector(`#${type}-modal-title`);
        const messageElement = modal.querySelector(`#${type}-modal-message`);

        if (titleElement) titleElement.textContent = title;
        if (messageElement) messageElement.textContent = message;

        // Buttons setup
        switch (type) {
            case 'confirm':
                const cancelBtn = modal.querySelector('.modal-btn-cancel');
                const confirmBtn = modal.querySelector('.modal-btn-confirm');
                if (cancelBtn) cancelBtn.textContent = closeMessage;
                if (confirmBtn) confirmBtn.textContent = confirmMessage;
                break;
                
            case 'info':
            case 'success':
            case 'error':
            case 'warning':
                const closeBtn = modal.querySelector('.modal-btn-close');
                if (closeBtn) closeBtn.textContent = closeMessage;
                break;
        }
    }

    focusAppropriateButton(modal, type) {
        let buttonToFocus = null;

        switch (type) {
            case 'confirm':
                buttonToFocus = modal.querySelector('.modal-btn-cancel');
                break;
            case 'info':
            case 'success':
            case 'error':
            case 'warning':
                buttonToFocus = modal.querySelector('.modal-btn-close');
                break;
        }

        if (buttonToFocus) {
            setTimeout(() => buttonToFocus.focus(), 100);
        }
    }

    hide() {
        if (this.currentModal) {
            this.currentModal.classList.remove('active');
            
            
            this.currentModal.style.display = 'none';
            this.modalContainer.classList.remove('active');
            this.currentModal = null;
            
            if (this.onCloseCallback) {
                this.onCloseCallback();
                this.onCloseCallback = null;
            }
        }
    }

    confirm(options) {
        this.show({ ...options, type: 'confirm' });
    }

    info(options) {
        this.show({ ...options, type: 'info' });
    }

    success(options) {
        this.show({ ...options, type: 'success' });
    }

    error(options) {
        this.show({ ...options, type: 'error' });
    }

    warning(options) {
        this.show({ ...options, type: 'warning' });
    }
}

const modalManager = new ModalManager();