package com.darija.learn.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.Exercise;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;

import java.util.Locale;

public class QuizExerciseFragment extends Fragment {

    private static final String ARG_LESSON_ID   = "lesson_id";
    private static final String ARG_EXERCISE_ID = "exercise_id";

    private TextToSpeech tts;
    private String selected;
    private boolean locked = false;
    private Exercise exercise;
    private LinearLayout optionsList;

    public static QuizExerciseFragment create(Exercise e) {
        QuizExerciseFragment f = new QuizExerciseFragment();
        Bundle b = new Bundle();
        b.putString(ARG_LESSON_ID,   e.lessonId);
        b.putString(ARG_EXERCISE_ID, e.id);
        f.setArguments(b);
        return f;
    }

    private Exercise resolve() {
        Bundle b = getArguments();
        if (b == null) return null;
        Lesson l = LessonRepository.findLesson(requireContext(), b.getString(ARG_LESSON_ID));
        if (l == null) return null;
        for (Exercise e : l.exercises) if (e.id.equals(b.getString(ARG_EXERCISE_ID))) return e;
        return null;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_quiz_exercise, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        exercise = resolve();
        if (exercise == null) return;

        ((TextView) v.findViewById(R.id.quiz_prompt)).setText(exercise.prompt);
        ((TextView) v.findViewById(R.id.quiz_question)).setText(exercise.question);
        ((TextView) v.findViewById(R.id.quiz_question_fr)).setText(exercise.questionFrench);

        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("ar"));
        });
        v.findViewById(R.id.quiz_speaker).setOnClickListener(view -> {
            if (tts != null) tts.speak(exercise.question, TextToSpeech.QUEUE_FLUSH, null, "q");
        });

        optionsList = v.findViewById(R.id.quiz_options);
        Button confirm = v.findViewById(R.id.quiz_confirm);
        confirm.setEnabled(false);
        confirm.setAlpha(0.4f);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String opt : exercise.options) {
            View row = inflater.inflate(R.layout.item_option, optionsList, false);
            // IMPORTANT : mutate() garantit que chaque option a son propre drawable
            // sans mutate(), Android partage le même objet GradientDrawable entre
            // toutes les vues → modifier une couleur change TOUTES les options.
            Drawable d = row.getBackground().mutate();
            row.setBackground(d);
            ((TextView) row.findViewById(R.id.option_text)).setText(opt);
            row.setOnClickListener(view -> {
                if (locked) return;
                selected = opt;
                refreshOptionStyles();
                confirm.setEnabled(true);
                confirm.setAlpha(1f);
            });
            optionsList.addView(row);
        }

        confirm.setOnClickListener(view -> {
            if (selected == null || locked) return;
            locked = true;
            applyResultStyles();
            confirm.setVisibility(View.GONE);
            if (getActivity() instanceof ExerciseCallback)
                ((ExerciseCallback) getActivity()).onAnswered(selected.equals(exercise.correctAnswer));
        });
    }

    private void refreshOptionStyles() {
        for (int i = 0; i < optionsList.getChildCount(); i++) {
            View child = optionsList.getChildAt(i);
            TextView text = child.findViewById(R.id.option_text);
            GradientDrawable bg = (GradientDrawable) child.getBackground();
            if (text.getText().toString().equals(selected)) {
                bg.setStroke(6, getResources().getColor(R.color.primary, requireActivity().getTheme()));
                bg.setColor(Color.parseColor("#f5e9d6"));
                text.setTextColor(getResources().getColor(R.color.primary, requireActivity().getTheme()));
            } else {
                bg.setStroke(2, getResources().getColor(R.color.border, requireActivity().getTheme()));
                bg.setColor(getResources().getColor(R.color.card, requireActivity().getTheme()));
                text.setTextColor(getResources().getColor(R.color.text_primary, requireActivity().getTheme()));
            }
        }
    }

    private void applyResultStyles() {
        for (int i = 0; i < optionsList.getChildCount(); i++) {
            View child = optionsList.getChildAt(i);
            TextView text = child.findViewById(R.id.option_text);
            GradientDrawable bg = (GradientDrawable) child.getBackground();
            String label = text.getText().toString();

            if (label.equals(exercise.correctAnswer)) {
                // Bonne réponse → VERT
                bg.setStroke(6, getResources().getColor(R.color.success, requireActivity().getTheme()));
                bg.setColor(Color.parseColor("#e8f5f0"));
                text.setTextColor(getResources().getColor(R.color.success, requireActivity().getTheme()));
            } else if (label.equals(selected)) {
                // Réponse choisie (fausse) → ROUGE
                bg.setStroke(6, getResources().getColor(R.color.destructive, requireActivity().getTheme()));
                bg.setColor(Color.parseColor("#fde8e8"));
                text.setTextColor(getResources().getColor(R.color.destructive, requireActivity().getTheme()));
            } else {
                // Autres options → grisées
                bg.setStroke(2, getResources().getColor(R.color.border, requireActivity().getTheme()));
                bg.setColor(getResources().getColor(R.color.card, requireActivity().getTheme()));
                text.setTextColor(getResources().getColor(R.color.text_muted, requireActivity().getTheme()));
            }
            child.setEnabled(false);
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
    }
}
