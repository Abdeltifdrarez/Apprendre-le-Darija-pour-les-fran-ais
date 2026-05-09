package com.darija.learn.data;

import java.util.List;

public class Lesson {
    public final String id;
    public final String title;
    public final String subtitle;
    public final String iconName;
    public final int xpReward;
    public final List<Exercise> exercises;

    public Lesson(String id, String title, String subtitle, String iconName,
                  int xpReward, List<Exercise> exercises) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.iconName = iconName;
        this.xpReward = xpReward;
        this.exercises = exercises;
    }
}
