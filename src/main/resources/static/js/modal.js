class ModalManager {
    constructor(modal) {
        this.modal = modal
        this.init();
    }

    init() {
        document.addEventListener('click', (e) => {
            if (e.target === this.modal) {
                this.close();
            }
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.modal.style.display === 'block') {
                this.close();
            }
        });

         document.querySelector('.close-button').addEventListener('click', () => {
            this.close();
        });
    }

    close() {
        if (this.modal) {
            this.modal.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => {
                this.modal.style.display = 'none';
                this.modal.style.animation = '';
                document.body.style.overflow = '';
                
                if (this.onCloseCallback) {
                    this.onCloseCallback();
                }
            }, 300);
        }
    }

    show({modalTitle, modalMessage}) {
        if (this.modal) {
            modalTitle && (document.querySelector('#modalTitle').innerText = modalTitle);
            modalMessage && (document.querySelector('#modalMessage').innerText = modalMessage);
            this.modal.style.display = 'block';
            this.modal.style.animation = 'slideIn 0.3s ease';
            document.body.style.overflow = 'hidden';
        }
    }
    
}

// Initialize modal manager
let confirmAction = null;

const dynamicModal = document.getElementById('dynamicModal')
let modalManager = null;
if (dynamicModal) {
    modalManager = new ModalManager(dynamicModal);
}

const hiddenModal = document.getElementById('hiddenModal')
let hiddenModalManager = null;
if (hiddenModal) {
    hiddenModalManager = new ModalManager(document.getElementById('hiddenModal'));
}

function closeModal() {
    if (dynamicModal) {
        modalManager.close();
    }
    if (hiddenModal) {
        hiddenModalManager.close();
    }
}

function showModal(modalTitle, modalMessage) {
    if (hiddenModal) {
        hiddenModalManager.show({modalTitle, modalMessage});
    }
}

window.closeModal = closeModal;