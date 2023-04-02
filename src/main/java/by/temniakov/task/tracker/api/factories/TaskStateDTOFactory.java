package by.temniakov.task.tracker.api.factories;

import by.temniakov.task.tracker.api.dto.TaskStateDTO;
import by.temniakov.task.tracker.store.entities.TaskStateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TaskStateDTOFactory {

    private final TaskDTOFactory taskDTOFactory;

    public TaskStateDTO mapTaskStateDTO(TaskStateEntity entity){
        return TaskStateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .leftTasksStateId(entity.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .ordinal(entity.getOrdinal())
                .tasks(
                        entity
                                .getTasks()
                                .stream()
                                .map(taskDTOFactory::mapTaskDTO)
                                .collect(Collectors.toList())
                )
                .build();
    }

}
