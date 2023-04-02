package by.temniakov.task.tracker.api.controllers;

import by.temniakov.task.tracker.api.controllers.helpers.ControllerHelper;
import by.temniakov.task.tracker.api.dto.AckDTO;
import by.temniakov.task.tracker.api.dto.ProjectDTO;
import by.temniakov.task.tracker.api.exceptions.BadRequestException;
import by.temniakov.task.tracker.api.exceptions.NotFoundException;
import by.temniakov.task.tracker.api.factories.ProjectDTOFactory;
import by.temniakov.task.tracker.store.entities.ProjectEntity;
import by.temniakov.task.tracker.store.repositories.ProjectRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {

    private final ControllerHelper controllerHelper;

    private final ProjectRepository projectRepository;

    private final ProjectDTOFactory projectDTOFactory;

    public static final String FETCH_PROJECTS = "/api/projects";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT= "/api/projects";

    @GetMapping(FETCH_PROJECTS)
    public List<ProjectDTO> fetchProjects(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream
                .map(projectDTOFactory::mapProjectDTO)
                .collect(Collectors.toList());
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDTO createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreated = !optionalProjectId.isPresent();

        if(isCreated && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty");
        }

        ProjectEntity projectEntity = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherRepository -> !Objects.equals(anotherRepository.getId(), projectEntity.getId()))
                            .ifPresent(project -> {
                                throw new BadRequestException(String.format("Project \"%s\" is already exists.", projectName));
                            });

                    projectEntity.setName(projectName);
                }
        );

        ProjectEntity savedProjectEntity = projectRepository.saveAndFlush(projectEntity);

        return projectDTOFactory.mapProjectDTO(savedProjectEntity);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDTO deleteProject(@PathVariable("project_id") Long projectId){
         controllerHelper.getProjectOrThrowException(projectId);

         projectRepository.deleteById(projectId);

         return AckDTO.makeDefault(true);
    }

}
