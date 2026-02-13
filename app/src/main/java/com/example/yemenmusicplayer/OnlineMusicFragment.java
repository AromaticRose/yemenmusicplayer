package com.example.yemenmusicplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class OnlineMusicFragment extends Fragment {

    private ListView onlineSongListView;
    private SongAdapter songAdapter;
    private List<Song> songList;
    private MainActivity mainActivity;
    private SongDownloader songDownloader;

    // Audius endpoint for user's tracks (replace username if needed)
    private static final String AUDIOUS_USER_TRACKS_URL = "https://discoveryprovider2.audius.co/v1/users/Nooqaqw/tracks";

    private String pendingDownloadUrl = null;
    private String pendingDownloadTitle = null;
    private ActivityResultLauncher<String> storagePermissionLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_music, container, false);
        onlineSongListView = view.findViewById(R.id.onlineSongListView);
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        onlineSongListView.setAdapter(songAdapter);
        songDownloader = new SongDownloader(requireContext());

        // register permission launcher
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                if (pendingDownloadUrl != null) {
                    songDownloader.downloadSong(pendingDownloadUrl, pendingDownloadTitle);
                }
            }
            pendingDownloadUrl = null;
            pendingDownloadTitle = null;
        });

        onlineSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view1, int position, long id) {
                Song selectedSong = songList.get(position);
                if (mainActivity != null) {
                    mainActivity.playSong(selectedSong);
                }
            }
        });

        // Fetch Audius songs using the provided endpoint
        new FetchAudiusSongsTask(requireContext(), AUDIOUS_USER_TRACKS_URL, new FetchAudiusSongsTask.OnSongsFetchedListener() {
            @Override
            public void onSongsFetched(List<Song> songs) {
                if (songs == null) return;
                songList.clear();
                songList.addAll(songs);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            songAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    songAdapter.notifyDataSetChanged();
                }
            }
        }).execute();

        return view;
    }

    private class SongAdapter extends android.widget.ArrayAdapter<Song> {
        public SongAdapter(Context context, List<Song> songs) {
            super(context, 0, songs);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_song, parent, false);
            }

            final Song currentSong = getItem(position);

            TextView songTitleTextView = convertView.findViewById(R.id.songTitleTextView);
            Button downloadButton = convertView.findViewById(R.id.downloadButton);

            songTitleTextView.setText(currentSong == null ? "Unknown" : currentSong.getTitle());

            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentSong != null && currentSong.getDownloadUrl() != null && !currentSong.getDownloadUrl().isEmpty()) {
                        // For Android < Q request WRITE_EXTERNAL_STORAGE
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                pendingDownloadUrl = currentSong.getDownloadUrl();
                                pendingDownloadTitle = currentSong.getTitle();
                                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                return;
                            }
                        }
                        songDownloader.downloadSong(currentSong.getDownloadUrl(), currentSong.getTitle());
                    }
                }
            });

            return convertView;
        }
    }
}