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
