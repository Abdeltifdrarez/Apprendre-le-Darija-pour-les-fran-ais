package com.darija.learn;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LessonCompleteActivity extends AppCompatActivity {

    public static final String EXTRA_LESSON_TITLE = "lesson_title";
    public static final String EXTRA_XP = "xp";
    public static final String EXTRA_CORRECT = "correct";
    public static final String EXTRA_TOTAL = "total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_complete);

        String title = getIntent().getStringExtra(EXTRA_LESSON_TITLE);
        int xp = getIntent().getIntExtra(EXTRA_XP, 0);
        int correct = getIntent().getIntExtra(EXTRA_CORRECT, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 1);
        int accuracy = total > 0 ? (int) ((correct / (float) total) * 100) : 100;

        ((TextView) findViewById(R.id.complete_subtitle))
                .setText("Tu as terminé : " + title);
        ((TextView) findViewById(R.id.stat_xp_value)).setText("+" + xp);
        ((TextView) findViewById(R.id.stat_accuracy_value)).setText(accuracy + "%");
        ((TextView) findViewById(R.id.stat_correct_value))
                .setText(correct + "/" + total);

        Button cont = findViewById(R.id.complete_continue);
        cont.setOnClickListener(v -> finish());
    }
}
