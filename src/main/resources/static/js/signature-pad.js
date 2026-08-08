/* Signature pad for digital consent */
window.ivuraSignaturePad = (function () {
    function init(canvasId, hiddenId, clearId) {
        var canvas = document.getElementById(canvasId);
        var hidden = document.getElementById(hiddenId);
        if (!canvas || !hidden) return;
        var ctx = canvas.getContext('2d');
        var drawing = false;
        var lastX = 0, lastY = 0;
        var existing = hidden.value;
        if (existing) {
            var img = new Image();
            img.onload = function () {
                ctx.fillStyle = '#ffffff';
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
            };
            img.src = existing;
        } else {
            ctx.fillStyle = '#ffffff';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.strokeStyle = '#1e293b';
            ctx.lineWidth = 2;
            ctx.lineCap = 'round';
            ctx.lineJoin = 'round';
        }

        function pos(e) {
            var rect = canvas.getBoundingClientRect();
            return {
                x: (e.clientX - rect.left) * (canvas.width / rect.width),
                y: (e.clientY - rect.top) * (canvas.height / rect.height)
            };
        }

        canvas.addEventListener('mousedown', function (e) {
            drawing = true;
            var p = pos(e);
            lastX = p.x; lastY = p.y;
        });
        canvas.addEventListener('mousemove', function (e) {
            if (!drawing) return;
            var p = pos(e);
            ctx.beginPath();
            ctx.moveTo(lastX, lastY);
            ctx.lineTo(p.x, p.y);
            ctx.stroke();
            lastX = p.x; lastY = p.y;
        });
        window.addEventListener('mouseup', function () { drawing = false; save(); });
        canvas.addEventListener('mouseleave', function () { drawing = false; save(); });

        canvas.addEventListener('touchstart', function (e) {
            e.preventDefault();
            drawing = true;
            var t = e.touches[0];
            var p = pos(t);
            lastX = p.x; lastY = p.y;
        });
        canvas.addEventListener('touchmove', function (e) {
            e.preventDefault();
            if (!drawing) return;
            var t = e.touches[0];
            var p = pos(t);
            ctx.beginPath();
            ctx.moveTo(lastX, lastY);
            ctx.lineTo(p.x, p.y);
            ctx.stroke();
            lastX = p.x; lastY = p.y;
        });
        canvas.addEventListener('touchend', function () { drawing = false; save(); });

        function isEmpty() {
            var data = ctx.getImageData(0, 0, canvas.width, canvas.height).data;
            for (var i = 3; i < data.length; i += 4) {
                if (data[i] !== 0) return false;
            }
            return true;
        }
        function save() {
            hidden.value = isEmpty() ? '' : canvas.toDataURL('image/png');
        }

        var clearBtn = document.getElementById(clearId);
        if (clearBtn) {
            clearBtn.addEventListener('click', function () {
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                ctx.fillStyle = '#ffffff';
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                ctx.strokeStyle = '#1e293b';
                ctx.lineWidth = 2;
                hidden.value = '';
            });
        }

        var form = canvas.closest('form');
        if (form) {
            form.addEventListener('submit', function () { save(); });
        }
    }

    return { init: init };
})();
