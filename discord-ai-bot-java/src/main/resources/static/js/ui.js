/**
 * UI Components - Toast, Modal, Drawer
 * Reusable UI components for the application
 */

const UI = {
    /**
     * Show toast notification
     */
    toast(message, type = 'info', duration = 3000) {
        const existing = document.querySelectorAll('.ui-toast');
        existing.forEach(toast => toast.remove());

        const toast = document.createElement('div');
        toast.className = `ui-toast ui-toast-${type}`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'polite');

        const icons = { success: '✓', error: '✕', warning: '⚠', info: 'ℹ' };
        const icon = icons[type] || icons.info;

        toast.innerHTML = `
            <div class="ui-toast-content">
                <span class="ui-toast-icon">${icon}</span>
                <span class="ui-toast-message">${message}</span>
                <button class="ui-toast-close" aria-label="Close">×</button>
            </div>
        `;

        this.injectToastStyles();
        document.body.appendChild(toast);

        requestAnimationFrame(() => toast.classList.add('ui-toast-show'));

        const closeBtn = toast.querySelector('.ui-toast-close');
        closeBtn.addEventListener('click', () => this.closeToast(toast));

        if (duration > 0) {
            setTimeout(() => this.closeToast(toast), duration);
        }

        return toast;
    },

    closeToast(toast) {
        toast.classList.remove('ui-toast-show');
        toast.classList.add('ui-toast-hide');
        setTimeout(() => toast.remove(), 300);
    },

    injectToastStyles() {
        if (document.getElementById('ui-toast-styles')) return;
        const style = document.createElement('style');
        style.id = 'ui-toast-styles';
        style.textContent = `
            .ui-toast { position: fixed; top: 20px; right: 20px; min-width: 300px; max-width: 500px;
                background: #ffffff !important; border: 1px solid #e5e7eb; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                z-index: 10000; opacity: 0; transform: translateX(100%); transition: all 0.3s ease; }
            .ui-toast-show { opacity: 1; transform: translateX(0); }
            .ui-toast-hide { opacity: 0; transform: translateX(100%); }
            .ui-toast-content { display: flex; align-items: center; padding: 16px; gap: 12px; }
            .ui-toast-icon { font-size: 20px; font-weight: bold; }
            .ui-toast-success .ui-toast-icon { color: #10b981; }
            .ui-toast-error .ui-toast-icon { color: #ef4444; }
            .ui-toast-warning .ui-toast-icon { color: #f59e0b; }
            .ui-toast-info .ui-toast-icon { color: #3b82f6; }
            .ui-toast-message { flex: 1; color: #000000 !important; font-size: 14px; font-weight: 500; line-height: 1.5; }
            .ui-toast-close { background: none; border: none; font-size: 24px; color: #000000 !important;
                cursor: pointer; padding: 0; width: 24px; height: 24px; display: flex; align-items: center;
                justify-content: center; line-height: 1; opacity: 0.7; }
            .ui-toast-close:hover { color: #000000 !important; opacity: 1; }
        `;
        document.head.appendChild(style);
    },

    /**
     * Show modal
     */
    modal(options = {}) {
        const { title = '', content = '', footer = '', onClose = null, closable = true, size = 'medium' } = options;
        const existing = document.querySelector('.ui-modal-overlay');
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.className = 'ui-modal-overlay';
        overlay.setAttribute('role', 'dialog');
        overlay.setAttribute('aria-modal', 'true');
        overlay.setAttribute('aria-labelledby', 'ui-modal-title');

        const modal = document.createElement('div');
        modal.className = `ui-modal ui-modal-${size}`;
        modal.innerHTML = `
            ${closable ? '<button class="ui-modal-close" aria-label="Close modal">×</button>' : ''}
            ${title ? `<h2 id="ui-modal-title" class="ui-modal-title">${title}</h2>` : ''}
            <div class="ui-modal-content">${content}</div>
            ${footer ? `<div class="ui-modal-footer">${footer}</div>` : ''}
        `;

        overlay.appendChild(modal);
        document.body.appendChild(overlay);
        this.injectModalStyles();

        requestAnimationFrame(() => overlay.classList.add('ui-modal-show'));

        const closeModal = () => {
            overlay.classList.remove('ui-modal-show');
            overlay.classList.add('ui-modal-hide');
            setTimeout(() => {
                overlay.remove();
                if (onClose) onClose();
            }, 300);
        };

        const closeBtn = modal.querySelector('.ui-modal-close');
        if (closeBtn) closeBtn.addEventListener('click', closeModal);

        overlay.addEventListener('click', (e) => {
            if (e.target === overlay && closable) closeModal();
        });

        const escHandler = (e) => {
            if (e.key === 'Escape' && closable) {
                closeModal();
                document.removeEventListener('keydown', escHandler);
            }
        };
        document.addEventListener('keydown', escHandler);
        this.trapFocus(modal);

        return {
            close: closeModal,
            updateContent: (html) => { modal.querySelector('.ui-modal-content').innerHTML = html; },
            updateFooter: (html) => {
                const footerEl = modal.querySelector('.ui-modal-footer');
                if (footerEl) footerEl.innerHTML = html;
            }
        };
    },

    injectModalStyles() {
        if (document.getElementById('ui-modal-styles')) return;
        const style = document.createElement('style');
        style.id = 'ui-modal-styles';
        style.textContent = `
            .ui-modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5);
                display: flex; align-items: center; justify-content: center; z-index: 10000; opacity: 0; transition: opacity 0.3s; }
            .ui-modal-overlay.ui-modal-show { opacity: 1; }
            .ui-modal-overlay.ui-modal-hide { opacity: 0; }
            .ui-modal { background: var(--bg-primary, #fff); border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
                max-height: 90vh; overflow: hidden; display: flex; flex-direction: column; transform: scale(0.95); transition: transform 0.3s; }
            .ui-modal-overlay.ui-modal-show .ui-modal { transform: scale(1); }
            .ui-modal-small { width: 90%; max-width: 400px; }
            .ui-modal-medium { width: 90%; max-width: 600px; }
            .ui-modal-large { width: 90%; max-width: 900px; }
            .ui-modal-close { position: absolute; top: 16px; right: 16px; background: none; border: none; font-size: 28px;
                color: var(--text-secondary, #6b7280); cursor: pointer; width: 32px; height: 32px; display: flex;
                align-items: center; justify-content: center; border-radius: 4px; z-index: 1; }
            .ui-modal-close:hover { background: var(--bg-secondary, #f3f4f6); color: var(--text-primary, #1f2937); }
            .ui-modal-title { margin: 0; padding: 24px 24px 16px; font-size: 24px; font-weight: 600; color: var(--text-primary, #1f2937); }
            .ui-modal-content { padding: 0 24px 24px; overflow-y: auto; flex: 1; }
            .ui-modal-footer { padding: 16px 24px; border-top: 1px solid var(--border-color, #e5e7eb);
                display: flex; gap: 12px; justify-content: flex-end; }
        `;
        document.head.appendChild(style);
    },

    /**
     * Show drawer (slide-in panel)
     */
    drawer(options = {}) {
        const { title = '', content = '', position = 'right', onClose = null, closable = true } = options;
        const existing = document.querySelector('.ui-drawer-overlay');
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.className = 'ui-drawer-overlay';
        const drawer = document.createElement('div');
        drawer.className = `ui-drawer ui-drawer-${position}`;
        drawer.innerHTML = `
            ${title ? `<div class="ui-drawer-header">
                <h3 class="ui-drawer-title">${title}</h3>
                ${closable ? '<button class="ui-drawer-close" aria-label="Close drawer">×</button>' : ''}
            </div>` : ''}
            <div class="ui-drawer-content">${content}</div>
        `;

        overlay.appendChild(drawer);
        document.body.appendChild(overlay);
        this.injectDrawerStyles();

        requestAnimationFrame(() => overlay.classList.add('ui-drawer-show'));

        const closeDrawer = () => {
            overlay.classList.remove('ui-drawer-show');
            overlay.classList.add('ui-drawer-hide');
            setTimeout(() => {
                overlay.remove();
                if (onClose) onClose();
            }, 300);
        };

        const closeBtn = drawer.querySelector('.ui-drawer-close');
        if (closeBtn) closeBtn.addEventListener('click', closeDrawer);

        overlay.addEventListener('click', (e) => {
            if (e.target === overlay && closable) closeDrawer();
        });

        const escHandler = (e) => {
            if (e.key === 'Escape' && closable) {
                closeDrawer();
                document.removeEventListener('keydown', escHandler);
            }
        };
        document.addEventListener('keydown', escHandler);

        return {
            close: closeDrawer,
            updateContent: (html) => { drawer.querySelector('.ui-drawer-content').innerHTML = html; }
        };
    },

    injectDrawerStyles() {
        if (document.getElementById('ui-drawer-styles')) return;
        const style = document.createElement('style');
        style.id = 'ui-drawer-styles';
        style.textContent = `
            .ui-drawer-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5);
                z-index: 10000; opacity: 0; transition: opacity 0.3s; }
            .ui-drawer-overlay.ui-drawer-show { opacity: 1; }
            .ui-drawer-overlay.ui-drawer-hide { opacity: 0; }
            .ui-drawer { position: fixed; background: var(--bg-primary, #fff); box-shadow: -2px 0 8px rgba(0,0,0,0.1);
                display: flex; flex-direction: column; max-height: 100vh; overflow: hidden; }
            .ui-drawer-right { top: 0; right: 0; width: 90%; max-width: 400px; height: 100%;
                transform: translateX(100%); transition: transform 0.3s; }
            .ui-drawer-left { top: 0; left: 0; width: 90%; max-width: 400px; height: 100%;
                transform: translateX(-100%); transition: transform 0.3s; }
            .ui-drawer-bottom { bottom: 0; left: 0; right: 0; width: 100%; max-height: 80vh;
                border-radius: 12px 12px 0 0; transform: translateY(100%); transition: transform 0.3s; }
            .ui-drawer-overlay.ui-drawer-show .ui-drawer-right,
            .ui-drawer-overlay.ui-drawer-show .ui-drawer-left { transform: translateX(0); }
            .ui-drawer-overlay.ui-drawer-show .ui-drawer-bottom { transform: translateY(0); }
            .ui-drawer-header { padding: 20px; border-bottom: 1px solid var(--border-color, #e5e7eb);
                display: flex; align-items: center; justify-content: space-between; }
            .ui-drawer-title { margin: 0; font-size: 20px; font-weight: 600; color: var(--text-primary, #1f2937); }
            .ui-drawer-close { background: none; border: none; font-size: 28px; color: var(--text-secondary, #6b7280);
                cursor: pointer; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
                border-radius: 4px; }
            .ui-drawer-close:hover { background: var(--bg-secondary, #f3f4f6); color: var(--text-primary, #1f2937); }
            .ui-drawer-content { flex: 1; overflow-y: auto; padding: 20px; }
        `;
        document.head.appendChild(style);
    },

    trapFocus(element) {
        const focusableElements = element.querySelectorAll(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        const handleTab = (e) => {
            if (e.key !== 'Tab') return;
            if (e.shiftKey) {
                if (document.activeElement === firstElement) {
                    e.preventDefault();
                    lastElement.focus();
                }
            } else {
                if (document.activeElement === lastElement) {
                    e.preventDefault();
                    firstElement.focus();
                }
            }
        };

        element.addEventListener('keydown', handleTab);
        if (firstElement) firstElement.focus();
    },

    loading(element, show = true) {
        if (!element) return;
        if (show) {
            element.style.position = 'relative';
            element.style.pointerEvents = 'none';
            element.style.opacity = '0.6';
            const spinner = document.createElement('div');
            spinner.className = 'ui-loading-spinner';
            spinner.innerHTML = '<div class="ui-spinner"></div>';
            element.appendChild(spinner);
        } else {
            element.style.pointerEvents = '';
            element.style.opacity = '1';
            const spinner = element.querySelector('.ui-loading-spinner');
            if (spinner) spinner.remove();
        }
    }
};

if (!document.getElementById('ui-loading-styles')) {
    const style = document.createElement('style');
    style.id = 'ui-loading-styles';
    style.textContent = `
        .ui-loading-spinner { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 10; }
        .ui-spinner { width: 40px; height: 40px; border: 4px solid rgba(0,0,0,0.1);
            border-top-color: var(--primary-blue, #3b82f6); border-radius: 50%; animation: ui-spin 1s linear infinite; }
        @keyframes ui-spin { to { transform: rotate(360deg); } }
    `;
    document.head.appendChild(style);
}

// Global wrapper functions for backward compatibility
function showToast(message, type = 'info', duration = 3000) {
    return UI.toast(message, type, duration);
}

function showLoading(element) {
    return UI.loading(element, true);
}

function hideLoading(element) {
    return UI.loading(element, false);
}

function showModal(content, title = '', footer = '') {
    return UI.modal({ content, title, footer });
}

function hideModal() {
    return UI.closeModal();
}

function showDrawer(content, className = '') {
    return UI.drawer({ content, className });
}

function hideDrawer() {
    return UI.closeDrawer();
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = UI;
}

