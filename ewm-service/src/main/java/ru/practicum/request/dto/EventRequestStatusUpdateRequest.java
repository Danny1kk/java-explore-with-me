package ru.practicum.request.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Request ids must not be empty")
    private List<Long> requestIds;

    @NotNull(message = "Status must not be null")
    private String status;  // "CONFIRMED" или "REJECTED"
}