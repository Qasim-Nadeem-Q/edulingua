-- ========================================
-- EduLingua Database Migration V2
-- Seed Initial Data
-- Database: PostgreSQL (Latest)
-- ========================================

-- ========================================
-- STEP 1: Insert Roles
-- ========================================
INSERT INTO roles (id, name, description) VALUES
    ('a0000000-0000-0000-0000-000000000001'::uuid, 'ADMIN', 'System Administrator with full access to all features'),
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'STATE', 'State-level coordinator managing districts within a state'),
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'DISTRICT', 'District-level coordinator managing schools within a district'),
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'SCHOOL', 'School-level coordinator managing classes and students'),
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'CLASS', 'Class teacher managing students in a specific class'),
    ('a0000000-0000-0000-0000-000000000006'::uuid, 'STUDENT', 'Student taking language proficiency tests');

-- ========================================
-- STEP 2: Insert Permissions
-- ========================================

-- User Management Permissions
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-000000000001'::uuid, 'VIEW_USERS', 'users', 'READ', 'View user information'),
    ('b0000000-0000-0000-0000-000000000002'::uuid, 'CREATE_USERS', 'users', 'WRITE', 'Create new users'),
    ('b0000000-0000-0000-0000-000000000003'::uuid, 'UPDATE_USERS', 'users', 'WRITE', 'Update user information'),
    ('b0000000-0000-0000-0000-000000000004'::uuid, 'DELETE_USERS', 'users', 'DELETE', 'Delete users'),
    ('b0000000-0000-0000-0000-000000000005'::uuid, 'MANAGE_ROLES', 'users', 'EXECUTE', 'Assign and manage user roles');

-- Role & Permission Management
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-000000000006'::uuid, 'VIEW_ROLES', 'roles', 'READ', 'View roles and permissions'),
    ('b0000000-0000-0000-0000-000000000007'::uuid, 'CREATE_ROLES', 'roles', 'WRITE', 'Create new roles'),
    ('b0000000-0000-0000-0000-000000000008'::uuid, 'UPDATE_ROLES', 'roles', 'WRITE', 'Update existing roles'),
    ('b0000000-0000-0000-0000-000000000009'::uuid, 'DELETE_ROLES', 'roles', 'DELETE', 'Delete roles'),
    ('b0000000-0000-0000-0000-00000000000a'::uuid, 'MANAGE_PERMISSIONS', 'roles', 'EXECUTE', 'Assign permissions to roles');

-- Test Management Permissions
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-00000000000b'::uuid, 'VIEW_TESTS', 'tests', 'READ', 'View test information'),
    ('b0000000-0000-0000-0000-00000000000c'::uuid, 'CREATE_TESTS', 'tests', 'WRITE', 'Create new tests'),
    ('b0000000-0000-0000-0000-00000000000d'::uuid, 'UPDATE_TESTS', 'tests', 'WRITE', 'Update test information'),
    ('b0000000-0000-0000-0000-00000000000e'::uuid, 'DELETE_TESTS', 'tests', 'DELETE', 'Delete tests'),
    ('b0000000-0000-0000-0000-00000000000f'::uuid, 'TAKE_TESTS', 'tests', 'EXECUTE', 'Take language proficiency tests');

-- Question Management Permissions
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-000000000010'::uuid, 'VIEW_QUESTIONS', 'questions', 'READ', 'View test questions'),
    ('b0000000-0000-0000-0000-000000000011'::uuid, 'CREATE_QUESTIONS', 'questions', 'WRITE', 'Create test questions'),
    ('b0000000-0000-0000-0000-000000000012'::uuid, 'UPDATE_QUESTIONS', 'questions', 'WRITE', 'Update test questions'),
    ('b0000000-0000-0000-0000-000000000013'::uuid, 'DELETE_QUESTIONS', 'questions', 'DELETE', 'Delete test questions');

-- Score Management Permissions
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-000000000014'::uuid, 'VIEW_SCORES', 'scores', 'READ', 'View test scores'),
    ('b0000000-0000-0000-0000-000000000015'::uuid, 'UPDATE_SCORES', 'scores', 'WRITE', 'Update and grade test scores'),
    ('b0000000-0000-0000-0000-000000000016'::uuid, 'VIEW_OWN_SCORES', 'scores', 'READ', 'View own test scores');

-- Report Management Permissions
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-000000000017'::uuid, 'VIEW_REPORTS', 'reports', 'READ', 'View reports and analytics'),
    ('b0000000-0000-0000-0000-000000000018'::uuid, 'GENERATE_REPORTS', 'reports', 'EXECUTE', 'Generate custom reports'),
    ('b0000000-0000-0000-0000-000000000019'::uuid, 'EXPORT_REPORTS', 'reports', 'EXECUTE', 'Export reports to various formats');

-- Audit Log Permissions
INSERT INTO permissions (id, name, resource, action, description) VALUES
    ('b0000000-0000-0000-0000-00000000001a'::uuid, 'VIEW_AUDIT_LOGS', 'audit', 'READ', 'View audit logs for accountability');

-- ========================================
-- STEP 3: Assign Permissions to Roles
-- ========================================

-- ADMIN Role - Full Access to Everything
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'a0000000-0000-0000-0000-000000000001'::uuid, id FROM permissions;

-- STATE Role - Manage districts, schools, view reports
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- User management (limited)
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000001'::uuid), -- VIEW_USERS
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000002'::uuid), -- CREATE_USERS
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000003'::uuid), -- UPDATE_USERS
    -- Test management
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-00000000000b'::uuid), -- VIEW_TESTS
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-00000000000c'::uuid), -- CREATE_TESTS
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-00000000000d'::uuid), -- UPDATE_TESTS
    -- Score management
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000014'::uuid), -- VIEW_SCORES
    -- Report management
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000017'::uuid), -- VIEW_REPORTS
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000018'::uuid), -- GENERATE_REPORTS
    ('a0000000-0000-0000-0000-000000000002'::uuid, 'b0000000-0000-0000-0000-000000000019'::uuid); -- EXPORT_REPORTS

-- DISTRICT Role - Manage schools within district
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- User management (limited)
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-000000000001'::uuid), -- VIEW_USERS
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-000000000002'::uuid), -- CREATE_USERS
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-000000000003'::uuid), -- UPDATE_USERS
    -- Test management
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-00000000000b'::uuid), -- VIEW_TESTS
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-00000000000c'::uuid), -- CREATE_TESTS
    -- Score management
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-000000000014'::uuid), -- VIEW_SCORES
    -- Report management
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-000000000017'::uuid), -- VIEW_REPORTS
    ('a0000000-0000-0000-0000-000000000003'::uuid, 'b0000000-0000-0000-0000-000000000018'::uuid); -- GENERATE_REPORTS

-- SCHOOL Role - Manage classes and students within school
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- User management (limited to school)
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-000000000001'::uuid), -- VIEW_USERS
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-000000000002'::uuid), -- CREATE_USERS
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-000000000003'::uuid), -- UPDATE_USERS
    -- Test management
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-00000000000b'::uuid), -- VIEW_TESTS
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-00000000000c'::uuid), -- CREATE_TESTS
    -- Question management
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-000000000010'::uuid), -- VIEW_QUESTIONS
    -- Score management
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-000000000014'::uuid), -- VIEW_SCORES
    -- Report management
    ('a0000000-0000-0000-0000-000000000004'::uuid, 'b0000000-0000-0000-0000-000000000017'::uuid); -- VIEW_REPORTS

-- CLASS Role - Manage students in class, grade tests
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- User management (view only)
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-000000000001'::uuid), -- VIEW_USERS
    -- Test management
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-00000000000b'::uuid), -- VIEW_TESTS
    -- Question management
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-000000000010'::uuid), -- VIEW_QUESTIONS
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-000000000011'::uuid), -- CREATE_QUESTIONS
    -- Score management
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-000000000014'::uuid), -- VIEW_SCORES
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-000000000015'::uuid), -- UPDATE_SCORES
    -- Report management
    ('a0000000-0000-0000-0000-000000000005'::uuid, 'b0000000-0000-0000-0000-000000000017'::uuid); -- VIEW_REPORTS

-- STUDENT Role - Take tests, view own scores
INSERT INTO role_permissions (role_id, permission_id) VALUES
    -- Test taking
    ('a0000000-0000-0000-0000-000000000006'::uuid, 'b0000000-0000-0000-0000-00000000000b'::uuid), -- VIEW_TESTS
    ('a0000000-0000-0000-0000-000000000006'::uuid, 'b0000000-0000-0000-0000-00000000000f'::uuid), -- TAKE_TESTS
    -- Score viewing (own scores only)
    ('a0000000-0000-0000-0000-000000000006'::uuid, 'b0000000-0000-0000-0000-000000000016'::uuid); -- VIEW_OWN_SCORES

-- ========================================
-- STEP 4: Create Default Admin User
-- Password: Admin@123 (BCrypt hashed)
-- Note: This should be changed immediately after first login
-- ========================================
INSERT INTO users (id, email, username, name, password, phone_number, active, email_verified)
VALUES (
    'c0000000-0000-0000-0000-000000000001'::uuid,
    'admin@edulingua.com',
    'admin',
    'System Administrator',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', -- Admin@123
    '+1234567890',
    true,
    true
);

-- Assign ADMIN role to default admin user
INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000001'::uuid, 'a0000000-0000-0000-0000-000000000001'::uuid);

-- ========================================
-- STEP 5: Create Sample Users for Testing
-- ========================================

-- Sample State Coordinator
INSERT INTO users (id, email, username, name, password, state_code, state_name, active, email_verified)
VALUES (
    'c0000000-0000-0000-0000-000000000002'::uuid,
    'state.coordinator@edulingua.com',
    'state_coord',
    'State Coordinator',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', -- Admin@123
    'MH',
    'Maharashtra',
    true,
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000002'::uuid, 'a0000000-0000-0000-0000-000000000002'::uuid);

-- Sample District Coordinator
INSERT INTO users (id, email, username, name, password, state_code, state_name, district_code, district_name, active, email_verified)
VALUES (
    'c0000000-0000-0000-0000-000000000003'::uuid,
    'district.coordinator@edulingua.com',
    'district_coord',
    'District Coordinator',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', -- Admin@123
    'MH',
    'Maharashtra',
    'MH01',
    'Mumbai',
    true,
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000003'::uuid, 'a0000000-0000-0000-0000-000000000003'::uuid);

-- Sample School Coordinator
INSERT INTO users (id, email, username, name, password, state_code, state_name, district_code, district_name, school_code, school_name, active, email_verified)
VALUES (
    'c0000000-0000-0000-0000-000000000004'::uuid,
    'school.coordinator@edulingua.com',
    'school_coord',
    'School Coordinator',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', -- Admin@123
    'MH',
    'Maharashtra',
    'MH01',
    'Mumbai',
    'MH01-001',
    'Model High School',
    true,
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000004'::uuid, 'a0000000-0000-0000-0000-000000000004'::uuid);

-- Sample Class Teacher
INSERT INTO users (id, email, username, name, password, state_code, state_name, district_code, district_name, school_code, school_name, class_code, class_name, active, email_verified)
VALUES (
    'c0000000-0000-0000-0000-000000000005'::uuid,
    'class.teacher@edulingua.com',
    'class_teacher',
    'Class Teacher',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', -- Admin@123
    'MH',
    'Maharashtra',
    'MH01',
    'Mumbai',
    'MH01-001',
    'Model High School',
    'CLASS-10A',
    'Class 10-A',
    true,
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000005'::uuid, 'a0000000-0000-0000-0000-000000000005'::uuid);

-- Sample Student
INSERT INTO users (id, email, username, name, password, state_code, state_name, district_code, district_name, school_code, school_name, class_code, class_name, roll_number, date_of_birth, parent_email, active, email_verified)
VALUES (
    'c0000000-0000-0000-0000-000000000006'::uuid,
    'student.demo@edulingua.com',
    'student_demo',
    'Demo Student',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhCu', -- Admin@123
    'MH',
    'Maharashtra',
    'MH01',
    'Mumbai',
    'MH01-001',
    'Model High School',
    'CLASS-10A',
    'Class 10-A',
    'ROLL-2024-001',
    '2010-05-15 00:00:00',
    'parent@example.com',
    true,
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000006'::uuid, 'a0000000-0000-0000-0000-000000000006'::uuid);

-- ========================================
-- STEP 6: Create Initial Audit Log Entry
-- ========================================
INSERT INTO audit_logs (user_id, user_email, user_roles, action, resource_type, description, ip_address)
VALUES (
    'c0000000-0000-0000-0000-000000000001'::uuid,
    'admin@edulingua.com',
    'ADMIN',
    'SYSTEM_INIT',
    'SYSTEM',
    'Database initialized with seed data',
    '127.0.0.1'
);
