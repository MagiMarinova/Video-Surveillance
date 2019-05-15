package org.elsys.videosurvelliance;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import yjkim.mjpegviewer.MjpegView;

/**
 * This activity shows the video stream
 *
 * @author Magrgarita Marinova
 * @version 1.0
 * @since   2019-03-25
 */

public class VideoStream extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream);

        findViewById(R.id.exitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the main activity
                startActivity(new Intent(VideoStream.this , MainActivity.class));
                //
            }
        });
        //Get the address value from the Intent
        String address = getIntent().getStringExtra("ADDRESS");
        //
        loadFeed(address);
    }

    /**
     * Method for loading the Mjpeg video stream into the view
     *
     *
     * @param address - url from which to get the stream
     */

    private void loadFeed(final String address){
        final MjpegView videoView = findViewById(R.id.videoView);
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoView.Start("http://" + address + ":8081");
            }
        }).start();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        // Go back to the main activity
        startActivity(new Intent(this , MainActivity.class));
    }

}
