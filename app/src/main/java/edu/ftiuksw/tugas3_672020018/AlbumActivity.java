package edu.ftiuksw.tugas3_672020018;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AlbumActivity extends AppCompatActivity {

    GridView galleryGridView;
    LoadAlbumGallery loadAlbumGalleryTask;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        galleryGridView = findViewById(R.id.gridViewAlbum);


        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (displayMetrics.densityDpi / 160f);
        if(dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        albumName = getIntent().getStringExtra("albumName");
        setTitle(albumName);
        loadAlbumGalleryTask = new LoadAlbumGallery();
        loadAlbumGalleryTask.execute();
    }

    class LoadAlbumGallery extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() { //persiapan sebeelum di eksekusi
            super.onPreExecute();
            albumList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            Uri uriInternal = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection =  {MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.BUCKET_ID,
                    MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name=?", new String[] {albumName}, null);
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name=?", new String[] {albumName}, null);
            Cursor mergeCursor = new MergeCursor(new Cursor[] {cursorExternal, cursorInternal});

            while (mergeCursor.moveToNext()){ //bagian membaca data
                @SuppressLint("Range") String path = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                @SuppressLint("Range") String album = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME));
                @SuppressLint("Range") String timestamp = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
                String time = Function.convertToTime(timestamp);
                String count = Function.getCounts(getApplicationContext(), album);


                albumList.add(Function.mappingData(album, path, timestamp, time, count));


            }
            mergeCursor.close();
            //proses sorting berdasarkan timestamp
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "desc"));
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //adapter yg datanya ambil dari albumlist
            SingleAlbumAdapter singleAlbumAdapter = new SingleAlbumAdapter(AlbumActivity.this, albumList);
            galleryGridView.setAdapter(singleAlbumAdapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(AlbumActivity.this, GalleryPreview.class);
                    intent.putExtra("path", albumList.get(position).get(Function.KEY_PATH));
                    startActivity(intent);
                }
            });
        }
    }
}