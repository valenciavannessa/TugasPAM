package edu.ftiuksw.tugas3_672020018;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class GalleryPreview extends AppCompatActivity {

    ImageView galleryPreview;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_preview);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        galleryPreview = findViewById(R.id.GalleryPreviewTag);
        Glide.with(GalleryPreview.this).load(new File(path)).into(galleryPreview);
    }
}