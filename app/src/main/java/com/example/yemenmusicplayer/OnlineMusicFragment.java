package com.example.yemenmusicplayer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class OnlineMusicFragment extends Fragment {

    private ListView onlineSongListView;
    private SongAdapter songAdapter;
    private List<Song> songList;
    private MainActivity mainActivity;
    private SongDownloader songDownloader;

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
        songDownloader = new SongDownloader(getContext());

        onlineSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song selectedSong = songList.get(position);
                if (mainActivity != null) {
                    mainActivity.playSong(selectedSong);
                }
            }
        });

        // Fetch Audius songs here
        new FetchAudiusSongsTask(new FetchAudiusSongsTask.OnSongsFetchedListener() {
            @Override
            public void onSongsFetched(List<Song> songs) {
                songList.clear();
                songList.addAll(songs);
                songAdapter.notifyDataSetChanged();
            }
        }).execute();

        return view;
    }

    private class SongAdapter extends ArrayAdapter<Song> {
        public SongAdapter(Context context, List<Song> songs) {
            super(context, 0, songs);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_song, parent, false);
            }

            Song currentSong = getItem(position);

            TextView songTitleTextView = convertView.findViewById(R.id.songTitleTextView);
            Button downloadButton = convertView.findViewById(R.id.downloadButton);

            songTitleTextView.setText(currentSong.getTitle());

            downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentSong.getDownloadUrl() != null) {
                        songDownloader.downloadSong(currentSong.getDownloadUrl(), currentSong.getTitle());
                    }
                }
            });

            return convertView;
        }
    }
}


