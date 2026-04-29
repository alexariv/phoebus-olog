package org.phoebus.olog.ai;

import org.phoebus.olog.TagRepository;
import org.phoebus.olog.LogbookRepository;
import org.phoebus.olog.entity.Tag;
import org.phoebus.olog.entity.Logbook;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MetadataService {

    private final TagRepository tagRepository;
    private final LogbookRepository logbookRepository;

    public MetadataService(TagRepository tagRepository,
                           LogbookRepository logbookRepository) {
        this.tagRepository = tagRepository;
        this.logbookRepository = logbookRepository;
    }

    public List<String> getTags() {
        return StreamSupport.stream(tagRepository.findAll().spliterator(), false)
                .map(Tag::getName)
                .filter(name -> name != null)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getLogbooks() {
        return StreamSupport.stream(logbookRepository.findAll().spliterator(), false)
                .map(Logbook::getName)
                .filter(name -> name != null)
                .sorted()
                .collect(Collectors.toList());
    }
}