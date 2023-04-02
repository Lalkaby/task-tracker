package by.temniakov.task.tracker.store.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task_state")
public class TaskStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column
    private Long ordinal;

    @OneToOne
    @JoinColumn(name = "left_task_state_id")
    private TaskStateEntity leftTaskState;

    @OneToOne
    @JoinColumn(name = "right_task_state_id")
    private TaskStateEntity rightTaskState;

    @Column
    @Builder.Default
    private Instant createdAt = Instant.now();

    @ManyToOne(targetEntity = ProjectEntity.class)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Column
    @OneToMany
    @Builder.Default
    @JoinColumn(name = "id_task_state", referencedColumnName = "id")
    private List<TaskEntity> tasks = new ArrayList<>();

    public Optional<TaskStateEntity> getRightTaskState() {
        return Optional.ofNullable(rightTaskState);
    }

    public Optional<TaskStateEntity> getLeftTaskState() {
        return Optional.ofNullable(leftTaskState);
    }
}
