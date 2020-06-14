package com.wsd.ska.messages;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MessagesRepository extends PagingAndSortingRepository<Message, Long> {
    List<Message> findByRequestId(long id);
    Page<Message> findByRequestId(long request_id, Pageable pageable);
}
