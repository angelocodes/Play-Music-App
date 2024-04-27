package com.ajikadev.playmusicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_PERMISSIONS_CODE = 123;
  private ImageButton playPauseButton, stopButton, nextButton, previousButton;
  private TextView currentSongTextView;

  private ListView songList;
  private MediaPlayer mediaPlayer;
  private int currentSongIndex = 0;
  private String[] audioFilePaths;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    playPauseButton = findViewById(R.id.btnPlayPause);
    playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    stopButton = findViewById(R.id.btnStop);
    nextButton = findViewById(R.id.btnNext);
    previousButton = findViewById(R.id.btnPrevious);
    songList = findViewById(R.id.List);
    currentSongTextView = findViewById(R.id.currentSongTextView);

    if (checkPermissions()){
    fetchAudioFiles();
    } else {
      requestPermissions();
    }

    /*ImageView imageView = (ImageView) findViewById(R.id.image_view);
    Picasso.get().load(R.drawable.your_gif_file).into(imageView);*/

    songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        playSong(position);
      }
    });

    playPauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
          pauseSong();
          playPauseButton.setImageResource(android.R.drawable.ic_media_play); // Change button image to play
        } else {
          if (checkPermissions()) {

            playSong(currentSongIndex);
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause); // Change button image to pause
          } else {
            requestPermissions();
          }
        }
      }
    });

    stopButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        stopSong();
      }
    });

    nextButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playNextSong();
      }
    });

    previousButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playPreviousSong();
      }
    });
  }

  private boolean checkPermissions() {
    int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    return readPermission == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermissions() {
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_PERMISSIONS_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        fetchAudioFiles();
      } else {
        Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void displaySongList() {
    String[] songNames = new String[audioFilePaths.length];
    for (int i = 0; i < audioFilePaths.length; i++) {
      songNames[i] = audioFilePaths[i].substring(audioFilePaths[i].lastIndexOf("/") + 1);
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songNames);
    songList.setAdapter(adapter);
  }
  private void fetchAudioFiles() {
    Toast.makeText(getApplicationContext(), "Fetching audio files...", Toast.LENGTH_SHORT).show();

    String[] projection = new String[]{
      MediaStore.Audio.AlbumColumns.ALBUM,
      MediaStore.Audio.Media.DATA
    };

    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), uri, projection,
      null, null, null);

    Cursor cursor = cursorLoader.loadInBackground();

    if (cursor != null && cursor.moveToFirst()) {
      audioFilePaths = new String[cursor.getCount()];
      int i = 0;
      do {
        String audioFilePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        audioFilePaths[i++] = audioFilePath;
      } while (cursor.moveToNext());
      cursor.close();
      displaySongList();
    }

  }

  private void playSong(int position) {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      stopSong(); // Stop the current song before playing a new one
    }
    if (audioFilePaths != null && audioFilePaths.length > 0) {
      try {
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 0) {
          mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
          mediaPlayer.start();
        } else {
          mediaPlayer = new MediaPlayer();
          mediaPlayer.setDataSource(audioFilePaths[position]);
          mediaPlayer.prepare();
          mediaPlayer.start();
        }
        currentSongIndex = position; // Update the current song index
        Toast.makeText(getApplicationContext(), "Playing: " + audioFilePaths[position], Toast.LENGTH_SHORT).show();

        String currentSongName = audioFilePaths[position].substring(audioFilePaths[position].lastIndexOf("/") + 1);
        currentSongTextView.setText(currentSongName);
        currentSongTextView.setSelected(true); //
        playPauseButton.setImageResource(android.R.drawable.ic_media_pause); // Change button image to pause
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Toast.makeText(getApplicationContext(), "No audio files found", Toast.LENGTH_SHORT).show();
    }
  }

  private void stopSong() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.release();
      playPauseButton.setImageResource(android.R.drawable.ic_media_play);
      mediaPlayer = null;
    }
  }

  private void pauseSong() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
      playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }
  }

  private void playNextSong() {
    if (mediaPlayer != null && currentSongIndex < audioFilePaths.length - 1) {
      stopSong();
      currentSongIndex++;
      playSong(currentSongIndex);
    } else {
      Toast.makeText(getApplicationContext(), "No next song available", Toast.LENGTH_SHORT).show();
    }
  }

  private void playPreviousSong() {
    if (mediaPlayer != null && currentSongIndex > 0) {
      stopSong();
      currentSongIndex--;
      playSong(currentSongIndex);
    } else {
      Toast.makeText(getApplicationContext(), "No previous song available", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopSong();
  }
}
