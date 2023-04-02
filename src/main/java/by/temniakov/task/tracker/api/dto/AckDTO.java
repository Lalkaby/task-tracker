package by.temniakov.task.tracker.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckDTO {

    private Boolean answer;

    public static AckDTO makeDefault(Boolean answer){
        return AckDTO.builder()
                .answer(answer)
                .build();
    }
}
