package sesac.spring_boot_security.security;
// security 패키지: 인증 / 인가에 관련된 클래스 모음

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sesac.spring_boot_security.config.JwtProperties.JwtProperties;
import sesac.spring_boot_security.entity.UserEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
// 사용자 정보를 받아서 JWT 를 생성하는 클래스
public class TokenProvider {
//    private static final String SECRET_KEY = "sesac-springboot-12341234"; // (임의문자)

    // [after] JwtProperties 클래스 이용하여 설정 파일 값 꺼내오기
    @Autowired
    private JwtProperties jwtProperties;

    // create(): JWT 생성
    public String create(UserEntity userEntity) {
        Date expiryDate = Date.from(
                Instant.now().plus(1, ChronoUnit.DAYS)
        );; // 기한: 지금으로부터 1일

        // JWT 토큰 생성
        return Jwts.builder()
                // header(token 구성요소 중 하나) 에 들어갈 내용 및 서명을 하기 위한 설정
//                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())

                // payload 에 들어갈 내용
                .setSubject(String.valueOf(userEntity.getId())) // 토큰 제목 (setSubject 는 string 을 매개변수로 받으므로 getId를 받아서 형변환 함)
//                .setIssuer("demo app") // iss: 토큰 발급자
                .setIssuer(jwtProperties.getIssuer()) // iss: 토큰 발급자
                .setIssuedAt(new Date()) // iat: 토큰이 발급된 시간
                .setExpiration(expiryDate) // exp: 토큰 만료 시간

                // 토큰 생성
                .compact();

    }

    // validateAndGetUserId(): 토큰 디코딩 및 파싱하고 토큰 위조 여부 확인 -> 사용자의 아이디 리턴
    public String validateAndGetUserId(String token) {
        // parseClaimsJws(): Base64 로 디코딩 / 파싱
        // - header 와 payload 를 setSigningKey() 의 인자로 넘어온 비밀키를 이용해 서명한 다음
        //      token 의 서명과 비교
        // - 두개의 값이 일치한다면 (일치한다면) payload 리턴
        // - 두개의 값이 불일치한다면 (위조되었으므로) 예외 날림
        // - 결국 userId 가 필요하므로 getBody 를 부름
        Claims claims = Jwts.parser() // 파서 생성
//                .setSigningKey(SECRET_KEY) // 서명 검증을 위해 비밀키 입력
                .setSigningKey(jwtProperties.getSecretKey()) // 서명 검증을 위해 비밀키 입력
                .parseClaimsJws(token) // 토큰을 파싱하고 서명 검증 -> 토큰 위조 예외 발생
                .getBody(); // 검증된 토큰의 본문(claims)을 가져옴

        // claims 에서 subject (사용자 id) 를 추출해서 반환
        return claims.getSubject();
    }

}
