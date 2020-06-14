package com.wsd.ska.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface PassTokenRepository extends JpaRepository<PassToken, Long> {

    PassToken findByToken(String token);
//    List<PassToken> findByToken(String token);
    PassToken findByUserUsername(String username);

}
