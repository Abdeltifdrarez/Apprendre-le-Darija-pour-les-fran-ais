package com.darija.learn.progress;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.darija.learn.data.DatabaseHelper;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Gère la progression de l'utilisateur (XP, série, cœurs, leçons terminées)
 * en utilisant la base de données SQLite interne (tables {@code progress} et
 * {@code completed_lessons}).
 *
 * Remplace l'ancienne implémentation basée sur SharedPreferences.
 */
public class ProgressManager {

    // ─── Clés dans la table progress ──────────────────────────────────────────
    private static final String KEY_XP           = "xp";
    private static final String KEY_STREAK       = "streak";
    private static final String KEY_LAST_SESSION = "last_session";
    private static final String KEY_HEARTS       = "hearts";

    private static final int DEFAULT_HEARTS = 5;

    // ─── Singleton ─────────────────────────────────────────────────────────────
    private static ProgressManager instance;

    public static synchronized ProgressManager get(Context ctx) {
        if (instance == null) {
            instance = new ProgressManager(ctx.getApplicationContext());
        }
        return instance;
    }

    private final DatabaseHelper dbHelper;

    private ProgressManager(Context ctx) {
        this.dbHelper = DatabaseHelper.getInstance(ctx);
    }

    // ─── Getters ───────────────────────────────────────────────────────────────

    public int getXp() {
        return getInt(KEY_XP, 0);
    }

    public int getStreak() {
        return getInt(KEY_STREAK, 0);
    }

    public int getHearts() {
        return getInt(KEY_HEARTS, DEFAULT_HEARTS);
    }

    public Set<String> getCompletedLessons() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Set<String> set = new HashSet<>();
        Cursor c = db.query(
                DatabaseHelper.TABLE_COMPLETED,
                new String[]{DatabaseHelper.C_LESSON_COMPLETED},
                null, null, null, null, null);
        while (c.moveToNext()) {
            set.add(c.getString(0));
        }
        c.close();
        return set;
    }

    public boolean isLessonCompleted(String lessonId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                DatabaseHelper.TABLE_COMPLETED,
                new String[]{DatabaseHelper.C_LESSON_COMPLETED},
                DatabaseHelper.C_LESSON_COMPLETED + "=?",
                new String[]{lessonId}, null, null, null);
        boolean found = c.moveToFirst();
        c.close();
        return found;
    }

    // ─── Actions ───────────────────────────────────────────────────────────────

    /** Marque une leçon comme terminée et crédite l'XP. */
    public void completeLesson(String lessonId, int xp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Enregistre la leçon terminée
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.C_LESSON_COMPLETED, lessonId);
            cv.put(DatabaseHelper.C_COMPLETED_AT, System.currentTimeMillis());
            db.insertWithOnConflict(
                    DatabaseHelper.TABLE_COMPLETED, null, cv,
                    SQLiteDatabase.CONFLICT_IGNORE);

            // Ajoute l'XP
            setInt(db, KEY_XP, getXp() + xp);

            // Met à jour la série
            updateStreak(db);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Ajoute de l'XP (sans marquer de leçon — pratique rapide). */
    public void addXp(int amount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            setInt(db, KEY_XP, getXp() + amount);
            updateStreak(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Retire un cœur (minimum 0). */
    public void loseHeart() {
        int current = getHearts();
        if (current > 0) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            setInt(db, KEY_HEARTS, current - 1);
        }
    }

    /** Remet les cœurs à 5. */
    public void refillHearts() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        setInt(db, KEY_HEARTS, DEFAULT_HEARTS);
    }

    /** Efface toute la progression (XP, série, cœurs, leçons terminées). */
    public void reset() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(DatabaseHelper.TABLE_PROGRESS, null, null);
            db.delete(DatabaseHelper.TABLE_COMPLETED, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ─── Série (streak) ────────────────────────────────────────────────────────

    private void updateStreak(SQLiteDatabase db) {
        String today = todayKey();
        String last  = getString(KEY_LAST_SESSION, null);
        int streak   = getInt(KEY_STREAK, 0);
        int next;
        if (today.equals(last)) {
            next = streak;                          // déjà joué aujourd'hui
        } else if (last != null && isYesterday(last)) {
            next = streak + 1;                      // jour consécutif
        } else {
            next = 1;                               // série cassée ou début
        }
        setInt(db, KEY_STREAK, next);
        setString(db, KEY_LAST_SESSION, today);
    }

    // ─── SQLite helpers ────────────────────────────────────────────────────────

    private int getInt(String key, int defaultVal) {
        String v = getString(key, null);
        if (v == null) return defaultVal;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return defaultVal; }
    }

    private String getString(String key, String defaultVal) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                DatabaseHelper.TABLE_PROGRESS,
                new String[]{DatabaseHelper.C_VALUE},
                DatabaseHelper.C_KEY + "=?", new String[]{key},
                null, null, null);
        String result = defaultVal;
        if (c.moveToFirst()) result = c.getString(0);
        c.close();
        return result;
    }

    private void setInt(SQLiteDatabase db, String key, int value) {
        setString(db, key, String.valueOf(value));
    }

    private void setString(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.C_KEY, key);
        cv.put(DatabaseHelper.C_VALUE, value);
        db.insertWithOnConflict(
                DatabaseHelper.TABLE_PROGRESS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    // ─── Date helpers ──────────────────────────────────────────────────────────

    private static String todayKey() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH);
    }

    private static boolean isYesterday(String key) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        String y = c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH);
        return y.equals(key);
    }
}
