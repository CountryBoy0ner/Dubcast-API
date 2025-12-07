package com.Tsimur.Dubcast.repository;

import com.Tsimur.Dubcast.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByOrderByCreatedAtDesc(Pageable pageable);


}
