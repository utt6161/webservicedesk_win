package com.wsd.ska.attachments;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentsRepository extends JpaRepository<Attachments, Long> {

    List<Attachments> findByOrderByImageDesc();
    List<Attachments> findByMessageId(long id);
}
