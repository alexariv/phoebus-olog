package org.phoebus.olog.ai;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.phoebus.olog.LevelRepository;
import org.phoebus.olog.LogbookRepository;
import org.phoebus.olog.TagRepository;
import org.phoebus.olog.entity.Level;
import org.phoebus.olog.entity.Logbook;
import org.phoebus.olog.entity.Tag;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    private final TagRepository tagRepository;
    private final LogbookRepository logbookRepository;
    private final LevelRepository levelRepository;

    public MetadataService(TagRepository tagRepository,
                           LogbookRepository logbookRepository,
                           LevelRepository levelRepository) {
        this.tagRepository = tagRepository;
        this.logbookRepository = logbookRepository;
        this.levelRepository = levelRepository;
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

    public List<String> getLevels() {
        return StreamSupport.stream(levelRepository.findAll().spliterator(), false)
                .map(Level::name)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }
}
