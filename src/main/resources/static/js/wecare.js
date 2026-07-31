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
