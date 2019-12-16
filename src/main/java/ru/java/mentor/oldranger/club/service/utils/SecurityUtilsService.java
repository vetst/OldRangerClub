package ru.java.mentor.oldranger.club.service.utils;

import ru.java.mentor.oldranger.club.model.user.Role;
import ru.java.mentor.oldranger.club.model.user.User;

import java.util.List;

public interface SecurityUtilsService {
    boolean isAuthorityReachableForLoggedUser(Role role);
    /**
     * Авторизованный пользователь не ниже ROLE_USER
     */
    boolean isLoggedUserIsUser();
    User getLoggedUser();
    List<Long> getUsersFromSessionRegistry();
}
