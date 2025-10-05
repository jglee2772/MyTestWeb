-- 초기 사용자 데이터 (개발/배포 공통)
INSERT INTO users (username, email, name, phone, password, status, is_admin, created_at) VALUES 
('admin', 'admin@mytestweb.com', '관리자', '010-1234-5678', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', true, CURRENT_TIMESTAMP),
('demo_user', 'demo@mytestweb.com', '데모사용자', '010-9876-5432', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', false, CURRENT_TIMESTAMP);

-- 초기 게시물 데이터 (개발/배포 공통)
INSERT INTO posts (title, content, author, created_at) VALUES 
('환영합니다!', 'MyTestWeb에 오신 것을 환영합니다!', 'admin', CURRENT_TIMESTAMP),
('첫 번째 게시물', '이것은 첫 번째 게시물입니다.', 'demo_user', CURRENT_TIMESTAMP);
