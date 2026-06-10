package com.energiefixers.backend.property.dto;

import com.energiefixers.backend.property.models.FixRound;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class FixRoundResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private long propertyCount;

    public static FixRoundResponse from(FixRound round, long propertyCount) {
        FixRoundResponse response = new FixRoundResponse();
        response.setId(round.getId());
        response.setName(round.getName());
        response.setDescription(round.getDescription());
        response.setStartDate(round.getStartDate());
        response.setEndDate(round.getEndDate());
        response.setCurrent(round.isCurrent());
        response.setPropertyCount(propertyCount);
        return response;
    }
}
