package ru.java.mentor.oldranger.club.dao.UserRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.java.mentor.oldranger.club.model.user.RequestInvitation;

public interface RequestInvitationRepository extends JpaRepository<RequestInvitation, Long> {
}