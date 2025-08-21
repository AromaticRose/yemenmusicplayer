package com.example.yemenmusicplayer;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import java.io.File;

public class SongDownloader {

    private Context context;

    public SongDownloader(Context context) {
        this.context = context;
    }

    public void downloadSong(String downloadUrl, String songTitle) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(downloadUrl);

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setTitle(songTitle);
        request.setDescription("Downloading " + songTitle);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Set the download destination
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), AudiusConfig.appName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String fileName = songTitle + ".mp3"; // You might need to adjust the file extension
        File destinationFile = new File(directory, fileName);
        request.setDestinationUri(Uri.fromFile(destinationFile));

        downloadManager.enqueue(request);
    }
}


