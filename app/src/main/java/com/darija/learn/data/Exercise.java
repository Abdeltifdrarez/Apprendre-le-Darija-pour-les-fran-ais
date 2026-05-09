package com.darija.learn.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Universal exercise model. Some fields are only meaningful for specific types.
 */
public class Exercise {
    public enum Type { TEXT, VOICE, VIDEO, QUIZ, WRITING }

    public final String id;
    public String lessonId;   // défini par LessonRepository lors du chargement
    public final Type type;
    public final String prompt;

    // TEXT / VOICE
    public VocabItem vocab;
    public String note;

    // VIDEO
    public int videoResId;
    public String description;
    public List<VocabItem> vocabList = new ArrayList<>();

    // QUIZ
    public String question;
    public String questionFrench;
    public List<String> options = new ArrayList<>();
    public String correctAnswer;

    // WRITING
    public String french;
    public List<String> acceptedAnswers = new ArrayList<>();
    public String hint;

    private Exercise(String id, Type type, String prompt) {
        this.id = id;
        this.type = type;
        this.prompt = prompt;
    }

    public static Exercise text(String id, String prompt, VocabItem v, String note) {
        Exercise e = new Exercise(id, Type.TEXT, prompt);
        e.vocab = v;
        e.note = note;
        return e;
    }

    public static Exercise voice(String id, String prompt, VocabItem v) {
        Exercise e = new Exercise(id, Type.VOICE, prompt);
        e.vocab = v;
        return e;
    }

    public static Exercise video(String id, String prompt, int videoResId,
                                 String description, VocabItem... vocab) {
        Exercise e = new Exercise(id, Type.VIDEO, prompt);
        e.videoResId = videoResId;
        e.description = description;
        e.vocabList = Arrays.asList(vocab);
        return e;
    }

    public static Exercise quiz(String id, String prompt, String question,
                                String questionFrench, String correctAnswer,
                                String... options) {
        Exercise e = new Exercise(id, Type.QUIZ, prompt);
        e.question = question;
        e.questionFrench = questionFrench;
        e.correctAnswer = correctAnswer;
        e.options = Arrays.asList(options);
        return e;
    }

    public static Exercise writing(String id, String prompt, String french,
                                   String hint, String... accepted) {
        Exercise e = new Exercise(id, Type.WRITING, prompt);
        e.french = french;
        e.hint = hint;
        e.acceptedAnswers = Arrays.asList(accepted);
        return e;
    }
}
