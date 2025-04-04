package com.alpsbte.plotsystem.core.system.review;

public class BuildTeamToggleCriteria {
    private final int buildTeamId;
    private final ToggleCriteria criteria;

    public BuildTeamToggleCriteria(int buildTeamId, ToggleCriteria criteria) {
        this.buildTeamId = buildTeamId;
        this.criteria = criteria;
    }

    public int getBuildTeamId() {
        return buildTeamId;
    }

    public ToggleCriteria getCriteria() {
        return criteria;
    }
}
