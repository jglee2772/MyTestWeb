-- H2 데이터베이스용 초기 데이터
-- H2는 create-drop 모드이므로 중복 체크 없이 INSERT 사용

INSERT INTO users (username, email, name, phone, password, status, is_admin, created_at) VALUES 
('admin', 'admin@mytestweb.com', '관리자', '010-1234-5678', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', true, CURRENT_TIMESTAMP),
('demo_user', 'demo@mytestweb.com', '데모사용자', '010-9876-5432', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', false, CURRENT_TIMESTAMP);

INSERT INTO posts (title, content, author_id, created_at) VALUES 
('환영합니다!', 'MyTestWeb에 오신 것을 환영합니다!', 1, CURRENT_TIMESTAMP),
('데모 게시글', '이것은 데모 게시글입니다.', 2, CURRENT_TIMESTAMP);
