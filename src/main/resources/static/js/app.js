/**
/**
 * Ahorrito Web SPA JavaScript Client
 * Handles SPA navigation, API requests, Chart.js updates, and UI logic.
 */

// --- STATE MANAGEMENT ---
const state = {
    categorias: [],
    carteras: [],
    transacciones: [],
    eventos: [],
    checklist: [],
    checklistNextMonth: [],
    perfil: null,
    activePanel: 'panel-dashboard',
    currentYear: new Date().getFullYear(),
    currentMonth: new Date().getMonth(), // 0-11
    selectedDateStr: new Date().toISOString().substring(0, 10), // 'YYYY-MM-DD'
    theme: 'classic-dark',
    charts: {
        categorias: null,
        mensual: null
    },
    activeDeleteTarget: null, // { type: 'cartera' | 'transaccion' | 'categoria' | 'evento' | 'gasto-fijo', id }
    lastCreatedCategoryId: null
};

// --- API ENDPOINTS ---
const API_CATEGORIAS = '/api/categorias';
const API_CARTERAS = '/api/carteras';
const API_TRANSACCIONES = '/api/transacciones';
const API_EVENTOS = '/api/eventos';
const API_PERFIL = '/api/perfil';
const API_CHECKLIST = '/api/checklist';

// Theme Presets configurations
const ACCENT_COLORS = {
    gold: { hex: '#dcb342', rgb: '220, 179, 66', light: '#f3d478', dark: '#b89128' },
    green: { hex: '#52c488', rgb: '82, 196, 136', light: '#7be3ab', dark: '#3aa66e' },
    blue: { hex: '#4fa4cf', rgb: '79, 164, 207', light: '#7ed0f5', dark: '#3782aa' },
    purple: { hex: '#a855f7', rgb: '168, 85, 247', light: '#c084fc', dark: '#7e22ce' },
    rose: { hex: '#ec4899', rgb: '236, 72, 153', light: '#f472b6', dark: '#be185d' }
};

// --- UTILITIES FOR CUSTOM COLORS AND TRANSPARENCIES ---
function hexToRgb(hex) {
    const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
    hex = hex.replace(shorthandRegex, (m, r, g, b) => r + r + g + g + b + b);
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : { r: 220, g: 179, b: 66 };
}

function adjustBrightness(hex, percent) {
    const rgb = hexToRgb(hex);
    let r = rgb.r;
    let g = rgb.g;
    let b = rgb.b;
    r = Math.max(0, Math.min(255, Math.round(r * (1 + percent))));
    g = Math.max(0, Math.min(255, Math.round(g * (1 + percent))));
    b = Math.max(0, Math.min(255, Math.round(b * (1 + percent))));
    const toHex = (c) => {
        const h = c.toString(16);
        return h.length === 1 ? '0' + h : h;
    };
    return `#${toHex(r)}${toHex(g)}${toHex(b)}`;
}

function getCustomAccentConfig(hex) {
    const rgbObj = hexToRgb(hex);
    const rgbStr = `${rgbObj.r}, ${rgbObj.g}, ${rgbObj.b}`;
    return {
        hex: hex,
        rgb: rgbStr,
        light: adjustBrightness(hex, 0.15),
        dark: adjustBrightness(hex, -0.15)
    };
}

function applyTransparency(enabled) {
    if (enabled === 'disabled' || enabled === false) {
        document.body.classList.add('no-transparency');
    } else {
        document.body.classList.remove('no-transparency');
    }
    const switchTrans = document.getElementById('switch-transparency');
    if (switchTrans) {
        switchTrans.checked = (enabled !== 'disabled' && enabled !== false);
    }
}

function applyAnimations(enabled) {
    if (enabled === 'disabled' || enabled === false) {
        document.body.classList.add('no-animations');
    } else {
        document.body.classList.remove('no-animations');
    }
    const switchAnim = document.getElementById('switch-animations');
    if (switchAnim) {
        switchAnim.checked = (enabled !== 'disabled' && enabled !== false);
    }
}

// Helper to get local date string YYYY-MM-DD
const getLocalDateString = (dateObj) => {
    const yyyy = dateObj.getFullYear();
    const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
    const dd = String(dateObj.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
};

// --- DOM ELEMENTS CACHE ---
const elements = {
    // Navigation
    navBtns: document.querySelectorAll('.nav-btn'),
    panels: document.querySelectorAll('.content-panel'),
    pageTitle: document.getElementById('page-title'),
    topbarDate: document.getElementById('topbar-date'),
    btnViewAllTransacciones: document.getElementById('btn-view-all-transacciones'),
    btnAddTransaccionShortcut: document.getElementById('btn-add-transaccion-shortcut'),

    // Dashboard Metrics
    metricBalance: document.getElementById('metric-balance'),
    metricAhorros: document.getElementById('metric-ahorros'),
    metricGastos: document.getElementById('metric-gastos'),
    metricGastosCount: document.getElementById('metric-gastos-count'),
    metricIngresos: document.getElementById('metric-ingresos'),
    metricIngresosCount: document.getElementById('metric-ingresos-count'),
    recentTransactionsList: document.getElementById('recent-transactions-list'),

    // Carteras Panel
    walletsContainer: document.getElementById('wallets-container'),
    goalsContainer: document.getElementById('goals-container'),
    btnNewCartera: document.getElementById('btn-new-cartera'),
    btnNewMeta: document.getElementById('btn-new-meta'),

    // Transacciones Panel
    filterSearch: document.getElementById('filter-search'),
    filterTipo: document.getElementById('filter-tipo'),
    filterCartera: document.getElementById('filter-cartera'),
    filterOrden: document.getElementById('filter-orden'),
    transaccionesTableBody: document.getElementById('transacciones-table-body'),
    transaccionesTableFooter: document.getElementById('transacciones-table-footer'),

    // Categorias Panel
    btnNewCategoria: document.getElementById('btn-new-categoria'),
    categoriesGrid: document.getElementById('categories-grid'),

    // Calendario Panel
    btnPrevMonth: document.getElementById('btn-prev-month'),
    btnNextMonth: document.getElementById('btn-next-month'),
    calendarMonthYear: document.getElementById('calendar-month-year'),
    calendarDaysBody: document.getElementById('calendar-days-body'),
    selectedDayTitle: document.getElementById('selected-day-title'),
    dayEventsList: document.getElementById('day-events-list'),
    formEvento: document.getElementById('form-evento'),
    eventoId: document.getElementById('evento-id'),
    eventoTitulo: document.getElementById('evento-titulo'),
    eventoTipo: document.getElementById('evento-tipo'),
    eventoFecha: document.getElementById('evento-fecha'),
    eventoDescripcion: document.getElementById('evento-descripcion'),
    btnSaveEvento: document.getElementById('btn-save-evento'),
    
    // Modal Evento
    modalEvento: document.getElementById('modal-evento'),
    modalEventoTitle: document.getElementById('modal-evento-title'),
    modalEventoClose: document.getElementById('modal-evento-close'),
    btnCancelEvento: document.getElementById('btn-cancel-evento'),
    btnNuevoEvento: document.getElementById('btn-nuevo-evento'),
    btnFabNuevoEvento: document.getElementById('btn-fab-nuevo-evento'),

    // Modals
    modalTransaccion: document.getElementById('modal-transaccion'),
    modalTransaccionTitle: document.getElementById('modal-transaccion-title'),
    modalTransaccionClose: document.getElementById('modal-transaccion-close'),
    formTransaccion: document.getElementById('form-transaccion'),
    transaccionId: document.getElementById('transaccion-id'),
    transaccionTipo: document.getElementById('transaccion-tipo'),
    transaccionMonto: document.getElementById('transaccion-monto'),
    transaccionDescripcion: document.getElementById('transaccion-descripcion'),
    transaccionFecha: document.getElementById('transaccion-fecha'),
    transaccionCartera: document.getElementById('transaccion-cartera'),
    transaccionCategoriaGroup: document.getElementById('transaccion-categoria-group'),
    transaccionCategoria: document.getElementById('transaccion-categoria'),
    btnCancelTransaccion: document.getElementById('btn-cancel-transaccion'),

    modalCartera: document.getElementById('modal-cartera'),
    modalCarteraTitle: document.getElementById('modal-cartera-title'),
    modalCarteraClose: document.getElementById('modal-cartera-close'),
    formCartera: document.getElementById('form-cartera'),
    carteraId: document.getElementById('cartera-id'),
    carteraEsObjetivo: document.getElementById('cartera-es-objetivo'),
    carteraNombre: document.getElementById('cartera-nombre'),
    carteraMontoActual: document.getElementById('cartera-monto-actual'),
    labelMontoActual: document.getElementById('label-monto-actual'),
    groupMontoObjetivo: document.getElementById('group-monto-objetivo'),
    carteraMontoObjetivo: document.getElementById('cartera-monto-objetivo'),
    carteraDescripcion: document.getElementById('cartera-descripcion'),
    btnCancelCartera: document.getElementById('btn-cancel-cartera'),

    modalCategoria: document.getElementById('modal-categoria'),
    modalCategoriaTitle: document.getElementById('modal-categoria-title'),
    modalCategoriaClose: document.getElementById('modal-categoria-close'),
    formCategoria: document.getElementById('form-categoria'),
    categoriaId: document.getElementById('categoria-id'),
    categoriaNombre: document.getElementById('categoria-nombre'),
    categoriaDescripcion: document.getElementById('categoria-descripcion'),
    categoriaLimite: document.getElementById('categoria-limite'),
    btnCancelCategoria: document.getElementById('btn-cancel-categoria'),

    modalConfirm: document.getElementById('modal-confirm'),
    modalConfirmClose: document.getElementById('modal-confirm-close'),
    btnConfirmCancel: document.getElementById('btn-confirm-cancel'),
    btnConfirmDelete: document.getElementById('btn-confirm-delete'),
    confirmMessage: document.getElementById('confirm-message'),

    toastContainer: document.getElementById('toast-container'),

    // Perfil
    userProfileContainer: document.getElementById('user-profile-container'),
    userAvatar: document.getElementById('user-avatar'),
    userNameDisplay: document.getElementById('user-name-display'),
    userRoleDisplay: document.getElementById('user-role-display'),
    modalPerfil: document.getElementById('modal-perfil'),
    modalPerfilClose: document.getElementById('modal-perfil-close'),
    formPerfil: document.getElementById('form-perfil'),
    perfilNombre: document.getElementById('perfil-nombre'),
    perfilRango: document.getElementById('perfil-rango'),
    btnCancelPerfil: document.getElementById('btn-cancel-perfil'),

    // Checklist Panel
    btnNewGastoChecklist: document.getElementById('btn-new-gasto-checklist'),
    checklistContainer: document.getElementById('checklist-container'),
    checklistMetricTotal: document.getElementById('checklist-metric-total'),
    checklistMetricPagado: document.getElementById('checklist-metric-pagado'),
    checklistMetricPagadoCount: document.getElementById('checklist-metric-pagado-count'),
    checklistMetricPendiente: document.getElementById('checklist-metric-pendiente'),
    checklistNextMonthLink: document.getElementById('checklist-next-month-link'),

    // Checklist Drawer
    drawerChecklistNextMonth: document.getElementById('drawer-checklist-next-month'),
    drawerChecklistClose: document.getElementById('drawer-checklist-close'),
    btnDrawerNewGasto: document.getElementById('btn-drawer-new-gasto'),
    drawerChecklistList: document.getElementById('drawer-checklist-list'),
    drawerChecklistTotal: document.getElementById('drawer-checklist-total'),

    // Modal Checklist
    modalGastoChecklist: document.getElementById('modal-gasto-checklist'),
    modalGastoChecklistTitle: document.getElementById('modal-gasto-checklist-title'),
    modalGastoChecklistClose: document.getElementById('modal-gasto-checklist-close'),
    formGastoChecklist: document.getElementById('form-gasto-checklist'),
    gastoChecklistId: document.getElementById('gasto-checklist-id'),
    gastoChecklistNombre: document.getElementById('gasto-checklist-nombre'),
    gastoChecklistMonto: document.getElementById('gasto-checklist-monto'),
    gastoChecklistFecha: document.getElementById('gasto-checklist-fecha'),
    gastoChecklistCartera: document.getElementById('gasto-checklist-cartera'),
    gastoChecklistCategoria: document.getElementById('gasto-checklist-categoria'),
    gastoChecklistDescripcion: document.getElementById('gasto-checklist-descripcion'),
    gastoChecklistPermanente: document.getElementById('gasto-checklist-permanente'),
    btnCancelGastoChecklist: document.getElementById('btn-cancel-gasto-checklist')
};

// Month Names in Spanish
const MONTH_NAMES = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
];

// --- INITIALIZATION ---
document.addEventListener('DOMContentLoaded', () => {
    // Set current date in topbar
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    elements.topbarDate.textContent = new Date().toLocaleDateString('es-ES', options);

    // Initial selected date
    state.selectedDateStr = getLocalDateString(new Date());

    // Apply saved theme configurations
    const savedTheme = localStorage.getItem('ahorrito-theme') || 'classic-dark';
    const savedTransparency = localStorage.getItem('ahorrito-theme-transparency') || 'enabled';
    const savedAnimations = localStorage.getItem('ahorrito-theme-animations') || 'enabled';
    applyTheme(savedTheme);
    applyTransparency(savedTransparency);
    applyAnimations(savedAnimations);

    // Apply saved sidebar state
    const sidebarCollapsed = localStorage.getItem('ahorrito-sidebar-collapsed') === 'true';
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggleBtn = document.getElementById('btn-sidebar-toggle');
    if (sidebarCollapsed && sidebar) {
        sidebar.classList.add('collapsed');
        if (sidebarToggleBtn) {
            sidebarToggleBtn.innerHTML = '<i data-lucide="chevron-right"></i>';
        }
    }

    // Initial load
    initApp();

    // Event Listeners setup
    setupEventListeners();
});

// Apply selected theme dynamically
function applyTheme(themeName) {
    const themeClasses = [
        'theme-classic-dark',
        'theme-classic-light',
        'theme-crema-beige',
        'theme-plum',
        'theme-emerald',
        'theme-sapphire',
        'theme-carbon-copper',
        'theme-midnight-aurora'
    ];

    // Clean inline override styles if present to prevent specificity issues
    const root = document.documentElement;
    root.style.removeProperty('--gold');
    root.style.removeProperty('--gold-rgb');
    root.style.removeProperty('--gold-light');
    root.style.removeProperty('--gold-dark');

    // Remove current classes and add new one
    themeClasses.forEach(cls => document.body.classList.remove(cls));
    document.body.classList.add(`theme-${themeName}`);

    // Handle general light-theme rules (classic-light and crema-beige are light)
    const isLightTheme = (themeName === 'classic-light' || themeName === 'crema-beige');
    if (isLightTheme) {
        document.body.classList.add('light-theme');
    } else {
        document.body.classList.remove('light-theme');
    }

    state.theme = themeName;

    // Update UI active state in Popover Menu
    document.querySelectorAll('.theme-btn').forEach(btn => {
        if (btn.getAttribute('data-theme') === themeName) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

async function saveThemeToBackend(themeName) {
    if (!state.perfil) return;
    const dto = {
        nombre: state.perfil.nombre,
        rango: state.perfil.rango,
        tema: themeName
    };
    try {
        const response = await apiFetch(API_PERFIL, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (response.ok) {
            state.perfil = await response.json();
        }
    } catch (error) {
        console.error('Error al sincronizar el tema con el servidor:', error);
    }
}

// Helper to get cookie value by name
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
}

// --- AUTHORIZED FETCH WRAPPER ---
async function apiFetch(url, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const safeMethods = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];
    if (!safeMethods.includes(method)) {
        const csrfToken = getCookie('XSRF-TOKEN');
        if (csrfToken) {
            options.headers = {
                ...options.headers,
                'X-XSRF-TOKEN': csrfToken
            };
        }
    }
    return fetch(url, options);
}

// --- LOAD DATA ---
async function initApp() {
    await fetchAllData();
    // Lucide icons render
    if (window.lucide) {
        window.lucide.createIcons();
    }
}

async function fetchAllData(domains = ['categorias', 'carteras', 'transacciones', 'eventos', 'perfil', 'checklist']) {
    const skeletonCards = document.querySelectorAll('.metric-card, .chart-card');
    skeletonCards.forEach(card => card.classList.add('loading-skeleton'));
    try {
        const today = new Date();
        const nextMonth = new Date(today.getFullYear(), today.getMonth() + 1, 1);
        const nextYearVal = nextMonth.getFullYear();
        const nextMonthVal = nextMonth.getMonth() + 1;

        const promises = [];
        const domainKeys = [];

        if (domains.includes('categorias')) {
            promises.push(apiFetch(API_CATEGORIAS).then(r => r.ok ? r.json() : null));
            domainKeys.push('categorias');
        }
        if (domains.includes('carteras')) {
            promises.push(apiFetch(API_CARTERAS).then(r => r.ok ? r.json() : null));
            domainKeys.push('carteras');
        }
        if (domains.includes('transacciones')) {
            promises.push(apiFetch(API_TRANSACCIONES).then(r => r.ok ? r.json() : null));
            domainKeys.push('transacciones');
        }
        if (domains.includes('eventos')) {
            promises.push(apiFetch(API_EVENTOS).then(r => r.ok ? r.json() : null));
            domainKeys.push('eventos');
        }
        if (domains.includes('perfil')) {
            promises.push(apiFetch(API_PERFIL).then(r => r.ok ? r.json() : null));
            domainKeys.push('perfil');
        }
        if (domains.includes('checklist')) {
            promises.push(apiFetch(`${API_CHECKLIST}?anio=${today.getFullYear()}&mes=${today.getMonth() + 1}`).then(r => r.ok ? r.json() : null));
            domainKeys.push('checklist');
            promises.push(apiFetch(`${API_CHECKLIST}?anio=${nextYearVal}&mes=${nextMonthVal}`).then(r => r.ok ? r.json() : null));
            domainKeys.push('checklistNextMonth');
        }

        const results = await Promise.all(promises);
        
        let hasError = false;
        for (let i = 0; i < domainKeys.length; i++) {
            const key = domainKeys[i];
            const val = results[i];
            if (val !== null) {
                state[key] = val;
            } else {
                hasError = true;
            }
        }

        if (hasError) {
            throw new Error('Error al cargar datos del servidor');
        }

        // Synchronize loaded profile theme
        if (domains.includes('perfil') && state.perfil && state.perfil.tema) {
            applyTheme(state.perfil.tema);
            localStorage.setItem('ahorrito-theme', state.perfil.tema);
        }

        updateFormDropdowns();
        renderAll();
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        skeletonCards.forEach(card => card.classList.remove('loading-skeleton'));
    }
}

// --- RENDER ROUTER ---
function renderAll() {
    renderDashboard();
    renderCarteras();
    renderTransacciones();
    renderCategorias();
    renderChecklist();
    renderCalendar();
    renderPerfil();
}

// --- TOAST NOTIFICATIONS ---
function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = type === 'success' ? 'check-circle' : 'alert-circle';
    
    toast.innerHTML = `
        <div class="toast-icon"><i data-lucide="${icon}"></i></div>
        <div class="toast-message">${message}</div>
    `;
    
    elements.toastContainer.appendChild(toast);
    
    if (window.lucide) {
        window.lucide.createIcons({ attrs: { class: 'toast-icon' } });
    }
    
    // Trigger animation
    setTimeout(() => toast.classList.add('active'), 50);
    
    // Auto-dismiss
    setTimeout(() => {
        toast.classList.remove('active');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// --- CURRENCY & DATE HELPERS ---
function formatCurrency(value) {
    return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' }).format(value);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const [year, month, day] = dateStr.split('-');
    return `${day}/${month}/${year}`;
}

function openQuickNewCategoriaModal() {
    elements.formCategoria.reset();
    elements.categoriaId.value = '';
    elements.modalCategoriaTitle.textContent = 'Nueva Categoría';
    
    // Default preset color selection
    document.getElementById('categoria-color').value = 'purple';
    highlightModalColorPicker('categoria-color-picker', 'purple');

    openModal(elements.modalCategoria);
}

// --- NAVIGATION & EVENT LISTENERS ---
function setupEventListeners() {
    // Navigation panels
    elements.navBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const target = btn.getAttribute('data-target');
            switchPanel(target);
        });
    });

    // Logo → volver al Dashboard
    const logoHomeBtn = document.getElementById('logo-home-btn');
    if (logoHomeBtn) {
        logoHomeBtn.addEventListener('click', (e) => {
            // No activar si el click fue en el botón de colapsar sidebar
            if (!e.target.closest('#btn-sidebar-toggle')) {
                switchPanel('panel-dashboard');
            }
        });
    }

    // View All link
    if (elements.btnViewAllTransacciones) {
        elements.btnViewAllTransacciones.addEventListener('click', () => {
            switchPanel('panel-transacciones');
        });
    }

    // Modal Triggers
    if (elements.btnAddTransaccionShortcut) {
        elements.btnAddTransaccionShortcut.addEventListener('click', () => {
            openNewTransaccionModal();
        });
    }

    if (elements.btnNewCartera) {
        elements.btnNewCartera.addEventListener('click', () => {
            openNewCarteraModal(false);
        });
    }

    if (elements.btnNewMeta) {
        elements.btnNewMeta.addEventListener('click', () => {
            openNewCarteraModal(true);
        });
    }

    if (elements.btnNewCategoria) {
        elements.btnNewCategoria.addEventListener('click', () => {
            openQuickNewCategoriaModal();
        });
    }

    // Quick category creation triggers
    const btnQuickCrearCategoria = document.getElementById('btn-quick-crear-categoria');
    if (btnQuickCrearCategoria) {
        btnQuickCrearCategoria.addEventListener('click', () => {
            openQuickNewCategoriaModal();
        });
    }

    if (elements.transaccionCategoria) {
        elements.transaccionCategoria.addEventListener('change', (e) => {
            if (e.target.value === 'CREATE_NEW_CATEGORY') {
                e.target.value = '';
                openQuickNewCategoriaModal();
            }
        });
    }

    // Modal Closing
    elements.modalTransaccionClose.addEventListener('click', () => closeModal(elements.modalTransaccion));
    elements.btnCancelTransaccion.addEventListener('click', () => closeModal(elements.modalTransaccion));

    elements.modalCarteraClose.addEventListener('click', () => closeModal(elements.modalCartera));
    elements.btnCancelCartera.addEventListener('click', () => closeModal(elements.modalCartera));

    elements.modalCategoriaClose.addEventListener('click', () => closeModal(elements.modalCategoria));
    elements.btnCancelCategoria.addEventListener('click', () => closeModal(elements.modalCategoria));

    elements.modalConfirmClose.addEventListener('click', () => closeModal(elements.modalConfirm));
    elements.btnConfirmCancel.addEventListener('click', () => closeModal(elements.modalConfirm));

    // Modal Evento
    elements.modalEventoClose.addEventListener('click', () => closeModal(elements.modalEvento));
    elements.btnCancelEvento.addEventListener('click', () => closeModal(elements.modalEvento));
    elements.btnNuevoEvento.addEventListener('click', () => openNewEventoModal());
    if (elements.btnFabNuevoEvento) {
        elements.btnFabNuevoEvento.addEventListener('click', () => openNewEventoModal());
    }

    // Modal Checklist
    if (elements.btnNewGastoChecklist) {
        elements.btnNewGastoChecklist.addEventListener('click', () => openNewGastoChecklistModal(false));
    }
    elements.modalGastoChecklistClose.addEventListener('click', () => closeModal(elements.modalGastoChecklist));
    elements.btnCancelGastoChecklist.addEventListener('click', () => closeModal(elements.modalGastoChecklist));

    // Drawer Checklist
    if (elements.checklistNextMonthLink) {
        elements.checklistNextMonthLink.addEventListener('click', () => openChecklistDrawer());
    }
    if (elements.drawerChecklistClose) {
        elements.drawerChecklistClose.addEventListener('click', () => closeChecklistDrawer());
    }
    if (elements.btnDrawerNewGasto) {
        elements.btnDrawerNewGasto.addEventListener('click', () => openNewGastoChecklistModal(true));
    }

    // Form Submissions
    elements.formTransaccion.addEventListener('submit', handleTransaccionSubmit);
    elements.formCartera.addEventListener('submit', handleCarteraSubmit);
    elements.formCategoria.addEventListener('submit', handleCategoriaSubmit);
    elements.formEvento.addEventListener('submit', handleEventoSubmit);
    if (elements.formGastoChecklist) {
        elements.formGastoChecklist.addEventListener('submit', handleGastoChecklistSubmit);
    }
    elements.btnConfirmDelete.addEventListener('click', executeDelete);

    // Filters event listeners
    elements.filterSearch.addEventListener('input', renderTransacciones);
    elements.filterTipo.addEventListener('change', renderTransacciones);
    elements.filterCartera.addEventListener('change', renderTransacciones);
    elements.filterOrden.addEventListener('change', renderTransacciones);

    // Transaction form type dynamic behaviors
    elements.transaccionTipo.addEventListener('change', (e) => {
        handleTransactionFormTypeChange(e.target.value);
    });

    // Calendar Month Navigation
    elements.btnPrevMonth.addEventListener('click', () => {
        state.currentMonth--;
        if (state.currentMonth < 0) {
            state.currentMonth = 11;
            state.currentYear--;
        }
        renderCalendar();
    });

    elements.btnNextMonth.addEventListener('click', () => {
        state.currentMonth++;
        if (state.currentMonth > 11) {
            state.currentMonth = 0;
            state.currentYear++;
        }
        renderCalendar();
    });

    // Theme customizer buttons binding
    document.querySelectorAll('.theme-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const themeName = btn.getAttribute('data-theme');
            applyTheme(themeName);
            localStorage.setItem('ahorrito-theme', themeName);
            saveThemeToBackend(themeName);
            renderDashboardCharts();
        });
    });

    // Transparency Switch premium listener
    const switchTransparency = document.getElementById('switch-transparency');
    if (switchTransparency) {
        switchTransparency.addEventListener('change', () => {
            const enabled = switchTransparency.checked ? 'enabled' : 'disabled';
            applyTransparency(enabled);
            localStorage.setItem('ahorrito-theme-transparency', enabled);
        });
    }

    // Animations Switch premium listener
    const switchAnimations = document.getElementById('switch-animations');
    if (switchAnimations) {
        switchAnimations.addEventListener('change', () => {
            const enabled = switchAnimations.checked ? 'enabled' : 'disabled';
            applyAnimations(enabled);
            localStorage.setItem('ahorrito-theme-animations', enabled);
        });
    }

    // Modals internal preset color picker click handler bindings
    const cartColorPicker = document.getElementById('cartera-color-picker');
    const cartCustomPicker = document.getElementById('cartera-custom-color');
    const cartCustomHex = document.getElementById('cartera-custom-hex');
    const cartCustomWrapper = document.getElementById('cartera-custom-color-wrapper');
    const cartColorHidden = document.getElementById('cartera-color');
    
    if (cartColorPicker) {
        cartColorPicker.querySelectorAll('.color-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                cartColorPicker.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
                if (cartCustomWrapper) cartCustomWrapper.classList.remove('active');
                if (cartCustomHex) cartCustomHex.value = '';
                btn.classList.add('active');
                if (cartColorHidden) cartColorHidden.value = btn.getAttribute('data-color');
            });
        });
    }

    if (cartCustomPicker && cartCustomHex) {
        cartCustomPicker.addEventListener('input', (e) => {
            const val = e.target.value;
            cartCustomHex.value = val.toUpperCase();
            if (cartColorPicker) {
                cartColorPicker.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
            }
            if (cartCustomWrapper) cartCustomWrapper.classList.add('active');
            if (cartColorHidden) cartColorHidden.value = val;
        });
        cartCustomHex.addEventListener('input', (e) => {
            let val = e.target.value.trim();
            if (val && !val.startsWith('#')) {
                val = '#' + val;
            }
            if (/^#[0-9A-F]{6}$/i.test(val)) {
                cartCustomPicker.value = val;
                if (cartColorPicker) {
                    cartColorPicker.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
                }
                if (cartCustomWrapper) cartCustomWrapper.classList.add('active');
                if (cartColorHidden) cartColorHidden.value = val;
            }
        });
    }

    const catColorPicker = document.getElementById('categoria-color-picker');
    const catCustomPicker = document.getElementById('categoria-custom-color');
    const catCustomHex = document.getElementById('categoria-custom-hex');
    const catCustomWrapper = document.getElementById('categoria-custom-color-wrapper');
    const catColorHidden = document.getElementById('categoria-color');
    
    if (catColorPicker) {
        catColorPicker.querySelectorAll('.color-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                catColorPicker.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
                if (catCustomWrapper) catCustomWrapper.classList.remove('active');
                if (catCustomHex) catCustomHex.value = '';
                btn.classList.add('active');
                if (catColorHidden) catColorHidden.value = btn.getAttribute('data-color');
            });
        });
    }

    if (catCustomPicker && catCustomHex) {
        catCustomPicker.addEventListener('input', (e) => {
            const val = e.target.value;
            catCustomHex.value = val.toUpperCase();
            if (catColorPicker) {
                catColorPicker.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
            }
            if (catCustomWrapper) catCustomWrapper.classList.add('active');
            if (catColorHidden) catColorHidden.value = val;
        });
        catCustomHex.addEventListener('input', (e) => {
            let val = e.target.value.trim();
            if (val && !val.startsWith('#')) {
                val = '#' + val;
            }
            if (/^#[0-9A-F]{6}$/i.test(val)) {
                catCustomPicker.value = val;
                if (catColorPicker) {
                    catColorPicker.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
                }
                if (catCustomWrapper) catCustomWrapper.classList.add('active');
                if (catColorHidden) catColorHidden.value = val;
            }
        });
    }

    // Sidebar collapse toggle behavior
    const btnSidebarToggle = document.getElementById('btn-sidebar-toggle');
    const sidebarEl = document.querySelector('.sidebar');
    if (btnSidebarToggle && sidebarEl) {
        btnSidebarToggle.addEventListener('click', () => {
            const isCollapsed = sidebarEl.classList.toggle('collapsed');
            localStorage.setItem('ahorrito-sidebar-collapsed', isCollapsed);
            
            // Toggle icon
            btnSidebarToggle.innerHTML = isCollapsed 
                ? '<i data-lucide="chevron-right"></i>' 
                : '<i data-lucide="chevron-left"></i>';
            
            if (window.lucide) {
                window.lucide.createIcons();
            }
        });
    }

    // Theme menu popover toggler
    const btnToggleThemeMenu = document.getElementById('btn-toggle-theme-menu');
    const themePopoverMenu = document.getElementById('theme-popover-menu');
    if (btnToggleThemeMenu && themePopoverMenu) {
        btnToggleThemeMenu.addEventListener('click', (e) => {
            e.stopPropagation();
            themePopoverMenu.classList.toggle('show');
        });

        // Close when clicking outside of the popover container
        document.addEventListener('click', (e) => {
            if (!themePopoverMenu.contains(e.target) && e.target !== btnToggleThemeMenu && !btnToggleThemeMenu.contains(e.target)) {
                themePopoverMenu.classList.remove('show');
            }
        });
    }

    // Perfil Modal events
    if (elements.userProfileContainer) {
        elements.userProfileContainer.addEventListener('click', () => {
            openPerfilModal();
        });
    }
    if (elements.modalPerfilClose) {
        elements.modalPerfilClose.addEventListener('click', () => closeModal(elements.modalPerfil));
    }
    if (elements.btnCancelPerfil) {
        elements.btnCancelPerfil.addEventListener('click', () => closeModal(elements.modalPerfil));
    }
    if (elements.formPerfil) {
        elements.formPerfil.addEventListener('submit', handlePerfilSubmit);
    }

}

function switchPanel(panelId) {
    elements.panels.forEach(panel => panel.classList.remove('active'));
    elements.navBtns.forEach(btn => btn.classList.remove('active'));

    const activePanel = document.getElementById(panelId);
    if (activePanel) activePanel.classList.add('active');

    const activeBtn = document.querySelector(`.nav-btn[data-target="${panelId}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    // Update topbar title
    let title = 'Dashboard';
    if (panelId === 'panel-carteras') title = 'Carteras y Metas';
    if (panelId === 'panel-transacciones') title = 'Transacciones';
    if (panelId === 'panel-categorias') title = 'Categorías';
    if (panelId === 'panel-checklist') title = 'Checklist';
    if (panelId === 'panel-calendario') title = 'Calendario';
    elements.pageTitle.textContent = title;

    state.activePanel = panelId;
}

function openModal(modal) {
    modal.classList.add('active');
}

function closeModal(modal) {
    modal.classList.remove('active');
    // Enable delete button on confirm modal just in case it was disabled
    if (modal === elements.modalConfirm) {
        elements.btnConfirmDelete.removeAttribute('disabled');
    }
}

// Utility to change highlighted button on preset color pickers
function highlightModalColorPicker(pickerId, colorName) {
    const picker = document.getElementById(pickerId);
    if (!picker) return;
    const isCustom = colorName && colorName.startsWith('#');
    
    picker.querySelectorAll('.color-btn').forEach(btn => {
        if (!isCustom && btn.getAttribute('data-color') === colorName) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });

    const customWrapper = picker.querySelector('.custom-color-input-wrapper');
    const customHex = picker.querySelector('.custom-color-hex-input');
    const customPicker = picker.querySelector('input[type="color"]');

    if (isCustom) {
        if (customWrapper) customWrapper.classList.add('active');
        if (customHex) customHex.value = colorName.toUpperCase();
        if (customPicker) customPicker.value = colorName;
    } else {
        if (customWrapper) customWrapper.classList.remove('active');
        if (customHex) customHex.value = '';
    }
}

// --- FORM DROPDOWNS POPULATION ---
function updateFormDropdowns() {
    // 1. Transaction form - Category selection dropdown
    const catSelect = elements.transaccionCategoria;
    const prevCatVal = catSelect.value;
    catSelect.innerHTML = '<option value="">Seleccione categoría...</option>';
    state.categorias.forEach(cat => {
        const opt = document.createElement('option');
        opt.value = cat.id;
        opt.textContent = cat.nombre;
        catSelect.appendChild(opt);
    });

    // Add quick option at the end
    const quickOpt = document.createElement('option');
    quickOpt.value = 'CREATE_NEW_CATEGORY';
    quickOpt.textContent = '+ Crear nueva categoría...';
    quickOpt.style.color = 'var(--gold)';
    quickOpt.style.fontWeight = 'bold';
    catSelect.appendChild(quickOpt);

    if (state.lastCreatedCategoryId) {
        catSelect.value = state.lastCreatedCategoryId;
        state.lastCreatedCategoryId = null;
    } else if (prevCatVal && prevCatVal !== 'CREATE_NEW_CATEGORY') {
        catSelect.value = prevCatVal;
    }

    // 2. Transacciones Filter - Account/Goal dropdown
    const filterCartSelect = elements.filterCartera;
    const prevFilterVal = filterCartSelect.value;
    filterCartSelect.innerHTML = '<option value="">Todas las cuentas</option>';
    state.carteras.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = c.esObjetivoAhorro ? `[Meta] ${c.nombre}` : `[Cuenta] ${c.nombre}`;
        filterCartSelect.appendChild(opt);
    });
    filterCartSelect.value = prevFilterVal;

    // 3. Checklist form - Wallet selection dropdown
    const chkCartSelect = elements.gastoChecklistCartera;
    if (chkCartSelect) {
        const prevChkCartVal = chkCartSelect.value;
        chkCartSelect.innerHTML = '<option value="">Seleccione cuenta...</option>';
        state.carteras.filter(c => !c.esObjetivoAhorro).forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;
            opt.textContent = `${c.nombre} (Saldo: ${formatCurrency(c.montoActual)})`;
            chkCartSelect.appendChild(opt);
        });
        chkCartSelect.value = prevChkCartVal;
    }

    // 4. Checklist form - Category selection dropdown
    const chkCatSelect = elements.gastoChecklistCategoria;
    if (chkCatSelect) {
        const prevChkCatVal = chkCatSelect.value;
        chkCatSelect.innerHTML = '<option value="">Seleccione categoría...</option>';
        state.categorias.forEach(cat => {
            const opt = document.createElement('option');
            opt.value = cat.id;
            opt.textContent = cat.nombre;
            chkCatSelect.appendChild(opt);
        });
        chkCatSelect.value = prevChkCatVal;
    }
}

// Dynamic wallet items in transaction form based on transaction type selection
function handleTransactionFormTypeChange(type, selectedWalletId = null) {
    const isGoal = (type === 'AHORRO');
    
    // Show/hide category dropdown group (Only needed for GASTO)
    if (type === 'GASTO') {
        elements.transaccionCategoriaGroup.style.display = 'block';
        elements.transaccionCategoria.setAttribute('required', 'required');
    } else {
        elements.transaccionCategoriaGroup.style.display = 'none';
        elements.transaccionCategoria.removeAttribute('required');
        elements.transaccionCategoria.value = '';
    }

    // Populate Affected Account dropdown
    const select = elements.transaccionCartera;
    select.innerHTML = '<option value="">Seleccione cuenta...</option>';
    
    const filtered = state.carteras.filter(c => c.esObjetivoAhorro === isGoal);
    filtered.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = isGoal
            ? `${c.nombre} (Objetivo: ${formatCurrency(c.montoObjetivo)})`
            : `${c.nombre} (Saldo: ${formatCurrency(c.montoActual)})`;
        select.appendChild(opt);
    });

    if (selectedWalletId) {
        select.value = selectedWalletId;
    }
}

// --- MODALS OPEN HELPERS ---
function openNewTransaccionModal() {
    elements.formTransaccion.reset();
    elements.transaccionId.value = '';
    elements.modalTransaccionTitle.textContent = 'Nueva Transacción';
    
    // Defaults
    elements.transaccionFecha.value = getLocalDateString(new Date());
    elements.transaccionTipo.value = 'GASTO';
    
    handleTransactionFormTypeChange('GASTO');
    openModal(elements.modalTransaccion);
}

function openNewCarteraModal(esObjetivo = false) {
    elements.formCartera.reset();
    elements.carteraId.value = '';
    elements.carteraEsObjetivo.value = esObjetivo ? 'true' : 'false';

    // Defaults color preset to gold
    document.getElementById('cartera-color').value = 'gold';
    highlightModalColorPicker('cartera-color-picker', 'gold');

    if (esObjetivo) {
        elements.modalCarteraTitle.textContent = 'Nuevo Ahorro / Meta';
        elements.labelMontoActual.textContent = 'Ahorro Inicial ($)';
        elements.groupMontoObjetivo.style.display = 'block';
        elements.carteraMontoObjetivo.setAttribute('required', 'required');
    } else {
        elements.modalCarteraTitle.textContent = 'Nueva Cartera / Cuenta';
        elements.labelMontoActual.textContent = 'Saldo Inicial ($)';
        elements.groupMontoObjetivo.style.display = 'none';
        elements.carteraMontoObjetivo.removeAttribute('required');
    }
    
    openModal(elements.modalCartera);
}

// --- USER PROFILE RENDER ---
function renderPerfil() {
    if (state.perfil) {
        if (elements.userNameDisplay) elements.userNameDisplay.textContent = state.perfil.nombre || 'Gino';
        if (elements.userRoleDisplay) elements.userRoleDisplay.textContent = state.perfil.rango || 'Miembro Premium';
        if (elements.userAvatar) {
            const firstLetter = (state.perfil.nombre || 'G').charAt(0).toUpperCase();
            elements.userAvatar.textContent = firstLetter;
        }
    }
}

// --- DASHBOARD PANEL RENDER ---
function renderDashboard() {
    const today = new Date();
    const currYear = today.getFullYear();
    const currMonth = today.getMonth() + 1; // 1-12

    // 1. Balance Total: Sum of wallets (esObjetivoAhorro == false)
    const normalWallets = state.carteras.filter(c => !c.esObjetivoAhorro);
    const balanceTotal = normalWallets.reduce((sum, w) => sum + w.montoActual, 0);

    // 2. Ahorros Acumulados: Sum of goals (esObjetivoAhorro == true)
    const goals = state.carteras.filter(c => c.esObjetivoAhorro);
    const savingsTotal = goals.reduce((sum, g) => sum + g.montoActual, 0);

    // 3. Gastos del Mes: GASTO type in current month
    const thisMonthGastos = state.transacciones.filter(t => {
        if (t.tipo !== 'GASTO' || !t.fecha) return false;
        const [y, m] = t.fecha.split('-');
        return parseInt(y) === currYear && parseInt(m) === currMonth;
    });
    const monthlyGastosTotal = thisMonthGastos.reduce((sum, t) => sum + t.monto, 0);

    // 4. Ingresos del Mes: INGRESO type in current month
    const thisMonthIngresos = state.transacciones.filter(t => {
        if (t.tipo !== 'INGRESO' || !t.fecha) return false;
        const [y, m] = t.fecha.split('-');
        return parseInt(y) === currYear && parseInt(m) === currMonth;
    });
    const monthlyIngresosTotal = thisMonthIngresos.reduce((sum, t) => sum + t.monto, 0);

    // Update DOM Metrics values
    elements.metricBalance.textContent = formatCurrency(balanceTotal);
    elements.metricAhorros.textContent = formatCurrency(savingsTotal);
    elements.metricGastos.textContent = formatCurrency(monthlyGastosTotal);
    elements.metricGastosCount.textContent = `${thisMonthGastos.length} consumos registrados este mes`;
    elements.metricIngresos.textContent = formatCurrency(monthlyIngresosTotal);
    elements.metricIngresosCount.textContent = `${thisMonthIngresos.length} entradas registradas este mes`;

    // Render Recent Activities (Last 5 transactions) - Optimized to O(N) and uses fast string comparison
    const recentTrans = [];
    for (let i = 0; i < state.transacciones.length; i++) {
        const current = state.transacciones[i];
        if (recentTrans.length < 5) {
            recentTrans.push(current);
            recentTrans.sort((a, b) => b.fecha.localeCompare(a.fecha));
        } else {
            const oldestInTop5 = recentTrans[4];
            if (current.fecha.localeCompare(oldestInTop5.fecha) > 0) {
                recentTrans[4] = current;
                recentTrans.sort((a, b) => b.fecha.localeCompare(a.fecha));
            }
        }
    }

    const recentList = elements.recentTransactionsList;
    recentList.innerHTML = '';

    if (recentTrans.length === 0) {
        recentList.innerHTML = '<div class="no-data">No hay transacciones recientes registradas.</div>';
    } else {
        recentTrans.forEach(t => {
            const item = document.createElement('div');

            // Inherit color styling from Categoria if it is GASTO
            let customColorClass = '';
            let customStyle = '';
            if (t.tipo === 'GASTO' && t.categoria && t.categoria.color) {
                const colorVal = t.categoria.color;
                if (colorVal.startsWith('#')) {
                    customColorClass = 'card-custom-color';
                    const rgb = hexToRgb(colorVal);
                    customStyle = `--card-accent: ${colorVal}; --card-accent-rgb: ${rgb.r}, ${rgb.g}, ${rgb.b};`;
                } else {
                    customColorClass = `card-color-${colorVal}`;
                }
            } else if (t.cartera && t.cartera.color) {
                // Otherwise default color based on the selected wallet
                const colorVal = t.cartera.color;
                if (colorVal.startsWith('#')) {
                    customColorClass = 'card-custom-color';
                    const rgb = hexToRgb(colorVal);
                    customStyle = `--card-accent: ${colorVal}; --card-accent-rgb: ${rgb.r}, ${rgb.g}, ${rgb.b};`;
                } else {
                    customColorClass = `card-color-${colorVal}`;
                }
            }
            item.className = `transaction-item ${customColorClass}`;
            if (customStyle) {
                item.style.cssText = customStyle;
            }

            // Select icon and amount class based on transaction type
            let icon = 'arrow-left-right';
            let amountClass = 'text-gold';
            let prefix = '';
            let subtitle = t.cartera ? escapeHtml(t.cartera.nombre) : 'No vinculada';

            if (t.tipo === 'INGRESO') {
                icon = 'trending-up';
                amountClass = 'badge-tipo ingreso';
                prefix = '+';
            } else if (t.tipo === 'GASTO') {
                icon = 'trending-down';
                amountClass = 'badge-tipo gasto';
                prefix = '-';
                if (t.categoria) {
                    subtitle += ` • ${escapeHtml(t.categoria.nombre)}`;
                }
            } else if (t.tipo === 'AHORRO') {
                icon = 'piggy-bank';
                amountClass = 'badge-tipo ahorro';
                prefix = '+';
            }

            item.innerHTML = `
                <div class="transaction-info">
                    <div class="category-badge-icon" style="border-color: var(--card-accent, var(--border-premium)); color: var(--card-accent, var(--text-secondary));">
                        <i data-lucide="${icon}"></i>
                    </div>
                    <div class="transaction-details">
                        <p>${escapeHtml(t.descripcion)}</p>
                        <span class="transaction-category">${subtitle}</span>
                    </div>
                </div>
                <div class="transaction-meta">
                    <span class="transaction-amount ${amountClass}" style="font-weight:600;">${prefix}${formatCurrency(t.monto)}</span>
                    <p class="transaction-date">${formatDate(t.fecha)}</p>
                </div>
            `;
            recentList.appendChild(item);
        });

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }

    // Refresh charts
    renderDashboardCharts();
}
function renderDashboardCharts() {
    const isLight = (state.theme === 'classic-light' || state.theme === 'crema-beige');
    const labelColor = isLight ? '#475569' : '#94a3b8';
    const gridColor = isLight ? 'rgba(0, 0, 0, 0.05)' : 'rgba(255, 255, 255, 0.05)';

    // --- CHART 1: Expenses by Category ---
    const expenses = state.transacciones.filter(t => t.tipo === 'GASTO');
    
    // Group by category
    const catTotals = {};
    expenses.forEach(t => {
        const catName = t.categoria ? t.categoria.nombre : 'Sin Categoría';
        catTotals[catName] = (catTotals[catName] || 0) + t.monto;
    });

    const catLabels = Object.keys(catTotals);
    const catData = Object.values(catTotals);

    const ctxCat = document.getElementById('chart-categorias');
    if (state.charts.categorias) {
        state.charts.categorias.destroy();
        state.charts.categorias = null;
    }

    if (catLabels.length === 0) {
        const ctx = ctxCat.getContext('2d');
        ctx.clearRect(0, 0, ctxCat.width, ctxCat.height);
        ctx.fillStyle = labelColor;
        ctx.textAlign = 'center';
        ctx.font = '14px Inter';
        ctx.fillText('Sin gastos registrados para graficar', ctxCat.width / 2, ctxCat.height / 2);
    } else {
        // Aesthetic color palette for luxury transparent look
        const colorPalette = [
            'rgba(220, 179, 66, 0.6)',
            'rgba(82, 196, 136, 0.6)',
            'rgba(79, 164, 207, 0.6)',
            'rgba(168, 85, 247, 0.6)',
            'rgba(236, 72, 153, 0.6)',
            'rgba(226, 97, 97, 0.6)'
        ];
        const borderColors = colorPalette.map(c => c.replace('0.6', '1'));

        state.charts.categorias = new Chart(ctxCat, {
            type: 'doughnut',
            data: {
                labels: catLabels,
                datasets: [{
                    data: catData,
                    backgroundColor: colorPalette,
                    borderColor: borderColors,
                    borderWidth: 1.5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            color: labelColor,
                            font: { family: 'Outfit', size: 12 }
                        }
                    },
                    tooltip: {
                        enabled: true,
                        backgroundColor: isLight ? 'rgba(255, 255, 255, 0.85)' : 'rgba(24, 24, 32, 0.85)',
                        titleColor: isLight ? '#1e293b' : '#fafafa',
                        bodyColor: isLight ? '#475569' : '#e4e4e7',
                        borderColor: isLight ? 'rgba(0, 0, 0, 0.08)' : 'rgba(255, 255, 255, 0.12)',
                        borderWidth: 1,
                        cornerRadius: 8,
                        padding: 10,
                        titleFont: { family: 'Outfit', size: 12, weight: '600' },
                        bodyFont: { family: 'Outfit', size: 12 },
                        displayColors: false
                    }
                },
                cutout: '65%'
            }
        });
    }

    // --- CHART 2: Grouped Bar Income vs Expense ---
    const monthsShortNames = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
    const monthlyDataMap = {};

    const last6Months = [];
    const today = new Date();
    for (let i = 5; i >= 0; i--) {
        const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
        monthlyDataMap[key] = { ingresos: 0, gastos: 0 };
        last6Months.push({
            key: key,
            label: `${monthsShortNames[d.getMonth()]} ${String(d.getFullYear()).substring(2)}`
        });
    }

    state.transacciones.forEach(t => {
        if (!t.fecha) return;
        const monthKey = t.fecha.substring(0, 7); // 'YYYY-MM'
        if (monthlyDataMap[monthKey]) {
            if (t.tipo === 'INGRESO') {
                monthlyDataMap[monthKey].ingresos += t.monto;
            } else if (t.tipo === 'GASTO') {
                monthlyDataMap[monthKey].gastos += t.monto;
            }
        }
    });

    const incomeDataSet = last6Months.map(m => monthlyDataMap[m.key].ingresos);
    const expenseDataSet = last6Months.map(m => monthlyDataMap[m.key].gastos);
    const barLabels = last6Months.map(m => m.label);

    const ctxTrend = document.getElementById('chart-mensual');
    if (state.charts.mensual) {
        state.charts.mensual.destroy();
        state.charts.mensual = null;
    }

    const ctx = ctxTrend.getContext('2d');
    const gradientIncome = ctx.createLinearGradient(0, 0, 0, 280);
    gradientIncome.addColorStop(0, 'rgba(82, 196, 136, 0.65)');
    gradientIncome.addColorStop(1, 'rgba(82, 196, 136, 0.02)');

    const gradientExpense = ctx.createLinearGradient(0, 0, 0, 280);
    gradientExpense.addColorStop(0, 'rgba(226, 97, 97, 0.65)');
    gradientExpense.addColorStop(1, 'rgba(226, 97, 97, 0.02)');

    state.charts.mensual = new Chart(ctxTrend, {
        type: 'bar',
        data: {
            labels: barLabels,
            datasets: [
                {
                    label: 'Ingresos (+)',
                    data: incomeDataSet,
                    backgroundColor: gradientIncome,
                    borderColor: 'rgba(82, 196, 136, 1)',
                    borderWidth: 1.5,
                    borderRadius: 4
                },
                {
                    label: 'Gastos (-)',
                    data: expenseDataSet,
                    backgroundColor: gradientExpense,
                    borderColor: 'rgba(226, 97, 97, 1)',
                    borderWidth: 1.5,
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: { color: labelColor, font: { family: 'Outfit', size: 11 } }
                },
                tooltip: {
                    enabled: true,
                    backgroundColor: isLight ? 'rgba(255, 255, 255, 0.85)' : 'rgba(24, 24, 32, 0.85)',
                    titleColor: isLight ? '#1e293b' : '#fafafa',
                    bodyColor: isLight ? '#475569' : '#e4e4e7',
                    borderColor: isLight ? 'rgba(0, 0, 0, 0.08)' : 'rgba(255, 255, 255, 0.12)',
                    borderWidth: 1,
                    cornerRadius: 8,
                    padding: 10,
                    titleFont: { family: 'Outfit', size: 12, weight: '600' },
                    bodyFont: { family: 'Outfit', size: 12 },
                    displayColors: false
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: labelColor, font: { family: 'Inter', size: 11 } }
                },
                y: {
                    grid: { color: gridColor },
                    ticks: { color: labelColor, font: { family: 'Inter', size: 11 } }
                }
            }
        }
    });
}

// --- CARTERAS PANEL RENDER ---
function renderCarteras() {
    const normalWallets = state.carteras.filter(c => !c.esObjetivoAhorro);
    const savingsGoals = state.carteras.filter(c => c.esObjetivoAhorro);

    // 1. Wallets Rendering
    const walletsGrid = elements.walletsContainer;
    walletsGrid.innerHTML = '';
    
    if (normalWallets.length === 0) {
        walletsGrid.innerHTML = '<div class="no-data" style="grid-column: 1/-1;">No tienes cuentas ni carteras creadas. Comienza agregando una para gestionar tus fondos.</div>';
    } else {
        normalWallets.forEach(w => {
            const card = document.createElement('div');
            
            // Color custom class
            let colorClass = '';
            const colorVal = w.color || 'gold';
            if (colorVal.startsWith('#')) {
                colorClass = 'card-custom-color';
                const rgb = hexToRgb(colorVal);
                card.style.cssText = `--card-accent: ${colorVal}; --card-accent-rgb: ${rgb.r}, ${rgb.g}, ${rgb.b};`;
            } else {
                colorClass = `card-color-${colorVal}`;
            }
            card.className = `wallet-card ${colorClass}`;
            card.setAttribute('data-id', w.id);
            
            card.innerHTML = `
                <div class="wallet-card-header">
                    <div>
                        <h4 class="wallet-card-title">${escapeHtml(w.nombre)}</h4>
                        <p class="wallet-card-desc">${w.descripcion ? escapeHtml(w.descripcion) : 'Sin notas descritas'}</p>
                    </div>
                    <div class="text-gold"><i data-lucide="credit-card"></i></div>
                </div>
                <h3 class="wallet-card-balance">${formatCurrency(w.montoActual)}</h3>
                <div class="wallet-card-footer">
                    <span class="text-gold" style="font-size:11px; text-transform:uppercase; letter-spacing:0.5px;">Disponible</span>
                    <div class="wallet-actions">
                        <button class="btn-icon edit-btn" onclick="openEditCartera(${w.id})" title="Editar"><i data-lucide="edit-3"></i></button>
                        <button class="btn-icon delete-btn" onclick="triggerDeleteCartera(${w.id})" title="Eliminar"><i data-lucide="trash-2"></i></button>
                    </div>
                </div>
            `;
            walletsGrid.appendChild(card);
        });
    }

    // 2. Savings Goals Rendering
    const goalsGrid = elements.goalsContainer;
    goalsGrid.innerHTML = '';

    if (savingsGoals.length === 0) {
        goalsGrid.innerHTML = '<div class="no-data" style="grid-column: 1/-1;">No has programado metas de ahorro aún. Divide tus ahorros según tus objetivos de inversión.</div>';
    } else {
        savingsGoals.forEach(g => {
            const pct = g.montoObjetivo && g.montoObjetivo > 0 
                ? Math.min((g.montoActual / g.montoObjetivo) * 100, 100) 
                : 0;

            const card = document.createElement('div');
            
            // Color custom class (defaults to blue if not selected)
            let colorClass = '';
            const colorVal = g.color || 'blue';
            if (colorVal.startsWith('#')) {
                colorClass = 'card-custom-color';
                const rgb = hexToRgb(colorVal);
                card.style.cssText = `--card-accent: ${colorVal}; --card-accent-rgb: ${rgb.r}, ${rgb.g}, ${rgb.b};`;
            } else {
                colorClass = `card-color-${colorVal}`;
            }
            card.className = `wallet-card ${colorClass}`;
            card.setAttribute('data-id', g.id);

            card.innerHTML = `
                <div class="wallet-card-header">
                    <div>
                        <h4 class="wallet-card-title">${escapeHtml(g.nombre)}</h4>
                        <p class="wallet-card-desc">${g.descripcion ? escapeHtml(g.descripcion) : 'Meta sin notas adicionales'}</p>
                    </div>
                    <div class="text-gold"><i data-lucide="piggy-bank"></i></div>
                </div>
                <div>
                    <h3 class="wallet-card-balance" style="margin-top:12px; margin-bottom: 0;">${formatCurrency(g.montoActual)}</h3>
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill" style="width: ${pct}%;"></div>
                    </div>
                    <div class="goal-numbers">
                        <span>Progreso: ${pct.toFixed(0)}%</span>
                        <span>Objetivo: ${formatCurrency(g.montoObjetivo || 0)}</span>
                    </div>
                </div>
                <div class="wallet-card-footer" style="margin-top:12px;">
                    <span class="text-gold" style="font-size:11px; text-transform:uppercase; letter-spacing:0.5px;">Meta de Ahorro</span>
                    <div class="wallet-actions">
                        <button class="btn-icon edit-btn" onclick="openEditCartera(${g.id})" title="Editar"><i data-lucide="edit-3"></i></button>
                        <button class="btn-icon delete-btn" onclick="triggerDeleteCartera(${g.id})" title="Eliminar"><i data-lucide="trash-2"></i></button>
                    </div>
                </div>
            `;
            goalsGrid.appendChild(card);
        });
    }

    // Setup drag and drop for cards
    setupDragAndDrop();

    if (window.lucide) {
        window.lucide.createIcons();
    }
}

// --- DRAG & DROP FOR WALLETS ---
function setupDragAndDrop() {
    const containers = [elements.walletsContainer, elements.goalsContainer];
    
    containers.forEach(container => {
        if (!container) return;
        
        const cards = container.querySelectorAll('.wallet-card');
        
        cards.forEach(card => {
            card.setAttribute('draggable', 'true');
            
            card.addEventListener('dragstart', (e) => {
                card.classList.add('dragging');
                e.dataTransfer.effectAllowed = 'move';
                e.dataTransfer.setData('text/plain', card.getAttribute('data-id'));
            });
            
            card.addEventListener('dragend', () => {
                card.classList.remove('dragging');
                saveCarterasOrder(container);
            });

            // Optimización de arrastre por punto medio para evitar flickering
            card.addEventListener('dragover', (e) => {
                e.preventDefault();
                const dragging = container.querySelector('.dragging');
                if (!dragging || dragging === card) return;

                // Asegurar que solo arrastramos dentro del mismo contenedor
                if (dragging.parentNode !== card.parentNode) return;

                const rect = card.getBoundingClientRect();
                const midX = rect.left + rect.width / 2;
                const midY = rect.top + rect.height / 2;

                // Determinar si la tarjeta que arrastramos está antes o después de la tarjeta destino en el DOM
                const isDraggingBeforeTarget = (dragging.compareDocumentPosition(card) & Node.DOCUMENT_POSITION_FOLLOWING) !== 0;

                if (isDraggingBeforeTarget) {
                    // Si arrastramos de izquierda a derecha / arriba a abajo
                    // Solo intercambiar si el cursor pasa la mitad horizontal o vertical de la tarjeta destino
                    if (e.clientX > midX || e.clientY > midY) {
                        container.insertBefore(dragging, card.nextSibling);
                    }
                } else {
                    // Si arrastramos de derecha a izquierda / abajo a arriba
                    // Solo intercambiar si el cursor retrocede de la mitad horizontal o vertical de la tarjeta destino
                    if (e.clientX < midX || e.clientY < midY) {
                        container.insertBefore(dragging, card);
                    }
                }
            });
        });
        
        container.addEventListener('dragover', (e) => {
            e.preventDefault();
        });
        
        container.addEventListener('dragenter', (e) => {
            e.preventDefault();
        });
    });
}

async function saveCarterasOrder(container) {
    const cards = [...container.querySelectorAll('.wallet-card')];
    const ids = cards.map(card => parseInt(card.getAttribute('data-id'))).filter(id => !isNaN(id));
    
    if (ids.length === 0) return;
    
    try {
        const response = await apiFetch(`${API_CARTERAS}/reorder`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        
        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Error al guardar el nuevo orden');
        }
        
        // Update local state.carteras
        const isGoalContainer = (container === elements.goalsContainer);
        
        // Assign new order index based on their new visual index
        state.carteras.forEach(c => {
            const cardEl = container.querySelector(`.wallet-card[data-id="${c.id}"]`);
            if (cardEl) {
                const index = cards.indexOf(cardEl);
                if (index !== -1) {
                    c.orden = index;
                }
            }
        });
        
        // Sort the state.carteras array locally to keep it in sync
        state.carteras.sort((a, b) => {
            // First check esObjetivoAhorro to keep group structure if sorted elsewhere,
            // but primarily sort by their assigned 'orden' field and id
            const aOrder = a.orden !== undefined && a.orden !== null ? a.orden : 999999;
            const bOrder = b.orden !== undefined && b.orden !== null ? b.orden : 999999;
            return aOrder - bOrder || a.id - b.id;
        });
        
        updateFormDropdowns();
        showToast('Orden de billeteras actualizado');
    } catch (error) {
        showToast(error.message, 'error');
        await fetchAllData();
    }
}

// --- TRANSACCIONES PANEL RENDER ---
function renderTransacciones() {
    const query = elements.filterSearch.value.toLowerCase().trim();
    const tipoVal = elements.filterTipo.value;
    const carteraVal = elements.filterCartera.value;
    const ordenVal = elements.filterOrden.value;

    let filtered = [...state.transacciones];

    // Search query match
    if (query) {
        filtered = filtered.filter(t => t.descripcion.toLowerCase().includes(query));
    }

    // Type match
    if (tipoVal) {
        filtered = filtered.filter(t => t.tipo === tipoVal);
    }

    // Cartera match
    if (carteraVal) {
        filtered = filtered.filter(t => t.cartera && t.cartera.id == carteraVal);
    }

    // Ordering
    filtered.sort((a, b) => {
        if (ordenVal === 'fecha-desc') return new Date(b.fecha) - new Date(a.fecha);
        if (ordenVal === 'fecha-asc') return new Date(a.fecha) - new Date(b.fecha);
        if (ordenVal === 'monto-desc') return b.monto - a.monto;
        if (ordenVal === 'monto-asc') return a.monto - b.monto;
        return 0;
    });

    // Populate Table
    const tbody = elements.transaccionesTableBody;
    tbody.innerHTML = '';

    if (filtered.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="no-data">No se encontraron transacciones con los criterios seleccionados.</td>
            </tr>
        `;
        elements.transaccionesTableFooter.textContent = 'Mostrando 0 registros';
    } else {
        filtered.forEach(t => {
            const tr = document.createElement('tr');
            
            const dateStr = formatDate(t.fecha);
            const desc = escapeHtml(t.descripcion);
            
            let tipoBadge = '';
            let amountStr = '';
            
            if (t.tipo === 'INGRESO') {
                tipoBadge = '<span class="badge-tipo ingreso">Ingreso</span>';
                amountStr = `<span class="badge-tipo ingreso" style="font-weight:600;">+ ${formatCurrency(t.monto)}</span>`;
            } else if (t.tipo === 'GASTO') {
                tipoBadge = '<span class="badge-tipo gasto">Gasto</span>';
                amountStr = `<span class="badge-tipo gasto" style="font-weight:600;">- ${formatCurrency(t.monto)}</span>`;
            } else if (t.tipo === 'AHORRO') {
                tipoBadge = '<span class="badge-tipo ahorro">Ahorro</span>';
                amountStr = `<span class="badge-tipo ahorro" style="font-weight:600;">+ ${formatCurrency(t.monto)}</span>`;
            }

            const accountName = t.cartera ? escapeHtml(t.cartera.nombre) : '-';
            
            // Categoria tag color inheriting
            let catColorClass = '';
            let catStyle = '';
            if (t.tipo === 'GASTO' && t.categoria && t.categoria.color) {
                const colorVal = t.categoria.color;
                if (colorVal.startsWith('#')) {
                    catColorClass = 'card-custom-color';
                    const rgb = hexToRgb(colorVal);
                    catStyle = `style="--card-accent: ${colorVal}; --card-accent-rgb: ${rgb.r}, ${rgb.g}, ${rgb.b};"`;
                } else {
                    catColorClass = `card-color-${colorVal}`;
                }
            }

            const catName = (t.tipo === 'GASTO' && t.categoria) 
                ? `<span class="categoria-tag ${catColorClass}" ${catStyle}>${escapeHtml(t.categoria.nombre)}</span>` 
                : '<span class="text-muted" style="font-size:12px;">No aplica</span>';

            tr.innerHTML = `
                <td>${dateStr}</td>
                <td style="font-weight:500;">${desc}</td>
                <td>${tipoBadge}</td>
                <td>${accountName}</td>
                <td>${catName}</td>
                <td class="text-right">${amountStr}</td>
                <td class="text-center actions-cell">
                    <button class="btn-icon edit-btn" onclick="openEditTransaccion(${t.id})" title="Editar"><i data-lucide="edit-3"></i></button>
                    <button class="btn-icon delete-btn" onclick="triggerDeleteTransaccion(${t.id})" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        elements.transaccionesTableFooter.textContent = `Mostrando ${filtered.length} registro(s)`;
        
        if (window.lucide) {
            window.lucide.createIcons();
        }
    }
}

// --- CATEGORIAS PANEL RENDER ---
function renderCategorias() {
    const grid = elements.categoriesGrid;
    grid.innerHTML = '';

    const today = new Date();
    const currYear = today.getFullYear();
    const currMonth = today.getMonth() + 1; // 1-12

    state.categorias.forEach(cat => {
        const associated = state.transacciones.filter(t => t.tipo === 'GASTO' && t.categoria && t.categoria.id === cat.id);
        const count = associated.length;
        const total = associated.reduce((sum, t) => sum + t.monto, 0);

        // Calculate current month spending
        const monthlyAssociated = associated.filter(t => {
            if (!t.fecha) return false;
            const [y, m] = t.fecha.split('-');
            return parseInt(y) === currYear && parseInt(m) === currMonth;
        });
        const monthlyTotal = monthlyAssociated.reduce((sum, t) => sum + t.monto, 0);

        // Budget / Limit HTML
        let budgetHtml = '';
        if (cat.limiteGasto && cat.limiteGasto > 0) {
            const percent = Math.min(Math.round((monthlyTotal / cat.limiteGasto) * 100), 100);
            let statusClass = '';
            let statusText = `Este mes: ${formatCurrency(monthlyTotal)} de ${formatCurrency(cat.limiteGasto)}`;
            let statusColorClass = 'text-muted';

            if (percent >= 100) {
                statusClass = 'danger';
                statusText = `¡Límite superado! (${formatCurrency(monthlyTotal)} de ${formatCurrency(cat.limiteGasto)})`;
                statusColorClass = 'text-danger';
            } else if (percent >= 80) {
                statusClass = 'warning';
                statusText = `Cerca del límite: ${formatCurrency(monthlyTotal)} de ${formatCurrency(cat.limiteGasto)}`;
                statusColorClass = 'text-warning';
            }

            budgetHtml = `
                <div class="category-budget-section" style="margin-top: 12px; margin-bottom: 8px; width: 100%;">
                    <div style="display: flex; justify-content: space-between; font-size: 12px; font-weight: 500; align-items: center; width: 100%;">
                        <span class="${statusColorClass}">${statusText}</span>
                        <span class="${statusColorClass}" style="font-weight: 600; margin-left: 8px; flex-shrink: 0;">${percent}%</span>
                    </div>
                    <div class="progress-bar-container" style="margin-top: 6px; height: 6px;">
                        <div class="progress-bar-fill ${statusClass}" style="width: ${percent}%;"></div>
                    </div>
                </div>
            `;
        } else {
            budgetHtml = `
                <div class="category-budget-section" style="margin-top: 12px; margin-bottom: 8px;">
                    <div style="font-size: 11px; color: var(--text-muted); font-style: italic;">
                        Sin límite de gasto mensual establecido
                    </div>
                </div>
            `;
        }

        const card = document.createElement('div');
        
        // Color custom class
        let colorClass = '';
        const colorVal = cat.color || 'purple';
        if (colorVal.startsWith('#')) {
            colorClass = 'card-custom-color';
            const rgb = hexToRgb(colorVal);
            card.style.cssText = `--card-accent: ${colorVal}; --card-accent-rgb: ${rgb.r}, ${rgb.g}, ${rgb.b};`;
        } else {
            colorClass = `card-color-${colorVal}`;
        }
        card.className = `category-card ${colorClass}`;

        card.innerHTML = `
            <div class="cat-header">
                <div>
                    <h4 class="cat-name">${escapeHtml(cat.nombre)}</h4>
                    <p class="cat-desc">${cat.descripcion ? escapeHtml(cat.descripcion) : 'Sin detalles añadidos'}</p>
                </div>
                <div class="text-gold"><i data-lucide="tag"></i></div>
            </div>
            ${budgetHtml}
            <div class="cat-footer">
                <div class="cat-count">
                    <span>${count} gastos asociados</span>
                    <p class="text-gold" style="font-weight:600; font-size:13px; margin-top:2px;">Total histórico: ${formatCurrency(total)}</p>
                </div>
                <div class="cat-actions">
                    <button class="btn-icon edit-btn" onclick="openEditCategoria(${cat.id})" title="Editar"><i data-lucide="edit-3"></i></button>
                    <button class="btn-icon delete-btn" onclick="triggerDeleteCategoria(${cat.id})" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });

    if (state.categorias.length === 0) {
        grid.innerHTML = '<div class="no-data" style="grid-column: 1/-1;">No hay categorías registradas. Comienza agregando una para clasificar tus gastos.</div>';
    }

    if (window.lucide) {
        window.lucide.createIcons();
    }
}

// --- CALENDAR PANEL RENDER ---
function renderCalendar() {
    elements.calendarMonthYear.textContent = `${MONTH_NAMES[state.currentMonth]} ${state.currentYear}`;

    const daysBody = elements.calendarDaysBody;
    daysBody.innerHTML = '';

    // Create a Set of event dates for O(1) daily lookup (Optimized O(D + E) rendering)
    const eventDatesSet = new Set(state.eventos.map(ev => ev.fecha));

    const firstDayIndex = new Date(state.currentYear, state.currentMonth, 1).getDay();
    const totalDaysInMonth = new Date(state.currentYear, state.currentMonth + 1, 0).getDate();

    let dayCount = 1;
    let row = document.createElement('tr');

    for (let i = 0; i < 7; i++) {
        if (i < firstDayIndex) {
            const td = document.createElement('td');
            const dayDiv = document.createElement('div');
            dayDiv.className = 'calendar-day empty-day';
            td.appendChild(dayDiv);
            row.appendChild(td);
        } else {
            break;
        }
    }

    let cellIndex = firstDayIndex;
    while (dayCount <= totalDaysInMonth) {
        if (cellIndex === 7) {
            daysBody.appendChild(row);
            row = document.createElement('tr');
            cellIndex = 0;
        }

        const td = document.createElement('td');
        const dayDiv = document.createElement('div');
        dayDiv.className = 'calendar-day';
        dayDiv.textContent = dayCount;

        const padM = String(state.currentMonth + 1).padStart(2, '0');
        const padD = String(dayCount).padStart(2, '0');
        const dateStr = `${state.currentYear}-${padM}-${padD}`;

        dayDiv.setAttribute('data-date', dateStr);

        const localTodayStr = getLocalDateString(new Date());
        if (dateStr === localTodayStr) {
            dayDiv.classList.add('today');
        }

        if (dateStr === state.selectedDateStr) {
            dayDiv.classList.add('selected');
        }

        const dayHasEvent = eventDatesSet.has(dateStr);
        if (dayHasEvent) {
            const dot = document.createElement('div');
            dot.className = 'event-dot';
            dayDiv.appendChild(dot);
        }

        dayDiv.addEventListener('click', () => {
            state.selectedDateStr = dateStr;
            document.querySelectorAll('.calendar-day').forEach(el => el.classList.remove('selected'));
            dayDiv.classList.add('selected');
            renderSelectedDayEvents();
        });

        td.appendChild(dayDiv);
        row.appendChild(td);
        dayCount++;
        cellIndex++;
    }

    if (cellIndex > 0 && cellIndex < 7) {
        for (let i = cellIndex; i < 7; i++) {
            const td = document.createElement('td');
            const dayDiv = document.createElement('div');
            dayDiv.className = 'calendar-day empty-day';
            td.appendChild(dayDiv);
            row.appendChild(td);
        }
        daysBody.appendChild(row);
    } else if (dayCount > totalDaysInMonth) {
        daysBody.appendChild(row);
    }

    renderSelectedDayEvents();
}

function renderSelectedDayEvents() {
    const formattedTitle = formatDate(state.selectedDateStr);
    elements.selectedDayTitle.textContent = `Eventos para el ${formattedTitle}`;
    elements.eventoFecha.value = state.selectedDateStr;

    const dayEvs = state.eventos.filter(ev => ev.fecha === state.selectedDateStr);
    const list = elements.dayEventsList;
    list.innerHTML = '';

    if (dayEvs.length === 0) {
        list.innerHTML = '<div class="no-data">No hay eventos programados para esta fecha.</div>';
    } else {
        // Render regular calendar events
        dayEvs.forEach(ev => {
            const item = document.createElement('div');
            item.className = 'event-item';
            
            const typeLower = ev.tipo.toLowerCase();
            const badgeClass = `event-badge ${typeLower}`;
            let badgeText = ev.tipo;
            if (ev.tipo === 'PAGO') badgeText = 'Pago / Vencimiento';
            else if (ev.tipo === 'RECORDATORIO') badgeText = 'Recordatorio';
            else if (ev.tipo === 'META') badgeText = 'Meta / Ahorro';
            else if (ev.tipo === 'OTRO') badgeText = 'Otro';

            item.innerHTML = `
                <div class="event-item-info">
                    <h5>${escapeHtml(ev.titulo)}</h5>
                    <p>${ev.descripcion ? escapeHtml(ev.descripcion) : 'Sin notas'}</p>
                    <span class="${badgeClass}">${badgeText}</span>
                </div>
                <div class="cat-actions" style="margin-left: 12px;">
                    <button class="btn-icon edit-btn" onclick="openEditEvento(${ev.id})" title="Editar"><i data-lucide="edit-3"></i></button>
                    <button class="btn-icon delete-btn" onclick="triggerDeleteEvento(${ev.id})" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </div>
            `;
            list.appendChild(item);
        });

        if (window.lucide) {
            window.lucide.createIcons();
        }
    }
}

// --- SUBMIT: TRANSACCION ---
async function handleTransaccionSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const submitBtnText = submitBtn ? submitBtn.querySelector('span') : null;
    const originalText = submitBtnText ? submitBtnText.textContent : 'Guardar';

    if (submitBtn) {
        submitBtn.disabled = true;
        if (submitBtnText) submitBtnText.textContent = 'Guardando...';
    }

    const id = elements.transaccionId.value;
    const tipo = elements.transaccionTipo.value;
    const rawCatId = elements.transaccionCategoria.value;

    const dto = {
        descripcion: elements.transaccionDescripcion.value.trim(),
        monto: parseFloat(elements.transaccionMonto.value),
        fecha: elements.transaccionFecha.value,
        tipo: tipo,
        carteraId: parseInt(elements.transaccionCartera.value),
        categoriaId: (tipo === 'GASTO' && rawCatId) ? parseInt(rawCatId) : null
    };

    try {
        let response;
        if (id) {
            response = await apiFetch(`${API_TRANSACCIONES}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            response = await apiFetch(API_TRANSACCIONES, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }

        const data = await response.json();

        if (!response.ok) {
            if (data.details) {
                const errs = Object.values(data.details).join(', ');
                throw new Error(errs);
            }
            throw new Error(data.message || 'Error al guardar la transacción');
        }

        showToast(id ? 'Transacción modificada con éxito' : 'Transacción registrada con éxito');
        closeModal(elements.modalTransaccion);
        await fetchAllData(['carteras', 'transacciones', 'categorias', 'checklist']);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            if (submitBtnText) submitBtnText.textContent = originalText;
        }
    }
}

// --- SUBMIT: CARTERA / META ---
async function handleCarteraSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const submitBtnText = submitBtn ? submitBtn.querySelector('span') : null;
    const originalText = submitBtnText ? submitBtnText.textContent : 'Guardar';

    if (submitBtn) {
        submitBtn.disabled = true;
        if (submitBtnText) submitBtnText.textContent = 'Guardando...';
    }

    const id = elements.carteraId.value;
    const esObjetivo = elements.carteraEsObjetivo.value === 'true';
    const rawGoal = elements.carteraMontoObjetivo.value;

    const dto = {
        nombre: elements.carteraNombre.value.trim(),
        montoActual: parseFloat(elements.carteraMontoActual.value) || 0.0,
        montoObjetivo: esObjetivo ? parseFloat(rawGoal) : null,
        descripcion: elements.carteraDescripcion.value.trim(),
        esObjetivoAhorro: esObjetivo,
        color: document.getElementById('cartera-color').value
    };

    try {
        let response;
        if (id) {
            response = await apiFetch(`${API_CARTERAS}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            response = await apiFetch(API_CARTERAS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }

        const data = await response.json();

        if (!response.ok) {
            if (data.details) {
                const errs = Object.values(data.details).join(', ');
                throw new Error(errs);
            }
            throw new Error(data.message || 'Error al guardar la cuenta');
        }

        showToast(id ? 'Cuenta modificada con éxito' : 'Cuenta registrada con éxito');
        closeModal(elements.modalCartera);
        await fetchAllData(['carteras']);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            if (submitBtnText) submitBtnText.textContent = originalText;
        }
    }
}

// --- SUBMIT: CATEGORIA ---
async function handleCategoriaSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const submitBtnText = submitBtn ? submitBtn.querySelector('span') : null;
    const originalText = submitBtnText ? submitBtnText.textContent : 'Guardar';

    if (submitBtn) {
        submitBtn.disabled = true;
        if (submitBtnText) submitBtnText.textContent = 'Guardando...';
    }

    const id = elements.categoriaId.value;
    const limiteInput = elements.categoriaLimite.value.trim();
    const dto = {
        nombre: elements.categoriaNombre.value.trim(),
        descripcion: elements.categoriaDescripcion.value.trim(),
        color: document.getElementById('categoria-color').value,
        limiteGasto: limiteInput ? parseFloat(limiteInput) : null
    };

    try {
        let response;
        if (id) {
            response = await apiFetch(`${API_CATEGORIAS}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            response = await apiFetch(API_CATEGORIAS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }

        const data = await response.json();

        if (!response.ok) {
            if (data.details) {
                const errs = Object.values(data.details).join(', ');
                throw new Error(errs);
            }
            throw new Error(data.message || 'Error al guardar la categoría');
        }

        showToast(id ? 'Categoría modificada con éxito' : 'Categoría registrada con éxito');
        closeModal(elements.modalCategoria);
        if (!id && data && data.id) {
            state.lastCreatedCategoryId = data.id;
        }
        await fetchAllData(['categorias']);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            if (submitBtnText) submitBtnText.textContent = originalText;
        }
    }
}

// --- SUBMIT: EVENTO CALENDARIO ---
async function handleEventoSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.textContent : '';

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Guardando...';
    }

    const id = elements.eventoId.value;
    const dto = {
        titulo: elements.eventoTitulo.value.trim(),
        descripcion: elements.eventoDescripcion.value.trim(),
        fecha: elements.eventoFecha.value,
        tipo: elements.eventoTipo.value
    };

    try {
        let response;
        if (id) {
            response = await apiFetch(`${API_EVENTOS}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            response = await apiFetch(API_EVENTOS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }

        const data = await response.json();

        if (!response.ok) {
            if (data.details) {
                const errs = Object.values(data.details).join(', ');
                throw new Error(errs);
            }
            throw new Error(data.message || 'Error al guardar el recordatorio');
        }

        showToast(id ? 'Recordatorio modificado' : 'Recordatorio programado');
        
        elements.formEvento.reset();
        elements.eventoId.value = '';
        elements.btnSaveEvento.textContent = 'Guardar Recordatorio';
        closeModal(elements.modalEvento);
        
        await fetchAllData(['eventos']);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            if (!id) {
                submitBtn.textContent = 'Guardar Recordatorio';
            } else {
                submitBtn.textContent = 'Modificar Recordatorio';
            }
        }
    }
}

// --- SUBMIT: PERFIL ---
function openPerfilModal() {
    if (state.perfil) {
        if (elements.perfilNombre) elements.perfilNombre.value = state.perfil.nombre || '';
        if (elements.perfilRango) elements.perfilRango.value = state.perfil.rango || '';
    }
    openModal(elements.modalPerfil);
}

async function handlePerfilSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const submitBtnText = submitBtn ? submitBtn.querySelector('span') : null;
    const originalText = submitBtnText ? submitBtnText.textContent : 'Guardar Cambios';

    if (submitBtn) {
        submitBtn.disabled = true;
        if (submitBtnText) submitBtnText.textContent = 'Guardando...';
    }

    const dto = {
        nombre: elements.perfilNombre.value.trim(),
        rango: elements.perfilRango.value.trim(),
        tema: state.theme
    };

    try {
        const response = await apiFetch(API_PERFIL, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });

        const data = await response.json();

        if (!response.ok) {
            if (data.details) {
                const errs = Object.values(data.details).join(', ');
                throw new Error(errs);
            }
            throw new Error(data.message || 'Error al guardar el perfil');
        }

        showToast('Perfil actualizado con éxito');
        closeModal(elements.modalPerfil);
        await fetchAllData(['perfil']);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            if (submitBtnText) submitBtnText.textContent = originalText;
        }
    }
}

// --- EDIT HANDLERS EXPOSED ON WINDOW ---
window.openEditCartera = function(id) {
    const c = state.carteras.find(item => item.id === id);
    if (!c) return;

    elements.carteraId.value = c.id;
    elements.carteraEsObjetivo.value = c.esObjetivoAhorro ? 'true' : 'false';
    elements.carteraNombre.value = c.nombre;
    elements.carteraMontoActual.value = c.montoActual;
    elements.carteraDescripcion.value = c.descripcion || '';

    // Color preset load
    const cColor = c.color || 'gold';
    document.getElementById('cartera-color').value = cColor;
    highlightModalColorPicker('cartera-color-picker', cColor);

    if (c.esObjetivoAhorro) {
        elements.modalCarteraTitle.textContent = 'Editar Ahorro / Meta';
        elements.labelMontoActual.textContent = 'Ahorro Acumulado ($)';
        elements.groupMontoObjetivo.style.display = 'block';
        elements.carteraMontoObjetivo.value = c.montoObjetivo;
        elements.carteraMontoObjetivo.setAttribute('required', 'required');
    } else {
        elements.modalCarteraTitle.textContent = 'Editar Cartera / Cuenta';
        elements.labelMontoActual.textContent = 'Saldo Disponible ($)';
        elements.groupMontoObjetivo.style.display = 'none';
        elements.carteraMontoObjetivo.value = '';
        elements.carteraMontoObjetivo.removeAttribute('required');
    }

    openModal(elements.modalCartera);
};

window.openEditTransaccion = function(id) {
    const t = state.transacciones.find(item => item.id === id);
    if (!t) return;

    elements.transaccionId.value = t.id;
    elements.transaccionDescripcion.value = t.descripcion;
    elements.transaccionMonto.value = t.monto;
    elements.transaccionFecha.value = t.fecha;
    elements.transaccionTipo.value = t.tipo;

    const activeWalletId = t.cartera ? t.cartera.id : null;
    handleTransactionFormTypeChange(t.tipo, activeWalletId);

    if (t.tipo === 'GASTO' && t.categoria) {
        elements.transaccionCategoria.value = t.categoria.id;
    }

    elements.modalTransaccionTitle.textContent = 'Editar Transacción';
    openModal(elements.modalTransaccion);
};

window.openEditCategoria = function(id) {
    const cat = state.categorias.find(item => item.id === id);
    if (!cat) return;

    elements.categoriaId.value = cat.id;
    elements.categoriaNombre.value = cat.nombre;
    elements.categoriaDescripcion.value = cat.descripcion || '';
    elements.categoriaLimite.value = cat.limiteGasto || '';

    // Preset color prefill
    const catColor = cat.color || 'purple';
    document.getElementById('categoria-color').value = catColor;
    highlightModalColorPicker('categoria-color-picker', catColor);

    elements.modalCategoriaTitle.textContent = 'Editar Categoría';
    openModal(elements.modalCategoria);
};

window.openEditEvento = function(id) {
    const ev = state.eventos.find(item => item.id === id);
    if (!ev) return;

    elements.eventoId.value = ev.id;
    elements.eventoTitulo.value = ev.titulo;
    elements.eventoTipo.value = ev.tipo;
    elements.eventoFecha.value = ev.fecha;
    elements.eventoDescripcion.value = ev.descripcion || '';

    elements.modalEventoTitle.textContent = 'Editar Recordatorio';
    elements.btnSaveEvento.textContent = 'Modificar Recordatorio';
    openModal(elements.modalEvento);
};

window.openNewEventoModal = function() {
    elements.eventoId.value = '';
    elements.eventoTitulo.value = '';
    elements.eventoTipo.value = 'RECORDATORIO';
    elements.eventoFecha.value = state.selectedDateStr;
    elements.eventoDescripcion.value = '';

    elements.modalEventoTitle.textContent = 'Programar Recordatorio';
    elements.btnSaveEvento.textContent = 'Guardar Recordatorio';
    openModal(elements.modalEvento);
};

// --- DELETE FLOWS EXPOSED ON WINDOW ---
window.triggerDeleteCartera = function(id) {
    state.activeDeleteTarget = { type: 'cartera', id };
    const c = state.carteras.find(item => item.id === id);
    if (!c) return;

    const countTrans = state.transacciones.filter(t => t.cartera && t.cartera.id === id).length;
    if (countTrans > 0) {
        elements.confirmMessage.innerHTML = `¿Estás seguro de eliminar la cuenta <strong>${escapeHtml(c.nombre)}</strong>?<br><br><span style="color:var(--danger); font-weight:bold;">¡Advertencia Importante!</span> Se eliminarán en cascada <strong>${countTrans} transacción(es) asociadas</strong> permanentemente. Esta acción no se puede deshacer.`;
    } else {
        elements.confirmMessage.innerHTML = `¿Deseas eliminar la cuenta <strong>${escapeHtml(c.nombre)}</strong>? Esta acción es irreversible.`;
    }
    openModal(elements.modalConfirm);
};

window.triggerDeleteTransaccion = function(id) {
    state.activeDeleteTarget = { type: 'transaccion', id };
    const t = state.transacciones.find(item => item.id === id);
    if (!t) return;

    elements.confirmMessage.innerHTML = `¿Estás seguro de eliminar esta transacción por <strong>${formatCurrency(t.monto)}</strong>?<br><br>El saldo de la cuenta afectada (<strong>${t.cartera ? escapeHtml(t.cartera.nombre) : 'No vinculada'}</strong>) se revertirá automáticamente.`;
    openModal(elements.modalConfirm);
};

window.triggerDeleteCategoria = function(id) {
    state.activeDeleteTarget = { type: 'categoria', id };
    const cat = state.categorias.find(item => item.id === id);
    if (!cat) return;

    const associatedCount = state.transacciones.filter(t => t.tipo === 'GASTO' && t.categoria && t.categoria.id === id).length;
    if (associatedCount > 0) {
        elements.confirmMessage.innerHTML = `<span style="color:var(--danger); font-weight:bold;">¡Eliminación Bloqueada!</span><br><br>La categoría <strong>${escapeHtml(cat.nombre)}</strong> posee <strong>${associatedCount} gasto(s) asociados</strong>.<br>Para proteger la integridad de tus datos, debes eliminar o reasignar dichos gastos primero.`;
        elements.btnConfirmDelete.setAttribute('disabled', 'disabled');
    } else {
        elements.confirmMessage.innerHTML = `¿Estás seguro de eliminar la categoría <strong>${escapeHtml(cat.nombre)}</strong>? No posee gastos vinculados.`;
    }
    openModal(elements.modalConfirm);
};

window.triggerDeleteEvento = function(id) {
    state.activeDeleteTarget = { type: 'evento', id };
    const ev = state.eventos.find(item => item.id === id);
    if (!ev) return;

    elements.confirmMessage.innerHTML = `¿Deseas cancelar y borrar el recordatorio: <strong>${escapeHtml(ev.titulo)}</strong>?`;
    openModal(elements.modalConfirm);
};

async function executeDelete() {
    const target = state.activeDeleteTarget;
    if (!target) return;

    let url = '';
    if (target.type === 'cartera') url = `${API_CARTERAS}/${target.id}`;
    else if (target.type === 'transaccion') url = `${API_TRANSACCIONES}/${target.id}`;
    else if (target.type === 'categoria') url = `${API_CATEGORIAS}/${target.id}`;
    else if (target.type === 'evento') url = `${API_EVENTOS}/${target.id}`;
    else if (target.type === 'gasto-checklist') url = `${API_CHECKLIST}/${target.id}`;

    const originalText = elements.btnConfirmDelete.textContent;
    elements.btnConfirmDelete.disabled = true;
    elements.btnConfirmDelete.textContent = 'Eliminando...';

    try {
        let domainsToReload = [];
        if (target.type === 'cartera') domainsToReload = ['carteras', 'transacciones'];
        else if (target.type === 'transaccion') domainsToReload = ['carteras', 'transacciones', 'categorias', 'checklist'];
        else if (target.type === 'categoria') domainsToReload = ['categorias', 'transacciones'];
        else if (target.type === 'evento') domainsToReload = ['eventos'];
        else if (target.type === 'gasto-checklist') domainsToReload = ['checklist', 'transacciones', 'carteras'];

        const response = await apiFetch(url, { method: 'DELETE' });

        if (!response.ok) {
            let msg = 'No se pudo eliminar el elemento';
            try {
                const data = await response.json();
                msg = data.message || msg;
            } catch(e) {}
            throw new Error(msg);
        }

        showToast('Elemento eliminado correctamente');
        closeModal(elements.modalConfirm);
        await fetchAllData(domainsToReload);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        elements.btnConfirmDelete.disabled = false;
        elements.btnConfirmDelete.textContent = originalText;
    }
}

// --- UTILS ---
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;")
              .replace(/</g, "&lt;")
              .replace(/>/g, "&gt;")
              .replace(/"/g, "&quot;")
              .replace(/'/g, "&#039;");
}

// --- CHECKLIST RENDER & LOGIC ---
function renderChecklist() {
    if (!elements.checklistContainer) return;

    // Update next month projection metric link and drawer total
    const nextMonthTotal = state.checklistNextMonth ? state.checklistNextMonth.reduce((sum, item) => sum + parseFloat(item.monto), 0) : 0;
    if (elements.checklistNextMonthLink) {
        elements.checklistNextMonthLink.textContent = `Próx. mes: ${formatCurrency(nextMonthTotal)}`;
    }
    if (elements.drawerChecklistTotal) {
        elements.drawerChecklistTotal.textContent = formatCurrency(nextMonthTotal);
    }

    let total = 0;
    let pagado = 0;
    let itemsPagados = 0;

    elements.checklistContainer.innerHTML = '';

    if (!state.checklist || state.checklist.length === 0) {
        elements.checklistContainer.innerHTML = `
            <div class="checklist-empty">
                <i data-lucide="clipboard-list"></i>
                <p>No tenés gastos proyectados cargados para este mes.</p>
            </div>
        `;
        if (window.lucide) window.lucide.createIcons();
        
        elements.checklistMetricTotal.textContent = formatCurrency(0);
        elements.checklistMetricPagado.textContent = formatCurrency(0);
        elements.checklistMetricPagadoCount.textContent = '0 de 0 ítems';
        elements.checklistMetricPendiente.textContent = formatCurrency(0);
        
        // Update drawer rendering even if active list is empty
        renderChecklistDrawer();
        return;
    }

    // Sort items by date descending (newest first) and ID descending
    const sortedChecklist = [...state.checklist].sort((a, b) => {
        const dateDiff = new Date(b.fecha) - new Date(a.fecha);
        if (dateDiff !== 0) return dateDiff;
        return b.id - a.id;
    });

    sortedChecklist.forEach(item => {
        const itemMonto = parseFloat(item.monto);
        total += itemMonto;
        if (item.completado) {
            pagado += itemMonto;
            itemsPagados++;
        }

        const card = document.createElement('div');
        card.className = `checklist-card ${item.completado ? 'completed' : ''}`;
        
        const fechaObj = new Date(item.fecha + 'T00:00:00');
        const formattedFecha = fechaObj.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' });

        const checkIcon = item.completado ? 'check-circle-2' : 'circle';

        card.innerHTML = `
            <div class="checklist-left">
                <button class="checklist-check-btn" onclick="toggleGastoChecklist(${item.id})" title="${item.completado ? 'Marcar como pendiente' : 'Marcar como completado'}">
                    <i data-lucide="${checkIcon}"></i>
                </button>
                <div class="checklist-info">
                    <div class="checklist-title-row">
                        <span class="checklist-title">${escapeHtml(item.nombre)}</span>
                        ${item.permanente ? '<span class="checklist-badge permanent"><i data-lucide="repeat"></i> Fijo</span>' : ''}
                        <span class="checklist-badge category" style="border-color: ${item.categoriaColor || 'var(--gold)'}; color: ${item.categoriaColor || 'var(--gold)'}; background-color: rgba(255,255,255,0.02)">${escapeHtml(item.categoriaNombre)}</span>
                    </div>
                    ${item.descripcion ? `<span class="checklist-desc">${escapeHtml(item.descripcion)}</span>` : ''}
                    <div class="checklist-meta">
                        <span class="checklist-meta-item"><i data-lucide="calendar"></i> ${formattedFecha}</span>
                        <span class="checklist-meta-item"><i data-lucide="wallet"></i> ${escapeHtml(item.carteraNombre)}</span>
                    </div>
                </div>
            </div>
            <div class="checklist-right">
                <span class="checklist-monto">${formatCurrency(itemMonto)}</span>
                <div class="checklist-actions">
                    <button class="checklist-action-btn" onclick="openEditGastoChecklist(${item.id})" title="Editar"><i data-lucide="edit-2"></i></button>
                    <button class="checklist-action-btn delete" onclick="triggerDeleteGastoChecklist(${item.id})" title="Eliminar"><i data-lucide="trash-2"></i></button>
                </div>
            </div>
        `;
        elements.checklistContainer.appendChild(card);
    });

    if (window.lucide) {
        window.lucide.createIcons();
    }

    // Update metrics
    const pendiente = total - pagado;
    elements.checklistMetricTotal.textContent = formatCurrency(total);
    elements.checklistMetricPagado.textContent = formatCurrency(pagado);
    elements.checklistMetricPagadoCount.textContent = `${itemsPagados} de ${state.checklist.length} ítems`;
    elements.checklistMetricPendiente.textContent = formatCurrency(pendiente);

    // Refresh drawer list
    renderChecklistDrawer();
}

window.openNewGastoChecklistModal = function(isNextMonth = false) {
    elements.gastoChecklistId.value = '';
    elements.gastoChecklistNombre.value = '';
    elements.gastoChecklistMonto.value = '';
    
    let defaultDate = new Date();
    if (isNextMonth) {
        // Set date to 1st day of next month
        defaultDate = new Date(defaultDate.getFullYear(), defaultDate.getMonth() + 1, 1);
    }
    elements.gastoChecklistFecha.value = getLocalDateString(defaultDate);
    
    elements.gastoChecklistCartera.value = '';
    elements.gastoChecklistCategoria.value = '';
    elements.gastoChecklistDescripcion.value = '';
    elements.gastoChecklistPermanente.checked = false;

    elements.modalGastoChecklistTitle.textContent = isNextMonth ? 'Nuevo Gasto Proyectado (Mes Siguiente)' : 'Nuevo Gasto Proyectado';
    openModal(elements.modalGastoChecklist);
};

window.openEditGastoChecklist = function(id) {
    const item = state.checklist.find(gc => gc.id === id) || state.checklistNextMonth.find(gc => gc.id === id);
    if (!item) return;

    elements.gastoChecklistId.value = item.id;
    elements.gastoChecklistNombre.value = item.nombre;
    elements.gastoChecklistMonto.value = item.monto;
    elements.gastoChecklistFecha.value = item.fecha;
    elements.gastoChecklistCartera.value = item.carteraId;
    elements.gastoChecklistCategoria.value = item.categoriaId;
    elements.gastoChecklistDescripcion.value = item.descripcion || '';
    elements.gastoChecklistPermanente.checked = item.permanente;

    elements.modalGastoChecklistTitle.textContent = 'Editar Gasto Proyectado';
    openModal(elements.modalGastoChecklist);
};

window.toggleGastoChecklist = async function(id) {
    try {
        const response = await apiFetch(`${API_CHECKLIST}/${id}/toggle`, {
            method: 'PATCH'
        });

        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Error al modificar el estado');
        }

        const data = await response.json();
        showToast(data.completado ? 'Gasto marcado como pagado' : 'Gasto marcado como pendiente');
        await fetchAllData(['checklist', 'transacciones', 'carteras']);
    } catch (error) {
        showToast(error.message, 'error');
    }
};

window.triggerDeleteGastoChecklist = function(id) {
    state.activeDeleteTarget = { type: 'gasto-checklist', id };
    const gc = state.checklist.find(item => item.id === id) || state.checklistNextMonth.find(item => item.id === id);
    if (!gc) return;

    elements.confirmMessage.innerHTML = `¿Estás seguro de eliminar el gasto proyectado: <strong>${escapeHtml(gc.nombre)}</strong>?<br><br>Si ya estaba pagado/completado, la transacción asociada se eliminará y el saldo de la cuenta afectada se revertirá automáticamente.`;
    openModal(elements.modalConfirm);
};

async function handleGastoChecklistSubmit(e) {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn ? submitBtn.textContent : '';

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Guardando...';
    }

    const id = elements.gastoChecklistId.value;
    const dto = {
        nombre: elements.gastoChecklistNombre.value.trim(),
        monto: parseFloat(elements.gastoChecklistMonto.value),
        fecha: elements.gastoChecklistFecha.value,
        carteraId: parseInt(elements.gastoChecklistCartera.value),
        categoriaId: parseInt(elements.gastoChecklistCategoria.value),
        descripcion: elements.gastoChecklistDescripcion.value.trim(),
        permanente: elements.gastoChecklistPermanente.checked
    };

    try {
        let response;
        if (id) {
            response = await apiFetch(`${API_CHECKLIST}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            response = await apiFetch(API_CHECKLIST, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }

        const data = await response.json();

        if (!response.ok) {
            if (data.details) {
                const errs = Object.values(data.details).join(', ');
                throw new Error(errs);
            }
            throw new Error(data.message || 'Error al guardar el gasto proyectado');
        }

        showToast(id ? 'Gasto modificado' : 'Gasto proyectado creado');
        
        elements.formGastoChecklist.reset();
        elements.gastoChecklistId.value = '';
        closeModal(elements.modalGastoChecklist);
        
        await fetchAllData(['checklist']);
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    }
}

window.openChecklistDrawer = function() {
    renderChecklistDrawer();
    elements.drawerChecklistNextMonth.classList.add('active');
};

window.closeChecklistDrawer = function() {
    elements.drawerChecklistNextMonth.classList.remove('active');
};

function renderChecklistDrawer() {
    if (!elements.drawerChecklistList) return;
    elements.drawerChecklistList.innerHTML = '';

    const nextMonthTotal = state.checklistNextMonth ? state.checklistNextMonth.reduce((sum, item) => sum + parseFloat(item.monto), 0) : 0;
    if (elements.drawerChecklistTotal) {
        elements.drawerChecklistTotal.textContent = formatCurrency(nextMonthTotal);
    }

    if (!state.checklistNextMonth || state.checklistNextMonth.length === 0) {
        elements.drawerChecklistList.innerHTML = `
            <div class="checklist-empty" style="padding: 24px 16px;">
                <i data-lucide="calendar-plus" style="width: 32px; height: 32px; margin-bottom: 8px;"></i>
                <p style="font-size: 13px;">Sin gastos proyectados para el próximo mes.</p>
            </div>
        `;
        if (window.lucide) window.lucide.createIcons();
        return;
    }

    // Sort next month checklist descending by date and ID
    const sortedNextMonth = [...state.checklistNextMonth].sort((a, b) => {
        const dateDiff = new Date(b.fecha) - new Date(a.fecha);
        if (dateDiff !== 0) return dateDiff;
        return b.id - a.id;
    });

    sortedNextMonth.forEach(item => {
        const itemMonto = parseFloat(item.monto);
        const card = document.createElement('div');
        card.className = 'checklist-card';
        
        const fechaObj = new Date(item.fecha + 'T00:00:00');
        const formattedFecha = fechaObj.toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit' });

        card.innerHTML = `
            <div class="checklist-left">
                <div style="color:var(--gold); display:flex; align-items:center; justify-content:center;">
                    <i data-lucide="calendar-clock" style="width:16px; height:16px;"></i>
                </div>
                <div class="checklist-info">
                    <div class="checklist-title-row">
                        <span class="checklist-title" style="font-size:14px;">${escapeHtml(item.nombre)}</span>
                        ${item.permanente ? '<span class="checklist-badge permanent" style="font-size:8px; padding:1px 4px; display:inline-flex; align-items:center; gap:2px;"><i data-lucide="repeat" style="width:8px; height:8px;"></i> Fijo</span>' : ''}
                    </div>
                    <div class="checklist-meta" style="font-size:11px; margin-top:2px; display:flex; gap:8px; color:var(--text-muted);">
                        <span class="checklist-meta-item" style="display:inline-flex; align-items:center; gap:2px;"><i data-lucide="calendar" style="width:10px; height:10px;"></i> ${formattedFecha}</span>
                        <span class="checklist-meta-item" style="display:inline-flex; align-items:center; gap:2px;"><i data-lucide="wallet" style="width:10px; height:10px;"></i> ${escapeHtml(item.carteraNombre)}</span>
                    </div>
                </div>
            </div>
            <div class="checklist-right" style="padding-left:8px; gap:12px; display:flex; align-items:center;">
                <span class="checklist-monto" style="font-size:15px;">${formatCurrency(itemMonto)}</span>
                <div class="checklist-actions" style="display:flex; gap:4px;">
                    <button class="checklist-action-btn" onclick="openEditGastoChecklist(${item.id})" style="padding:4px;" title="Editar"><i data-lucide="edit-2" style="width:14px; height:14px;"></i></button>
                    <button class="checklist-action-btn delete" onclick="triggerDeleteGastoChecklist(${item.id})" style="padding:4px;" title="Eliminar"><i data-lucide="trash-2" style="width:14px; height:14px;"></i></button>
                </div>
            </div>
        `;
        elements.drawerChecklistList.appendChild(card);
    });

    if (window.lucide) {
        window.lucide.createIcons();
    }
}
