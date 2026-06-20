function switchTab(tab) {
    document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
    
    if (tab === 'login') {
        document.querySelectorAll('.auth-tab')[0].classList.add('active');
        document.getElementById('loginForm').classList.add('active');
    } else {
        document.querySelectorAll('.auth-tab')[1].classList.add('active');
        document.getElementById('registerForm').classList.add('active');
    }
}

async function login() {
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');
    
    // Configurar basic auth no localStorage para simplificar o protótipo
    const credentials = btoa(email + ":" + password);
    
    try {
        const response = await fetch('/api/auth/me', {
            headers: {
                'Authorization': 'Basic ' + credentials
            }
        });
        
        if (response.ok) {
            const user = await response.json();
            localStorage.setItem('auth', credentials);
            localStorage.setItem('user', JSON.stringify(user));
            window.location.href = 'dashboard.html';
        } else {
            errorDiv.style.display = 'block';
            errorDiv.textContent = 'E-mail ou senha incorretos.';
        }
    } catch (e) {
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Erro ao conectar no servidor.';
    }
}

async function register() {
    const name = document.getElementById('regName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const errorDiv = document.getElementById('registerError');
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name, email, password, role: 'CLIENTE'
            })
        });
        
        if (response.ok) {
            alert('Cadastro realizado com sucesso! Faça login.');
            switchTab('login');
        } else {
            const errText = await response.text();
            errorDiv.style.display = 'block';
            errorDiv.textContent = errText;
        }
    } catch (e) {
        errorDiv.style.display = 'block';
        errorDiv.textContent = 'Erro ao conectar no servidor.';
    }
}

function logout() {
    localStorage.removeItem('auth');
    localStorage.removeItem('user');
    window.location.href = 'index.html';
}

function getAuthHeaders() {
    const auth = localStorage.getItem('auth');
    return {
        'Authorization': 'Basic ' + auth,
        'Content-Type': 'application/json'
    };
}

async function loadDiets() {
    const container = document.getElementById('dietsList');
    if (!container) return;
    try {
        const res = await fetch('/api/diets/my', { headers: getAuthHeaders() });
        if (res.ok) {
            const diets = await res.json();
            if (diets.length === 0) {
                container.innerHTML = '<p style="color: var(--text-muted);">Nenhuma dieta cadastrada ainda.</p>';
            } else {
                container.innerHTML = diets.map(d => `
                    <div class="diet-item">
                        <h4 style="margin-bottom: 0.5rem; color: #fff;">Dieta de ${d.creationDate}</h4>
                        <p style="white-space: pre-line; color: var(--text-muted);">${d.description}</p>
                    </div>
                `).join('');
            }
        }
    } catch(e) {
        container.innerHTML = '<p class="error-message">Erro ao carregar dietas.</p>';
    }
}

async function loadAppointments() {
    const container = document.getElementById('aptList');
    if (!container) return;
    try {
        const res = await fetch('/api/appointments/my', { headers: getAuthHeaders() });
        if (res.ok) {
            const apts = await res.json();
            if (apts.length === 0) {
                container.innerHTML = '<p style="color: var(--text-muted); font-size: 0.9rem;">Nenhum agendamento futuro.</p>';
            } else {
                container.innerHTML = apts.map(a => `
                    <div class="apt-item" style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <p>${a.date}</p>
                            <span>${a.startTime} às ${a.endTime} - ${a.status}</span>
                        </div>
                        ${a.status !== 'CANCELADO' ? `<button onclick="cancelApt(${a.id})" class="btn btn-outline" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Cancelar</button>` : ''}
                    </div>
                `).join('');
            }
        }
    } catch(e) {
        container.innerHTML = '<p class="error-message">Erro ao carregar agendamentos.</p>';
    }
}

async function cancelApt(id) {
    if(!confirm('Deseja realmente cancelar este agendamento?')) return;
    try {
        const res = await fetch('/api/appointments/' + id, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        if (res.ok) {
            loadAppointments();
        } else {
            alert('Erro ao cancelar.');
        }
    } catch(e) {}
}

let selectedSlot = null;

async function fetchTimeSlots() {
    const date = document.getElementById('agendaDate').value;
    if (!date) return;
    
    document.getElementById('slotsContainer').style.display = 'block';
    const container = document.getElementById('timeSlots');
    container.innerHTML = '<p>Buscando...</p>';
    document.getElementById('btnConfirmar').style.display = 'none';
    selectedSlot = null;
    
    try {
        const res = await fetch('/api/appointments/date/' + date, { headers: getAuthHeaders() });
        const existingApts = await res.json();
        
        // Gerar horários (9h às 17h)
        const allSlots = ['09:00', '10:00', '11:00', '13:00', '14:00', '15:00', '16:00', '17:00'];
        
        container.innerHTML = '';
        allSlots.forEach(time => {
            const isOccupied = existingApts.some(a => a.status !== 'CANCELADO' && a.startTime.startsWith(time));
            
            const div = document.createElement('div');
            div.className = `time-slot ${isOccupied ? 'occupied' : ''}`;
            div.textContent = time;
            
            if (!isOccupied) {
                div.onclick = () => selectSlot(div, time);
            }
            container.appendChild(div);
        });
        
    } catch(e) {
        container.innerHTML = '<p class="error-message">Erro ao carregar horários.</p>';
    }
}

function selectSlot(element, time) {
    document.querySelectorAll('.time-slot').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    selectedSlot = time;
    document.getElementById('btnConfirmar').style.display = 'block';
}

async function confirmAppointment() {
    if (!selectedSlot) return;
    
    const date = document.getElementById('agendaDate').value;
    const msg = document.getElementById('agendaMsg');
    
    // Calcula startTime e endTime (1 hora de duração)
    const startTime = selectedSlot + ':00';
    const hour = parseInt(selectedSlot.split(':')[0]);
    const endTime = (hour + 1).toString().padStart(2, '0') + ':00:00';
    
    try {
        const res = await fetch('/api/appointments', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                date,
                startTime,
                endTime
            })
        });
        
        if (res.ok) {
            msg.style.color = 'var(--primary-color)';
            msg.textContent = 'Agendamento confirmado com sucesso!';
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 2000);
        } else {
            const err = await res.text();
            msg.style.color = 'var(--danger)';
            msg.textContent = err;
        }
    } catch(e) {
        msg.style.color = 'var(--danger)';
        msg.textContent = 'Erro ao confirmar agendamento.';
    }
}
