class NotificationManager {
    constructor(container = document.body) {
        this.container = container;
        this.notificationId = 0;
    }

    show(message, type = 'info', options = {}) {
        const {
            duration = 5000, // Erros não fecham automaticamente
            position = 'top-right',
            closeable = true,
        } = options;

        const notification = this.createNotification(message, type, closeable);
        this.addToContainer(notification, position);

        if (duration > 0) {
            setTimeout(() => this.remove(notification), duration);
        }

        return notification.id;
    }

    createNotification(message, type, closeable) {
        this.notificationId++;
        const id = `notification-${this.notificationId}`;

        const notification = document.createElement('div');
        notification.id = id;
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-message">${this.escapeHtml(message)}</span>
            </div>
            ${closeable ? '<button class="notification-close" aria-label="Fechar">&times;</button>' : ''}
        `;

        if (closeable) {
            const closeBtn = notification.querySelector('.notification-close');
            closeBtn.addEventListener('click', () => this.remove(notification));
        }

        return notification;
    }

    addToContainer(notification, position) {
        const positionClass = `notification-container-${position}`;
        let container = this.container.querySelector(`.${positionClass}`);

        if (!container) {
            container = document.createElement('div');
            container.className = `notification-container ${positionClass}`;
            this.container.appendChild(container);
        }

        container.appendChild(notification);
        
        // Animações
        requestAnimationFrame(() => {
            notification.style.transform = 'translateX(0)';
            notification.style.opacity = '1';
        });
    }

    remove(notificationOrId) {
        let notification;
        
        if (typeof notificationOrId === 'string') {
            notification = document.getElementById(notificationOrId);
        } else {
            notification = notificationOrId;
        }

        if (notification) {
            notification.style.transform = 'translateX(100%)';
            notification.style.opacity = '0';
            
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }
    }

    success(message, options = {}) {
        return this.show(message, 'success', options);
    }

    error(message, options = {}) {
        return this.show(message, 'error', options);
    }

    info(message, options = {}) {
        return this.show(message, 'info', options);
    }

    warning(message, options = {}) {
        return this.show(message, 'warning', options);
    }

    clearAll() {
        const containers = this.container.querySelectorAll('.notification-container');
        containers.forEach(container => {
            container.querySelectorAll('.notification').forEach(notification => {
                this.remove(notification);
            });
        });
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

const notificationManager = new NotificationManager();