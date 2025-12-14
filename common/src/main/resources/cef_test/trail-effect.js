// trail-effect.js
const TrailEffect = (() => {
    let canvas, ctx;
    let width, height;
    let trail = [];
    let maxTrail = 30;
    let color = '255,200,50'; // 默认尾巴颜色
    let size = 8; // 尾巴最大半径

    const init = (options = {}) => {
        // 可选参数覆盖默认值
        maxTrail = options.maxTrail || maxTrail;
        color = options.color || color;
        size = options.size || size;

        canvas = document.createElement('canvas');
        document.body.appendChild(canvas);
        canvas.style.position = 'fixed';
        canvas.style.top = '0';
        canvas.style.left = '0';
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        canvas.style.pointerEvents = 'none'; // 不阻塞鼠标事件
        canvas.style.zIndex = '9999';
        canvas.style.background = 'transparent'; // 透明背景
        ctx = canvas.getContext('2d');

        resizeCanvas();
        window.addEventListener('resize', resizeCanvas);

        document.addEventListener('mousemove', e => {
            trail.push({x: e.clientX, y: e.clientY});
            if(trail.length > maxTrail) trail.shift();
        });

        animate();
    };

    const resizeCanvas = () => {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    };

    const animate = () => {
        ctx.clearRect(0, 0, width, height);

        for(let i = 0; i < trail.length; i++){
            const t = trail[i];
            const alpha = i / trail.length;
            ctx.fillStyle = `rgba(${color},${alpha})`;
            ctx.beginPath();
            ctx.arc(t.x, t.y, size * alpha, 0, Math.PI*2);
            ctx.fill();
        }

        requestAnimationFrame(animate);
    };

    return { init };
})();
