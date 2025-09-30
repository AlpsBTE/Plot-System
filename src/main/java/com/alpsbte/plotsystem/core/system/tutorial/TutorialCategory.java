package com.alpsbte.plotsystem.core.system.tutorial;

public enum TutorialCategory {
    BEGINNER(0);


    final int id;

    TutorialCategory(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TutorialCategory byId(int id) {
        for (TutorialCategory theme : values())
            if (theme.getId() == id)
                return theme;
        return null;
    }
}
