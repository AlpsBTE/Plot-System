package com.alpsbte.plotsystem.core.system.review;

public class ToggleCriteria {
    private String criteriaName;
    private boolean isOptional;

    public ToggleCriteria(String criteriaName, boolean isOptional) {
        this.criteriaName = criteriaName;
        this.isOptional = isOptional;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public boolean isOptional() {
        return isOptional;
    }
}
