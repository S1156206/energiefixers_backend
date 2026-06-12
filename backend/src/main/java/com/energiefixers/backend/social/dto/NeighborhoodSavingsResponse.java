package com.energiefixers.backend.social.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NeighborhoodSavingsResponse {
    private final String regionName;
    private final int totalNeighbors;
    private final List<NeighborSavingsEntry> neighbors;
}
