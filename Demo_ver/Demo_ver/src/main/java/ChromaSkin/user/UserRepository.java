package ChromaSkin.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // username으로 사용자 찾기
    User findByUsername(String username);

    // 이메일로 사용자 찾기 (아이디 찾기 기능용)
    User findByEmail(String email);

    // 닉네임으로 사용자 찾기
    User findByNickname(String nickname);

    // 아이디와 이메일로 사용자 찾기 (비밀번호 찾기 기능용)
    User findByUsernameAndEmail(String username, String email);

    // 중복 아이디, 닉네임, 이메일 체크를 위한 사용자 존재 여부 확인 (추가된 메서드)
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
}

