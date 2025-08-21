package com.example.yemenmusicplayer;

import android.media.MediaPlayer;
import java.io.IOException;

public class MusicPlayer {

    private MediaPlayer mediaPlayer;

    public MusicPlayer() {
        mediaPlayer = new MediaPlayer();
    }

    public void play(String streamUrl) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(streamUrl);
            mediaPlayer.prepareAsync(); // Use prepareAsync() for network streams
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }
}


