package ru.practicum.comment.utill;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class CommentGetParam {
    private long eventId;
    private List<Long> authorIds;
    private SortOrder sortBy;
    private int from;
    private int size;
}
