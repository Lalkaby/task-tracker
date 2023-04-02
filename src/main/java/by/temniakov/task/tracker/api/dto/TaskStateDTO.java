package by.temniakov.task.tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStateDTO {

    @NonNull
    private Long id;

    @NonNull
    private String name;

    private Long ordinal;

    @JsonProperty("left_task_state_id")
    private Long leftTasksStateId;

    @JsonProperty("right_task_state_id")
    private Long rightTaskStateId;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;

    @NonNull
    private List<TaskDTO> tasks;
}
