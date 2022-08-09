package com.System.repository;

import com.System.entity.Poll;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<Poll,Long> {

    Optional<Poll> findById(Long pollId);

    List<Poll> findByIdIn(List<Long> pollIds, Sort sort);
}
