package com.guham.guham.tag;

import com.guham.guham.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Transactional(readOnly = true)
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTagTitle(String tagTitle);
}
