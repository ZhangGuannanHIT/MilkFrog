package cn.zgn.milkfrog;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private Button btnLoop;
    @SuppressWarnings("unused")
    private Button btnSwitch;

    private boolean isVideo1 = true;
    private boolean isLooping = false;

    private MediaPlayer currentMediaPlayer;

    private Uri video1Uri;
    private Uri video2Uri;

    private int video1Width = 1280;
    private int video1Height = 720;
    private int video2Width = 720;
    private int video2Height = 1280;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable firstFrameRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentMediaPlayer != null) {
                try {
                    currentMediaPlayer.pause();
                    currentMediaPlayer.seekTo(0);
                } catch (IllegalStateException e) {
                    // MediaPlayer released, ignore
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.video_view);
        btnLoop = findViewById(R.id.btn_loop);
        btnSwitch = findViewById(R.id.btn_switch);

        styleButtons();
        buildResourceUris();
        retrieveVideoDimensions();
        setupVideoListeners();
        setupButtonListeners();

        loadVideo();
    }

    // ========== Resource URIs ==========

    private void buildResourceUris() {
        video1Uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video1);
        video2Uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video2);
    }

    // ========== Video Dimensions ==========

    private void retrieveVideoDimensions() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.video1);
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            String w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (w != null && h != null) {
                video1Width = Integer.parseInt(w);
                video1Height = Integer.parseInt(h);
            }
            afd.close();
        } catch (Exception e) {
            // use fallback 1280×720
        }

        try {
            AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.video2);
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            String w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (w != null && h != null) {
                video2Width = Integer.parseInt(w);
                video2Height = Integer.parseInt(h);
            }
            afd.close();
        } catch (Exception e) {
            // use fallback 720×1280
        }

        try {
            retriever.release();
        } catch (Exception e) {
            // ignore
        }
    }

    // ========== Video Listeners ==========

    private void setupVideoListeners() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                currentMediaPlayer = mp;
                mp.setLooping(isLooping);
                applyVideoScaling();
                showFirstFrame();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
    }

    private void showFirstFrame() {
        handler.removeCallbacks(firstFrameRunnable);
        videoView.start();
        handler.postDelayed(firstFrameRunnable, 400);
    }

    // ========== Button Listeners ==========

    private void setupButtonListeners() {
        btnLoop.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLooping = !isLooping;
                if (isLooping) {
                    btnLoop.setText(R.string.stop_play);
                    btnLoop.getBackground().setTint(getColor(R.color.stop_orange));
                    if (currentMediaPlayer != null) {
                        currentMediaPlayer.setLooping(true);
                        try {
                            if (!currentMediaPlayer.isPlaying()) {
                                currentMediaPlayer.start();
                            }
                        } catch (IllegalStateException e) {
                            // ignore
                        }
                    }
                } else {
                    btnLoop.setText(R.string.loop_play);
                    btnLoop.getBackground().setTint(getColor(R.color.loop_green));
                    if (currentMediaPlayer != null) {
                        currentMediaPlayer.setLooping(false);
                        try {
                            currentMediaPlayer.pause();
                        } catch (IllegalStateException e) {
                            // ignore
                        }
                    }
                }
            }
        });

        btnSwitch.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If currently looping, stop first (matching HarmonyOS behavior)
                if (isLooping) {
                    handler.removeCallbacks(firstFrameRunnable);
                    videoView.stopPlayback();
                    currentMediaPlayer = null;
                    isLooping = false;
                    btnLoop.setText(R.string.loop_play);
                    btnLoop.getBackground().setTint(getColor(R.color.loop_green));
                }
                // Toggle video source
                isVideo1 = !isVideo1;
                loadVideo();
            }
        });
    }

    // ========== Video Loading ==========

    private void loadVideo() {
        handler.removeCallbacks(firstFrameRunnable);
        Uri uri = isVideo1 ? video1Uri : video2Uri;
        videoView.setVideoURI(uri);
    }

    // ========== Video Scaling ==========

    private void applyVideoScaling() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        if (isVideo1) {
            // Landscape video → Cover scaling
            // Calculate width needed to fill screen height at native aspect ratio,
            // then center-crop by overflowing sides beyond parent bounds
            float aspectRatio = (float) video1Width / (float) video1Height;
            int targetWidth = (int) (screenHeight * aspectRatio);
            int horizontalOffset = (targetWidth - screenWidth) / 2;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                targetWidth, screenHeight);
            params.leftMargin = -horizontalOffset;
            params.topMargin = 0;
            videoView.setLayoutParams(params);
        } else {
            // Portrait video → Contain scaling
            // match_parent lets VideoView fit within bounds, white background shows in letterbox
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
            params.leftMargin = 0;
            params.topMargin = 0;
            videoView.setLayoutParams(params);
        }
    }

    // ========== Button Styling ==========

    private void styleButtons() {
        float cornerRadius = dpToPx(22);

        GradientDrawable loopBg = new GradientDrawable();
        loopBg.setShape(GradientDrawable.RECTANGLE);
        loopBg.setCornerRadius(cornerRadius);
        loopBg.setColor(getColor(R.color.loop_green));
        btnLoop.setBackground(loopBg);
        btnLoop.setPadding(0, 0, 0, 0);

        GradientDrawable switchBg = new GradientDrawable();
        switchBg.setShape(GradientDrawable.RECTANGLE);
        switchBg.setCornerRadius(cornerRadius);
        switchBg.setColor(getColor(R.color.switch_blue));
        btnSwitch.setBackground(switchBg);
        btnSwitch.setPadding(0, 0, 0, 0);
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // ========== Lifecycle ==========

    @Override
    protected void onPause() {
        super.onPause();
        if (currentMediaPlayer != null) {
            try {
                if (currentMediaPlayer.isPlaying()) {
                    currentMediaPlayer.pause();
                }
            } catch (IllegalStateException e) {
                // ignore
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        videoView.stopPlayback();
        currentMediaPlayer = null;
    }
}
