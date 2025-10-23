// app.js - corrected and hardened for login, JSON payloads, ID typing, and error handling

const API_BASE_URL = '/api'; // backend base

let currentUser = null; // Stores logged-in user object

// --- UI Utility Functions ---

function showView(viewId) {
  document.querySelectorAll('.view').forEach(view => view.classList.remove('active'));
  const target = document.getElementById(viewId);
  if (target) target.classList.add('active');
}

function updateHeader() {
  const welcomeMsg = document.getElementById('welcome-message');
  const logoutBtn = document.getElementById('logout-btn');

  if (currentUser) {
    welcomeMsg.textContent = `Welcome, ${currentUser.name} (${currentUser.role})`;
    welcomeMsg.style.display = 'inline';
    logoutBtn.style.display = 'inline';
    document.querySelectorAll('.error, .success').forEach(el => el.textContent = '');
  } else {
    welcomeMsg.style.display = 'none';
    logoutBtn.style.display = 'none';
    showView('public-view');
  }
}

function showAdminSubView(subViewId) {
  document.querySelectorAll('#admin-sub-views .sub-view').forEach(view => view.classList.remove('active'));
  const el = document.getElementById(subViewId);
  if (el) el.classList.add('active');
  if (subViewId === 'approve-courses-view') loadUnapprovedCourses();
}

function showInstructorSubView(subViewId) {
  document.querySelectorAll('#instructor-sub-views .sub-view').forEach(view => view.classList.remove('active'));
  const el = document.getElementById(subViewId);
  if (el) el.classList.add('active');
  if (subViewId === 'manage-courses-view') loadInstructorCourses();
  document.getElementById('update-course-msg').textContent = '';
}

function showStudentSubView(subViewId) {
  document.querySelectorAll('#student-sub-views .sub-view').forEach(view => view.classList.remove('active'));
  const el = document.getElementById(subViewId);
  if (el) el.classList.add('active');
  if (subViewId === 'view-courses-view') loadAvailableCourses();
  else if (subViewId === 'my-enrollments-view') loadStudentEnrollments();
}

// --- Helper utilities ---

async function safeJson(response) {
  const text = await response.text();
  try {
    return text ? JSON.parse(text) : null;
  } catch {
    return text;
  }
}

function ensureNumber(val) {
  if (val === null || val === undefined) return null;
  return typeof val === 'number' ? val : Number(val);
}

// --- Authentication (Register/Login/Logout) ---

document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const errorMsg = document.getElementById('register-error');
  errorMsg.textContent = '';

  const name = document.getElementById('reg-name').value;
  const email = document.getElementById('reg-email').value;
  const password = document.getElementById('reg-password').value;
  const role = document.getElementById('reg-role').value;

  try {
    const response = await fetch(`${API_BASE_URL}/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password, role })
    });

    if (!response.ok) {
      const body = await safeJson(response);
      throw new Error(body && body.message ? body.message : (typeof body === 'string' ? body : 'Registration failed'));
    }

    alert(`✅ Registered successfully as ${role}! Please log in.`);
    e.target.reset();
    showView('login-view');
  } catch (error) {
    errorMsg.textContent = `❌ ${error.message}`;
  }
});

document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const errorMsg = document.getElementById('login-error');
  errorMsg.textContent = '';

  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;

  // Debugging log removed in production; keep while diagnosing
  console.debug('DEBUG sending login', { email, passwordPresent: password != null && password !== '' });

  try {
    if (!email || !password) throw new Error('Email and password must be provided');

    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email, password: password })
    });

    const body = await safeJson(response);

    if (!response.ok) {
      const msg = (body && (body.message || body)) || 'Login failed. Check your credentials.';
      throw new Error(msg);
    }

    currentUser = body;
    // Ensure id is numeric for comparisons used elsewhere
    if (currentUser && currentUser.id) currentUser.id = ensureNumber(currentUser.id);
    updateHeader();
    e.target.reset();

    if (currentUser.role === 'ADMIN') {
      showView('admin-view');
      showAdminSubView('approve-courses-view');
    } else if (currentUser.role === 'INSTRUCTOR') {
      showView('instructor-view');
      showInstructorSubView('create-course-view');
    } else if (currentUser.role === 'STUDENT') {
      showView('student-view');
      showStudentSubView('view-courses-view');
    } else {
      // Unknown role: fallback to public
      showView('public-view');
    }
  } catch (error) {
    errorMsg.textContent = `❌ ${error.message}`;
  }
});

document.getElementById('logout-btn').addEventListener('click', () => {
  currentUser = null;
  updateHeader();
});

// --- Admin Functions ---

async function loadUnapprovedCourses() {
  const tableBody = document.querySelector('#unapproved-courses-table tbody');
  tableBody.innerHTML = '<tr><td colspan="4">Loading...</td></tr>';
  document.getElementById('approve-msg').textContent = '';

  try {
    const response = await fetch(`${API_BASE_URL}/courses/unapproved`);
    if (!response.ok) throw new Error('Failed to fetch unapproved courses from the server.');
    const courses = await safeJson(response);
    if (!Array.isArray(courses)) {
      tableBody.innerHTML = '<tr><td colspan="4" class="error">❌ Server returned invalid data format.</td></tr>';
      return;
    }

    tableBody.innerHTML = '';
    if (courses.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="4">No courses awaiting approval.</td></tr>';
      return;
    }

    courses.forEach(course => {
      const row = tableBody.insertRow();
      row.insertCell().textContent = course.id;
      row.insertCell().textContent = course.title || '';
      row.insertCell().textContent = course.instructor ? course.instructor.name : 'N/A';
      const actionCell = row.insertCell();

      const approveBtn = document.createElement('button');
      approveBtn.textContent = 'Approve';
      approveBtn.style.backgroundColor = '#28a745';
      approveBtn.onclick = () => handleCourseApproval(course.id, true);

      const rejectBtn = document.createElement('button');
      rejectBtn.textContent = 'Reject';
      rejectBtn.style.backgroundColor = '#dc3545';
      rejectBtn.style.marginLeft = '10px';
      rejectBtn.onclick = () => handleCourseApproval(course.id, false);

      actionCell.appendChild(approveBtn);
      actionCell.appendChild(rejectBtn);
    });

  } catch (error) {
    tableBody.innerHTML = `<tr><td colspan="4" class="error">❌ Error loading courses: ${error.message}</td></tr>`;
  }
}

async function handleCourseApproval(courseId, approve) {
  const msg = document.getElementById('approve-msg');
  msg.textContent = '';
  try {
    const response = await fetch(`${API_BASE_URL}/courses/${courseId}/approve?approve=${approve}`, {
      method: 'POST'
    });
    const body = await safeJson(response);
    if (!response.ok) {
      throw new Error((body && (body.message || body)) || `Failed to ${approve ? 'approve' : 'reject'} course.`);
    }
    msg.className = 'success';
    msg.textContent = `✅ Course ID ${courseId} ${approve ? 'approved' : 'rejected'} successfully!`;
    loadUnapprovedCourses();
  } catch (error) {
    msg.className = 'error';
    msg.textContent = `❌ ${error.message}`;
  }
}

async function generateReports() {
  const outputDiv = document.getElementById('reports-output');
  outputDiv.innerHTML = '<p>Generating reports...</p>';
  try {
    const [studentsRes, revenueRes, statusRes] = await Promise.all([
      fetch(`${API_BASE_URL}/reports/students-per-course`),
      fetch(`${API_BASE_URL}/reports/revenue-per-course`),
      fetch(`${API_BASE_URL}/reports/enrollment-status`)
    ]);

    if (!studentsRes.ok || !revenueRes.ok || !statusRes.ok) {
      throw new Error('One or more report services failed to respond.');
    }

    const students = await safeJson(studentsRes);
    const revenue = await safeJson(revenueRes);
    const status = await safeJson(statusRes);

    let html = '';

    html += '<h4>📊 Students Enrolled per Course</h4><ul>';
    for (const [course, count] of Object.entries(students || {})) {
      html += `<li>${course}: ${count}</li>`;
    }
    html += '</ul>';

    html += '<h4>💰 Revenue Collected per Course</h4><ul>';
    for (const [course, amount] of Object.entries(revenue || {})) {
      const amt = typeof amount === 'number' ? amount : Number(amount || 0);
      html += `<li>${course}: ₹${amt.toFixed(2)}</li>`;
    }
    html += '</ul>';

    html += '<h4>📑 Enrollment Status Report</h4><ul>';
    for (const [stat, count] of Object.entries(status || {})) {
      html += `<li>${stat}: ${count}</li>`;
    }
    html += '</ul>';

    outputDiv.innerHTML = html;
  } catch (error) {
    outputDiv.innerHTML = `<p class="error">❌ Error generating reports: ${error.message}</p>`;
  }
}

// --- Instructor Functions ---

document.getElementById('create-course-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const msg = document.getElementById('create-course-msg');
  msg.textContent = '';

  const courseData = {
    title: document.getElementById('cc-title').value,
    description: document.getElementById('cc-description').value,
    duration: parseInt(document.getElementById('cc-duration').value, 10),
    fee: parseFloat(document.getElementById('cc-fee').value),
    category: document.getElementById('cc-category').value,
    instructor: { id: currentUser ? ensureNumber(currentUser.id) : null }
  };

  try {
    const response = await fetch(`${API_BASE_URL}/courses`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(courseData)
    });
    const body = await safeJson(response);
    if (!response.ok) throw new Error((body && (body.message || body)) || 'Course creation failed.');
    msg.className = 'success';
    msg.textContent = '✅ Course created and sent for Admin approval!';
    e.target.reset();
  } catch (error) {
    msg.className = 'error';
    msg.textContent = `❌ ${error.message}`;
  }
});

async function loadInstructorCourses() {
  const list = document.getElementById('instructor-courses-list');
  list.innerHTML = '<li>Loading your courses...</li>';

  try {
    const response = await fetch(`${API_BASE_URL}/courses/all`);
    if (!response.ok) throw new Error('Failed to fetch courses from the server.');
    const courses = await safeJson(response);
    if (!Array.isArray(courses)) {
      list.innerHTML = '<li class="error">❌ Error: API did not return a list of courses.</li>';
      return;
    }

    const myCourses = courses.filter(c => c.instructor && ensureNumber(c.instructor.id) === ensureNumber(currentUser?.id));
    list.innerHTML = '';
    if (myCourses.length === 0) {
      list.innerHTML = '<li>You have not created any courses yet.</li>';
      return;
    }

    myCourses.forEach(course => {
      const status = course.approved ? '✅ Approved' : '⏳ Pending/Rejected';
      const item = document.createElement('li');
      item.innerHTML = `
        <strong>${course.title}</strong> (ID: ${course.id}) - ${status}<br>
        ${course.description || ''} - Fee: ₹${(course.fee || 0).toFixed(2)}<br>
        <button onclick="editCourse(${course.id})">Edit</button>
        <button onclick="deleteCourse(${course.id})" style="background-color:#dc3545; margin-left:10px;">Delete</button>
        <hr>
      `;
      list.appendChild(item);
    });

  } catch (error) {
    list.innerHTML = `<li class="error">❌ Error loading courses: ${error.message}</li>`;
  }
}

function editCourse(courseId) {
  fetch(`${API_BASE_URL}/courses/all`)
    .then(res => res.json())
    .then(courses => {
      const course = Array.isArray(courses) ? courses.find(c => c.id === courseId) : null;
      if (course) {
        document.getElementById('uc-id').value = course.id;
        document.getElementById('uc-title').value = course.title;
        document.getElementById('uc-description').value = course.description;
        document.getElementById('uc-duration').value = course.duration;
        document.getElementById('uc-fee').value = course.fee;
        document.getElementById('uc-category').value = course.category;
        showInstructorSubView('update-course-form-view');
      } else {
        alert('Course not found.');
      }
    })
    .catch(error => alert(`Error fetching course details: ${error.message}`));
}

document.getElementById('update-course-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const msg = document.getElementById('update-course-msg');
  msg.textContent = '';

  const courseId = document.getElementById('uc-id').value;
  const courseData = {
    title: document.getElementById('uc-title').value,
    description: document.getElementById('uc-description').value,
    duration: parseInt(document.getElementById('uc-duration').value, 10),
    fee: parseFloat(document.getElementById('uc-fee').value),
    category: document.getElementById('uc-category').value,
    instructor: { id: currentUser ? ensureNumber(currentUser.id) : null }
  };

  try {
    const response = await fetch(`${API_BASE_URL}/courses/${courseId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(courseData)
    });
    const body = await safeJson(response);
    if (!response.ok) throw new Error((body && (body.message || body)) || 'Course update failed.');
    msg.className = 'success';
    msg.textContent = '✅ Course updated successfully!';
  } catch (error) {
    msg.className = 'error';
    msg.textContent = `❌ ${error.message}`;
  }
});

async function deleteCourse(courseId) {
  if (!confirm(`Are you sure you want to delete course ID ${courseId}?`)) return;
  try {
    const response = await fetch(`${API_BASE_URL}/courses/${courseId}`, { method: 'DELETE' });
    const body = await safeJson(response);
    if (!response.ok) throw new Error((body && (body.message || body)) || 'Course deletion failed.');
    alert('✅ Course deleted successfully!');
    loadInstructorCourses();
  } catch (error) {
    alert(`❌ Deletion failed: ${error.message}`);
  }
}

// --- Student Functions ---

async function loadAvailableCourses() {
  const tableBody = document.querySelector('#available-courses-table tbody');
  tableBody.innerHTML = '<tr><td colspan="4">Loading...</td></tr>';
  document.getElementById('enroll-msg').textContent = '';

  try {
    const response = await fetch(`${API_BASE_URL}/courses`);
    if (!response.ok) throw new Error('Failed to fetch approved courses.');
    const courses = await safeJson(response);
    if (!Array.isArray(courses)) {
      tableBody.innerHTML = '<tr><td colspan="4" class="error">❌ Error: Invalid data from API.</td></tr>';
      return;
    }

    tableBody.innerHTML = '';
    if (courses.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="4">No approved courses available yet.</td></tr>';
      return;
    }

    courses.forEach(course => {
      const row = tableBody.insertRow();
      row.insertCell().textContent = course.id;
      row.insertCell().textContent = course.title || '';
      row.insertCell().textContent = `₹${((course.fee || 0)).toFixed(2)}`;
      const actionCell = row.insertCell();
      const enrollBtn = document.createElement('button');
      enrollBtn.textContent = 'Enroll';
      enrollBtn.onclick = () => enrollInCourse(course.id);
      actionCell.appendChild(enrollBtn);
    });

  } catch (error) {
    tableBody.innerHTML = `<tr><td colspan="4" class="error">❌ Error loading courses: ${error.message}</td></tr>`;
  }
}

async function enrollInCourse(courseId) {
  const msg = document.getElementById('enroll-msg');
  msg.textContent = '';
  try {
    if (!currentUser || !currentUser.id) throw new Error('Not logged in');

    const response = await fetch(`${API_BASE_URL}/enrollments?studentId=${ensureNumber(currentUser.id)}&courseId=${courseId}`, {
      method: 'POST'
    });
    const data = await safeJson(response);

    if (!response.ok) {
      const errorText = (data && (data.message || data)) || 'Enrollment failed.';
      throw new Error(errorText);
    }

    msg.className = 'success';
    msg.textContent = `✅ Enrolled successfully! Enrollment ID: ${data.id}`;
    loadAvailableCourses();
  } catch (error) {
    msg.className = 'error';
    msg.textContent = `❌ ${error.message}`;
  }
}

async function loadStudentEnrollments() {
  const tableBody = document.querySelector('#my-enrollments-table tbody');
  tableBody.innerHTML = '<tr><td colspan="4">Loading...</td></tr>';
  document.getElementById('progress-msg').textContent = '';

  try {
    if (!currentUser || !currentUser.id) throw new Error('Not logged in');

    const response = await fetch(`${API_BASE_URL}/enrollments/student/${ensureNumber(currentUser.id)}`);
    if (!response.ok) throw new Error('Failed to fetch enrollments.');
    const enrollments = await safeJson(response);

    if (!Array.isArray(enrollments)) {
      tableBody.innerHTML = '<tr><td colspan="4" class="error">❌ Error: Invalid data from API.</td></tr>';
      return;
    }

    tableBody.innerHTML = '';
    if (enrollments.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="4">You are not enrolled in any courses.</td></tr>';
      return;
    }

    enrollments.forEach(enrollment => {
      const row = tableBody.insertRow();
      row.insertCell().textContent = enrollment.id;
      row.insertCell().textContent = (enrollment.course && enrollment.course.title) ? enrollment.course.title : '—';
      row.insertCell().textContent = `${enrollment.progress}%`;
      const actionCell = row.insertCell();
      const updateBtn = document.createElement('button');
      updateBtn.textContent = 'Update Progress';
      updateBtn.onclick = () => showProgressUpdateForm(enrollment);
      actionCell.appendChild(updateBtn);
    });

  } catch (error) {
    tableBody.innerHTML = `<tr><td colspan="4" class="error">❌ Error loading enrollments: ${error.message}</td></tr>`;
  }
}

function showProgressUpdateForm(enrollment) {
  document.getElementById('up-enrollment-id').value = enrollment.id;
  document.getElementById('up-course-title').textContent = (enrollment.course && enrollment.course.title) ? enrollment.course.title : '';
  document.getElementById('up-progress').value = enrollment.progress || 0;
  showView('progress-update-view');
}

document.getElementById('update-progress-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const enrollmentId = document.getElementById('up-enrollment-id').value;
  const newProgress = document.getElementById('up-progress').value;
  const msg = document.getElementById('progress-msg');
  msg.textContent = '';

  try {
    const response = await fetch(`${API_BASE_URL}/enrollments/${enrollmentId}/progress?progress=${encodeURIComponent(newProgress)}`, {
      method: 'PUT'
    });
    const body = await safeJson(response);
    if (!response.ok) throw new Error((body && (body.message || body)) || 'Failed to update progress.');
    msg.className = 'success';
    msg.textContent = '✅ Progress updated successfully!';
    showView('student-view');
    showStudentSubView('my-enrollments-view');
  } catch (error) {
    msg.className = 'error';
    msg.textContent = `❌ ${error.message}`;
    showView('student-view');
    showStudentSubView('my-enrollments-view');
  }
});

// --- Initial Setup ---

document.addEventListener('DOMContentLoaded', () => {
  updateHeader();
});