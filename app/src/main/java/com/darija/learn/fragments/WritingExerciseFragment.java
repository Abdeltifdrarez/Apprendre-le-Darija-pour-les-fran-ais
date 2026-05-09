package com.darija.learn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.Exercise;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;

public class WritingExerciseFragment extends Fragment {

    private static final String ARG_LESSON_ID   = "lesson_id";
    private static final String ARG_EXERCISE_ID = "exercise_id";

    public static WritingExerciseFragment create(Exercise e) {
        WritingExerciseFragment f = new WritingExerciseFragment();
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
        return i.inflate(R.layout.fragment_writing_exercise, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Exercise exercise = resolve();
        if (exercise == null) return;

        ((TextView) v.findViewById(R.id.writing_prompt)).setText(exercise.prompt);
        ((TextView) v.findViewById(R.id.writing_french)).setText(exercise.french);

        EditText input   = v.findViewById(R.id.writing_input);
        Button hintBtn   = v.findViewById(R.id.writing_hint);
        Button check     = v.findViewById(R.id.writing_check);
        TextView feedback = v.findViewById(R.id.writing_feedback);
        TextView hintText = v.findViewById(R.id.writing_hint_text);

        if (exercise.hint == null) hintBtn.setVisibility(View.GONE);

        hintBtn.setOnClickListener(view -> {
            if (hintText.getVisibility() == View.VISIBLE) {
                hintText.setVisibility(View.GONE); hintBtn.setText("Indice");
            } else {
                hintText.setText("💡 " + exercise.hint);
                hintText.setVisibility(View.VISIBLE); hintBtn.setText("Cacher");
            }
        });

        check.setOnClickListener(view -> {
            String val = input.getText().toString();
            if (val.trim().isEmpty()) return;
            String norm = normalize(val);
            boolean correct = false;
            for (String acc : exercise.acceptedAnswers)
                if (normalize(acc).equals(norm)) { correct = true; break; }
            input.setEnabled(false);
            check.setVisibility(View.GONE);
            hintBtn.setVisibility(View.GONE);
            feedback.setVisibility(View.VISIBLE);
            if (correct) {
                feedback.setText("Bravo !");
                feedback.setTextColor(getResources().getColor(R.color.success, requireActivity().getTheme()));
            } else {
                feedback.setText("Pas tout à fait. Réponse : " + exercise.acceptedAnswers.get(0));
                feedback.setTextColor(getResources().getColor(R.color.destructive, requireActivity().getTheme()));
            }
            if (getActivity() instanceof ExerciseCallback)
                ((ExerciseCallback) getActivity()).onAnswered(correct);
        });
    }

    private static String normalize(String s) {
        return s.toLowerCase().trim()
                .replaceAll("[''`\\-]", "").replaceAll("\\s+", " ");
    }
}
