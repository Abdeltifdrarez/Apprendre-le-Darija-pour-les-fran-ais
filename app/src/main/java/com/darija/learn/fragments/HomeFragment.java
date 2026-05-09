package com.darija.learn.fragments;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.darija.learn.LessonActivity;
import com.darija.learn.R;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;
import com.darija.learn.data.Unit;
import com.darija.learn.progress.ProgressManager;

public class HomeFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_home, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        ProgressManager pm = ProgressManager.get(requireContext());
        ((TextView) v.findViewById(R.id.stat_streak)).setText(String.valueOf(pm.getStreak()));
        ((TextView) v.findViewById(R.id.stat_xp)).setText(String.valueOf(pm.getXp()));
        ((TextView) v.findViewById(R.id.stat_hearts)).setText(String.valueOf(pm.getHearts()));

        LinearLayout container = v.findViewById(R.id.units_container);
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Lecture depuis SQLite
        for (Unit unit : LessonRepository.getUnits(requireContext())) {
            View unitView = inflater.inflate(R.layout.item_unit, container, false);

            ((TextView) unitView.findViewById(R.id.unit_title)).setText(unit.title);
            ((TextView) unitView.findViewById(R.id.unit_desc)).setText(unit.description);

            int done = 0;
            for (Lesson l : unit.lessons) if (pm.isLessonCompleted(l.id)) done++;
            ((TextView) unitView.findViewById(R.id.unit_progress))
                    .setText(done + "/" + unit.lessons.size());

            GradientDrawable bg = (GradientDrawable)
                    unitView.findViewById(R.id.unit_header).getBackground();
            bg.setColor(unit.color);

            LinearLayout lessonsList = unitView.findViewById(R.id.unit_lessons);
            lessonsList.removeAllViews();

            for (Lesson lesson : unit.lessons) {
                View lv = inflater.inflate(R.layout.item_lesson, lessonsList, false);
                ((TextView) lv.findViewById(R.id.lesson_title)).setText(lesson.title);
                ((TextView) lv.findViewById(R.id.lesson_subtitle)).setText(lesson.subtitle);

                GradientDrawable cbg = (GradientDrawable)
                        lv.findViewById(R.id.lesson_circle).getBackground();
                boolean completed = pm.isLessonCompleted(lesson.id);
                cbg.setColor(completed
                        ? getResources().getColor(R.color.success, requireActivity().getTheme())
                        : unit.color);

                ((TextView) lv.findViewById(R.id.lesson_icon))
                        .setText(completed ? "✓" : iconChar(lesson.iconName));

                lv.setOnClickListener(view -> {
                    Intent i2 = new Intent(requireContext(), LessonActivity.class);
                    i2.putExtra(LessonActivity.EXTRA_LESSON_ID, lesson.id);
                    startActivity(i2);
                });
                lessonsList.addView(lv);
            }
            container.addView(unitView);
        }
    }

    private String iconChar(String name) {
        if (name == null) return "★";
        switch (name) {
            case "sun": return "☀";
            case "heart": return "♥";
            case "users": return "👥";
            case "coffee": return "☕";
            case "hash": return "#";
            default: return "★";
        }
    }
}
