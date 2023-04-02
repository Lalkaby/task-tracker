package by.temniakov.task.tracker.api.controllers;

import by.temniakov.task.tracker.api.controllers.helpers.ControllerHelper;
import by.temniakov.task.tracker.api.dto.AckDTO;
import by.temniakov.task.tracker.api.dto.TaskStateDTO;
import by.temniakov.task.tracker.api.exceptions.BadRequestException;
import by.temniakov.task.tracker.api.exceptions.NotFoundException;
import by.temniakov.task.tracker.api.factories.TaskStateDTOFactory;
import by.temniakov.task.tracker.store.entities.ProjectEntity;
import by.temniakov.task.tracker.store.entities.TaskStateEntity;
import by.temniakov.task.tracker.store.repositories.TaskStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@RestController
public class TaskStateController {

    private final TaskStateRepository taskStateRepository;

    private final TaskStateDTOFactory taskStateDTOFactory;

    private final ControllerHelper controllerHelper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/projects/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE= "/api/task-states/{task_state_id}";
    public static final String CHANGE_TASK_STATE_POSITION = "/api/task-states/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/task-states/{task_state_id}";

    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDTO> getTaskStates(
            @PathVariable(name = "project_id") Long projectId) {
        ProjectEntity projectEntity = controllerHelper.getProjectOrThrowException(projectId);

        return projectEntity
                .getTaskStates()
                .stream()
                .map(taskStateDTOFactory::mapTaskStateDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDTO createTaskState(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(value = "task_state_name") String taskStateName
    ){
        if (taskStateName.isBlank()){
            throw new BadRequestException("Task state name can't be empty");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskStateEntity> optionalAnotherTaskState = Optional.empty();

        for (TaskStateEntity taskState : project.getTaskStates()){

            if (taskState.getName().equalsIgnoreCase(taskStateName)){
                throw new BadRequestException(String.format("Task state \"%s\" already exists.", taskStateName));
            }

            if ( !taskState.getRightTaskState().isPresent()){
                optionalAnotherTaskState = Optional.of(taskState);
                break;
            }
        }

        TaskStateEntity taskState = taskStateRepository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStateName)
                        .project(project)
                        .build()) ;

        optionalAnotherTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    taskStateRepository.saveAndFlush(anotherTaskState);
                });
        final TaskStateEntity savedTaskState = taskStateRepository.saveAndFlush(taskState);

        return taskStateDTOFactory.mapTaskStateDTO(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDTO updateTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(value = "task_state_name") String taskStateName) {

        if (taskStateName.isBlank()){
            throw new BadRequestException("Task state name can't be empty");
        }

        TaskStateEntity taskState = getTaskStateOrThrowException(taskStateId);

        taskStateRepository
                .findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName)
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task state \"%s\" already exists", taskStateName));
                });

        taskState.setName(taskStateName);
        taskState = taskStateRepository.saveAndFlush(taskState);
        return taskStateDTOFactory.mapTaskStateDTO(taskState);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public TaskStateDTO changeTaskStatePosition(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(value = "left_task_state_id") Optional<Long> optionalLeftTaskStateId) {

        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);
        ProjectEntity project = changeTaskState.getProject();

       Optional<Long> optionalOldLeftTaskStateId = changeTaskState
                .getLeftTaskState()
               .map(TaskStateEntity::getId);

       if (optionalOldLeftTaskStateId.equals(optionalLeftTaskStateId)){
                return taskStateDTOFactory.mapTaskStateDTO(changeTaskState);}

            Optional<TaskStateEntity>optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId ->{
                    if (taskStateId.equals(leftTaskStateId)){
                        throw new BadRequestException("Left task state id equals changed task state id.");
                    }

                    TaskStateEntity leftTaskStateEntity = getTaskStateOrThrowException(leftTaskStateId);

                    if (!project.getId().equals(leftTaskStateEntity.getProject().getId())){
                        throw new BadRequestException("Task state position can be changed within the same project.");
                    }

                    return leftTaskStateEntity;
                });

       Optional<TaskStateEntity> optionalNewRightTaskState;
       if (!optionalNewLeftTaskState.isPresent()){
           optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> !anotherTaskState.getLeftTaskState().isPresent())
                    .findAny() ;
       }else{
           optionalNewRightTaskState = optionalNewLeftTaskState
                   .get()
                   .getRightTaskState();
       }

        replaceOldTaskStatePosition(changeTaskState);

        if ( optionalNewLeftTaskState.isPresent()){
            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changeTaskState.setLeftTaskState(null);
        }
        
        if ( optionalNewRightTaskState.isPresent()){
            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);
        } else {
            changeTaskState.setRightTaskState(null);
        }

       changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);
     optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        return taskStateDTOFactory.mapTaskStateDTO(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AckDTO deleteTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId) {
        TaskStateEntity changeTaskState = getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStatePosition(changeTaskState);

        changeTaskState.setLeftTaskState(null);
        changeTaskState.setRightTaskState(null);

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        taskStateRepository.delete(changeTaskState);

        return AckDTO.builder().answer(true).build();
    }

    private void replaceOldTaskStatePosition(TaskStateEntity changeTaskState) {
        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it ->{
                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));
                    taskStateRepository.saveAndFlush(it);
                });

        optionalOldRightTaskState
                .ifPresent(it ->
                {
                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));
                    taskStateRepository.saveAndFlush(it);
                });
    }

    private TaskStateEntity getTaskStateOrThrowException(Long taskStateId){
        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Task state with \"%s\" id doesn't exist.", taskStateId)));
    }
}
