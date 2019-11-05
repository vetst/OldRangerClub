package ru.java.mentor.oldranger.club.service.user.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.java.mentor.oldranger.club.exceptions.passwordrecovery.PasswordRecoveryInvalidToken;
import ru.java.mentor.oldranger.club.exceptions.passwordrecovery.PasswordRecoveryTokenExpired;
import ru.java.mentor.oldranger.club.model.user.PasswordRecoveryToken;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.service.mail.MailService;
import ru.java.mentor.oldranger.club.service.user.PasswordRecoveryService;
import ru.java.mentor.oldranger.club.service.user.PasswordRecoveryTokenService;
import ru.java.mentor.oldranger.club.service.user.UserService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordRecoveryTokenService passwordRecoveryTokenService;

    @Value("${server.protocol}")
    private String SERVER_PROTOCOL;

    @Value("${server.name}")
    private String SERVER_HOST;

    @Value("${server.port}")
    private String SERVER_PORT;

    @Value("${project.secret-word-jwt}")
    private String JWT_SECRET;

    @Value("${project.password-recovery-token-expiration}")
    private String PASSWORD_RECOVERY_TOKEN_EXPIRATION_PATTERN;

    private final String TOKEN_CLAIM = "userid";

    @Override
    public void sendRecoveryTokenToEmail(User user) {
        LocalDateTime issueTime = LocalDateTime.now();
        LocalDateTime expirationTime = calculateExpirationTime(issueTime);

        String token = createTokenForUser(user, localDateTimeToDate(expirationTime));
        saveTokenToDB(user, token, issueTime);

        composeAndSendTokenEmail(user, token);
    }

    @Override
    public PasswordRecoveryToken validateToken(String recoveryToken) throws PasswordRecoveryInvalidToken, PasswordRecoveryTokenExpired {
        verifyToken(recoveryToken);
        Long userId = getUserIdFromToken(recoveryToken);
        return verifyTokenPersistCompare(recoveryToken, userId);
    }

    @Override
    public void updatePassword(PasswordRecoveryToken recoveryToken, String password) {
        User user = recoveryToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        userService.save(user);
        passwordRecoveryTokenService.delete(recoveryToken);
    }

    private void composeAndSendTokenEmail(User user, String token) {
        String msgSubject = "Восстановление пароля";
        String recoverURL = getFullHostName() + "/passwordrecovery/token/" + token;
        mailService.send(user.getEmail(), msgSubject, recoverURL);
    }

    private PasswordRecoveryToken verifyTokenPersistCompare(String token, long userId) throws PasswordRecoveryInvalidToken {
        PasswordRecoveryToken dbToken = passwordRecoveryTokenService.getByUserId(userId);
        if (!(dbToken != null && dbToken.getToken().equals(token))) {
            throw new PasswordRecoveryInvalidToken();
        }
        return dbToken;
    }

    private void verifyToken(String token) throws PasswordRecoveryInvalidToken, PasswordRecoveryTokenExpired {
        try {
            JWT.require(Algorithm.HMAC512(JWT_SECRET))
                    .build()
                    .verify(token);
        } catch (TokenExpiredException e) {
            throw new PasswordRecoveryTokenExpired();
        } catch (Exception e) {
            throw new PasswordRecoveryInvalidToken();
        }
    }

    private Long getUserIdFromToken(String token) throws PasswordRecoveryInvalidToken {
        DecodedJWT decodedToken;
        try {
            decodedToken = JWT.decode(token);
        } catch (JWTDecodeException e) {
            throw new PasswordRecoveryInvalidToken();
        }
        Long userId = decodedToken.getClaim(TOKEN_CLAIM).asLong();
        if (userId == null) {
            throw new PasswordRecoveryInvalidToken();
        }
        return userId;
    }

    private String getFullHostName() {
        //TODO construct host name for production in another way
        return SERVER_PROTOCOL + "://" + SERVER_HOST + ":" + SERVER_PORT;
    }

    private void saveTokenToDB(User user, String token, LocalDateTime issueTime) {
        PasswordRecoveryToken recoveryToken = new PasswordRecoveryToken(user, issueTime, token);
        passwordRecoveryTokenService.saveOrUpdateIfExist(recoveryToken);
    }

    private String createTokenForUser(User user, Date expirationTime) {
        return JWT
                .create()
                .withClaim(TOKEN_CLAIM, user.getId())
                .withExpiresAt(expirationTime)
                .sign(Algorithm.HMAC512(JWT_SECRET));
    }

    private LocalDateTime calculateExpirationTime(LocalDateTime startTime) {
        String[] DHM = PASSWORD_RECOVERY_TOKEN_EXPIRATION_PATTERN.split("-");
        long daysOffset = Long.parseLong(DHM[0]);
        long hoursOffset = Long.parseLong(DHM[1]);
        long minutesOffset = Long.parseLong(DHM[2]);
        return startTime
                .plusDays(daysOffset)
                .plusHours(hoursOffset)
                .plusMinutes(minutesOffset);
    }

    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
