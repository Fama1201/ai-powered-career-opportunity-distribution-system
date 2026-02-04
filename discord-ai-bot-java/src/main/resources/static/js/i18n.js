/**
 * i18n System - Internationalization
 * Handles language switching and translations
 */

const I18n = {
    currentLang: 'en',
    translations: {
        en: {
            profile: 'Profile', notifications: 'Notifications', logout: 'Logout',
            welcome: 'Welcome to Jobify', dashboard: 'Dashboard', jobMatches: 'Job Matches',
            myApplications: 'My Applications', cvManagement: 'CV Management', careerAdvisor: 'Career Advisor',
            applied: 'Applied', interview: 'Interview', rejected: 'Rejected',
            newJobMatches: 'New Job Matches', seeAll: 'See all',
            yourCareerAssistant: 'Your Career Assistant', ask: 'Ask...',
            fullTime: 'Full-Time', partTime: 'Part-Time', internship: 'Internship',
            apply: 'Apply', save: 'Save', message: 'Message',
            jobDetails: 'Job Details', company: 'Company', location: 'Location',
            salary: 'Salary', deadline: 'Deadline', description: 'Description',
            requirements: 'Requirements', benefits: 'Benefits', matchScore: 'Match Score',
            loading: 'Loading...', error: 'Error', success: 'Success',
            applySuccess: 'Application submitted successfully!', saveSuccess: 'Job saved successfully!',
            confirmLogout: 'Are you sure you want to logout?', yes: 'Yes', no: 'No',
            noMatches: 'No job matches found', noApplications: 'No applications found',
            noNotifications: 'No notifications', markAllRead: 'Mark all as read',
            askPlaceholder: 'Ask me about skills, CV, interviews...', send: 'Send',
            close: 'Close', cancel: 'Cancel', confirm: 'Confirm'
        },
        cs: {
            profile: 'Profil', notifications: 'Notifikace', logout: 'Odhlásit se',
            welcome: 'Vítejte v Jobify', dashboard: 'Nástěnka', jobMatches: 'Shody práce',
            myApplications: 'Moje přihlášky', cvManagement: 'Správa CV', careerAdvisor: 'Kariérní poradce',
            applied: 'Přihlášeno', interview: 'Pohovor', rejected: 'Odmítnuto',
            newJobMatches: 'Nové shody práce', seeAll: 'Zobrazit vše',
            yourCareerAssistant: 'Váš kariérní asistent', ask: 'Zeptat se...',
            fullTime: 'Plný úvazek', partTime: 'Částečný úvazek', internship: 'Stáž',
            apply: 'Přihlásit se', save: 'Uložit', message: 'Zpráva',
            jobDetails: 'Detaily práce', company: 'Společnost', location: 'Místo',
            salary: 'Plat', deadline: 'Termín', description: 'Popis',
            requirements: 'Požadavky', benefits: 'Výhody', matchScore: 'Skóre shody',
            loading: 'Načítání...', error: 'Chyba', success: 'Úspěch',
            applySuccess: 'Přihláška byla úspěšně odeslána!', saveSuccess: 'Práce byla úspěšně uložena!',
            confirmLogout: 'Opravdu se chcete odhlásit?', yes: 'Ano', no: 'Ne',
            noMatches: 'Nebyly nalezeny žádné shody', noApplications: 'Nebyly nalezeny žádné přihlášky',
            noNotifications: 'Žádné notifikace', markAllRead: 'Označit vše jako přečtené',
            askPlaceholder: 'Zeptejte se mě na dovednosti, CV, pohovory...', send: 'Odeslat',
            close: 'Zavřít', cancel: 'Zrušit', confirm: 'Potvrdit'
        },
        tr: {
            profile: 'Profil', notifications: 'Bildirimler', logout: 'Çıkış Yap',
            welcome: 'Jobify\'ye Hoş Geldiniz', dashboard: 'Kontrol Paneli', jobMatches: 'İş Eşleşmeleri',
            myApplications: 'Başvurularım', cvManagement: 'CV Yönetimi', careerAdvisor: 'Kariyer Danışmanı',
            applied: 'Başvuruldu', interview: 'Mülakat', rejected: 'Reddedildi',
            newJobMatches: 'Yeni İş Eşleşmeleri', seeAll: 'Tümünü Gör',
            yourCareerAssistant: 'Kariyer Asistanınız', ask: 'Sor...',
            fullTime: 'Tam Zamanlı', partTime: 'Yarı Zamanlı', internship: 'Staj',
            apply: 'Başvur', save: 'Kaydet', message: 'Mesaj',
            jobDetails: 'İş Detayları', company: 'Şirket', location: 'Konum',
            salary: 'Maaş', deadline: 'Son Tarih', description: 'Açıklama',
            requirements: 'Gereksinimler', benefits: 'Avantajlar', matchScore: 'Eşleşme Skoru',
            loading: 'Yükleniyor...', error: 'Hata', success: 'Başarılı',
            applySuccess: 'Başvuru başarıyla gönderildi!', saveSuccess: 'İş başarıyla kaydedildi!',
            confirmLogout: 'Çıkış yapmak istediğinizden emin misiniz?', yes: 'Evet', no: 'Hayır',
            noMatches: 'İş eşleşmesi bulunamadı', noApplications: 'Başvuru bulunamadı',
            noNotifications: 'Bildirim yok', markAllRead: 'Tümünü okundu işaretle',
            askPlaceholder: 'Bana beceriler, CV, mülakatlar hakkında sorun...', send: 'Gönder',
            close: 'Kapat', cancel: 'İptal', confirm: 'Onayla'
        },
        es: {
            profile: 'Perfil', notifications: 'Notificaciones', logout: 'Cerrar sesión',
            welcome: 'Bienvenido a Jobify', dashboard: 'Panel', jobMatches: 'Coincidencias de trabajo',
            myApplications: 'Mis solicitudes', cvManagement: 'Gestión de CV', careerAdvisor: 'Asesor de carrera',
            applied: 'Aplicado', interview: 'Entrevista', rejected: 'Rechazado',
            newJobMatches: 'Nuevas coincidencias', seeAll: 'Ver todo',
            yourCareerAssistant: 'Tu asistente de carrera', ask: 'Preguntar...',
            fullTime: 'Tiempo completo', partTime: 'Medio tiempo', internship: 'Prácticas',
            apply: 'Aplicar', save: 'Guardar', message: 'Mensaje',
            jobDetails: 'Detalles del trabajo', company: 'Empresa', location: 'Ubicación',
            salary: 'Salario', deadline: 'Fecha límite', description: 'Descripción',
            requirements: 'Requisitos', benefits: 'Beneficios', matchScore: 'Puntuación de coincidencia',
            loading: 'Cargando...', error: 'Error', success: 'Éxito',
            applySuccess: '¡Solicitud enviada con éxito!', saveSuccess: '¡Trabajo guardado con éxito!',
            confirmLogout: '¿Estás seguro de que quieres cerrar sesión?', yes: 'Sí', no: 'No',
            noMatches: 'No se encontraron coincidencias', noApplications: 'No se encontraron solicitudes',
            noNotifications: 'Sin notificaciones', markAllRead: 'Marcar todo como leído',
            askPlaceholder: 'Pregúntame sobre habilidades, CV, entrevistas...', send: 'Enviar',
            close: 'Cerrar', cancel: 'Cancelar', confirm: 'Confirmar'
        }
    },

    /**
     * Initialize i18n system
     */
    init() {
        const savedLang = localStorage.getItem('language') || 'en';
        this.setLanguage(savedLang);
    },

    /**
     * Set current language
     */
    setLanguage(lang) {
        if (!this.translations[lang]) lang = 'en';
        this.currentLang = lang;
        localStorage.setItem('language', lang);
        this.applyTranslations();
    },

    /**
     * Get translation
     */
    t(key) {
        return this.translations[this.currentLang]?.[key] || this.translations.en[key] || key;
    },

    /**
     * Apply translations to DOM
     */
    applyTranslations() {
        document.querySelectorAll('[data-i18n]').forEach(element => {
            const key = element.getAttribute('data-i18n');
            element.textContent = this.t(key);
        });
    }
};

// Initialize on load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => I18n.init());
} else {
    I18n.init();
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = I18n;
}

