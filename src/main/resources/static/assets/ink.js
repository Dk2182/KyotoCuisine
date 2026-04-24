/* Kyoto Cuisine — shared JS: fountain-pen ink cursor trail + scroll reveal + nav behavior */

(function () {
    // Honor reduced motion preference
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const touchDevice = window.matchMedia('(pointer: coarse)').matches;

    // ==== Scroll reveal ====
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                const delay = entry.target.dataset.delay || 0;
                setTimeout(() => entry.target.classList.add('in'), delay);
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.15 });
    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('.reveal').forEach((el) => observer.observe(el));
    });

    // ==== Nav scroll state ====
    const nav = () => document.querySelector('.kc-nav');
    let lastScroll = -1;
    function updateNav() {
        const s = window.scrollY;
        if (s === lastScroll) return;
        lastScroll = s;
        const n = nav();
        if (!n || n.classList.contains('solid')) return;
        if (s > 80) n.classList.add('scrolled');
        else n.classList.remove('scrolled');
    }
    window.addEventListener('scroll', updateNav, { passive: true });
    document.addEventListener('DOMContentLoaded', updateNav);

    // ==== Fountain-pen ink cursor trail — DISABLED ====
    // Kept the canvas + scroll reveal code above; no mouse ink follow.
    return;
    // eslint-disable-next-line no-unreachable
    if (reduceMotion || touchDevice) return;

    document.addEventListener('DOMContentLoaded', () => {
        const canvas = document.getElementById('ink-canvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const dpr = window.devicePixelRatio || 1;

        function resize() {
            canvas.width = window.innerWidth * dpr;
            canvas.height = window.innerHeight * dpr;
            canvas.style.width = window.innerWidth + 'px';
            canvas.style.height = window.innerHeight + 'px';
            ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        }
        resize();
        window.addEventListener('resize', resize);

        function getInk() {
            const cs = getComputedStyle(document.documentElement);
            return (cs.getPropertyValue('--ink') || '#0f0e0c').trim();
        }

        const segments = [];
        let lastX = null, lastY = null, lastTime = 0, lastW = 3;

        function onMove(e) {
            const now = performance.now();
            const x = e.clientX, y = e.clientY;
            if (lastX !== null) {
                const dx = x - lastX, dy = y - lastY;
                const dist = Math.hypot(dx, dy);
                const dt = Math.max(1, now - lastTime);
                const speed = dist / dt;
                const targetW = Math.max(0.8, Math.min(5.5, 5.5 - speed * 1.4));
                const w = lastW * 0.55 + targetW * 0.45;
                if (dist > 0.5 && dist < 120) {
                    segments.push({
                        x1: lastX, y1: lastY, x2: x, y2: y,
                        w1: lastW, w2: w,
                        alpha: 0.92, born: now,
                    });
                }
                lastW = w;
            } else {
                lastW = 3;
            }
            lastX = x; lastY = y; lastTime = now;
        }
        function onLeave() { lastX = null; lastY = null; }

        window.addEventListener('mousemove', onMove, { passive: true });
        window.addEventListener('mouseleave', onLeave);

        function render() {
            ctx.clearRect(0, 0, window.innerWidth, window.innerHeight);
            const now = performance.now();
            const ink = getInk();

            for (let i = segments.length - 1; i >= 0; i--) {
                const s = segments[i];
                const age = now - s.born;
                const fade = age < 400 ? 1 : Math.max(0, 1 - (age - 400) / 3400);
                if (fade <= 0) { segments.splice(i, 1); continue; }
                const a = s.alpha * fade;
                const avgW = (s.w1 + s.w2) / 2;

                ctx.save();
                ctx.strokeStyle = ink;
                ctx.lineCap = 'round';
                ctx.lineJoin = 'round';
                ctx.globalAlpha = a;
                ctx.lineWidth = avgW;
                ctx.beginPath();
                ctx.moveTo(s.x1, s.y1);
                ctx.lineTo(s.x2, s.y2);
                ctx.stroke();

                if (fade > 0.3) {
                    ctx.globalAlpha = a * 0.12;
                    ctx.lineWidth = avgW + 2.5;
                    ctx.stroke();
                }
                ctx.restore();
            }

            // cap memory — never let segments grow unbounded
            if (segments.length > 300) segments.splice(0, segments.length - 300);

            requestAnimationFrame(render);
        }
        render();
    });
})();

// ==== Session helpers (shared by all pages) ====
window.kcSession = {
    get() { try { return JSON.parse(localStorage.getItem('kyoto_session') || 'null'); } catch { return null; } },
    clear() { localStorage.removeItem('kyoto_session'); },
    token() { const s = this.get(); return s ? s.token : null; }
};
window.kcCart = {
    get() { try { return JSON.parse(localStorage.getItem('kyoto_cart') || '[]'); } catch { return []; } },
    set(c) { localStorage.setItem('kyoto_cart', JSON.stringify(c)); },
    clear() { localStorage.removeItem('kyoto_cart'); },
    count() { return this.get().reduce((s, i) => s + i.quantity, 0); }
};

// ==== Nav builder — shared across pages ====
window.buildKyotoNav = function (activeLink) {
    const session = window.kcSession.get();
    const cartCount = window.kcCart.count();

    let links = [];
    // Landing sections (anchor links) when on /
    const onLanding = window.location.pathname === '/' || window.location.pathname === '/index.html';
    if (onLanding) {
        links.push(['Story', '#story']);
        links.push(['Menu', '#menu']);
        links.push(['Reserve', '/reservation.html']);
        links.push(['Visit', '#visit']);
    } else {
        links.push(['Home', '/']);
        links.push(['Menu', '/menuView.html']);
        links.push(['Reserve', '/reservation.html']);
        if (session && (session.role === 'STAFF' || session.role === 'ADMIN')) links.push(['Staff', '/staff.html']);
        if (session && session.role === 'ADMIN') links.push(['Admin', '/admin.html']);
        if (session) links.push(['Account', '/account.html']);
    }

    const linksHtml = links.map(([text, href]) => {
        const act = activeLink && activeLink.toLowerCase() === text.toLowerCase();
        const style = act ? 'style="color:var(--accent)"' : '';
        if (href.startsWith('#')) {
            return `<a href="${href}" onclick="event.preventDefault();document.querySelector('${href}')?.scrollIntoView({behavior:'smooth'})" ${style}>${text}</a>`;
        }
        return `<a href="${href}" ${style}>${text}</a>`;
    }).join('');

    const userChip = session
        ? `<button class="user-chip" onclick="if(confirm('Sign out?')){window.kcSession.clear();location.reload();}">${session.firstName || 'Account'}</button>`
        : `<a href="/login.html" class="user-chip">Sign in</a>`;

    const cartChip = `<a href="/menuView.html" class="kc-cart-btn" title="View cart">
        Cart<span class="cart-count">${cartCount}</span>
    </a>`;

    return `
    <nav class="kc-nav">
        <a href="/" class="kc-logo">
            <span class="kc-logo-mark">京</span>
            <span>Kyoto<em>Cuisine</em></span>
        </a>
        <div class="kc-nav-links">${linksHtml}</div>
        <div class="kc-nav-meta">
            ${cartChip}
            ${userChip}
            <span class="divider"></span>
            <span>Dubrovnik · HR</span>
        </div>
    </nav>
    `;
};
