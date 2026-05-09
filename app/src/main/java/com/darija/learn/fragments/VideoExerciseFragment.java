package com.darija.learn.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.darija.learn.R;
import com.darija.learn.data.Exercise;
import com.darija.learn.data.Lesson;
import com.darija.learn.data.LessonRepository;
import com.darija.learn.data.VocabItem;

public class VideoExerciseFragment extends Fragment {

    private static final String ARG_LESSON_ID   = "lesson_id";
    private static final String ARG_EXERCISE_ID = "exercise_id";
    private VideoView videoView;
    private MediaController mediaController;

    public static VideoExerciseFragment create(Exercise e) {
        VideoExerciseFragment f = new VideoExerciseFragment();
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
        return i.inflate(R.layout.fragment_video_exercise, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Exercise e = resolve();
        if (e == null) return;

        ((TextView) v.findViewById(R.id.video_prompt)).setText(e.prompt);
        ((TextView) v.findViewById(R.id.video_description)).setText(e.description);

        videoView = v.findViewById(R.id.video_view);

        // MediaController : barre de lecture (play/pause/avance rapide)
        mediaController = new MediaController(requireContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        Uri uri = Uri.parse("android.resource://"
                + requireContext().getPackageName() + "/" + e.videoResId);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener((MediaPlayer mp) -> {
            // Ajuste la hauteur du VideoView pour respecter le ratio 16:9
            int videoWidth  = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            if (videoWidth > 0 && videoHeight > 0) {
                int viewWidth = videoView.getWidth();
                if (viewWidth > 0) {
                    int newHeight = (int) ((float) viewWidth * videoHeight / videoWidth);
                    ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                    lp.height = newHeight;
                    videoView.setLayoutParams(lp);
                }
            }
            mp.setLooping(true);
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            TextView prompt = v.findViewById(R.id.video_prompt);
            prompt.setText("Impossible de lire la vidéo. Vérifiez que le fichier est présent dans res/raw/.");
            return true;
        });

        // Affichage du vocabulaire
        LinearLayout vocabBox = v.findViewById(R.id.video_vocab);
        vocabBox.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (VocabItem item : e.vocabList) {
            View row = inflater.inflate(R.layout.item_vocab_row, vocabBox, false);
            ((TextView) row.findViewById(R.id.vocab_darija)).setText(item.darija);
            ((TextView) row.findViewById(R.id.vocab_french)).setText(item.french);
            ((TextView) row.findViewById(R.id.vocab_arabic)).setText(item.arabic);
            vocabBox.addView(row);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.resume();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (videoView != null) { videoView.stopPlayback(); videoView = null; }
        if (mediaController != null) { mediaController = null; }
    }
}
