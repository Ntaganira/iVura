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

    // Confirm deletes
    const confirmDeleteMsg = (window.ivuraMessages && window.ivuraMessages.confirmDelete)
        || 'Are you sure you want to delete this item?';
    document.querySelectorAll('.confirm-delete').forEach(link => {
        link.addEventListener('click', function (e) {
            if (!confirm(confirmDeleteMsg)) {
                e.preventDefault();
            }
        });
    });

    // Confirm actions with a custom message (data-confirm)
    document.querySelectorAll('.confirm-action').forEach(link => {
        link.addEventListener('click', function (e) {
            const msg = this.getAttribute('data-confirm') || confirmDeleteMsg;
            if (!confirm(msg)) {
                e.preventDefault();
            }
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
