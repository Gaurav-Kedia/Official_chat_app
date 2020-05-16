package com.gaurav.chat_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {
    private ImageView imageView;
    private String imageurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = (ImageView) findViewById(R.id.imageviewer);
        imageurl = getIntent().getStringExtra("url");
        Picasso.get().load(imageurl).into(imageView);
    }
}
