package com.example.yemenmusicplayer;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchAudiusSongsTask extends AsyncTask<Void, Void, List<Song>> {

    public interface OnSongsFetchedListener {
        void onSongsFetched(List<Song> songs);
    }

    private OnSongsFetchedListener listener;

    public FetchAudiusSongsTask(OnSongsFetchedListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<Song> doInBackground(Void... voids) {
        List<Song> songs = new ArrayList<>();
        try {
            // Fetch user's tracks directly
            String apiUrl = AudiusConfig.audiusApiBaseUrl + "/users/" + AudiusConfig.audiusUserId + "/tracks?app_name=" + AudiusConfig.appName;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(result.toString());
            JSONArray tracks = jsonResponse.getJSONArray("data");

            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i);
                String title = track.getString("title");
                String streamUrl = AudiusConfig.audiusApiBaseUrl + "/tracks/" + track.getString("id") + "/stream?app_name=" + AudiusConfig.appName;
                String downloadUrl = track.optString("download_url", null); // Audius API might not provide direct download URL for all tracks

                Song song = new Song(title, streamUrl, downloadUrl);
                songs.add(song);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions appropriately
        }
        return songs;
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        if (listener != null) {
            listener.onSongsFetched(songs);
        }
    }
}


