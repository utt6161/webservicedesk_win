package com.wsd.ska.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegTokenRepository extends JpaRepository<RegToken,Long> {
    RegToken findByUserId(long id);
}
