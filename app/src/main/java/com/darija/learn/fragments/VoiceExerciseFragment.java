package com.darija.learn.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.Exercise;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceExerciseFragment extends Fragment {

    private static final String ARG_LESSON_ID   = "lesson_id";
    private static final String ARG_EXERCISE_ID = "exercise_id";

    private TextToSpeech     tts;
    private SpeechRecognizer speechRecognizer;
    private Exercise         exercise;
    private boolean          isListening    = false;
    private boolean          hasAttempted   = false;

    // Views
    private TextView tvStatus;
    private TextView tvResult;
    private TextView tvDarija;
    private TextView tvArabic;
    private Button   btnMic;
    private Button   btnReveal;
    private Button   btnNext;
    private View     feedbackCorrect;
    private View     feedbackWrong;

    private final ActivityResultLauncher<String> permLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) startListening();
            else tvStatus.setText("Permission micro refusée — activez-la dans les paramètres");
        });

    // ─────────────────────────────────────────────────────────────────────────
    public static VoiceExerciseFragment create(Exercise e) {
        VoiceExerciseFragment f = new VoiceExerciseFragment();
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
        for (Exercise e : l.exercises)
            if (e.id.equals(b.getString(ARG_EXERCISE_ID))) return e;
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        exercise = resolve();
        if (exercise == null) return;

        ((TextView) v.findViewById(R.id.voice_prompt)).setText(exercise.prompt);
        ((TextView) v.findViewById(R.id.voice_french)).setText(exercise.vocab.french);

        tvStatus        = v.findViewById(R.id.voice_status);
        tvResult        = v.findViewById(R.id.voice_result);
        tvDarija        = v.findViewById(R.id.voice_darija);
        tvArabic        = v.findViewById(R.id.voice_arabic);
        btnMic          = v.findViewById(R.id.voice_mic);
        btnReveal       = v.findViewById(R.id.voice_reveal);
        btnNext         = v.findViewById(R.id.voice_next);
        feedbackCorrect = v.findViewById(R.id.feedback_correct);
        feedbackWrong   = v.findViewById(R.id.feedback_wrong);

        // TTS — tente d'abord l'arabe marocain, sinon arabe standard
        tts = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(new Locale("ar", "MA"));
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(new Locale("ar"));
                }
            }
        });

        v.findViewById(R.id.voice_play).setOnClickListener(x -> speak(1.0f));
        v.findViewById(R.id.voice_slow).setOnClickListener(x -> speak(0.5f));

        btnMic.setOnClickListener(x -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                permLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        btnReveal.setOnClickListener(x -> {
            revealAnswer();
            btnNext.setVisibility(View.VISIBLE);
        });

        btnNext.setOnClickListener(x -> {
            ExerciseCallback cb = (ExerciseCallback) getActivity();
            if (cb != null) cb.onExerciseComplete();
        });

        initSpeechRecognizer();
    }

    // ─── TTS ─────────────────────────────────────────────────────────────────
    private void speak(float rate) {
        if (tts == null) return;
        tts.setSpeechRate(rate);
        tts.speak(exercise.vocab.darija, TextToSpeech.QUEUE_FLUSH, null, "tts_v");
    }

    // ─── Speech recognizer setup ─────────────────────────────────────────────
    private void initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            tvStatus.setText("Reconnaissance vocale non disponible sur cet appareil");
            btnMic.setEnabled(false);
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params)   { tvStatus.setText("Parlez maintenant..."); btnMic.setText("⏹"); isListening = true; }
            @Override public void onBeginningOfSpeech()              { tvStatus.setText("J'écoute..."); }
            @Override public void onRmsChanged(float rmsdB)          {}
            @Override public void onBufferReceived(byte[] buffer)    {}
            @Override public void onEndOfSpeech()                    { tvStatus.setText("Analyse en cours..."); isListening = false; }
            @Override public void onEvent(int eventType, Bundle params) {}
            @Override public void onPartialResults(Bundle partialResults) {}

            @Override public void onError(int error) {
                isListening = false;
                btnMic.setText("🎤");
                tvStatus.setText("Réessayez — appuyez sur 🎤");
            }

            @Override public void onResults(Bundle results) {
                isListening = false;
                btnMic.setText("🎤");
                ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    handleResult(matches.get(0));
                } else {
                    tvStatus.setText("Rien compris — réessayez");
                }
            }
        });
    }

    private void startListening() {
        if (isListening || speechRecognizer == null) return;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Priorité : arabe marocain, fallback arabe standard
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-MA");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        tvStatus.setText("Préparation du micro...");
        speechRecognizer.startListening(intent);
    }

    // ─── Traitement du résultat ───────────────────────────────────────────────
    private void handleResult(String recognized) {
        hasAttempted = true;

        tvResult.setText("Vous avez dit : « " + recognized + " »");
        tvResult.setVisibility(View.VISIBLE);

        String expected = normalize(exercise.vocab.darija);
        String actual   = normalize(recognized);

        boolean correct = actual.contains(expected)
                       || expected.contains(actual)
                       || similarity(actual, expected) >= 0.55f;

        revealAnswer();

        feedbackCorrect.setVisibility(correct ? View.VISIBLE : View.GONE);
        feedbackWrong.setVisibility(correct   ? View.GONE    : View.VISIBLE);

        tvStatus.setText(correct
            ? "Bravo ! Continuez comme ça ! 🎉"
            : "Pas tout à fait — réécoutez et réessayez");

        btnReveal.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void revealAnswer() {
        tvDarija.setText(exercise.vocab.darija);
        tvArabic.setText(exercise.vocab.arabic);
        tvDarija.setVisibility(View.VISIBLE);
        tvArabic.setVisibility(View.VISIBLE);
        btnReveal.setVisibility(View.GONE);
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────
    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[\\p{Punct}]", "")
                .replaceAll("\\s+", " ");
    }

    /** Distance de Levenshtein → score 0..1 */
    private float similarity(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return 0f;
        int la = a.length(), lb = b.length();
        int[][] dp = new int[la + 1][lb + 1];
        for (int i = 0; i <= la; i++) dp[i][0] = i;
        for (int j = 0; j <= lb; j++) dp[0][j] = j;
        for (int i = 1; i <= la; i++)
            for (int j = 1; j <= lb; j++)
                dp[i][j] = a.charAt(i-1) == b.charAt(j-1)
                    ? dp[i-1][j-1]
                    : 1 + Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1]));
        return 1f - (float) dp[la][lb] / Math.max(la, lb);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Override public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        if (speechRecognizer != null) { speechRecognizer.destroy(); speechRecognizer = null; }
    }
}
