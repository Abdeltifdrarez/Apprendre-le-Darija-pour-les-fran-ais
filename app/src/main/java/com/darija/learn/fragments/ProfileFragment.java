package com.darija.learn.fragments;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;
import com.darija.learn.data.Unit;
import com.darija.learn.progress.ProgressManager;

public class ProfileFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_profile, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        ProgressManager pm = ProgressManager.get(requireContext());

        ((TextView) v.findViewById(R.id.profile_streak)).setText(String.valueOf(pm.getStreak()));
        ((TextView) v.findViewById(R.id.profile_xp)).setText(String.valueOf(pm.getXp()));
        ((TextView) v.findViewById(R.id.profile_hearts)).setText(String.valueOf(pm.getHearts()));

        // Progression globale (depuis SQLite)
        int total = LessonRepository.getAllLessons(requireContext()).size();
        int done  = pm.getCompletedLessons().size();
        int pct   = total > 0 ? (int) ((done / (float) total) * 100) : 0;

        ((TextView) v.findViewById(R.id.profile_progress_pct)).setText(pct + "%");
        ((TextView) v.findViewById(R.id.profile_progress_meta))
                .setText(done + " / " + total + " leçons terminées");
        ((ProgressBar) v.findViewById(R.id.profile_progress_bar)).setProgress(pct);

        LinearLayout unitsList = v.findViewById(R.id.profile_units);
        unitsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Unit unit : LessonRepository.getUnits(requireContext())) {
            View row = inflater.inflate(R.layout.item_unit_progress, unitsList, false);
            ((TextView) row.findViewById(R.id.up_title)).setText(unit.title);
            int udone = 0;
            for (Lesson l : unit.lessons) if (pm.isLessonCompleted(l.id)) udone++;
            int upct = unit.lessons.isEmpty() ? 0
                    : (int) ((udone / (float) unit.lessons.size()) * 100);
            ((TextView) row.findViewById(R.id.up_meta))
                    .setText(udone + "/" + unit.lessons.size() + " · " + upct + "%");
            GradientDrawable dot = (GradientDrawable)
                    row.findViewById(R.id.up_dot).getBackground();
            dot.setColor(unit.color);
            unitsList.addView(row);
        }

        v.findViewById(R.id.profile_refill).setOnClickListener(view -> {
            pm.refillHearts();
            ((TextView) v.findViewById(R.id.profile_hearts))
                    .setText(String.valueOf(pm.getHearts()));
        });

        v.findViewById(R.id.profile_reset).setOnClickListener(view ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Réinitialiser")
                        .setMessage("Toute ta progression sera effacée.")
                        .setNegativeButton("Annuler", null)
                        .setPositiveButton("Réinitialiser", (d, w) -> {
                            pm.reset();
                            onViewCreated(v, null);
                        }).show());
    }
}
