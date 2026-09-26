package com.alpsbte.plotsystem.api.model;

import java.util.UUID;

public record ReviewSnapshot(
        int id,
        PlotSnapshot plot,
        UUID reviewer,
        int score,
        int splitScore,
        String feedback
) {}
