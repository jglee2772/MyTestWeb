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
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("데이터 초기화 시작");
        
        // 테이블이 없으면 생성 (ddl-auto: update는 테이블이 없으면 생성하지 않을 수 있음)
        ensureTablesExist();
        
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
        try {
            // jdbcTemplate을 사용하여 커넥션 풀 관리 (더 안전함)
            logger.info("데이터베이스 테이블 확인 시작");
            
            // users 테이블 확인 및 생성
            boolean usersTableExists = tableExistsDirect();
            logger.info("users 테이블 존재 여부: {}", usersTableExists);
            if (!usersTableExists) {
                logger.warn("users 테이블이 없습니다. 생성합니다...");
                createUsersTable();
                // 생성 후 다시 확인
                if (tableExistsDirect()) {
                    logger.info("✓ users 테이블 생성 성공");
                } else {
                    logger.error("✗ users 테이블 생성 실패");
                    throw new RuntimeException("users 테이블 생성 실패");
                }
            } else {
                logger.info("✓ users 테이블 이미 존재");
            }
            
            // posts 테이블 확인 및 생성
            boolean postsTableExists = tableExistsDirect("posts");
            logger.info("posts 테이블 존재 여부: {}", postsTableExists);
            if (!postsTableExists) {
                logger.warn("posts 테이블이 없습니다. 생성합니다...");
                createPostsTable();
                if (tableExistsDirect("posts")) {
                    logger.info("✓ posts 테이블 생성 성공");
                } else {
                    logger.error("✗ posts 테이블 생성 실패");
                    throw new RuntimeException("posts 테이블 생성 실패");
                }
            } else {
                logger.info("✓ posts 테이블 이미 존재");
            }
            
            // chat_messages 테이블 확인 및 생성
            boolean chatMessagesTableExists = tableExistsDirect("chat_messages");
            logger.info("chat_messages 테이블 존재 여부: {}", chatMessagesTableExists);
            if (!chatMessagesTableExists) {
                logger.warn("chat_messages 테이블이 없습니다. 생성합니다...");
                createChatMessagesTable();
                if (tableExistsDirect("chat_messages")) {
                    logger.info("✓ chat_messages 테이블 생성 성공");
                } else {
                    logger.error("✗ chat_messages 테이블 생성 실패");
                    throw new RuntimeException("chat_messages 테이블 생성 실패");
                }
            } else {
                logger.info("✓ chat_messages 테이블 이미 존재");
            }
            
            // 인덱스 생성
            createIndexes();
            
            logger.info("✓ 테이블 확인/생성 완료");
            
        } catch (Exception e) {
            logger.error("✗ 테이블 확인/생성 중 오류 발생", e);
            throw new RuntimeException("테이블 초기화 실패", e);
        }
    }
    
    private boolean tableExistsDirect() {
        return tableExistsDirect("users");
    }
    
    private boolean tableExistsDirect(String tableName) {
        try {
            String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName.toLowerCase());
            return exists != null && exists;
        } catch (Exception e) {
            logger.warn("테이블 확인 중 오류 (테이블이 없을 수 있음): {}", e.getMessage());
            return false;
        }
    }


    private void createUsersTable() {
        // PostgreSQL에서 스키마 명시
        String sql = """
            CREATE TABLE IF NOT EXISTS public.users (
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
        try {
            jdbcTemplate.execute(sql);
            logger.info("users 테이블 생성 SQL 실행 완료");
        } catch (Exception e) {
            logger.error("users 테이블 생성 SQL 실행 중 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createPostsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.posts (
                id BIGSERIAL PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                author_id BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (author_id) REFERENCES public.users(id)
            )
            """;
        try {
            jdbcTemplate.execute(sql);
            logger.info("posts 테이블 생성 SQL 실행 완료");
        } catch (Exception e) {
            logger.error("posts 테이블 생성 SQL 실행 중 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createChatMessagesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS public.chat_messages (
                id BIGSERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                message TEXT NOT NULL,
                message_type VARCHAR(20) NOT NULL DEFAULT 'CHAT',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try {
            jdbcTemplate.execute(sql);
            logger.info("chat_messages 테이블 생성 SQL 실행 완료");
        } catch (Exception e) {
            logger.error("chat_messages 테이블 생성 SQL 실행 중 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void createIndexes() {
        try {
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON public.users(username)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_status ON public.users(status)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON public.chat_messages(created_at)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_posts_author_id ON public.posts(author_id)");
            logger.debug("인덱스 생성 완료");
        } catch (Exception e) {
            logger.warn("인덱스 생성 중 오류 (이미 존재할 수 있음): {}", e.getMessage());
        }
    }
}

