package io.spring.sample;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

  @Modifying
  @Query("delete from Todo t where t.completed=true")
  @Transactional
  void deleteCompleted();

}
