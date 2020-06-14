package com.wsd.ska.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.token = :token WHERE u.username = :username")
    @Transactional
    int updateTokenByUsername(@Param("username") String username, @Param("token") String token);

    User findByPasswordTokenToken(String token);
    User findByRegisterTokenToken(String token);



    User findByPhonenumber(String phone);
    User findByUsername(String username);
    User findByEmail(String email);
    Page<User> findByUsernameLikeIgnoreCase(String username, Pageable Pageable);
    Page<User> findByEmailLikeIgnoreCase(String email, Pageable Pageable);
    Page<User> findByPostLikeIgnoreCase(String post, Pageable Pageable);
    Page<User> findByFullnameLikeIgnoreCase(String fullname, Pageable Pageable);
    Page<User> findByPhonenumberLikeIgnoreCase(String phonenumber, Pageable Pageable);
    User findById(long id);
    Page<User> findByActive(boolean active, Pageable Pageable);
    Page<User> findAll(Pageable Pageable);
}
