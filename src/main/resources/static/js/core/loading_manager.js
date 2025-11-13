class LoadingManager {
    constructor() {
        this.loadingCount = 0;
    }

    show(options = {}) {
        this.loadingCount++;
        
        if (this.loadingCount === 1) {
            this.createLoadingOverlay(options);
        }

        return this.loadingCount;
    }

    hide() {
        this.loadingCount = Math.max(0, this.loadingCount - 1);
        
        if (this.loadingCount === 0) {
            this.removeLoadingOverlay();
        }

        return this.loadingCount;
    }

    createLoadingOverlay(options = {}) {
        const {
            message = 'Carregando...',
            spinner = true,
            backdrop = true,
            target = document.body
        } = options;

        const overlay = document.createElement('div');
        overlay.className = 'loading-overlay';
        overlay.innerHTML = `
            ${backdrop ? '<div class="loading-backdrop"></div>' : ''}
            <div class="loading-content">
                ${spinner ? '<div class="loading-spinner"></div>' : ''}
                <p class="loading-message">${this.escapeHtml(message)}</p>
            </div>
        `;

        overlay.id = 'global-loading-overlay';
        target.appendChild(overlay);

        // Força reflow para animação
        requestAnimationFrame(() => {
            overlay.classList.add('active');
        });
    }

    removeLoadingOverlay() {
        const overlay = document.getElementById('global-loading-overlay');
        if (overlay) {
            overlay.classList.remove('active');
            setTimeout(() => {
                if (overlay.parentNode) {
                    overlay.parentNode.removeChild(overlay);
                }
            }, 300);
        }
    }

    wrapAsync(fn, options = {}) {
        return async (...args) => {
            this.show(options);
            try {
                const result = await fn(...args);
                return result;
            } finally {
                this.hide();
            }
        };
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Instância global
const loadingManager = new LoadingManager();