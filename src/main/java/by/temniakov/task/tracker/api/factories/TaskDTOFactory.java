package by.temniakov.task.tracker.api.factories;

import by.temniakov.task.tracker.api.dto.TaskDTO;
import by.temniakov.task.tracker.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDTOFactory {

    public TaskDTO mapTaskDTO(TaskEntity entity){
        return TaskDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .build();
    }

}
