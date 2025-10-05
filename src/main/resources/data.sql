-- 개발용 초기 데이터 (ID 자동 생성)
INSERT INTO users (username, email, name, phone, password, status, is_admin, created_at) VALUES 
('admin', 'admin@example.com', '관리자', '010-1234-5678', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', true, CURRENT_TIMESTAMP),
('user1', 'user1@example.com', '사용자1', '010-2345-6789', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', false, CURRENT_TIMESTAMP),
('user2', 'user2@example.com', '사용자2', '010-3456-7890', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', false, CURRENT_TIMESTAMP);

INSERT INTO posts (title, content, author_id, created_at) VALUES 
('첫 번째 게시글', '안녕하세요! 첫 번째 게시글입니다.', 1, CURRENT_TIMESTAMP),
('두 번째 게시글', '두 번째 게시글의 내용입니다.', 2, CURRENT_TIMESTAMP),
('세 번째 게시글', '세 번째 게시글입니다.', 1, CURRENT_TIMESTAMP);
