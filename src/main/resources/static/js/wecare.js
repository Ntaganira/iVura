// WeCare Dashboard JS

document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transition = 'opacity .5s ease';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Dismissible error summary
    document.querySelectorAll('[data-error-close]').forEach(btn => {
        btn.addEventListener('click', function () {
            const box = this.closest('.error-summary');
            if (!box) return;
            box.classList.add('is-closing');
            setTimeout(() => box.remove(), 260);
        });
    });

    // Custom confirm dialog (replaces native confirm())
    const ivuraMsg = (key, fallback) =>
        (window.ivuraMessages && window.ivuraMessages[key]) || fallback;

    const confirmDialog = (function () {
        let overlay = null;
        let onConfirm = null;

        function ensure() {
            if (overlay) return overlay;
            overlay = document.createElement('div');
            overlay.className = 'modal-overlay';
            overlay.innerHTML =
                '<div class="modal confirm-modal" role="dialog" aria-modal="true">' +
                '  <div class="modal-header">' +
                '    <h3 class="confirm-title"></h3>' +
                '    <button type="button" class="modal-close confirm-cancel" aria-label="Close">&times;</button>' +
                '  </div>' +
                '  <div class="modal-body">' +
                '    <div class="confirm-icon">' +
                '      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
                '        <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path>' +
                '        <path d="M12 9v4"></path><path d="M12 17h.01"></path>' +
                '      </svg>' +
                '    </div>' +
                '    <p class="confirm-message"></p>' +
                '  </div>' +
                '  <div class="modal-footer">' +
                '    <button type="button" class="btn btn-outline confirm-cancel"></button>' +
                '    <button type="button" class="btn confirm-ok"></button>' +
                '  </div>' +
                '</div>';
            document.body.appendChild(overlay);

            const close = function () {
                overlay.classList.remove('open');
                onConfirm = null;
            };
            overlay.querySelectorAll('.confirm-cancel').forEach(btn => {
                btn.addEventListener('click', close);
            });
            overlay.querySelector('.confirm-ok').addEventListener('click', function () {
                const cb = onConfirm;
                close();
                if (cb) cb();
            });
            overlay.addEventListener('click', function (e) {
                if (e.target === overlay) close();
            });
            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape' && overlay.classList.contains('open')) close();
            });
            return overlay;
        }

        return {
            open: function (opts) {
                const el = ensure();
                el.querySelector('.confirm-title').textContent =
                    opts.title || ivuraMsg('confirmTitle', 'Are you sure?');
                el.querySelector('.confirm-message').textContent =
                    opts.message || ivuraMsg('confirmDelete', 'Are you sure?');
                const cancelBtns = el.querySelectorAll('.confirm-cancel');
                cancelBtns[cancelBtns.length - 1].textContent =
                    opts.cancelText || ivuraMsg('confirmCancel', 'Not now');
                const ok = el.querySelector('.confirm-ok');
                ok.textContent = opts.okText || ivuraMsg('confirmOk', 'Yes, Continue');
                ok.className = 'btn ' + (opts.danger ? 'btn-danger' : 'btn-primary');
                el.dataset.danger = opts.danger ? 'true' : 'false';
                onConfirm = opts.onConfirm;
                el.classList.add('open');
            },
            close: function () {
                if (overlay) overlay.classList.remove('open');
                onConfirm = null;
            }
        };
    })();

    // Deletes (plain confirm message)
    const confirmDeleteMsg = ivuraMsg('confirmDelete', 'Are you sure you want to delete this item?');
    document.querySelectorAll('.confirm-delete').forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            confirmDialog.open({
                message: confirmDeleteMsg,
                danger: true,
                onConfirm: function () { window.location.href = href; }
            });
        });
    });

    // Actions with a custom message (data-confirm)
    document.querySelectorAll('.confirm-action').forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const href = this.getAttribute('href');
            const msg = this.getAttribute('data-confirm') || confirmDeleteMsg;
            const danger = this.classList.contains('delete');
            confirmDialog.open({
                message: msg,
                danger: danger,
                onConfirm: function () { window.location.href = href; }
            });
        });
    });

    // Modals
    const modalOpen = function (id) {
        const overlay = document.getElementById(id);
        if (overlay) overlay.classList.add('open');
    };
    const modalClose = function (id) {
        const overlay = document.getElementById(id);
        if (overlay) overlay.classList.remove('open');
    };
    document.querySelectorAll('[data-modal-close]').forEach(btn => {
        btn.addEventListener('click', function () {
            modalClose(this.getAttribute('data-modal-close'));
        });
    });
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) overlay.classList.remove('open');
        });
    });

    // Reset password modal
    document.querySelectorAll('.open-reset-modal').forEach(btn => {
        btn.addEventListener('click', function () {
            const id = this.getAttribute('data-id');
            const name = this.getAttribute('data-name');
            const nameEl = document.getElementById('reset-user-name');
            const form = document.getElementById('reset-form');
            const pw = document.getElementById('reset-password');
            if (nameEl) nameEl.textContent = name;
            if (form) form.action = '/users/reset-password/' + id;
            if (pw) pw.value = '';
            modalOpen('reset-modal');
        });
    });

    // Select-all checkboxes in permission/page trees
    document.querySelectorAll('.perm-select-all').forEach(selectAll => {
        const updateSelectAll = function () {
            const group = selectAll.closest('.perm-group');
            const checks = group ? group.querySelectorAll('.perm-check') : [];
            let checked = 0;
            checks.forEach(c => { if (c.checked) checked++; });
            selectAll.checked = checks.length > 0 && checked === checks.length;
            selectAll.indeterminate = checked > 0 && checked < checks.length;
        };
        selectAll.addEventListener('change', function () {
            const group = selectAll.closest('.perm-group');
            const checks = group ? group.querySelectorAll('.perm-check') : [];
            checks.forEach(c => { c.checked = selectAll.checked; });
        });
        const group = selectAll.closest('.perm-group');
        if (group) {
            group.querySelectorAll('.perm-check').forEach(c => {
                c.addEventListener('change', updateSelectAll);
            });
        }
        updateSelectAll();
    });

    // Tabs
    document.querySelectorAll('.tabs').forEach(tabs => {
        tabs.querySelectorAll('.tab').forEach(tab => {
            tab.addEventListener('click', function () {
                tabs.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                const target = document.getElementById(tab.getAttribute('data-tab'));
                const body = tab.closest('.section');
                if (body) body.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
                if (target) target.classList.add('active');
            });
        });
    });

    // Sidebar toggle
    const sidebarToggle = document.getElementById('sidebar-toggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function () {
            const collapsed = document.documentElement.classList.toggle('sidebar-collapsed');
            try { localStorage.setItem('ivura-sidebar', collapsed ? 'collapsed' : 'expanded'); } catch (e) {}
        });
    }

    // Theme toggle
    const themeToggle = document.getElementById('theme-toggle');
    if (themeToggle) {
        const themeLabels = (window.ivuraMessages && window.ivuraMessages.theme)
            || { light: 'Light Mode', dark: 'Dark Mode' };
        const syncThemeUI = function () {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            themeToggle.setAttribute('aria-label', isDark ? themeLabels.light : themeLabels.dark);
        };
        themeToggle.addEventListener('click', function () {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            const next = isDark ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('ivura-theme', next);
            syncThemeUI();
        });
        syncThemeUI();
    }

    // Live table search + status filter
    const toolbar = document.querySelector('.table-toolbar');
    const searchInput = toolbar ? toolbar.querySelector('.table-search') : null;
    const filterSelect = toolbar ? toolbar.querySelector('.toolbar-filter') : null;
    const table = toolbar ? toolbar.closest('.section').querySelector('table') : null;

    if (table) {
        const rows = Array.from(table.querySelectorAll('tbody tr'));
        const emptyState = rows.find(r => r.querySelector('.empty-state'));

        function applyFilters() {
            const term = (searchInput ? searchInput.value : '').toLowerCase().trim();
            const status = filterSelect ? filterSelect.value.toLowerCase() : '';
            let visible = 0;

            rows.forEach(row => {
                if (emptyState && row === emptyState) return;
                const text = row.textContent.toLowerCase();
                const rowStatus = row.dataset.status ? row.dataset.status.toLowerCase() : '';
                const matchTerm = !term || text.includes(term);
                const matchStatus = !status || rowStatus === status;
                const show = matchTerm && matchStatus;
                row.style.display = show ? '' : 'none';
                if (show) visible++;
            });

            if (emptyState) {
                emptyState.style.display = visible === 0 ? '' : 'none';
            }
        }

        if (searchInput) searchInput.addEventListener('input', applyFilters);
        if (filterSelect) filterSelect.addEventListener('change', applyFilters);
    }
});
