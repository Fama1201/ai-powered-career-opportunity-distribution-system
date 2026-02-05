// hr-candidates.js
// HR Candidates page functionality

let allJobs = [];
let allCandidates = [];
let currentJobId = null;
let currentStatusFilter = '';
let currentSearchQuery = '';
let currentEditingApplicationId = null;

document.addEventListener('DOMContentLoaded', () => {
    initCandidates();
});

async function initCandidates() {
    loadSavedTheme();
    loadSavedLanguage();
    await loadUserProfile();
    setupEventListeners();
    await loadJobs();
    
    // Check if jobId is in URL params
    const urlParams = new URLSearchParams(window.location.search);
    const jobIdParam = urlParams.get('jobId');
    if (jobIdParam) {
        currentJobId = parseInt(jobIdParam);
        await loadCandidates();
    }
}

function setupEventListeners() {
    // Job filter
    setupJobFilter();
    
    // Status filter
    setupStatusFilter();
    
    // Search
    document.getElementById('searchBtn')?.addEventListener('click', () => {
        performSearch();
    });
    document.getElementById('searchInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            performSearch();
        }
    });

    // Candidate detail modal
    document.getElementById('candidateDetailCloseBtn')?.addEventListener('click', closeCandidateDetailModal);
    document.getElementById('candidateDetailModalOverlay')?.addEventListener('click', (e) => {
        if (e.target.id === 'candidateDetailModalOverlay') {
            closeCandidateDetailModal();
        }
    });

    // Status update modal
    document.getElementById('statusUpdateCloseBtn')?.addEventListener('click', closeStatusUpdateModal);
    document.getElementById('cancelStatusUpdateBtn')?.addEventListener('click', closeStatusUpdateModal);
    document.getElementById('statusUpdateForm')?.addEventListener('submit', handleStatusUpdate);
    document.getElementById('statusUpdateModalOverlay')?.addEventListener('click', (e) => {
        if (e.target.id === 'statusUpdateModalOverlay') {
            closeStatusUpdateModal();
        }
    });
    
    // Setup status select dropdown
    setupStatusSelect();

    // Add note modal
    document.getElementById('addNoteCloseBtn')?.addEventListener('click', closeAddNoteModal);
    document.getElementById('cancelNoteBtn')?.addEventListener('click', closeAddNoteModal);
    document.getElementById('addNoteForm')?.addEventListener('submit', handleAddNote);
    document.getElementById('addNoteModalOverlay')?.addEventListener('click', (e) => {
        if (e.target.id === 'addNoteModalOverlay') {
            closeAddNoteModal();
        }
    });

    // Theme toggle
    document.getElementById('themeToggle')?.addEventListener('click', toggleTheme);

    // Language dropdown
    document.getElementById('languageBtn')?.addEventListener('click', toggleLanguageMenu);
    document.querySelectorAll('.language-option').forEach(btn => {
        btn.addEventListener('click', (e) => handleLanguageChange(e.currentTarget));
    });

    // User profile dropdown
    document.getElementById('userProfileBtn')?.addEventListener('click', toggleUserProfileMenu);

    // Logout
    document.getElementById('logoutItem')?.addEventListener('click', handleLogout);

    // Sidebar navigation
    document.querySelectorAll('.sidebar-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const route = item.getAttribute('data-route');
            if (route) {
                navigateTo(route);
            }
        });
    });

    // Profile and Notifications dropdown items
    document.getElementById('profileItem')?.addEventListener('click', (e) => {
        e.preventDefault();
        const route = e.currentTarget.getAttribute('data-route');
        if (route) {
            navigateTo(route);
        }
    });
    document.getElementById('notificationsItem')?.addEventListener('click', (e) => {
        e.preventDefault();
        const route = e.currentTarget.getAttribute('data-route');
        if (route) {
            navigateTo(route);
        }
    });

    // Click outside to close dropdowns
    document.addEventListener('click', (e) => {
        const userDropdownMenu = document.getElementById('userDropdownMenu');
        const languageMenu = document.getElementById('languageMenu');

        if (userDropdownMenu && !e.target.closest('.user-profile-dropdown')) {
            userDropdownMenu.classList.remove('active');
        }
        if (languageMenu && !e.target.closest('.language-dropdown')) {
            languageMenu.classList.remove('active');
        }
    });
}

async function loadJobs() {
    try {
        const jobs = await HrAPI.getJobs();
        allJobs = Array.isArray(jobs) ? jobs : [];
        populateJobFilter();
    } catch (error) {
        console.error('Failed to load jobs:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load jobs.', 'error');
        }
    }
}

function populateJobFilter() {
    const dropdown = document.getElementById('jobFilterDropdown');
    if (!dropdown) return;

    // Clear existing options except "All Jobs"
    const allJobsOption = dropdown.querySelector('[data-value=""]');
    dropdown.innerHTML = '';
    if (allJobsOption) {
        dropdown.appendChild(allJobsOption);
    } else {
        const allOption = document.createElement('button');
        allOption.type = 'button';
        allOption.className = 'custom-select-option';
        allOption.setAttribute('data-value', '');
        allOption.textContent = 'All Jobs';
        dropdown.appendChild(allOption);
    }

    // Add job options
    allJobs.forEach(job => {
        const option = document.createElement('button');
        option.type = 'button';
        option.className = 'custom-select-option';
        option.setAttribute('data-value', job.id);
        option.textContent = job.title || `Job #${job.id}`;
        dropdown.appendChild(option);
    });

    // Re-setup event listeners
    setupJobFilter();
}

function setupJobFilter() {
    const jobFilterBtn = document.getElementById('jobFilterBtn');
    const jobFilterDropdown = document.getElementById('jobFilterDropdown');
    const jobFilterOptions = jobFilterDropdown?.querySelectorAll('.custom-select-option');
    const jobFilterText = document.getElementById('jobFilterText');
    const jobFilterHidden = document.getElementById('jobFilter');
    const jobFilterWrapper = jobFilterBtn?.closest('.custom-select-wrapper');

    if (!jobFilterBtn || !jobFilterDropdown) return;

    // Remove existing listeners by cloning
    const newBtn = jobFilterBtn.cloneNode(true);
    jobFilterBtn.parentNode.replaceChild(newBtn, jobFilterBtn);
    const newDropdown = jobFilterDropdown.cloneNode(true);
    jobFilterDropdown.parentNode.replaceChild(newDropdown, jobFilterDropdown);

    // Re-get elements
    const btn = document.getElementById('jobFilterBtn');
    const dropdown = document.getElementById('jobFilterDropdown');
    const options = dropdown?.querySelectorAll('.custom-select-option');
    const wrapper = btn?.closest('.custom-select-wrapper');

    // Toggle dropdown
    btn?.addEventListener('click', (e) => {
        e.stopPropagation();
        const isActive = dropdown.classList.toggle('active');
        if (wrapper) {
            if (isActive) {
                wrapper.classList.add('active');
            } else {
                wrapper.classList.remove('active');
            }
        }
    });

    // Select option
    options?.forEach(option => {
        option.addEventListener('click', async (e) => {
            e.stopPropagation();
            const value = option.getAttribute('data-value');
            const text = option.textContent;

            // Update hidden input
            if (jobFilterHidden) {
                jobFilterHidden.value = value;
            }

            // Update button text
            if (jobFilterText) {
                jobFilterText.textContent = text;
            }

            // Update active state
            options.forEach(opt => opt.classList.remove('active'));
            option.classList.add('active');

            // Close dropdown
            dropdown.classList.remove('active');
            if (wrapper) {
                wrapper.classList.remove('active');
            }

            // Load candidates for selected job
            currentJobId = value ? parseInt(value) : null;
            await loadCandidates();
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!btn.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('active');
            if (wrapper) {
                wrapper.classList.remove('active');
            }
        }
    });

    // Set initial active option
    if (currentJobId) {
        options?.forEach(option => {
            if (option.getAttribute('data-value') === String(currentJobId)) {
                option.classList.add('active');
                if (jobFilterText) {
                    jobFilterText.textContent = option.textContent;
                }
            }
        });
    } else {
        options?.forEach(option => {
            if (option.getAttribute('data-value') === '') {
                option.classList.add('active');
            }
        });
    }
}

function setupStatusFilter() {
    const statusFilterBtn = document.getElementById('statusFilterBtn');
    const statusFilterDropdown = document.getElementById('statusFilterDropdown');
    const statusFilterOptions = statusFilterDropdown?.querySelectorAll('.custom-select-option');
    const statusFilterText = document.getElementById('statusFilterText');
    const statusFilterHidden = document.getElementById('statusFilter');
    const statusFilterWrapper = statusFilterBtn?.closest('.custom-select-wrapper');

    if (!statusFilterBtn || !statusFilterDropdown) return;

    // Toggle dropdown
    statusFilterBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isActive = statusFilterDropdown.classList.toggle('active');
        if (statusFilterWrapper) {
            if (isActive) {
                statusFilterWrapper.classList.add('active');
            } else {
                statusFilterWrapper.classList.remove('active');
            }
        }
    });

    // Select option
    statusFilterOptions.forEach(option => {
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            const value = option.getAttribute('data-value');
            const text = option.textContent;

            // Update hidden input
            if (statusFilterHidden) {
                statusFilterHidden.value = value;
            }

            // Update button text
            if (statusFilterText) {
                statusFilterText.textContent = text;
            }

            // Update active state
            statusFilterOptions.forEach(opt => opt.classList.remove('active'));
            option.classList.add('active');

            // Close dropdown
            statusFilterDropdown.classList.remove('active');
            if (statusFilterWrapper) {
                statusFilterWrapper.classList.remove('active');
            }

            // Apply filter
            currentStatusFilter = value;
            filterAndRenderCandidates();
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!statusFilterBtn.contains(e.target) && !statusFilterDropdown.contains(e.target)) {
            statusFilterDropdown.classList.remove('active');
            if (statusFilterWrapper) {
                statusFilterWrapper.classList.remove('active');
            }
        }
    });

    // Set initial active option
    statusFilterOptions.forEach(option => {
        if (option.getAttribute('data-value') === '') {
            option.classList.add('active');
        }
    });
}

async function loadCandidates() {
    if (!currentJobId) {
        allCandidates = [];
        renderCandidates([]);
        return;
    }

    const grid = document.getElementById('candidatesGrid');
    const loadingState = document.getElementById('loadingState');
    const emptyState = document.getElementById('emptyState');

    if (grid) {
        showLoading(grid);
    }
    if (loadingState) {
        loadingState.style.display = 'block';
    }
    if (emptyState) {
        emptyState.style.display = 'none';
    }

    try {
        const candidates = await HrAPI.getJobApplications(currentJobId);
        allCandidates = Array.isArray(candidates) ? candidates : [];
        filterAndRenderCandidates();
    } catch (error) {
        console.error('Failed to load candidates:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load candidates. Please try again.', 'error');
        }
        if (emptyState) {
            emptyState.style.display = 'block';
        }
    } finally {
        if (grid) {
            hideLoading(grid);
        }
        if (loadingState) {
            loadingState.style.display = 'none';
        }
    }
}

function filterAndRenderCandidates() {
    let filteredCandidates = [...allCandidates];

    // Apply status filter
    if (currentStatusFilter) {
        filteredCandidates = filteredCandidates.filter(candidate => 
            candidate.status === currentStatusFilter
        );
    }

    // Apply search filter
    if (currentSearchQuery) {
        const query = currentSearchQuery.toLowerCase();
        filteredCandidates = filteredCandidates.filter(candidate => 
            candidate.studentName?.toLowerCase().includes(query) ||
            candidate.studentEmail?.toLowerCase().includes(query)
        );
    }

    renderCandidates(filteredCandidates);
}

function performSearch() {
    const searchInput = document.getElementById('searchInput');
    currentSearchQuery = searchInput?.value.trim() || '';
    filterAndRenderCandidates();
}

function renderCandidates(candidates) {
    const grid = document.getElementById('candidatesGrid');
    const emptyState = document.getElementById('emptyState');
    const loadingState = document.getElementById('loadingState');
    const emptyStateMessage = document.getElementById('emptyStateMessage');

    if (loadingState) {
        loadingState.style.display = 'none';
    }

    if (!candidates || candidates.length === 0) {
        if (grid) {
            grid.innerHTML = '';
        }
        if (emptyState) {
            emptyState.style.display = 'block';
        }
        if (emptyStateMessage) {
            if (!currentJobId) {
                emptyStateMessage.textContent = 'Select a job to view candidates.';
            } else if (currentStatusFilter || currentSearchQuery) {
                emptyStateMessage.textContent = 'No candidates match your filters. Try adjusting your search criteria.';
            } else {
                emptyStateMessage.textContent = 'No candidates have applied for this job yet.';
            }
        }
        return;
    }

    if (emptyState) {
        emptyState.style.display = 'none';
    }
    if (grid) {
        grid.innerHTML = '';
    }

    candidates.forEach(candidate => {
        const card = createCandidateCard(candidate);
        if (grid && card) {
            grid.appendChild(card);
        }
    });
}

function createCandidateCard(candidate) {
    const card = document.createElement('div');
    card.className = 'candidate-card';
    card.setAttribute('data-application-id', candidate.applicationId);

    // Get initials
    const initials = getInitials(candidate.studentName || candidate.studentEmail || 'C');

    // Status badge
    const statusClass = getStatusClass(candidate.status);
    const statusLabel = getStatusLabel(candidate.status);

    // Applied date
    const appliedDate = formatDate(candidate.appliedAt);

    card.innerHTML = `
        <div class="candidate-header">
            <div class="candidate-avatar">${initials}</div>
            <div class="candidate-info">
                <h3 class="candidate-name">${escapeHtml(candidate.studentName || 'Unknown')}</h3>
                <p class="candidate-email">${escapeHtml(candidate.studentEmail || 'No email')}</p>
            </div>
        </div>
        <div class="candidate-body">
            <div class="info-row">
                <i class="fas fa-calendar"></i>
                <span>Applied: ${appliedDate}</span>
            </div>
            <div class="status-badge ${statusClass}">${statusLabel}</div>
        </div>
        <div class="candidate-footer">
            <button class="btn btn-sm btn-primary view-detail-btn" data-application-id="${candidate.applicationId}">
                <i class="fas fa-eye"></i> View Details
            </button>
        </div>
    `;

    // Add event listener
    const viewBtn = card.querySelector('.view-detail-btn');
    viewBtn?.addEventListener('click', () => {
        viewCandidateDetails(candidate.applicationId);
    });

    return card;
}

function getInitials(name) {
    if (!name) return '?';
    const names = name.trim().split(' ');
    if (names.length >= 2) {
        return (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

function getStatusClass(status) {
    const statusUpper = (status || '').toUpperCase();
    switch (statusUpper) {
        case 'APPLIED': return 'applied';
        case 'SHORTLISTED': return 'shortlisted';
        case 'INTERVIEW': return 'interview';
        case 'REJECTED': return 'rejected';
        case 'HIRED': return 'hired';
        case 'WITHDRAWN': return 'withdrawn';
        default: return 'applied';
    }
}

function getStatusLabel(status) {
    const statusUpper = (status || '').toUpperCase();
    switch (statusUpper) {
        case 'APPLIED': return 'Applied';
        case 'SHORTLISTED': return 'Shortlisted';
        case 'INTERVIEW': return 'Interview';
        case 'REJECTED': return 'Rejected';
        case 'HIRED': return 'Hired';
        case 'WITHDRAWN': return 'Withdrawn';
        default: return 'Applied';
    }
}

async function viewCandidateDetails(applicationId) {
    try {
        const detail = await HrAPI.getApplicationDetail(applicationId);
        showCandidateDetailModal(detail);
    } catch (error) {
        console.error('Failed to load candidate details:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load candidate details.', 'error');
        }
    }
}

function showCandidateDetailModal(detail) {
    const modal = document.getElementById('candidateDetailModalOverlay');
    const content = document.getElementById('candidateDetailContent');
    const title = document.getElementById('candidateDetailTitle');

    if (title) {
        title.textContent = `${detail.studentName || 'Candidate'} - Application Details`;
    }

    const actionsContainer = document.getElementById('candidateDetailActions');
    
    if (content) {
        const statusClass = getStatusClass(detail.status);
        const statusLabel = getStatusLabel(detail.status);
        const appliedDate = formatDate(detail.appliedAt);
        const initials = getInitials(detail.studentName || detail.studentEmail || 'C');

        content.innerHTML = `
            <div class="candidate-detail-header">
                <div class="candidate-detail-avatar">${initials}</div>
                <div class="candidate-detail-info">
                    <h3>${escapeHtml(detail.studentName || 'Unknown')}</h3>
                    <p class="candidate-detail-email">${escapeHtml(detail.studentEmail || 'No email')}</p>
                    <div class="status-badge ${statusClass}">${statusLabel}</div>
                </div>
            </div>

            <div class="candidate-detail-section">
                <h4>Application Information</h4>
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">Applied Date:</span>
                        <span class="detail-value">${appliedDate}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">Status:</span>
                        <span class="detail-value status-badge ${statusClass}">${statusLabel}</span>
                    </div>
                </div>
            </div>

            ${detail.studentSkills ? `
            <div class="candidate-detail-section">
                <h4>Skills</h4>
                <p class="candidate-skills">${escapeHtml(detail.studentSkills)}</p>
            </div>
            ` : ''}

            ${detail.studentCareerInterest ? `
            <div class="candidate-detail-section">
                <h4>Career Interest</h4>
                <p class="candidate-career-interest">${escapeHtml(detail.studentCareerInterest)}</p>
            </div>
            ` : ''}

            ${detail.studentCvText ? `
            <div class="candidate-detail-section">
                <h4>CV Preview</h4>
                <div class="cv-preview">
                    <pre class="cv-text">${escapeHtml(detail.studentCvText)}</pre>
                </div>
            </div>
            ` : ''}

            ${detail.notes ? `
            <div class="candidate-detail-section">
                <h4>Notes</h4>
                <div class="notes-preview">
                    <pre class="notes-text">${escapeHtml(detail.notes)}</pre>
                </div>
            </div>
            ` : ''}
        `;
    }

    if (actionsContainer) {
        actionsContainer.innerHTML = `
            <button class="btn btn-secondary" id="addNoteBtn" data-application-id="${detail.applicationId}">
                <i class="fas fa-sticky-note"></i> Add Note
            </button>
            <button class="btn btn-secondary" id="updateStatusBtn" data-application-id="${detail.applicationId}">
                <i class="fas fa-edit"></i> Update Status
            </button>
            <button class="btn btn-primary" id="viewCvBtn" data-student-id="${detail.studentUserId}">
                <i class="fas fa-file-pdf"></i> View Full CV
            </button>
        `;

        // Add event listeners
        document.getElementById('addNoteBtn')?.addEventListener('click', () => {
            openAddNoteModal(detail.applicationId);
        });
        document.getElementById('updateStatusBtn')?.addEventListener('click', () => {
            openStatusUpdateModal(detail.applicationId, detail.status);
        });
        document.getElementById('viewCvBtn')?.addEventListener('click', async () => {
            await viewFullCv(detail.studentUserId);
        });
    }

    if (modal) {
        modal.style.display = 'flex';
    }
}

async function viewFullCv(studentUserId) {
    try {
        const cv = await HrAPI.getStudentCv(studentUserId);
        if (cv && cv.cvText) {
            // Download CV as a text file
            downloadCv(cv.cvText, studentUserId);
            
            if (typeof UI !== 'undefined' && UI.toast) {
                UI.toast('CV downloaded successfully!', 'success');
            }
        } else {
            if (typeof UI !== 'undefined' && UI.toast) {
                UI.toast('CV not available for this candidate.', 'info');
            }
        }
    } catch (error) {
        console.error('Failed to load CV:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to load CV.', 'error');
        }
    }
}

function downloadCv(cvText, studentUserId) {
    // Create a blob with the CV text
    const blob = new Blob([cvText], { type: 'text/plain;charset=utf-8' });
    
    // Create a download link
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    // Generate filename with timestamp
    const timestamp = new Date().toISOString().split('T')[0];
    link.download = `CV_${studentUserId}_${timestamp}.txt`;
    
    // Trigger download
    document.body.appendChild(link);
    link.click();
    
    // Clean up
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

function closeCandidateDetailModal() {
    const modal = document.getElementById('candidateDetailModalOverlay');
    if (modal) {
        modal.style.display = 'none';
    }
}

function adjustDropdownPosition(dropdown, button) {
    if (!dropdown || !button) return;
    
    // Dropdown'u geçici olarak görünür yap (ölçüm için)
    dropdown.style.visibility = 'hidden';
    dropdown.style.display = 'block';
    dropdown.style.opacity = '0';
    dropdown.style.position = 'absolute';
    dropdown.style.top = 'calc(100% + 0.5rem)';
    dropdown.style.bottom = 'auto';
    dropdown.style.maxHeight = 'none';
    
    // requestAnimationFrame ile ölçümü yap
    requestAnimationFrame(() => {
        const buttonRect = button.getBoundingClientRect();
        const viewportHeight = window.innerHeight;
        const modalBody = button.closest('.modal-body');
        const modalContent = button.closest('.modal-content');
        const modalOverlay = button.closest('.modal-overlay');
        
        // Dropdown'un gerçek içerik yüksekliğini ölç (tüm seçenekler dahil)
        const options = dropdown.querySelectorAll('.custom-select-option');
        const optionCount = options.length;
        const estimatedHeight = optionCount * 48; // Her seçenek yaklaşık 48px (padding dahil)
        
        // Modal body ve modal content'in sınırlarını bul
        let availableSpaceBelow = viewportHeight - buttonRect.bottom - 30; // 30px padding
        let availableSpaceAbove = buttonRect.top - 120; // 120px üstten boşluk (header + padding)
        
        if (modalBody) {
            const modalBodyRect = modalBody.getBoundingClientRect();
            availableSpaceBelow = modalBodyRect.bottom - buttonRect.bottom - 30;
            availableSpaceAbove = buttonRect.top - modalBodyRect.top - 30;
        }
        
        if (modalContent) {
            const modalContentRect = modalContent.getBoundingClientRect();
            // Modal content'in alt sınırını da kontrol et
            const spaceToModalBottom = modalContentRect.bottom - buttonRect.bottom - 30;
            if (spaceToModalBottom < availableSpaceBelow) {
                availableSpaceBelow = spaceToModalBottom;
            }
        }
        
        // Dropdown'un tüm seçeneklerini göstermek için yeterli alan var mı?
        // 7 seçenek için: Select status, Applied, Shortlisted, Interview, Rejected, Hired, Withdrawn
        const neededHeight = Math.min(estimatedHeight, 336); // 7 seçenek * 48px = 336px
        
        // Aşağı doğru açılabilir mi? (tercih edilen)
        if (availableSpaceBelow >= neededHeight) {
            // Aşağı doğru aç (varsayılan)
            dropdown.classList.remove('dropdown-up');
            dropdown.style.top = 'calc(100% + 0.5rem)';
            dropdown.style.bottom = 'auto';
            dropdown.style.maxHeight = `${neededHeight}px`;
        } 
        // Yukarı doğru açılabilir mi?
        else if (availableSpaceAbove >= neededHeight) {
            // Yukarı doğru aç
            dropdown.classList.add('dropdown-up');
            dropdown.style.top = 'auto';
            dropdown.style.bottom = 'calc(100% + 0.5rem)';
            dropdown.style.maxHeight = `${neededHeight}px`;
        }
        // Her iki tarafta da yeterli alan yoksa, daha büyük olanı seç
        else {
            if (availableSpaceBelow >= availableSpaceAbove && availableSpaceBelow >= 200) {
                // Aşağı doğru aç, scroll ile
                dropdown.classList.remove('dropdown-up');
                dropdown.style.top = 'calc(100% + 0.5rem)';
                dropdown.style.bottom = 'auto';
                dropdown.style.maxHeight = `${availableSpaceBelow}px`;
            } else if (availableSpaceAbove >= 200) {
                // Yukarı doğru aç, scroll ile
                dropdown.classList.add('dropdown-up');
                dropdown.style.top = 'auto';
                dropdown.style.bottom = 'calc(100% + 0.5rem)';
                dropdown.style.maxHeight = `${availableSpaceAbove}px`;
            } else {
                // Minimum alan varsa, aşağı doğru aç
                dropdown.classList.remove('dropdown-up');
                dropdown.style.top = 'calc(100% + 0.5rem)';
                dropdown.style.bottom = 'auto';
                dropdown.style.maxHeight = `${Math.max(200, Math.min(availableSpaceBelow, availableSpaceAbove))}px`;
            }
        }
        
        // Dropdown'u tekrar görünür yap
        dropdown.style.visibility = 'visible';
        dropdown.style.opacity = '1';
    });
}

function setupStatusSelect() {
    const statusSelectBtn = document.getElementById('statusSelectBtn');
    const statusSelectDropdown = document.getElementById('statusSelectDropdown');
    const statusSelectText = document.getElementById('statusSelectText');
    const statusSelectHidden = document.getElementById('newStatus');
    const statusSelectWrapper = document.getElementById('statusSelectWrapper');
    const statusSelectOptions = statusSelectDropdown?.querySelectorAll('.custom-select-option');

    if (!statusSelectBtn || !statusSelectDropdown || !statusSelectText || !statusSelectHidden) return;

    // Toggle dropdown
    statusSelectBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isActive = statusSelectDropdown.classList.toggle('active');
        if (statusSelectWrapper) {
            if (isActive) {
                statusSelectWrapper.classList.add('active');
                // Dropdown pozisyonunu ayarla
                adjustDropdownPosition(statusSelectDropdown, statusSelectBtn);
            } else {
                statusSelectWrapper.classList.remove('active');
            }
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (statusSelectWrapper && !statusSelectWrapper.contains(e.target)) {
            statusSelectDropdown.classList.remove('active');
            if (statusSelectWrapper) {
                statusSelectWrapper.classList.remove('active');
            }
        }
    });

    // Handle option selection
    statusSelectOptions?.forEach(option => {
        option.addEventListener('click', () => {
            const value = option.getAttribute('data-value');
            const text = option.textContent;

            // Update hidden input
            statusSelectHidden.value = value;

            // Update button text
            statusSelectText.textContent = text;

            // Update active state
            statusSelectOptions.forEach(opt => opt.classList.remove('active'));
            option.classList.add('active');

            // Close dropdown
            statusSelectDropdown.classList.remove('active');
            if (statusSelectWrapper) {
                statusSelectWrapper.classList.remove('active');
            }
        });
    });
}

function openStatusUpdateModal(applicationId, currentStatus) {
    currentEditingApplicationId = applicationId;
    const modal = document.getElementById('statusUpdateModalOverlay');
    const statusSelectHidden = document.getElementById('newStatus');
    const statusSelectText = document.getElementById('statusSelectText');
    const statusSelectOptions = document.querySelectorAll('#statusSelectDropdown .custom-select-option');

    if (statusSelectHidden) {
        statusSelectHidden.value = currentStatus || '';
    }

    // Update button text and active state
    if (statusSelectText && statusSelectOptions) {
        const statusLabels = {
            '': 'Select status',
            'APPLIED': 'Applied',
            'SHORTLISTED': 'Shortlisted',
            'INTERVIEW': 'Interview',
            'REJECTED': 'Rejected',
            'HIRED': 'Hired',
            'WITHDRAWN': 'Withdrawn'
        };

        const selectedValue = currentStatus || '';
        statusSelectText.textContent = statusLabels[selectedValue] || 'Select status';

        // Update active state
        statusSelectOptions.forEach(option => {
            const optionValue = option.getAttribute('data-value');
            if (optionValue === selectedValue) {
                option.classList.add('active');
            } else {
                option.classList.remove('active');
            }
        });
    }

    if (modal) {
        modal.style.display = 'flex';
    }
    closeCandidateDetailModal();
}

function closeStatusUpdateModal() {
    const modal = document.getElementById('statusUpdateModalOverlay');
    const form = document.getElementById('statusUpdateForm');
    
    if (modal) {
        modal.style.display = 'none';
    }
    if (form) {
        form.reset();
    }
    currentEditingApplicationId = null;
}

async function handleStatusUpdate(e) {
    e.preventDefault();
    
    const saveBtn = document.getElementById('saveStatusBtn');
    const saveBtnText = document.getElementById('saveStatusBtnText');
    const saveBtnLoading = document.getElementById('saveStatusBtnLoading');
    const statusSelect = document.getElementById('newStatus');

    const newStatus = statusSelect?.value;
    if (!newStatus) {
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Please select a status', 'error');
        }
        return;
    }

    // Show loading state
    saveBtn.disabled = true;
    saveBtnText.style.display = 'none';
    saveBtnLoading.style.display = 'inline';

    try {
        await HrAPI.updateApplicationStatus(currentEditingApplicationId, newStatus);
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Status updated successfully!', 'success');
        }
        
        closeStatusUpdateModal();
        await loadCandidates();
    } catch (error) {
        console.error('Failed to update status:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to update status. Please try again.', 'error');
        }
        
        // Re-enable button on error
        saveBtn.disabled = false;
        saveBtnText.style.display = 'inline';
        saveBtnLoading.style.display = 'none';
    }
}

function openAddNoteModal(applicationId) {
    currentEditingApplicationId = applicationId;
    const modal = document.getElementById('addNoteModalOverlay');
    const noteTextarea = document.getElementById('noteText');

    if (noteTextarea) {
        noteTextarea.value = '';
    }

    if (modal) {
        modal.style.display = 'flex';
    }
    closeCandidateDetailModal();
}

function closeAddNoteModal() {
    const modal = document.getElementById('addNoteModalOverlay');
    const form = document.getElementById('addNoteForm');
    
    if (modal) {
        modal.style.display = 'none';
    }
    if (form) {
        form.reset();
    }
    currentEditingApplicationId = null;
}

async function handleAddNote(e) {
    e.preventDefault();
    
    const saveBtn = document.getElementById('saveNoteBtn');
    const saveBtnText = document.getElementById('saveNoteBtnText');
    const saveBtnLoading = document.getElementById('saveNoteBtnLoading');
    const noteTextarea = document.getElementById('noteText');

    const note = noteTextarea?.value.trim();
    if (!note) {
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Please enter a note', 'error');
        }
        return;
    }

    // Show loading state
    saveBtn.disabled = true;
    saveBtnText.style.display = 'none';
    saveBtnLoading.style.display = 'inline';

    try {
        await HrAPI.addApplicationNote(currentEditingApplicationId, note);
        
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast('Note added successfully!', 'success');
        }
        
        closeAddNoteModal();
        // Reload candidate details if modal is open
        await viewCandidateDetails(currentEditingApplicationId);
    } catch (error) {
        console.error('Failed to add note:', error);
        if (typeof UI !== 'undefined' && UI.toast) {
            UI.toast(error.message || 'Failed to add note. Please try again.', 'error');
        }
        
        // Re-enable button on error
        saveBtn.disabled = false;
        saveBtnText.style.display = 'inline';
        saveBtnLoading.style.display = 'none';
    }
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// User profile functions
async function loadUserProfile() {
    const firstName = localStorage.getItem(CONFIG.STORAGE_KEYS.FIRST_NAME);
    const lastName = localStorage.getItem(CONFIG.STORAGE_KEYS.LAST_NAME);
    const email = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_EMAIL);
    const role = localStorage.getItem(CONFIG.STORAGE_KEYS.ROLE) || 'HR';

    let fullName = '';
    if (firstName && lastName) {
        fullName = `${firstName} ${lastName}`;
    } else if (firstName) {
        fullName = firstName;
    } else if (email) {
        fullName = email.split('@')[0];
    } else {
        fullName = 'HR Professional';
    }

    updateUserProfileDisplay(fullName, role);
}

function updateUserProfileDisplay(fullName, role) {
    const profileNameElement = document.querySelector('.profile-name');
    const profileRoleElement = document.querySelector('.profile-role');
    const profileInitialsElement = document.querySelector('.profile-initials');

    if (profileNameElement) {
        profileNameElement.textContent = fullName;
    }

    if (profileRoleElement) {
        profileRoleElement.textContent = role === 'HR' ? 'HR Professional' : role;
    }

    if (profileInitialsElement && fullName) {
        const names = fullName.trim().split(' ');
        let initials = '';
        if (names.length >= 2) {
            initials = (names[0].charAt(0) + names[names.length - 1].charAt(0)).toUpperCase();
        } else if (names.length === 1) {
            initials = names[0].substring(0, 2).toUpperCase();
        }
        profileInitialsElement.textContent = initials || 'HR';
    }
}

function loadSavedTheme() {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
    } else {
        document.body.classList.remove('light-theme');
    }
}

function loadSavedLanguage() {
    const savedLang = localStorage.getItem('language') || 'en';
    if (typeof I18n !== 'undefined' && I18n.setLanguage) {
        I18n.setLanguage(savedLang);
    }
    const langButtons = document.querySelectorAll('.language-option');
    langButtons.forEach(btn => {
        const lang = btn.getAttribute('data-lang');
        const langName = btn.getAttribute('data-lang-name');
        if (lang === savedLang) {
            btn.classList.add('active');
            const currentLangEl = document.getElementById('currentLanguage');
            if (currentLangEl) {
                currentLangEl.textContent = langName || 'English';
            }
        } else {
            btn.classList.remove('active');
        }
    });
}

function toggleTheme() {
    const body = document.body;
    const isLightTheme = body.classList.contains('light-theme');
    if (isLightTheme) {
        body.classList.remove('light-theme');
        localStorage.setItem('theme', 'dark');
    } else {
        body.classList.add('light-theme');
        localStorage.setItem('theme', 'light');
    }
}

function toggleLanguageMenu() {
    document.getElementById('languageMenu')?.classList.toggle('active');
}

function handleLanguageChange(button) {
    const lang = button.getAttribute('data-lang');
    const langName = button.getAttribute('data-lang-name');
    document.getElementById('currentLanguage').textContent = langName;
    document.querySelectorAll('.language-option').forEach(opt => opt.classList.remove('active'));
    button.classList.add('active');
    localStorage.setItem('language', lang);
    if (typeof I18n !== 'undefined' && I18n.setLanguage) {
        I18n.setLanguage(lang);
    }
    document.getElementById('languageMenu')?.classList.remove('active');
}

function toggleUserProfileMenu() {
    document.getElementById('userDropdownMenu')?.classList.toggle('active');
}

async function handleLogout(e) {
    e.preventDefault();
    if (confirm('Are you sure you want to logout?')) {
        try {
            await apiRequest('/auth/logout', { method: 'POST' });
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            clearAuthData();
            window.location.href = '/pages/auth/login.html';
        }
    }
}

function navigateTo(route) {
    if (typeof router !== 'undefined' && router.navigate) {
        router.navigate(route);
    } else {
        window.location.hash = route;
    }
}

function showLoading(element) {
    if (element) {
        element.style.opacity = '0.5';
        element.style.pointerEvents = 'none';
    }
}

function hideLoading(element) {
    if (element) {
        element.style.opacity = '1';
        element.style.pointerEvents = 'auto';
    }
}

