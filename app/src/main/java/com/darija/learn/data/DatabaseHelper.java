package com.darija.learn.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import com.darija.learn.R;

/**
 * SQLiteOpenHelper — gère la création et la mise à jour de la base de données.
 *
 * Tables :
 *   units            — unités de cours
 *   lessons          — leçons par unité
 *   exercises        — exercices par leçon (tous types dans une seule table)
 *   quiz_options     — choix multiples pour les exercices de type QUIZ
 *   video_vocab      — vocabulaire affiché après la vidéo
 *   writing_answers  — réponses acceptées pour les exercices d'écriture
 *   progress         — paires clé/valeur : xp, streak, hearts, last_session
 *   completed_lessons — leçons terminées par l'utilisateur
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "darija_learn.db";
    private static final int    DB_VERSION = 4;   // v4: ajout unité Sport + corrections quiz/vidéo

    // ─── Table names ───────────────────────────────────────────────────────────
    public static final String TABLE_UNITS      = "units";
    public static final String TABLE_LESSONS    = "lessons";
    public static final String TABLE_EXERCISES  = "exercises";
    public static final String TABLE_QUIZ_OPT   = "quiz_options";
    public static final String TABLE_VIDEO_VOC  = "video_vocab";
    public static final String TABLE_WR_ANS     = "writing_answers";
    public static final String TABLE_PROGRESS   = "progress";
    public static final String TABLE_COMPLETED  = "completed_lessons";

    // ─── Column names ──────────────────────────────────────────────────────────
    // units
    public static final String C_ID          = "id";
    public static final String C_TITLE       = "title";
    public static final String C_DESCRIPTION = "description";
    public static final String C_COLOR       = "color";
    public static final String C_POSITION    = "position";

    // lessons
    public static final String C_UNIT_ID    = "unit_id";
    public static final String C_SUBTITLE   = "subtitle";
    public static final String C_ICON_NAME  = "icon_name";
    public static final String C_XP_REWARD  = "xp_reward";

    // exercises
    public static final String C_LESSON_ID     = "lesson_id";
    public static final String C_TYPE          = "type";
    public static final String C_PROMPT        = "prompt";
    // TEXT / VOICE
    public static final String C_VOCAB_DARIJA  = "vocab_darija";
    public static final String C_VOCAB_ARABIC  = "vocab_arabic";
    public static final String C_VOCAB_FRENCH  = "vocab_french";
    public static final String C_VOCAB_ENGLISH = "vocab_english";
    public static final String C_NOTE          = "note";
    // VIDEO
    public static final String C_VIDEO_RES_ID  = "video_res_id";
    public static final String C_VIDEO_DESC    = "video_description";
    // QUIZ
    public static final String C_QUIZ_Q        = "quiz_question";
    public static final String C_QUIZ_Q_FR     = "quiz_question_fr";
    public static final String C_QUIZ_CORRECT  = "quiz_correct_answer";
    // WRITING
    public static final String C_WR_FRENCH     = "writing_french";
    public static final String C_WR_HINT       = "writing_hint";

    // quiz_options / video_vocab / writing_answers
    public static final String C_EXERCISE_ID   = "exercise_id";
    public static final String C_OPTION_TEXT   = "option_text";

    // progress  (NB: "key" est réservé en SQLite → on utilise "pref_key")
    public static final String C_KEY   = "pref_key";
    public static final String C_VALUE = "value";

    // completed_lessons
    public static final String C_LESSON_COMPLETED = "lesson_id";
    public static final String C_COMPLETED_AT     = "completed_at";

    // ─── Singleton ─────────────────────────────────────────────────────────────
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    // ─── onCreate ──────────────────────────────────────────────────────────────
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        createTables(db);
        seedData(db);
    }

    // ─── onUpgrade ─────────────────────────────────────────────────────────────
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppression complète de toutes les tables, puis recréation.
        // En développement, la progression est réinitialisée lors d'un changement de schéma.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WR_ANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEO_VOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_OPT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UNITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETED);
        onCreate(db);
    }

    // ─── Table creation ────────────────────────────────────────────────────────
    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_UNITS + " ("
                + C_ID          + " TEXT PRIMARY KEY, "
                + C_TITLE       + " TEXT NOT NULL, "
                + C_DESCRIPTION + " TEXT, "
                + C_COLOR       + " INTEGER NOT NULL, "
                + C_POSITION    + " INTEGER NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_LESSONS + " ("
                + C_ID       + " TEXT PRIMARY KEY, "
                + C_UNIT_ID  + " TEXT NOT NULL, "
                + C_TITLE    + " TEXT NOT NULL, "
                + C_SUBTITLE + " TEXT, "
                + C_ICON_NAME + " TEXT, "
                + C_XP_REWARD + " INTEGER NOT NULL DEFAULT 10, "
                + C_POSITION  + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + C_UNIT_ID + ") REFERENCES " + TABLE_UNITS + "(" + C_ID + ")"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_EXERCISES + " ("
                + C_ID             + " TEXT PRIMARY KEY, "
                + C_LESSON_ID      + " TEXT NOT NULL, "
                + C_TYPE           + " TEXT NOT NULL, "
                + C_PROMPT         + " TEXT, "
                + C_POSITION       + " INTEGER NOT NULL, "
                + C_VOCAB_DARIJA   + " TEXT, "
                + C_VOCAB_ARABIC   + " TEXT, "
                + C_VOCAB_FRENCH   + " TEXT, "
                + C_VOCAB_ENGLISH  + " TEXT, "
                + C_NOTE           + " TEXT, "
                + C_VIDEO_RES_ID   + " INTEGER, "
                + C_VIDEO_DESC     + " TEXT, "
                + C_QUIZ_Q         + " TEXT, "
                + C_QUIZ_Q_FR      + " TEXT, "
                + C_QUIZ_CORRECT   + " TEXT, "
                + C_WR_FRENCH      + " TEXT, "
                + C_WR_HINT        + " TEXT, "
                + "FOREIGN KEY(" + C_LESSON_ID + ") REFERENCES " + TABLE_LESSONS + "(" + C_ID + ")"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_QUIZ_OPT + " ("
                + C_EXERCISE_ID + " TEXT NOT NULL, "
                + C_OPTION_TEXT + " TEXT NOT NULL, "
                + C_POSITION    + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + C_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + C_ID + ")"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_VIDEO_VOC + " ("
                + C_EXERCISE_ID  + " TEXT NOT NULL, "
                + C_VOCAB_DARIJA + " TEXT, "
                + C_VOCAB_ARABIC + " TEXT, "
                + C_VOCAB_FRENCH + " TEXT, "
                + C_VOCAB_ENGLISH + " TEXT, "
                + C_POSITION      + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + C_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + C_ID + ")"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_WR_ANS + " ("
                + C_EXERCISE_ID + " TEXT NOT NULL, "
                + C_VALUE       + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + C_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + C_ID + ")"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_PROGRESS + " ("
                + C_KEY   + " TEXT PRIMARY KEY, "
                + C_VALUE + " TEXT NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + TABLE_COMPLETED + " ("
                + C_LESSON_COMPLETED + " TEXT PRIMARY KEY, "
                + C_COMPLETED_AT     + " INTEGER NOT NULL"
                + ");");
    }

    // ─── Seed data ─────────────────────────────────────────────────────────────
    /**
     * Toutes les leçons / exercices / vocabulaire sont insérés ici lors de la
     * première installation (onCreate). Pour ajouter du contenu, ajoutez des
     * lignes dans les méthodes seedUnit*, seedLesson*, seedExercise* ci-dessous.
     */
    private void seedData(SQLiteDatabase db) {
        // ===== Unité 1 — Salutations =====
        insertUnit(db, "u1", "Salam — Salutations",
                "Dis bonjour comme un Marocain",
                Color.parseColor("#c8541c"), 1);

        insertLesson(db, "l1-1", "u1", "Bonjour & Au revoir", "Premiers mots", "sun", 10, 1);
        insertText(db,   "e1-1-1", "l1-1", 1, "Apprends ce mot",
                "Salam", "سلام", "Bonjour / Salut", "Hello",
                "Utilisé à n'importe quel moment de la journée.");
        insertVoice(db,  "e1-1-2", "l1-1", 2, "Écoute la prononciation",
                "Labas?", "لا باس؟", "Ça va ?", "How are you?");
        insertQuiz(db,   "e1-1-3", "l1-1", 3, "Choisis la bonne traduction",
                "Salam", "Que veut dire 'Salam' ?", "Bonjour",
                new String[]{"Merci", "Bonjour", "Au revoir", "Oui"});
        insertWriting(db,"e1-1-4", "l1-1", 4, "Écris en darija", "Au revoir",
                "Commence par 'b'", new String[]{"bslama", "b'slama", "bslamah"});
        insertText(db,   "e1-1-5", "l1-1", 5, "Apprends ce mot",
                "Bslama", "بسلامة", "Au revoir", "Goodbye", null);

        insertLesson(db, "l1-2", "u1", "Mots polis", "Merci et s'il te plaît", "heart", 10, 2);
        insertText(db,   "e1-2-1", "l1-2", 1, "Apprends ce mot",
                "Choukran", "شكرا", "Merci", "Thank you", null);
        insertVoice(db,  "e1-2-2", "l1-2", 2, "Écoute et répète",
                "3afak", "عافاك", "S'il te plaît", "Please");
        insertQuiz(db,   "e1-2-3", "l1-2", 3, "Comment dit-on 'Merci' ?",
                "Merci", "Choisis la bonne traduction", "Choukran",
                new String[]{"3afak", "Choukran", "Smehli", "Iyeh"});
        insertWriting(db,"e1-2-4", "l1-2", 4, "Traduis en darija", "Excuse-moi",
                "Commence par 'sm'", new String[]{"smehli", "smeh li", "smehlii"});

        // ===== Unité 2 — Famille =====
        insertUnit(db, "u2", "L'3a'ila — Famille",
                "Parle de tes proches",
                Color.parseColor("#1f8a70"), 2);

        insertLesson(db, "l2-1", "u2", "Parents", "Maman & Papa", "users", 10, 1);
        insertText(db,   "e2-1-1", "l2-1", 1, "Apprends ce mot",
                "L'oum", "الأم", "La mère", "Mother", null);
        insertText(db,   "e2-1-2", "l2-1", 2, "Apprends ce mot",
                "L'ab", "الأب", "Le père", "Father", null);
        insertVoice(db,  "e2-1-3", "l2-1", 3, "Écoute attentivement",
                "Khoya", "خويا", "Mon frère", "My brother");
        insertQuiz(db,   "e2-1-4", "l2-1", 4, "Que veut dire 'Khti' ?",
                "Khti", "Choisis la bonne traduction", "Ma sœur",
                new String[]{"Mon frère", "Mon père", "Ma sœur", "Ma mère"});
        insertWriting(db,"e2-1-5", "l2-1", 5, "Traduis en darija", "Mon frère",
                null, new String[]{"khoya", "khouya"});

        // ===== Unité 3 — Thé & Nourriture =====
        insertUnit(db, "u3", "Atay — Thé & Nourriture",
                "Commande comme un local",
                Color.parseColor("#3a7ca5"), 3);

        insertLesson(db, "l3-1", "u3", "Le thé marocain", "Regarde et apprends", "coffee", 15, 1);
        insertVideo(db,  "e3-1-1", "l3-1", 1, "Regarde cette courte scène",
                R.raw.tea_video,
                "Au Maroc, le thé à la menthe (atay b'na3na3) est un rituel quotidien. "
                + "Le thé est versé de haut pour créer la mousse — signe d'un thé bien servi.",
                new String[][]{
                    {"Atay",   "أتاي",  "Le thé",  "Tea"},
                    {"Na3na3", "نعناع", "La menthe","Mint"},
                    {"Sokar",  "سكر",   "Le sucre", "Sugar"}
                });
        insertText(db,   "e3-1-2", "l3-1", 2, "Apprends ce mot",
                "Khobz", "خبز", "Le pain", "Bread", null);
        insertVoice(db,  "e3-1-3", "l3-1", 3, "Écoute et apprends",
                "Ma", "ما", "L'eau", "Water");
        insertQuiz(db,   "e3-1-4", "l3-1", 4, "Que veut dire 'Atay' ?",
                "Atay", "Choisis la bonne traduction", "Le thé",
                new String[]{"Le café", "Le thé", "Le lait", "Le pain"});
        insertWriting(db,"e3-1-5", "l3-1", 5, "Traduis en darija", "Le pain",
                null, new String[]{"khobz", "khoubz"});

        // ===== Unité 4 — Nombres =====
        insertUnit(db, "u4", "L'argam — Nombres",
                "Compte de 1 à 10",
                Color.parseColor("#e8a93b"), 4);

        insertLesson(db, "l4-1", "u4", "1 à 5", "Nombres de base", "hash", 10, 1);
        insertText(db,   "e4-1-1", "l4-1", 1, "Apprends ce nombre",
                "Wahed", "واحد", "Un", "One", null);
        insertText(db,   "e4-1-2", "l4-1", 2, "Apprends ce nombre",
                "Jouj", "جوج", "Deux", "Two", null);
        insertVoice(db,  "e4-1-3", "l4-1", 3, "Écoute attentivement",
                "Tlata", "ثلاثة", "Trois", "Three");
        insertQuiz(db,   "e4-1-4", "l4-1", 4, "Que veut dire 'Khamsa' ?",
                "Khamsa", "Choisis le bon nombre", "5",
                new String[]{"3", "4", "5", "6"});
        insertWriting(db,"e4-1-5", "l4-1", 5, "Traduis en darija", "Quatre",
                "Contient le 3 arabe (ع)",
                new String[]{"rb3a", "rb3ah", "rba3a", "arba3a"});

        // ===== Unité 5 — Sport =====
        insertUnit(db, "u5", "Riyada — Sport",
                "Parle de sport comme un fan",
                Color.parseColor("#2563eb"), 5);

        // Leçon 5-1 : Vocabulaire football
        insertLesson(db, "l5-1", "u5", "Football", "Le sport roi", "star", 15, 1);
        insertText(db,   "e5-1-1", "l5-1", 1, "Apprends ce mot",
                "Kora", "كرة", "Ballon / Football", "Ball / Football",
                "Kora dial qadam = football (kora = ballon, qadam = pied).");
        insertText(db,   "e5-1-2", "l5-1", 2, "Apprends ce mot",
                "Mara", "مارة", "But", "Goal",
                "Tu peux aussi dire 'goal' — très utilisé au Maroc.");
        insertVoice(db,  "e5-1-3", "l5-1", 3, "Écoute et répète",
                "Fariq", "فريق", "Équipe", "Team");
        insertQuiz(db,   "e5-1-4", "l5-1", 4, "Que veut dire 'Kora' ?",
                "Kora", "Choisis la bonne traduction", "Ballon / Football",
                new String[]{"Équipe", "But", "Ballon / Football", "Arbitre"});
        insertWriting(db,"e5-1-5", "l5-1", 5, "Traduis en darija", "Équipe",
                "Commence par 'f'", new String[]{"fariq", "fariiq"});
        insertText(db,   "e5-1-6", "l5-1", 6, "Apprends ce mot",
                "Hakim", "حكم", "Arbitre", "Referee", null);
        insertQuiz(db,   "e5-1-7", "l5-1", 7, "Que veut dire 'Mara' ?",
                "Mara", "Choisis la bonne traduction", "But",
                new String[]{"Ballon", "But", "Équipe", "Terrain"});

        // Leçon 5-2 : Activités sportives
        insertLesson(db, "l5-2", "u5", "Activités", "Courir, nager, jouer", "activity", 15, 2);
        insertText(db,   "e5-2-1", "l5-2", 1, "Apprends ce mot",
                "Jri", "جري", "Course / Courir", "Running",
                "Ana kajri = je cours.");
        insertText(db,   "e5-2-2", "l5-2", 2, "Apprends ce mot",
                "3om", "عوم", "Natation / Nager", "Swimming", null);
        insertVoice(db,  "e5-2-3", "l5-2", 3, "Écoute et répète",
                "Tel3ab", "تلعب", "Tu joues", "You play");
        insertQuiz(db,   "e5-2-4", "l5-2", 4, "Que veut dire 'Jri' ?",
                "Jri", "Choisis la bonne traduction", "Course / Courir",
                new String[]{"Natation", "Course / Courir", "Vélo", "Boxe"});
        insertText(db,   "e5-2-5", "l5-2", 5, "Apprends ce mot",
                "Bisiklat", "بيسيكلات", "Vélo / Cyclisme", "Cycling", null);
        insertWriting(db,"e5-2-6", "l5-2", 6, "Traduis en darija", "Nager",
                "Commence par '3'", new String[]{"3om", "3awm", "3awem"});
        insertQuiz(db,   "e5-2-7", "l5-2", 7, "Comment dit-on 'Vélo' en darija ?",
                "Vélo", "Choisis la bonne traduction", "Bisiklat",
                new String[]{"Bisiklat", "Karusa", "Moteur", "Tren"});

        // Leçon 5-3 : Expressions du sport
        insertLesson(db, "l5-3", "u5", "Expressions", "Gagner, perdre, jouer ensemble", "trophy", 20, 3);
        insertText(db,   "e5-3-1", "l5-3", 1, "Apprends cette expression",
                "Rbeh", "ربح", "Gagner / Il a gagné", "To win",
                "Rbehna = on a gagné !");
        insertText(db,   "e5-3-2", "l5-3", 2, "Apprends cette expression",
                "Khser", "خسر", "Perdre / Il a perdu", "To lose", null);
        insertVoice(db,  "e5-3-3", "l5-3", 3, "Écoute attentivement",
                "N'lebo kora", "نلعبو كرة", "On joue au foot", "Let's play football");
        insertQuiz(db,   "e5-3-4", "l5-3", 4, "Que veut dire 'Rbeh' ?",
                "Rbeh", "Choisis la bonne traduction", "Gagner",
                new String[]{"Perdre", "Égaliser", "Gagner", "Jouer"});
        insertWriting(db,"e5-3-5", "l5-3", 5, "Traduis en darija", "On a gagné",
                "Commence par 'rbeh'", new String[]{"rbehna", "rbahna"});
        insertText(db,   "e5-3-6", "l5-3", 6, "Apprends cette expression",
                "Match nul", "ماتش نول", "Match nul", "Draw",
                "Emprunté du français — très courant dans le langage marocain.");
        insertQuiz(db,   "e5-3-7", "l5-3", 7, "Que veut dire 'Khser' ?",
                "Khser", "Choisis la bonne traduction", "Perdre",
                new String[]{"Gagner", "Jouer", "Perdre", "Marquer"});
    }

    // ─── Helper insert methods ─────────────────────────────────────────────────

    private void insertUnit(SQLiteDatabase db, String id, String title,
                            String desc, int color, int pos) {
        ContentValues v = new ContentValues();
        v.put(C_ID, id); v.put(C_TITLE, title); v.put(C_DESCRIPTION, desc);
        v.put(C_COLOR, color); v.put(C_POSITION, pos);
        db.insert(TABLE_UNITS, null, v);
    }

    private void insertLesson(SQLiteDatabase db, String id, String unitId,
                              String title, String subtitle, String icon,
                              int xp, int pos) {
        ContentValues v = new ContentValues();
        v.put(C_ID, id); v.put(C_UNIT_ID, unitId); v.put(C_TITLE, title);
        v.put(C_SUBTITLE, subtitle); v.put(C_ICON_NAME, icon);
        v.put(C_XP_REWARD, xp); v.put(C_POSITION, pos);
        db.insert(TABLE_LESSONS, null, v);
    }

    private void insertText(SQLiteDatabase db, String id, String lessonId, int pos,
                            String prompt, String darija, String arabic,
                            String french, String english, String note) {
        ContentValues v = baseExercise(id, lessonId, "TEXT", prompt, pos);
        v.put(C_VOCAB_DARIJA, darija); v.put(C_VOCAB_ARABIC, arabic);
        v.put(C_VOCAB_FRENCH, french); v.put(C_VOCAB_ENGLISH, english);
        if (note != null) v.put(C_NOTE, note);
        db.insert(TABLE_EXERCISES, null, v);
    }

    private void insertVoice(SQLiteDatabase db, String id, String lessonId, int pos,
                             String prompt, String darija, String arabic,
                             String french, String english) {
        ContentValues v = baseExercise(id, lessonId, "VOICE", prompt, pos);
        v.put(C_VOCAB_DARIJA, darija); v.put(C_VOCAB_ARABIC, arabic);
        v.put(C_VOCAB_FRENCH, french); v.put(C_VOCAB_ENGLISH, english);
        db.insert(TABLE_EXERCISES, null, v);
    }

    private void insertVideo(SQLiteDatabase db, String id, String lessonId, int pos,
                             String prompt, int resId, String desc,
                             String[][] vocab) {
        ContentValues v = baseExercise(id, lessonId, "VIDEO", prompt, pos);
        v.put(C_VIDEO_RES_ID, resId); v.put(C_VIDEO_DESC, desc);
        db.insert(TABLE_EXERCISES, null, v);
        // vocab rows
        for (int i = 0; i < vocab.length; i++) {
            ContentValues vv = new ContentValues();
            vv.put(C_EXERCISE_ID, id);
            vv.put(C_VOCAB_DARIJA,  vocab[i][0]);
            vv.put(C_VOCAB_ARABIC,  vocab[i][1]);
            vv.put(C_VOCAB_FRENCH,  vocab[i][2]);
            vv.put(C_VOCAB_ENGLISH, vocab[i][3]);
            vv.put(C_POSITION, i);
            db.insert(TABLE_VIDEO_VOC, null, vv);
        }
    }

    private void insertQuiz(SQLiteDatabase db, String id, String lessonId, int pos,
                            String prompt, String question, String questionFr,
                            String correct, String[] options) {
        ContentValues v = baseExercise(id, lessonId, "QUIZ", prompt, pos);
        v.put(C_QUIZ_Q, question); v.put(C_QUIZ_Q_FR, questionFr);
        v.put(C_QUIZ_CORRECT, correct);
        db.insert(TABLE_EXERCISES, null, v);
        for (int i = 0; i < options.length; i++) {
            ContentValues ov = new ContentValues();
            ov.put(C_EXERCISE_ID, id);
            ov.put(C_OPTION_TEXT, options[i]);
            ov.put(C_POSITION, i);
            db.insert(TABLE_QUIZ_OPT, null, ov);
        }
    }

    private void insertWriting(SQLiteDatabase db, String id, String lessonId, int pos,
                               String prompt, String french, String hint,
                               String[] accepted) {
        ContentValues v = baseExercise(id, lessonId, "WRITING", prompt, pos);
        v.put(C_WR_FRENCH, french);
        if (hint != null) v.put(C_WR_HINT, hint);
        db.insert(TABLE_EXERCISES, null, v);
        for (String a : accepted) {
            ContentValues av = new ContentValues();
            av.put(C_EXERCISE_ID, id);
            av.put(C_VALUE, a);
            db.insert(TABLE_WR_ANS, null, av);
        }
    }

    private ContentValues baseExercise(String id, String lessonId,
                                       String type, String prompt, int pos) {
        ContentValues v = new ContentValues();
        v.put(C_ID, id); v.put(C_LESSON_ID, lessonId);
        v.put(C_TYPE, type); v.put(C_PROMPT, prompt);
        v.put(C_POSITION, pos);
        return v;
    }
}
