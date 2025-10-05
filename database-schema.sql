-- PostgreSQL 데이터베이스 스키마 생성
-- Render PostgreSQL 데이터베이스에서 실행

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 게시글 테이블
CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
);

-- 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'CHAT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);

-- 초기 데이터 삽입
INSERT INTO users (username, email, name, phone, password, status, is_admin, created_at) VALUES 
('admin', 'admin@mytestweb.com', '관리자', '010-1234-5678', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', true, CURRENT_TIMESTAMP),
('demo_user', 'demo@mytestweb.com', '데모사용자', '010-9876-5432', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'APPROVED', false, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

INSERT INTO posts (title, content, author_id, created_at) VALUES 
('환영합니다!', 'MyTestWeb에 오신 것을 환영합니다!', 1, CURRENT_TIMESTAMP),
('데모 게시글', '이것은 데모 게시글입니다.', 2, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
