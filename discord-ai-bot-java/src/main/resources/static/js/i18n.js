/**
 * i18n System - Internationalization
 * Handles language switching and translations
 * Supports: English (en), Czech (cs), Turkish (tr), Spanish (es)
 */

const I18n = {
    currentLang: 'en',
    translations: {
        en: {
            // Common
            profile: 'Profile',
            notifications: 'Notifications',
            logout: 'Logout',
            loading: 'Loading...',
            error: 'Error',
            success: 'Success',
            close: 'Close',
            cancel: 'Cancel',
            confirm: 'Confirm',
            yes: 'Yes',
            no: 'No',
            save: 'Save',
            edit: 'Edit',
            delete: 'Delete',
            search: 'Search',
            filter: 'Filter',
            apply: 'Apply',
            view: 'View',
            update: 'Update',
            add: 'Add',
            remove: 'Remove',
            submit: 'Submit',
            send: 'Send',
            back: 'Back',
            next: 'Next',
            previous: 'Previous',
            select: 'Select',
            all: 'All',
            none: 'None',
            clear: 'Clear',
            refresh: 'Refresh',
            download: 'Download',
            upload: 'Upload',
            create: 'Create',
            manage: 'Manage',
            settings: 'Settings',
            help: 'Help',
            about: 'About',
            
            // Navigation
            dashboard: 'Dashboard',
            jobMatches: 'Job Matches',
            myApplications: 'My Applications',
            cvManagement: 'CV Management',
            careerAdvisor: 'Career Advisor',
            codePractice: 'Code Practice',
            jobOpenings: 'Job Openings',
            candidates: 'Candidates',
            hires: 'Hires',
            analytics: 'Analytics',
            postJob: 'Post Job',
            
            // Dashboard
            welcome: 'Welcome to Jobify',
            newJobMatches: 'New Job Matches',
            seeAll: 'See all',
            yourCareerAssistant: 'Your Career Assistant',
            ask: 'Ask...',
            askPlaceholder: 'Ask me about skills, CV, interviews...',
            
            // Job Status
            applied: 'Applied',
            interview: 'Interview',
            rejected: 'Rejected',
            shortlisted: 'Shortlisted',
            hired: 'Hired',
            withdrawn: 'Withdrawn',
            open: 'Open',
            closed: 'Closed',
            archived: 'Archived',
            
            // Job Types
            fullTime: 'Full-Time',
            partTime: 'Part-Time',
            internship: 'Internship',
            contract: 'Contract',
            freelance: 'Freelance',
            
            // Job Details
            jobDetails: 'Job Details',
            company: 'Company',
            location: 'Location',
            salary: 'Salary',
            deadline: 'Deadline',
            description: 'Description',
            requirements: 'Requirements',
            benefits: 'Benefits',
            matchScore: 'Match Score',
            message: 'Message',
            
            // Applications
            applicationDetails: 'Application Details',
            applicationInformation: 'Application Information',
            appliedDate: 'Applied Date',
            status: 'Status',
            viewDetails: 'View Details',
            viewFullCv: 'View Full CV',
            updateStatus: 'Update Status',
            addNote: 'Add Note',
            note: 'Note',
            notes: 'Notes',
            newStatus: 'New Status',
            selectStatus: 'Select status',
            updateApplicationStatus: 'Update Application Status',
            updating: 'Updating...',
            saving: 'Saving...',
            addYourNotes: 'Add your notes about this candidate...',
            
            // Candidates
            candidates: 'Candidates',
            reviewAndManage: 'Review and manage job applications',
            selectJob: 'Select Job',
            allJobs: 'All Jobs',
            allStatus: 'All Status',
            searchByNameOrEmail: 'Search by name or email...',
            loadingCandidates: 'Loading candidates...',
            noCandidatesFound: 'No Candidates Found',
            selectJobToView: 'Select a job to view candidates or adjust your filters.',
            noCandidatesMatch: 'No candidates match your filters. Try adjusting your search criteria.',
            noCandidatesApplied: 'No candidates have applied for this job yet.',
            candidateDetails: 'Candidate Details',
            unknown: 'Unknown',
            noEmail: 'No email',
            skills: 'Skills',
            careerInterest: 'Career Interest',
            cvPreview: 'CV Preview',
            applicant: 'Applicant',
            applicants: 'Applicants',
            
            // Job Openings
            manageJobPostings: 'Manage your job postings and track applications',
            postNewJob: 'Post New Job',
            editJob: 'Edit Job',
            closeApplication: 'Close Application',
            jobTitle: 'Job Title',
            jobType: 'Job Type',
            workLocation: 'Work Location',
            wageSalary: 'Wage/Salary',
            applicationDeadline: 'Application Deadline',
            technicalRequirements: 'Technical Requirements',
            formalRequirements: 'Formal Requirements',
            contactPerson: 'Contact Person',
            applicationUrl: 'Application URL',
            basicInformation: 'Basic Information',
            
            // Code Practice
            codePractice: 'Code Practice',
            createNewAssignment: 'Create New Assignment',
            programmingLanguage: 'Programming Language',
            difficultyLevel: 'Difficulty Level',
            category: 'Category (Optional)',
            generateAssignments: 'Generate Assignments',
            yourAssignments: 'Your Assignments',
            completedAssignments: 'Completed Assignments',
            refreshAssignments: 'Refresh Assignments',
            noAssignments: 'No assignments found',
            assignmentDetails: 'Assignment Details',
            submitCode: 'Submit Code',
            clearCode: 'Clear Code',
            estimatedTime: 'Estimated Time',
            created: 'Created',
            submitted: 'Submitted',
            reviewed: 'Reviewed',
            feedback: 'Feedback',
            summary: 'Summary',
            issues: 'Issues',
            suggestions: 'Suggestions',
            
            // Messages
            applySuccess: 'Application submitted successfully!',
            saveSuccess: 'Job saved successfully!',
            confirmLogout: 'Are you sure you want to logout?',
            noMatches: 'No job matches found',
            noApplications: 'No applications found',
            noNotifications: 'No notifications',
            markAllRead: 'Mark all as read',
            cvDownloaded: 'CV downloaded successfully!',
            cvNotAvailable: 'CV not available for this candidate.',
            statusUpdated: 'Status updated successfully!',
            noteAdded: 'Note added successfully!',
            jobUpdated: 'Job updated successfully!',
            jobPosted: 'Job posted successfully!',
            jobDeleted: 'Job deleted successfully!',
            applicationClosed: 'Application closed successfully!',
            
            // Student Role
            student: 'Student',
            hrProfessional: 'HR Professional',
            
            // Common Labels
            name: 'Name',
            email: 'Email',
            date: 'Date',
            time: 'Time',
            actions: 'Actions',
            options: 'Options',
            more: 'More',
            less: 'Less',
            showMore: 'Show More',
            showLess: 'Show Less',
            
            // Empty States
            noData: 'No data available',
            tryAgain: 'Try again',
            reload: 'Reload',
            
            // Language Names
            english: 'English',
            czech: 'Čeština',
            turkish: 'Türkçe',
            spanish: 'Español'
        },
        cs: {
            // Common
            profile: 'Profil',
            notifications: 'Notifikace',
            logout: 'Odhlásit se',
            loading: 'Načítání...',
            error: 'Chyba',
            success: 'Úspěch',
            close: 'Zavřít',
            cancel: 'Zrušit',
            confirm: 'Potvrdit',
            yes: 'Ano',
            no: 'Ne',
            save: 'Uložit',
            edit: 'Upravit',
            delete: 'Smazat',
            search: 'Hledat',
            filter: 'Filtr',
            apply: 'Přihlásit se',
            view: 'Zobrazit',
            update: 'Aktualizovat',
            add: 'Přidat',
            remove: 'Odebrat',
            submit: 'Odeslat',
            send: 'Odeslat',
            back: 'Zpět',
            next: 'Další',
            previous: 'Předchozí',
            select: 'Vybrat',
            all: 'Vše',
            none: 'Žádné',
            clear: 'Vymazat',
            refresh: 'Obnovit',
            download: 'Stáhnout',
            upload: 'Nahrát',
            create: 'Vytvořit',
            manage: 'Spravovat',
            settings: 'Nastavení',
            help: 'Nápověda',
            about: 'O aplikaci',
            
            // Navigation
            dashboard: 'Nástěnka',
            jobMatches: 'Shody práce',
            myApplications: 'Moje přihlášky',
            cvManagement: 'Správa CV',
            careerAdvisor: 'Kariérní poradce',
            codePractice: 'Cvičení kódu',
            jobOpenings: 'Volná místa',
            candidates: 'Kandidáti',
            hires: 'Nábor',
            analytics: 'Analytika',
            postJob: 'Zveřejnit práci',
            
            // Dashboard
            welcome: 'Vítejte v Jobify',
            newJobMatches: 'Nové shody práce',
            seeAll: 'Zobrazit vše',
            yourCareerAssistant: 'Váš kariérní asistent',
            ask: 'Zeptat se...',
            askPlaceholder: 'Zeptejte se mě na dovednosti, CV, pohovory...',
            
            // Job Status
            applied: 'Přihlášeno',
            interview: 'Pohovor',
            rejected: 'Odmítnuto',
            shortlisted: 'V užším výběru',
            hired: 'Přijato',
            withdrawn: 'Staženo',
            open: 'Otevřeno',
            closed: 'Zavřeno',
            archived: 'Archivováno',
            
            // Job Types
            fullTime: 'Plný úvazek',
            partTime: 'Částečný úvazek',
            internship: 'Stáž',
            contract: 'Smlouva',
            freelance: 'Na volné noze',
            
            // Job Details
            jobDetails: 'Detaily práce',
            company: 'Společnost',
            location: 'Místo',
            salary: 'Plat',
            deadline: 'Termín',
            description: 'Popis',
            requirements: 'Požadavky',
            benefits: 'Výhody',
            matchScore: 'Skóre shody',
            message: 'Zpráva',
            
            // Applications
            applicationDetails: 'Detaily přihlášky',
            applicationInformation: 'Informace o přihlášce',
            appliedDate: 'Datum přihlášky',
            status: 'Stav',
            viewDetails: 'Zobrazit detaily',
            viewFullCv: 'Zobrazit celé CV',
            updateStatus: 'Aktualizovat stav',
            addNote: 'Přidat poznámku',
            note: 'Poznámka',
            notes: 'Poznámky',
            newStatus: 'Nový stav',
            selectStatus: 'Vyberte stav',
            updateApplicationStatus: 'Aktualizovat stav přihlášky',
            updating: 'Aktualizování...',
            saving: 'Ukládání...',
            addYourNotes: 'Přidejte své poznámky o tomto kandidátovi...',
            
            // Candidates
            candidates: 'Kandidáti',
            reviewAndManage: 'Prohlížet a spravovat pracovní přihlášky',
            selectJob: 'Vybrat práci',
            allJobs: 'Všechny práce',
            allStatus: 'Všechny stavy',
            searchByNameOrEmail: 'Hledat podle jména nebo e-mailu...',
            loadingCandidates: 'Načítání kandidátů...',
            noCandidatesFound: 'Nebyli nalezeni žádní kandidáti',
            selectJobToView: 'Vyberte práci pro zobrazení kandidátů nebo upravte filtry.',
            noCandidatesMatch: 'Žádní kandidáti neodpovídají vašim filtrům. Zkuste upravit kritéria vyhledávání.',
            noCandidatesApplied: 'Zatím se nikdo na tuto práci nepřihlásil.',
            candidateDetails: 'Detaily kandidáta',
            unknown: 'Neznámý',
            noEmail: 'Bez e-mailu',
            skills: 'Dovednosti',
            careerInterest: 'Kariérní zájem',
            cvPreview: 'Náhled CV',
            applicant: 'Přihlášený',
            applicants: 'Přihlášení',
            
            // Job Openings
            manageJobPostings: 'Spravujte své pracovní nabídky a sledujte přihlášky',
            postNewJob: 'Zveřejnit novou práci',
            editJob: 'Upravit práci',
            closeApplication: 'Zavřít přihlášku',
            jobTitle: 'Název práce',
            jobType: 'Typ práce',
            workLocation: 'Místo práce',
            wageSalary: 'Mzda/Plat',
            applicationDeadline: 'Termín přihlášky',
            technicalRequirements: 'Technické požadavky',
            formalRequirements: 'Formální požadavky',
            contactPerson: 'Kontaktní osoba',
            applicationUrl: 'URL přihlášky',
            basicInformation: 'Základní informace',
            
            // Code Practice
            codePractice: 'Cvičení kódu',
            createNewAssignment: 'Vytvořit nové zadání',
            programmingLanguage: 'Programovací jazyk',
            difficultyLevel: 'Úroveň obtížnosti',
            category: 'Kategorie (volitelné)',
            generateAssignments: 'Generovat zadání',
            yourAssignments: 'Vaše zadání',
            completedAssignments: 'Dokončená zadání',
            refreshAssignments: 'Obnovit zadání',
            noAssignments: 'Nebyla nalezena žádná zadání',
            assignmentDetails: 'Detaily zadání',
            submitCode: 'Odeslat kód',
            clearCode: 'Vymazat kód',
            estimatedTime: 'Odhadovaný čas',
            created: 'Vytvořeno',
            submitted: 'Odesláno',
            reviewed: 'Zkontrolováno',
            feedback: 'Zpětná vazba',
            summary: 'Shrnutí',
            issues: 'Problémy',
            suggestions: 'Návrhy',
            
            // Messages
            applySuccess: 'Přihláška byla úspěšně odeslána!',
            saveSuccess: 'Práce byla úspěšně uložena!',
            confirmLogout: 'Opravdu se chcete odhlásit?',
            noMatches: 'Nebyly nalezeny žádné shody',
            noApplications: 'Nebyly nalezeny žádné přihlášky',
            noNotifications: 'Žádné notifikace',
            markAllRead: 'Označit vše jako přečtené',
            cvDownloaded: 'CV bylo úspěšně staženo!',
            cvNotAvailable: 'CV není k dispozici pro tohoto kandidáta.',
            statusUpdated: 'Stav byl úspěšně aktualizován!',
            noteAdded: 'Poznámka byla úspěšně přidána!',
            jobUpdated: 'Práce byla úspěšně aktualizována!',
            jobPosted: 'Práce byla úspěšně zveřejněna!',
            jobDeleted: 'Práce byla úspěšně smazána!',
            applicationClosed: 'Přihláška byla úspěšně uzavřena!',
            
            // Student Role
            student: 'Student',
            hrProfessional: 'HR Profesionál',
            
            // Common Labels
            name: 'Jméno',
            email: 'E-mail',
            date: 'Datum',
            time: 'Čas',
            actions: 'Akce',
            options: 'Možnosti',
            more: 'Více',
            less: 'Méně',
            showMore: 'Zobrazit více',
            showLess: 'Zobrazit méně',
            
            // Empty States
            noData: 'Žádná data k dispozici',
            tryAgain: 'Zkusit znovu',
            reload: 'Znovu načíst',
            
            // Language Names
            english: 'English',
            czech: 'Čeština',
            turkish: 'Türkçe',
            spanish: 'Español'
        },
        tr: {
            // Common
            profile: 'Profil',
            notifications: 'Bildirimler',
            logout: 'Çıkış Yap',
            loading: 'Yükleniyor...',
            error: 'Hata',
            success: 'Başarılı',
            close: 'Kapat',
            cancel: 'İptal',
            confirm: 'Onayla',
            yes: 'Evet',
            no: 'Hayır',
            save: 'Kaydet',
            edit: 'Düzenle',
            delete: 'Sil',
            search: 'Ara',
            filter: 'Filtre',
            apply: 'Başvur',
            view: 'Görüntüle',
            update: 'Güncelle',
            add: 'Ekle',
            remove: 'Kaldır',
            submit: 'Gönder',
            send: 'Gönder',
            back: 'Geri',
            next: 'İleri',
            previous: 'Önceki',
            select: 'Seç',
            all: 'Tümü',
            none: 'Hiçbiri',
            clear: 'Temizle',
            refresh: 'Yenile',
            download: 'İndir',
            upload: 'Yükle',
            create: 'Oluştur',
            manage: 'Yönet',
            settings: 'Ayarlar',
            help: 'Yardım',
            about: 'Hakkında',
            
            // Navigation
            dashboard: 'Kontrol Paneli',
            jobMatches: 'İş Eşleşmeleri',
            myApplications: 'Başvurularım',
            cvManagement: 'CV Yönetimi',
            careerAdvisor: 'Kariyer Danışmanı',
            codePractice: 'Kod Pratiği',
            jobOpenings: 'İş İlanları',
            candidates: 'Adaylar',
            hires: 'İşe Alımlar',
            analytics: 'Analitik',
            postJob: 'İş İlanı Yayınla',
            
            // Dashboard
            welcome: 'Jobify\'ye Hoş Geldiniz',
            newJobMatches: 'Yeni İş Eşleşmeleri',
            seeAll: 'Tümünü Gör',
            yourCareerAssistant: 'Kariyer Asistanınız',
            ask: 'Sor...',
            askPlaceholder: 'Bana beceriler, CV, mülakatlar hakkında sorun...',
            
            // Job Status
            applied: 'Başvuruldu',
            interview: 'Mülakat',
            rejected: 'Reddedildi',
            shortlisted: 'Ön Liste',
            hired: 'İşe Alındı',
            withdrawn: 'Geri Çekildi',
            open: 'Açık',
            closed: 'Kapalı',
            archived: 'Arşivlendi',
            
            // Job Types
            fullTime: 'Tam Zamanlı',
            partTime: 'Yarı Zamanlı',
            internship: 'Staj',
            contract: 'Sözleşmeli',
            freelance: 'Serbest',
            
            // Job Details
            jobDetails: 'İş Detayları',
            company: 'Şirket',
            location: 'Konum',
            salary: 'Maaş',
            deadline: 'Son Tarih',
            description: 'Açıklama',
            requirements: 'Gereksinimler',
            benefits: 'Avantajlar',
            matchScore: 'Eşleşme Skoru',
            message: 'Mesaj',
            
            // Applications
            applicationDetails: 'Başvuru Detayları',
            applicationInformation: 'Başvuru Bilgileri',
            appliedDate: 'Başvuru Tarihi',
            status: 'Durum',
            viewDetails: 'Detayları Görüntüle',
            viewFullCv: 'Tam CV\'yi Görüntüle',
            updateStatus: 'Durumu Güncelle',
            addNote: 'Not Ekle',
            note: 'Not',
            notes: 'Notlar',
            newStatus: 'Yeni Durum',
            selectStatus: 'Durum seçin',
            updateApplicationStatus: 'Başvuru Durumunu Güncelle',
            updating: 'Güncelleniyor...',
            saving: 'Kaydediliyor...',
            addYourNotes: 'Bu aday hakkında notlarınızı ekleyin...',
            
            // Candidates
            candidates: 'Adaylar',
            reviewAndManage: 'İş başvurularını inceleyin ve yönetin',
            selectJob: 'İş Seç',
            allJobs: 'Tüm İşler',
            allStatus: 'Tüm Durumlar',
            searchByNameOrEmail: 'İsim veya e-posta ile ara...',
            loadingCandidates: 'Adaylar yükleniyor...',
            noCandidatesFound: 'Aday Bulunamadı',
            selectJobToView: 'Adayları görüntülemek için bir iş seçin veya filtrelerinizi ayarlayın.',
            noCandidatesMatch: 'Hiçbir aday filtrelerinize uymuyor. Arama kriterlerinizi ayarlamayı deneyin.',
            noCandidatesApplied: 'Henüz hiç kimse bu işe başvurmadı.',
            candidateDetails: 'Aday Detayları',
            unknown: 'Bilinmiyor',
            noEmail: 'E-posta yok',
            skills: 'Beceriler',
            careerInterest: 'Kariyer İlgisi',
            cvPreview: 'CV Önizleme',
            applicant: 'Başvuran',
            applicants: 'Başvuranlar',
            
            // Job Openings
            manageJobPostings: 'İş ilanlarınızı yönetin ve başvuruları takip edin',
            postNewJob: 'Yeni İş İlanı Yayınla',
            editJob: 'İşi Düzenle',
            closeApplication: 'Başvuruyu Kapat',
            jobTitle: 'İş Unvanı',
            jobType: 'İş Türü',
            workLocation: 'Çalışma Yeri',
            wageSalary: 'Ücret/Maaş',
            applicationDeadline: 'Başvuru Son Tarihi',
            technicalRequirements: 'Teknik Gereksinimler',
            formalRequirements: 'Formal Gereksinimler',
            contactPerson: 'İletişim Kişisi',
            applicationUrl: 'Başvuru URL\'si',
            basicInformation: 'Temel Bilgiler',
            
            // Code Practice
            codePractice: 'Kod Pratiği',
            createNewAssignment: 'Yeni Görev Oluştur',
            programmingLanguage: 'Programlama Dili',
            difficultyLevel: 'Zorluk Seviyesi',
            category: 'Kategori (İsteğe Bağlı)',
            generateAssignments: 'Görevler Oluştur',
            yourAssignments: 'Görevleriniz',
            completedAssignments: 'Tamamlanan Görevler',
            refreshAssignments: 'Görevleri Yenile',
            noAssignments: 'Görev bulunamadı',
            assignmentDetails: 'Görev Detayları',
            submitCode: 'Kodu Gönder',
            clearCode: 'Kodu Temizle',
            estimatedTime: 'Tahmini Süre',
            created: 'Oluşturuldu',
            submitted: 'Gönderildi',
            reviewed: 'İncelendi',
            feedback: 'Geri Bildirim',
            summary: 'Özet',
            issues: 'Sorunlar',
            suggestions: 'Öneriler',
            
            // Messages
            applySuccess: 'Başvuru başarıyla gönderildi!',
            saveSuccess: 'İş başarıyla kaydedildi!',
            confirmLogout: 'Çıkış yapmak istediğinizden emin misiniz?',
            noMatches: 'İş eşleşmesi bulunamadı',
            noApplications: 'Başvuru bulunamadı',
            noNotifications: 'Bildirim yok',
            markAllRead: 'Tümünü okundu işaretle',
            cvDownloaded: 'CV başarıyla indirildi!',
            cvNotAvailable: 'Bu aday için CV mevcut değil.',
            statusUpdated: 'Durum başarıyla güncellendi!',
            noteAdded: 'Not başarıyla eklendi!',
            jobUpdated: 'İş başarıyla güncellendi!',
            jobPosted: 'İş başarıyla yayınlandı!',
            jobDeleted: 'İş başarıyla silindi!',
            applicationClosed: 'Başvuru başarıyla kapatıldı!',
            
            // Student Role
            student: 'Öğrenci',
            hrProfessional: 'İK Profesyoneli',
            
            // Common Labels
            name: 'İsim',
            email: 'E-posta',
            date: 'Tarih',
            time: 'Saat',
            actions: 'İşlemler',
            options: 'Seçenekler',
            more: 'Daha Fazla',
            less: 'Daha Az',
            showMore: 'Daha Fazla Göster',
            showLess: 'Daha Az Göster',
            
            // Empty States
            noData: 'Veri bulunmuyor',
            tryAgain: 'Tekrar dene',
            reload: 'Yeniden yükle',
            
            // Language Names
            english: 'English',
            czech: 'Čeština',
            turkish: 'Türkçe',
            spanish: 'Español'
        },
        es: {
            // Common
            profile: 'Perfil',
            notifications: 'Notificaciones',
            logout: 'Cerrar sesión',
            loading: 'Cargando...',
            error: 'Error',
            success: 'Éxito',
            close: 'Cerrar',
            cancel: 'Cancelar',
            confirm: 'Confirmar',
            yes: 'Sí',
            no: 'No',
            save: 'Guardar',
            edit: 'Editar',
            delete: 'Eliminar',
            search: 'Buscar',
            filter: 'Filtrar',
            apply: 'Aplicar',
            view: 'Ver',
            update: 'Actualizar',
            add: 'Añadir',
            remove: 'Eliminar',
            submit: 'Enviar',
            send: 'Enviar',
            back: 'Atrás',
            next: 'Siguiente',
            previous: 'Anterior',
            select: 'Seleccionar',
            all: 'Todo',
            none: 'Ninguno',
            clear: 'Limpiar',
            refresh: 'Actualizar',
            download: 'Descargar',
            upload: 'Subir',
            create: 'Crear',
            manage: 'Gestionar',
            settings: 'Configuración',
            help: 'Ayuda',
            about: 'Acerca de',
            
            // Navigation
            dashboard: 'Panel',
            jobMatches: 'Coincidencias de trabajo',
            myApplications: 'Mis solicitudes',
            cvManagement: 'Gestión de CV',
            careerAdvisor: 'Asesor de carrera',
            codePractice: 'Práctica de código',
            jobOpenings: 'Ofertas de trabajo',
            candidates: 'Candidatos',
            hires: 'Contrataciones',
            analytics: 'Analítica',
            postJob: 'Publicar trabajo',
            
            // Dashboard
            welcome: 'Bienvenido a Jobify',
            newJobMatches: 'Nuevas coincidencias',
            seeAll: 'Ver todo',
            yourCareerAssistant: 'Tu asistente de carrera',
            ask: 'Preguntar...',
            askPlaceholder: 'Pregúntame sobre habilidades, CV, entrevistas...',
            
            // Job Status
            applied: 'Aplicado',
            interview: 'Entrevista',
            rejected: 'Rechazado',
            shortlisted: 'Preseleccionado',
            hired: 'Contratado',
            withdrawn: 'Retirado',
            open: 'Abierto',
            closed: 'Cerrado',
            archived: 'Archivado',
            
            // Job Types
            fullTime: 'Tiempo completo',
            partTime: 'Medio tiempo',
            internship: 'Prácticas',
            contract: 'Contrato',
            freelance: 'Freelance',
            
            // Job Details
            jobDetails: 'Detalles del trabajo',
            company: 'Empresa',
            location: 'Ubicación',
            salary: 'Salario',
            deadline: 'Fecha límite',
            description: 'Descripción',
            requirements: 'Requisitos',
            benefits: 'Beneficios',
            matchScore: 'Puntuación de coincidencia',
            message: 'Mensaje',
            
            // Applications
            applicationDetails: 'Detalles de la solicitud',
            applicationInformation: 'Información de la solicitud',
            appliedDate: 'Fecha de solicitud',
            status: 'Estado',
            viewDetails: 'Ver detalles',
            viewFullCv: 'Ver CV completo',
            updateStatus: 'Actualizar estado',
            addNote: 'Añadir nota',
            note: 'Nota',
            notes: 'Notas',
            newStatus: 'Nuevo estado',
            selectStatus: 'Seleccionar estado',
            updateApplicationStatus: 'Actualizar estado de la solicitud',
            updating: 'Actualizando...',
            saving: 'Guardando...',
            addYourNotes: 'Añade tus notas sobre este candidato...',
            
            // Candidates
            candidates: 'Candidatos',
            reviewAndManage: 'Revisar y gestionar solicitudes de trabajo',
            selectJob: 'Seleccionar trabajo',
            allJobs: 'Todos los trabajos',
            allStatus: 'Todos los estados',
            searchByNameOrEmail: 'Buscar por nombre o correo...',
            loadingCandidates: 'Cargando candidatos...',
            noCandidatesFound: 'No se encontraron candidatos',
            selectJobToView: 'Selecciona un trabajo para ver candidatos o ajusta tus filtros.',
            noCandidatesMatch: 'Ningún candidato coincide con tus filtros. Intenta ajustar tus criterios de búsqueda.',
            noCandidatesApplied: 'Nadie ha solicitado este trabajo todavía.',
            candidateDetails: 'Detalles del candidato',
            unknown: 'Desconocido',
            noEmail: 'Sin correo',
            skills: 'Habilidades',
            careerInterest: 'Interés profesional',
            cvPreview: 'Vista previa del CV',
            applicant: 'Solicitante',
            applicants: 'Solicitantes',
            
            // Job Openings
            manageJobPostings: 'Gestiona tus ofertas de trabajo y rastrea solicitudes',
            postNewJob: 'Publicar nuevo trabajo',
            editJob: 'Editar trabajo',
            closeApplication: 'Cerrar solicitud',
            jobTitle: 'Título del trabajo',
            jobType: 'Tipo de trabajo',
            workLocation: 'Ubicación de trabajo',
            wageSalary: 'Salario/Sueldo',
            applicationDeadline: 'Fecha límite de solicitud',
            technicalRequirements: 'Requisitos técnicos',
            formalRequirements: 'Requisitos formales',
            contactPerson: 'Persona de contacto',
            applicationUrl: 'URL de solicitud',
            basicInformation: 'Información básica',
            
            // Code Practice
            codePractice: 'Práctica de código',
            createNewAssignment: 'Crear nueva tarea',
            programmingLanguage: 'Lenguaje de programación',
            difficultyLevel: 'Nivel de dificultad',
            category: 'Categoría (Opcional)',
            generateAssignments: 'Generar tareas',
            yourAssignments: 'Tus tareas',
            completedAssignments: 'Tareas completadas',
            refreshAssignments: 'Actualizar tareas',
            noAssignments: 'No se encontraron tareas',
            assignmentDetails: 'Detalles de la tarea',
            submitCode: 'Enviar código',
            clearCode: 'Limpiar código',
            estimatedTime: 'Tiempo estimado',
            created: 'Creado',
            submitted: 'Enviado',
            reviewed: 'Revisado',
            feedback: 'Retroalimentación',
            summary: 'Resumen',
            issues: 'Problemas',
            suggestions: 'Sugerencias',
            
            // Messages
            applySuccess: '¡Solicitud enviada con éxito!',
            saveSuccess: '¡Trabajo guardado con éxito!',
            confirmLogout: '¿Estás seguro de que quieres cerrar sesión?',
            noMatches: 'No se encontraron coincidencias',
            noApplications: 'No se encontraron solicitudes',
            noNotifications: 'Sin notificaciones',
            markAllRead: 'Marcar todo como leído',
            cvDownloaded: '¡CV descargado con éxito!',
            cvNotAvailable: 'CV no disponible para este candidato.',
            statusUpdated: '¡Estado actualizado con éxito!',
            noteAdded: '¡Nota añadida con éxito!',
            jobUpdated: '¡Trabajo actualizado con éxito!',
            jobPosted: '¡Trabajo publicado con éxito!',
            jobDeleted: '¡Trabajo eliminado con éxito!',
            applicationClosed: '¡Solicitud cerrada con éxito!',
            
            // Student Role
            student: 'Estudiante',
            hrProfessional: 'Profesional de RRHH',
            
            // Common Labels
            name: 'Nombre',
            email: 'Correo',
            date: 'Fecha',
            time: 'Hora',
            actions: 'Acciones',
            options: 'Opciones',
            more: 'Más',
            less: 'Menos',
            showMore: 'Mostrar más',
            showLess: 'Mostrar menos',
            
            // Empty States
            noData: 'No hay datos disponibles',
            tryAgain: 'Intentar de nuevo',
            reload: 'Recargar',
            
            // Language Names
            english: 'English',
            czech: 'Čeština',
            turkish: 'Türkçe',
            spanish: 'Español'
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
        // Trigger custom event for language change
        document.dispatchEvent(new CustomEvent('languageChanged', { detail: { lang } }));
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
        // Update all elements with data-i18n attribute
        document.querySelectorAll('[data-i18n]').forEach(element => {
            const key = element.getAttribute('data-i18n');
            const translation = this.t(key);
            if (translation && translation !== key) {
                element.textContent = translation;
            }
        });
        
        // Update placeholder attributes
        document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
            const key = element.getAttribute('data-i18n-placeholder');
            const translation = this.t(key);
            if (translation && translation !== key) {
                element.placeholder = translation;
            }
        });
        
        // Update title attributes
        document.querySelectorAll('[data-i18n-title]').forEach(element => {
            const key = element.getAttribute('data-i18n-title');
            const translation = this.t(key);
            if (translation && translation !== key) {
                element.title = translation;
            }
        });
    }
};

// Initialize on load
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => I18n.init());
} else {
    I18n.init();
}

// Listen for language changes
document.addEventListener('languageChanged', () => {
    I18n.applyTranslations();
});

if (typeof module !== 'undefined' && module.exports) {
    module.exports = I18n;
}
