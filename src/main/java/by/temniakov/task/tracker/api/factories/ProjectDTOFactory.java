package by.temniakov.task.tracker.api.factories;

import by.temniakov.task.tracker.api.dto.ProjectDTO;
import by.temniakov.task.tracker.store.entities.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectDTOFactory {

    public ProjectDTO mapProjectDTO(ProjectEntity entity){
        return ProjectDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
