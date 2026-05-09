package com.darija.learn.fragments;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.Exercise;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;

import java.util.Locale;

public class TextExerciseFragment extends Fragment {

    private static final String ARG_LESSON_ID   = "lesson_id";
    private static final String ARG_EXERCISE_ID = "exercise_id";

    private TextToSpeech tts;

    public static TextExerciseFragment create(Exercise e) {
        TextExerciseFragment f = new TextExerciseFragment();
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
        return i.inflate(R.layout.fragment_text_exercise, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Exercise exercise = resolve();
        if (exercise == null) return;

        ((TextView) v.findViewById(R.id.text_prompt)).setText(exercise.prompt);
        ((TextView) v.findViewById(R.id.text_darija)).setText(exercise.vocab.darija);
        ((TextView) v.findViewById(R.id.text_arabic)).setText(exercise.vocab.arabic);
        ((TextView) v.findViewById(R.id.text_french)).setText(exercise.vocab.french);

        TextView noteView = v.findViewById(R.id.text_note);
        if (exercise.note != null) { noteView.setVisibility(View.VISIBLE); noteView.setText(exercise.note); }
        else noteView.setVisibility(View.GONE);

        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("ar"));
                tts.speak(exercise.vocab.darija, TextToSpeech.QUEUE_FLUSH, null, "intro");
            }
        });
        v.findViewById(R.id.text_speak).setOnClickListener(view -> {
            if (tts != null) tts.speak(exercise.vocab.darija, TextToSpeech.QUEUE_FLUSH, null, "m");
        });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
    }
}
