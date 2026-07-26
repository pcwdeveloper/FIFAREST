package com.fifa.fifarest.dto;

import com.fifa.fifarest.domain.Sport;

public record SportResponse(Long id, String name) {
    public static SportResponse from(Sport sport) {
        return new SportResponse(sport.getId(), sport.getName());
    }
}
