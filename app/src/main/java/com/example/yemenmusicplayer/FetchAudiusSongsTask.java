package com.example.yemenmusicplayer;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * FetchAudiusSongsTask
 * - Uses the provided Audius endpoint (discoveryprovider2) to fetch tracks for a user
 * - Parses JSON using org.json (no external dependencies)
 */
public class FetchAudiusSongsTask extends AsyncTask<Void, Void, List<Song>> {
    private static final String TAG = "FetchAudiusSongsTask";

    public interface OnSongsFetchedListener {
        void onSongsFetched(List<Song> songs);
    }

    private final OnSongsFetchedListener listener;
    private final String endpointUrl;

    public FetchAudiusSongsTask(String endpointUrl, OnSongsFetchedListener listener) {
        this.listener = listener;
        this.endpointUrl = endpointUrl;
    }

    @Override
    protected List<Song> doInBackground(Void... voids) {
        List<Song> result = new ArrayList<>();
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpointUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int code = conn.getResponseCode();
            InputStream in = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            String body = readStream(in);
            Log.d(TAG, "Fetched JSON: " + body);

            // Parse JSON: response may be an array or an object with data field
            try {
                Object root = new org.json.JSONTokener(body).nextValue();
                JSONArray tracksArray = null;

                if (root instanceof JSONArray) {
                    tracksArray = (JSONArray) root;
                } else if (root instanceof JSONObject) {
                    JSONObject rootObj = (JSONObject) root;
                    if (rootObj.has("data") && rootObj.get("data") instanceof JSONArray) {
                        tracksArray = rootObj.getJSONArray("data");
                    } else if (rootObj.has("tracks") && rootObj.get("tracks") instanceof JSONArray) {
                        tracksArray = rootObj.getJSONArray("tracks");
                    }
                }

                if (tracksArray == null) {
                    Log.w(TAG, "No tracks array found in response");
                    return result;
                }

                for (int i = 0; i < tracksArray.length(); i++) {
                    try {
                        JSONObject t = tracksArray.getJSONObject(i);
                        String title = t.optString("title", "Unknown");
                        String trackId = t.has("id") ? t.optString("id") : null;
                        String streamUrl = null;
                        String downloadUrl = null;

                        // try common fields
                        if (t.has("stream_url") && !t.isNull("stream_url")) {
                            streamUrl = t.optString("stream_url", null);
                        }

                        // media array often contains url entries
                        if (streamUrl == null && t.has("media") && !t.isNull("media")) {
                            try {
                                JSONArray media = t.getJSONArray("media");
                                if (media.length() > 0) {
                                    JSONObject m0 = media.getJSONObject(0);
                                    if (m0.has("url") && !m0.isNull("url")) {
                                        streamUrl = m0.optString("url", null);
                                    }
                                }
                            } catch (JSONException ignored) {}
                        }

                        // fallback build a stream endpoint
                        if (streamUrl == null && trackId != null) {
                            streamUrl = "https://discoveryprovider2.audius.co/v1/tracks/" + trackId + "/stream";
                        }

                        // download url attempts
                        if (t.has("download_url") && !t.isNull("download_url")) {
                            downloadUrl = t.optString("download_url", null);
                        } else if (t.has("download") && !t.isNull("download")) {
                            try {
                                JSONObject dl = t.getJSONObject("download");
                                if (dl.has("url") && !dl.isNull("url")) {
                                    downloadUrl = dl.optString("url", null);
                                }
                            } catch (JSONException ignored) {}
                        } else if (t.has("permalink") && !t.isNull("permalink")) {
                            downloadUrl = t.optString("permalink", null);
                        }

                        Song s = new Song(title, streamUrl, downloadUrl);
                        result.add(s);
                    } catch (Exception e) {
                        Log.w(TAG, "Skipping track due to parse error", e);
                    }
                }
            } catch (JSONException je) {
                Log.e(TAG, "JSON parse error", je);
            }

        } catch (IOException e) {
            Log.e(TAG, "Network I/O error fetching tracks", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
        return result;
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        if (listener != null) listener.onSongsFetched(songs);
    }

    private static String readStream(InputStream in) throws IOException {
        if (in == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}