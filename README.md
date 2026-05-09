# Darija Learn — Android Studio (Java)

Application Android native pour apprendre le **Darija** (arabe marocain), inspirée de Duolingo.

- **Langage** : Java
- **IDE** : Android Studio (Iguana / Koala / Panda 2 ou plus récent)
- **minSdk** : 24 (Android 7.0 Nougat)
- **targetSdk / compileSdk** : 34
- **Architecture** : `MainActivity` + `BottomNavigationView` + 3 `Fragment`
  (Apprendre / Pratiquer / Profil), `LessonActivity` qui enchaîne 5 types d'exercices via `Fragment` :
  - `TextExerciseFragment` (carte de vocabulaire + TTS arabe)
  - `VoiceExerciseFragment` (`TextToSpeech` en `Locale("ar")`)
  - `VideoExerciseFragment` (`VideoView` + vidéo locale)
  - `QuizExerciseFragment` (QCM)
  - `WritingExerciseFragment` (saisie texte avec validation)

La progression (XP, série, cœurs, leçons terminées) est sauvegardée avec `SharedPreferences`.

## Ouvrir le projet

1. Lancez **Android Studio**.
2. `File` → `Open` → choisissez le dossier `darija-android`.
3. Android Studio va proposer d'installer/configurer le **Gradle Wrapper** automatiquement
   à la première synchronisation. Acceptez. (Le wrapper binaire `gradle-wrapper.jar`
   n'est pas inclus dans cette archive — Android Studio le télécharge tout seul.)
4. Patientez le temps de la synchronisation Gradle.
5. Branchez un téléphone Android (ou démarrez un AVD), puis cliquez sur **Run ▶**.

## Si la synchronisation Gradle ne génère pas le wrapper

Ouvrez un terminal dans le dossier du projet et lancez :

```bash
gradle wrapper --gradle-version 8.7
```

(Nécessite `gradle` installé. Sinon, Android Studio le fait automatiquement.)

## Personnaliser le contenu

Tout le contenu pédagogique (unités, leçons, exercices, vocabulaire) est dans :

```
app/src/main/java/com/darija/learn/data/LessonRepository.java
```

Pour ajouter une nouvelle leçon, ajoutez un `Lesson` à l'unité correspondante avec
sa liste d'`Exercise`.

## Permissions

L'app n'a besoin que de l'accès à Internet (déclaré dans le manifeste pour de futurs
contenus en ligne — la version actuelle fonctionne 100% hors-ligne).
