package com.guham.guham.tag;

import com.guham.guham.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Tag findOrCreate(String tagTitle) {
        Tag byTitle = tagRepository.findByTitle(tagTitle);
        if(byTitle == null){
            byTitle = tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        return byTitle;
    }
}
