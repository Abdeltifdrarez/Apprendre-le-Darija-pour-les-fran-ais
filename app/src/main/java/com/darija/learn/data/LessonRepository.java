package com.darija.learn.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Fournit les données de cours en les lisant depuis la base SQLite.
 * Toutes les méthodes sont synchrones et bloquantes — à appeler depuis
 * un thread d'arrière-plan si les listes deviennent grandes.
 */
public class LessonRepository {

    /**
     * Charge toutes les unités avec leurs leçons et exercices depuis SQLite.
     */
    public static List<Unit> getUnits(Context ctx) {
        SQLiteDatabase db = DatabaseHelper.getInstance(ctx).getReadableDatabase();
        List<Unit> units = new ArrayList<>();

        Cursor uc = db.query(
                DatabaseHelper.TABLE_UNITS, null, null, null, null, null,
                DatabaseHelper.C_POSITION + " ASC");

        while (uc.moveToNext()) {
            String id    = uc.getString(uc.getColumnIndexOrThrow(DatabaseHelper.C_ID));
            String title = uc.getString(uc.getColumnIndexOrThrow(DatabaseHelper.C_TITLE));
            String desc  = uc.getString(uc.getColumnIndexOrThrow(DatabaseHelper.C_DESCRIPTION));
            int    color = uc.getInt(uc.getColumnIndexOrThrow(DatabaseHelper.C_COLOR));
            units.add(new Unit(id, title, desc, color, getLessons(db, id)));
        }
        uc.close();
        return units;
    }

    private static List<Lesson> getLessons(SQLiteDatabase db, String unitId) {
        List<Lesson> lessons = new ArrayList<>();
        Cursor lc = db.query(
                DatabaseHelper.TABLE_LESSONS, null,
                DatabaseHelper.C_UNIT_ID + "=?", new String[]{unitId},
                null, null, DatabaseHelper.C_POSITION + " ASC");

        while (lc.moveToNext()) {
            String id       = lc.getString(lc.getColumnIndexOrThrow(DatabaseHelper.C_ID));
            String title    = lc.getString(lc.getColumnIndexOrThrow(DatabaseHelper.C_TITLE));
            String subtitle = lc.getString(lc.getColumnIndexOrThrow(DatabaseHelper.C_SUBTITLE));
            String icon     = lc.getString(lc.getColumnIndexOrThrow(DatabaseHelper.C_ICON_NAME));
            int    xp       = lc.getInt(lc.getColumnIndexOrThrow(DatabaseHelper.C_XP_REWARD));
            lessons.add(new Lesson(id, title, subtitle, icon, xp, getExercises(db, id)));
        }
        lc.close();
        return lessons;
    }

    private static List<Exercise> getExercises(SQLiteDatabase db, String lessonId) {
        List<Exercise> exercises = new ArrayList<>();
        Cursor ec = db.query(
                DatabaseHelper.TABLE_EXERCISES, null,
                DatabaseHelper.C_LESSON_ID + "=?", new String[]{lessonId},
                null, null, DatabaseHelper.C_POSITION + " ASC");

        while (ec.moveToNext()) {
            String id     = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_ID));
            String type   = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_TYPE));
            String prompt = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_PROMPT));
            Exercise e = buildExercise(db, id, type, prompt, ec);
            if (e != null) {
                e.lessonId = lessonId;
                exercises.add(e);
            }
        }
        ec.close();
        return exercises;
    }

    private static Exercise buildExercise(SQLiteDatabase db, String id,
                                          String type, String prompt, Cursor ec) {
        switch (type) {
            case "TEXT": {
                String darija  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_DARIJA));
                String arabic  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_ARABIC));
                String french  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_FRENCH));
                String english = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_ENGLISH));
                String note    = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_NOTE));
                return Exercise.text(id, prompt, new VocabItem(darija, arabic, french, english), note);
            }
            case "VOICE": {
                String darija  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_DARIJA));
                String arabic  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_ARABIC));
                String french  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_FRENCH));
                String english = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_ENGLISH));
                return Exercise.voice(id, prompt, new VocabItem(darija, arabic, french, english));
            }
            case "VIDEO": {
                int    resId = ec.getInt(ec.getColumnIndexOrThrow(DatabaseHelper.C_VIDEO_RES_ID));
                String desc  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_VIDEO_DESC));
                List<VocabItem> vocab = getVideoVocab(db, id);
                return Exercise.video(id, prompt, resId, desc,
                        vocab.toArray(new VocabItem[0]));
            }
            case "QUIZ": {
                String question = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_QUIZ_Q));
                String qFr      = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_QUIZ_Q_FR));
                String correct  = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_QUIZ_CORRECT));
                List<String> opts = getQuizOptions(db, id);
                return Exercise.quiz(id, prompt, question, qFr, correct,
                        opts.toArray(new String[0]));
            }
            case "WRITING": {
                String wrFrench = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_WR_FRENCH));
                String wrHint   = ec.getString(ec.getColumnIndexOrThrow(DatabaseHelper.C_WR_HINT));
                List<String> answers = getWritingAnswers(db, id);
                return Exercise.writing(id, prompt, wrFrench, wrHint,
                        answers.toArray(new String[0]));
            }
            default: return null;
        }
    }

    private static List<VocabItem> getVideoVocab(SQLiteDatabase db, String exerciseId) {
        List<VocabItem> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_VIDEO_VOC, null,
                DatabaseHelper.C_EXERCISE_ID + "=?", new String[]{exerciseId},
                null, null, DatabaseHelper.C_POSITION + " ASC");
        while (c.moveToNext()) {
            list.add(new VocabItem(
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_DARIJA)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_ARABIC)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_FRENCH)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.C_VOCAB_ENGLISH))));
        }
        c.close();
        return list;
    }

    private static List<String> getQuizOptions(SQLiteDatabase db, String exerciseId) {
        List<String> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_QUIZ_OPT, null,
                DatabaseHelper.C_EXERCISE_ID + "=?", new String[]{exerciseId},
                null, null, DatabaseHelper.C_POSITION + " ASC");
        while (c.moveToNext()) {
            list.add(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.C_OPTION_TEXT)));
        }
        c.close();
        return list;
    }

    private static List<String> getWritingAnswers(SQLiteDatabase db, String exerciseId) {
        List<String> list = new ArrayList<>();
        Cursor c = db.query(DatabaseHelper.TABLE_WR_ANS, null,
                DatabaseHelper.C_EXERCISE_ID + "=?", new String[]{exerciseId},
                null, null, null);
        while (c.moveToNext()) {
            list.add(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.C_VALUE)));
        }
        c.close();
        return list;
    }

    // ─── Convenience lookups ──────────────────────────────────────────────────

    public static Lesson findLesson(Context ctx, String lessonId) {
        for (Unit u : getUnits(ctx)) {
            for (Lesson l : u.lessons) {
                if (l.id.equals(lessonId)) return l;
            }
        }
        return null;
    }

    public static List<Lesson> getAllLessons(Context ctx) {
        List<Lesson> all = new ArrayList<>();
        for (Unit u : getUnits(ctx)) all.addAll(u.lessons);
        return all;
    }

    /** Collect all VocabItems from TEXT and VOICE exercises (for Practice mode). */
    public static List<VocabItem> getAllVocab(Context ctx) {
        List<VocabItem> list = new ArrayList<>();
        for (Lesson l : getAllLessons(ctx)) {
            for (Exercise e : l.exercises) {
                if (e.type == Exercise.Type.TEXT || e.type == Exercise.Type.VOICE) {
                    list.add(e.vocab);
                } else if (e.type == Exercise.Type.VIDEO) {
                    list.addAll(e.vocabList);
                }
            }
        }
        return list;
    }
}
