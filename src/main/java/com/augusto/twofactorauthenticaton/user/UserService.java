package com.augusto.twofactorauthenticaton.user;

import com.augusto.twofactorauthenticaton.exceptions.BadRequestException;
import com.augusto.twofactorauthenticaton.sms.SmsSender;
import com.augusto.twofactorauthenticaton.user.role.Role;
import com.augusto.twofactorauthenticaton.user.role.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.augusto.twofactorauthenticaton.security.SecurityConstants.HEADER_OTP;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Autowired
    @Lazy
    RoleService roleService;

    @Autowired
    @Lazy
    UserRepository userRepository;

    @Autowired
    @Lazy
    SmsSender smsSender;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        Role role = roleService.findByName("user");
        return userRepository
                .findByUsernameAndRole(username, role)
                .orElseThrow(() -> new BadRequestException("User not authenticated"));
    }

    public void validateOTP(HttpServletResponse res, Authentication authentication, User creds) {
        User principal = (User) authentication.getPrincipal();
        if(principal.isTwoFactorAuthentication()) {
            if (isEmpty(creds.getCode())) {
                sendToken(principal);
                res.addHeader(HEADER_OTP, "OTP_REQUIRED");
                throw new RuntimeException();
            }

            if (!StringUtils.equalsIgnoreCase(creds.getCode(), principal.getCode())) {
                throw new RuntimeException();
            }

            long minutes = principal.getExpiration().until(LocalDateTime.now(), ChronoUnit.MINUTES);
            if(minutes > 30) {
                sendToken(principal);
                throw new RuntimeException();
            }

            resetOTP(principal);
        }
    }

    @Async
    public void resetOTP(User user) {
        user.setCode(null);
        user.setExpiration(null);
        userRepository.save(user);
    }

    @Async
    public void sendToken(User user) {
        String code = UUID.randomUUID().toString();
        user.setCode(code);
        user.setExpiration(LocalDateTime.now());
        userRepository.save(user);

        smsSender.send(user.getPhone(), code);

        log.info(code);
    }
}
