-- 초기 사용자 데이터 (개발/배포 공통)
-- PostgreSQL 호환성을 위해 ON CONFLICT 처리 추가
INSERT INTO users (username, email, name, phone, password, status, is_admin, created_at) VALUES 
('admin', 'admin@mytestweb.com', '관리자', '010-1234-5678', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', true, CURRENT_TIMESTAMP),
('demo_user', 'demo@mytestweb.com', '데모사용자', '010-9876-5432', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', false, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
