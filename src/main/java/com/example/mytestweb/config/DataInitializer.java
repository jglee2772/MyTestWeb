package com.example.mytestweb.config;

import com.example.mytestweb.entity.User;
import com.example.mytestweb.entity.UserStatus;
import com.example.mytestweb.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Optional;

@Component
@Order(1)  // 다른 초기화보다 먼저 실행
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("데이터 초기화 시작");
        
        // Hibernate가 ddl-auto: update로 테이블을 생성하므로, 여기서는 사용자 데이터만 관리
        // 관리자 계정 확인 및 생성/업데이트
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@mytestweb.com");
            admin.setName("관리자");
            admin.setPhone("010-1234-5678");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setStatus(UserStatus.APPROVED);
            admin.setAdmin(true);
            admin.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(admin);
            logger.info("관리자 계정 생성 완료: admin");
        } else {
            User admin = adminOpt.get();
            // 관리자 계정이 있지만 상태가 APPROVED가 아니거나 권한이 없으면 업데이트
            if (admin.getStatus() != UserStatus.APPROVED || !admin.isAdmin()) {
                admin.setStatus(UserStatus.APPROVED);
                admin.setAdmin(true);
                // 비밀번호가 올바른 형식이 아니면 재설정
                if (!admin.getPassword().startsWith("$2a$")) {
                    admin.setPassword(passwordEncoder.encode("password"));
                }
                userRepository.save(admin);
                logger.info("관리자 계정 업데이트 완료: admin");
            } else {
                logger.debug("관리자 계정 이미 존재: admin");
            }
        }

        // 데모 사용자 계정 확인 및 생성/업데이트
        Optional<User> demoOpt = userRepository.findByUsername("demo_user");
        if (demoOpt.isEmpty()) {
            User demoUser = new User();
            demoUser.setUsername("demo_user");
            demoUser.setEmail("demo@mytestweb.com");
            demoUser.setName("데모사용자");
            demoUser.setPhone("010-9876-5432");
            demoUser.setPassword(passwordEncoder.encode("password"));
            demoUser.setStatus(UserStatus.APPROVED);
            demoUser.setAdmin(false);
            demoUser.setCreatedAt(java.time.LocalDateTime.now());
            userRepository.save(demoUser);
            logger.info("데모 사용자 계정 생성 완료: demo_user");
        } else {
            User demoUser = demoOpt.get();
            // 데모 사용자 계정이 있지만 상태가 APPROVED가 아니면 업데이트
            if (demoUser.getStatus() != UserStatus.APPROVED) {
                demoUser.setStatus(UserStatus.APPROVED);
                // 비밀번호가 올바른 형식이 아니면 재설정
                if (!demoUser.getPassword().startsWith("$2a$")) {
                    demoUser.setPassword(passwordEncoder.encode("password"));
                }
                userRepository.save(demoUser);
                logger.info("데모 사용자 계정 업데이트 완료: demo_user");
            } else {
                logger.debug("데모 사용자 계정 이미 존재: demo_user");
            }
        }
        
        logger.info("데이터 초기화 완료");
    }

    private void ensureTablesExist() {
        try (Connection connection = dataSource.getConnection()) {
            logger.info("데이터베이스 연결 확인: {}", connection.getMetaData().getDatabaseProductName());
            DatabaseMetaData metaData = connection.getMetaData();
            
            // users 테이블 확인
            boolean usersTableExists = tableExists(metaData, "users");
            logger.info("users 테이블 존재 여부: {}", usersTableExists);
            if (!usersTableExists) {
                logger.warn("users 테이블이 없습니다. 생성합니다...");
                createUsersTable();
                // 생성 후 다시 확인
                if (tableExists(metaData, "users")) {
                    logger.info("users 테이블 생성 성공");
                } else {
                    logger.error("users 테이블 생성 실패");
                }
            } else {
                logger.info("users 테이블 이미 존재");
            }
            
            // posts 테이블 확인
            boolean postsTableExists = tableExists(metaData, "posts");
            logger.info("posts 테이블 존재 여부: {}", postsTableExists);
            if (!postsTableExists) {
                logger.warn("posts 테이블이 없습니다. 생성합니다...");
                createPostsTable();
            } else {
                logger.info("posts 테이블 이미 존재");
            }
            
            // chat_messages 테이블 확인
            boolean chatMessagesTableExists = tableExists(metaData, "chat_messages");
            logger.info("chat_messages 테이블 존재 여부: {}", chatMessagesTableExists);
            if (!chatMessagesTableExists) {
                logger.warn("chat_messages 테이블이 없습니다. 생성합니다...");
                createChatMessagesTable();
            } else {
                logger.info("chat_messages 테이블 이미 존재");
            }
            
            // 인덱스 생성
            createIndexes();
            
            logger.info("테이블 확인/생성 완료");
            
        } catch (Exception e) {
            logger.error("테이블 확인/생성 중 오류 발생", e);
            throw new RuntimeException("테이블 초기화 실패", e);
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws Exception {
        // PostgreSQL에서는 여러 방법으로 확인
        // 방법 1: getTables로 확인 (소문자, 대문자, 원본 모두 시도)
        String[] patterns = {tableName.toLowerCase(), tableName.toUpperCase(), tableName};
        for (String pattern : patterns) {
            try (ResultSet tables = metaData.getTables(null, "public", pattern, new String[]{"TABLE"})) {
                if (tables.next()) {
                    logger.debug("테이블 존재 확인 (패턴: {}): {} = true", pattern, tableName);
                    return true;
                }
            }
        }
        
        // 방법 2: 직접 SQL 쿼리로 확인 (더 확실함)
        try {
            String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName.toLowerCase());
            logger.debug("테이블 존재 확인 (SQL): {} = {}", tableName, exists);
            return exists != null && exists;
        } catch (Exception e) {
            logger.warn("SQL로 테이블 확인 중 오류 (무시): {}", e.getMessage());
            return false;
        }
    }

    private void createUsersTable() {
        String sql = """
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
            )
            """;
        jdbcTemplate.execute(sql);
        logger.info("users 테이블 생성 완료");
    }

    private void createPostsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS posts (
                id BIGSERIAL PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                author_id BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (author_id) REFERENCES users(id)
            )
            """;
        jdbcTemplate.execute(sql);
        logger.info("posts 테이블 생성 완료");
    }

    private void createChatMessagesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS chat_messages (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                message TEXT NOT NULL,
                message_type VARCHAR(20) NOT NULL DEFAULT 'CHAT',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        jdbcTemplate.execute(sql);
        logger.info("chat_messages 테이블 생성 완료");
    }

    private void createIndexes() {
        try {
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_status ON users(status)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id)");
            logger.debug("인덱스 생성 완료");
        } catch (Exception e) {
            logger.warn("인덱스 생성 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
        }
    }
}

