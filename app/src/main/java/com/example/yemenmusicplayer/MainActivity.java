package com.example.yemenmusicplayer;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private MusicPlayer musicPlayer;
    private TextView currentSongTitle;
    private Button playPauseButton, prevButton, nextButton;
    private SeekBar songProgressBar;
    private Handler handler = new Handler();
    private Song currentPlayingSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(new LocalMusicFragment(), "موسيقى محلي");
        adapter.addFragment(new OnlineMusicFragment(), "موسيقى أونلاين");
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getFragmentTitle(position));
        }).attach();

        // Initialize music player controls
        musicPlayer = new MusicPlayer();
        currentSongTitle = findViewById(R.id.currentSongTitle);
        playPauseButton = findViewById(R.id.playPauseButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        songProgressBar = findViewById(R.id.songProgressBar);

        playPauseButton.setOnClickListener(v -> {
            if (musicPlayer.isPlaying()) {
                musicPlayer.pause();
                playPauseButton.setText("Play");
            } else {
                if (currentPlayingSong != null) {
                    musicPlayer.play(currentPlayingSong.getStreamUrl());
                    playPauseButton.setText("Pause");
                }
            }
        });

        // Update seek bar progress
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicPlayer != null && musicPlayer.isPlaying()){
                    int mCurrentPosition = musicPlayer.getCurrentPosition() / 1000;
                    songProgressBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });

        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicPlayer != null && fromUser){
                    musicPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void playSong(Song song) {
        currentPlayingSong = song;
        currentSongTitle.setText(song.getTitle());
        musicPlayer.play(song.getStreamUrl());
        playPauseButton.setText("Pause");
        songProgressBar.setMax(musicPlayer.getDuration() / 1000);
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }

        public String getFragmentTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}


