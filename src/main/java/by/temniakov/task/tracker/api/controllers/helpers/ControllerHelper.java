package by.temniakov.task.tracker.api.controllers.helpers;

import by.temniakov.task.tracker.api.exceptions.NotFoundException;
import by.temniakov.task.tracker.store.entities.ProjectEntity;
import by.temniakov.task.tracker.store.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class ControllerHelper {

    private final ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn't exists.", projectId
                                )
                        )
                );
    }
}
