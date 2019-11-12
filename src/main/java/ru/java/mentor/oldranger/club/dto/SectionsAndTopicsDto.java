package ru.java.mentor.oldranger.club.dto;

import lombok.Getter;
import lombok.Setter;
import ru.java.mentor.oldranger.club.model.forum.Section;
import ru.java.mentor.oldranger.club.model.forum.Topic;
import java.util.List;

@Getter
@Setter
public class SectionsAndTopicsDto {
    private Section section;
    private List<Topic> topics;

    public SectionsAndTopicsDto(Section section, List<Topic> topics) {
        this.section = section;
        this.topics = topics;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}