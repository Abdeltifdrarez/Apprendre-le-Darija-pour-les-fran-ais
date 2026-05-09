package com.darija.learn.data;

import java.util.List;

public class Unit {
    public final String id;
    public final String title;
    public final String description;
    public final int color;
    public final List<Lesson> lessons;

    public Unit(String id, String title, String description, int color,
                List<Lesson> lessons) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.color = color;
        this.lessons = lessons;
    }
}
