package com.darija.learn.fragments;

import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.LessonRepository;
import com.darija.learn.data.VocabItem;
import com.darija.learn.progress.ProgressManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PracticeFragment extends Fragment {

    private TextToSpeech tts;
    private List<VocabItem> allVocab;
    private VocabItem currentQuestion;
    private List<VocabItem> currentOptions;
    private int score = 0;
    private int streak = 0;
    private boolean locked = false;

    private TextView darijaText, arabicText, scoreText;
    private LinearLayout optionsContainer;
    private Button nextButton;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        return i.inflate(R.layout.fragment_practice, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        // Lecture depuis SQLite
        allVocab = LessonRepository.getAllVocab(requireContext());

        darijaText = v.findViewById(R.id.practice_darija);
        arabicText = v.findViewById(R.id.practice_arabic);
        scoreText  = v.findViewById(R.id.practice_score);
        optionsContainer = v.findViewById(R.id.practice_options);
        nextButton = v.findViewById(R.id.practice_next);

        nextButton.setOnClickListener(view -> nextRound());
        v.findViewById(R.id.practice_speaker)
                .setOnClickListener(view -> speak(currentQuestion.darija));

        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(new Locale("ar"));
        });

        nextRound();
    }

    private void nextRound() {
        if (allVocab.isEmpty()) return;
        locked = false;
        nextButton.setVisibility(View.GONE);
        Collections.shuffle(allVocab);
        currentQuestion = allVocab.get(0);
        currentOptions  = new ArrayList<>(allVocab.subList(0, Math.min(4, allVocab.size())));
        Collections.shuffle(currentOptions);

        darijaText.setText(currentQuestion.darija);
        arabicText.setText(currentQuestion.arabic);
        scoreText.setText("Score : " + score + "  ·  Série : " + streak);

        optionsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (VocabItem opt : currentOptions) {
            View row  = inflater.inflate(R.layout.item_option, optionsContainer, false);
            TextView t = row.findViewById(R.id.option_text);
            t.setText(opt.french);
            row.setOnClickListener(view -> handleSelect(row, t, opt));
            optionsContainer.addView(row);
        }
    }

    private void handleSelect(View row, TextView text, VocabItem opt) {
        if (locked) return;
        locked = true;
        boolean correct = opt.darija.equals(currentQuestion.darija);
        GradientDrawable bg = (GradientDrawable) row.getBackground();
        if (correct) {
            bg.setStroke(6, getResources().getColor(R.color.success, requireActivity().getTheme()));
            bg.setColor(Color.parseColor("#e8f5f0"));
            text.setTextColor(getResources().getColor(R.color.success, requireActivity().getTheme()));
            score++; streak++;
            ProgressManager.get(requireContext()).addXp(2);
        } else {
            bg.setStroke(6, getResources().getColor(R.color.destructive, requireActivity().getTheme()));
            bg.setColor(Color.parseColor("#fde8e8"));
            text.setTextColor(getResources().getColor(R.color.destructive, requireActivity().getTheme()));
            streak = 0;
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                View child = optionsContainer.getChildAt(i);
                if (currentOptions.get(i).darija.equals(currentQuestion.darija)) {
                    GradientDrawable cbg = (GradientDrawable) child.getBackground();
                    cbg.setStroke(6, getResources().getColor(R.color.success, requireActivity().getTheme()));
                    cbg.setColor(Color.parseColor("#e8f5f0"));
                }
            }
        }
        nextButton.setVisibility(View.VISIBLE);
        scoreText.setText("Score : " + score + "  ·  Série : " + streak);
    }

    private void speak(String text) {
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "practice");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
    }
}
