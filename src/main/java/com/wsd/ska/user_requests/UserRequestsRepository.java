package com.wsd.ska.user_requests;

import com.wsd.ska.messages.Message;
import com.wsd.ska.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface UserRequestsRepository extends PagingAndSortingRepository<UserRequests, Long> {

    User findByUser(User user, Sort sort);
    UserRequests findById(long id);

    List<UserRequests> findAll();
    Page<UserRequests> findByUser(User user, Pageable Pageable);
    List<UserRequests> findByUser(User user);
    Page<UserRequests> findByUserUsernameLikeIgnoreCase(String username, Pageable Pageable);
    Page<UserRequests> findByTitleLikeIgnoreCase(String title, Pageable Pageable);
    Page<UserRequests> findByDescriptionLikeIgnoreCase(String description, Pageable Pageable);

    Page<UserRequests> findByTitleLikeIgnoreCaseAndUserUsername(String title, String name, Pageable Pageable);
    Page<UserRequests> findByDescriptionLikeIgnoreCaseAndUserUsername(String description, String name, Pageable Pageable);

    long countAllByUserUsername(String username);
    Page<UserRequests> findByStatus(int status, Pageable Pageable);

}