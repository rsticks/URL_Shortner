package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.UserUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserUrlRepository extends JpaRepository<UserUrl, String> {
    List<UserUrl> findAllByUser_Id(Long userId);

    List<UserUrl> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}


