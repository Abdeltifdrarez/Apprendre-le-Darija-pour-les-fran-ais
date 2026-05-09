package com.darija.learn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.darija.learn.data.Exercise;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;
import com.darija.learn.fragments.ExerciseCallback;
import com.darija.learn.fragments.QuizExerciseFragment;
import com.darija.learn.fragments.TextExerciseFragment;
import com.darija.learn.fragments.VideoExerciseFragment;
import com.darija.learn.fragments.VoiceExerciseFragment;
import com.darija.learn.fragments.WritingExerciseFragment;
import com.darija.learn.progress.ProgressManager;

public class LessonActivity extends AppCompatActivity implements ExerciseCallback {

    public static final String EXTRA_LESSON_ID = "lesson_id";

    private Lesson lesson;
    private int step = 0;
    private int correctCount = 0;
    private int wrongCount = 0;

    private ProgressBar progressBar;
    private TextView stepLabel;
    private Button continueButton;
    private TextView feedbackLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        String lessonId = getIntent().getStringExtra(EXTRA_LESSON_ID);
        // Lecture depuis SQLite
        lesson = LessonRepository.findLesson(this, lessonId);
        if (lesson == null) {
            Toast.makeText(this, "Leçon introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar   = findViewById(R.id.lesson_progress);
        stepLabel     = findViewById(R.id.lesson_step);
        continueButton = findViewById(R.id.lesson_continue);
        feedbackLabel  = findViewById(R.id.lesson_feedback);

        ((ImageButton) findViewById(R.id.lesson_close)).setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> goNext());

        showStep();
    }

    private void showStep() {
        Exercise current = lesson.exercises.get(step);
        Fragment frag;
        switch (current.type) {
            case TEXT:    frag = TextExerciseFragment.create(current);    break;
            case VOICE:   frag = VoiceExerciseFragment.create(current);   break;
            case VIDEO:   frag = VideoExerciseFragment.create(current);   break;
            case QUIZ:    frag = QuizExerciseFragment.create(current);    break;
            case WRITING: default:
                          frag = WritingExerciseFragment.create(current); break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.exercise_container, frag)
                .commitNow();

        feedbackLabel.setVisibility(View.GONE);

        boolean autoContinue = current.type == Exercise.Type.TEXT
                || current.type == Exercise.Type.VOICE
                || current.type == Exercise.Type.VIDEO;
        continueButton.setEnabled(autoContinue);
        continueButton.setAlpha(autoContinue ? 1f : 0.4f);

        updateProgress();
    }

    private void updateProgress() {
        int total = lesson.exercises.size();
        int pct = (int) (((float) step / total) * 100);
        progressBar.setProgress(pct);
        stepLabel.setText((step + 1) + "/" + total);
    }

    @Override
    public void onAnswered(boolean correct) {
        if (correct) correctCount++;
        else {
            wrongCount++;
            ProgressManager.get(this).loseHeart();
        }
        feedbackLabel.setVisibility(View.VISIBLE);
        feedbackLabel.setText(correct ? "Excellent !" : "Pas grave, on continue.");
        feedbackLabel.setTextColor(getResources().getColor(
                correct ? R.color.success : R.color.destructive, getTheme()));
        continueButton.setEnabled(true);
        continueButton.setAlpha(1f);
    }

    @Override
    public void onExerciseComplete() {

    }

    private void goNext() {
        if (step + 1 >= lesson.exercises.size()) {
            int earnedXp = Math.max(5, lesson.xpReward - wrongCount * 2);
            ProgressManager.get(this).completeLesson(lesson.id, earnedXp);

            Intent i = new Intent(this, LessonCompleteActivity.class);
            i.putExtra(LessonCompleteActivity.EXTRA_LESSON_TITLE, lesson.title);
            i.putExtra(LessonCompleteActivity.EXTRA_XP, earnedXp);
            i.putExtra(LessonCompleteActivity.EXTRA_CORRECT, correctCount);
            i.putExtra(LessonCompleteActivity.EXTRA_TOTAL, lesson.exercises.size());
            startActivity(i);
            finish();
            return;
        }
        step++;
        showStep();
    }
}
