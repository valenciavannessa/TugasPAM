package edu.ftiuksw.tugas3_672020018;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    GridView galleryGridView;
    static final int REQUEST_PERMISSION_CODE = 1;
    LoadAlbum loadAlbumTask;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<>(); //tempat menyimpan data yg diambil dari galery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        galleryGridView = findViewById(R.id.gridViewGallery);

        int iDisplayWidth = getResources().getDisplayMetrics().widthPixels;
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float dp = iDisplayWidth / (displayMetrics.densityDpi / 160f);
        if(dp < 360) {
            dp = (dp - 17) / 2;
            float px = Function.convertDpToPixel(dp, getApplicationContext());
            galleryGridView.setColumnWidth(Math.round(px));
        }

        String permissions[] = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadAlbumTask = new LoadAlbum();
                    loadAlbumTask.execute();
                } else {
                    Toast.makeText(this, "You must allow this app", Toast.LENGTH_SHORT).show();
                }
        }
    }

    class LoadAlbum extends AsyncTask<String, Void, String> {
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
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, null, null, null);
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, null, null, null);
            Cursor mergeCursor = new MergeCursor(new Cursor[] {cursorExternal, cursorInternal});
            String currentBucketID = "";
            while (mergeCursor.moveToNext()){ //bagian membaca data
                @SuppressLint("Range") String bucket_id = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID));
                @SuppressLint("Range") String path = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                @SuppressLint("Range") String album = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME));
                @SuppressLint("Range") String timestamp = mergeCursor.getString(mergeCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED));
                String time = Function.convertToTime(timestamp);
                String count = Function.getCounts(getApplicationContext(), album);

                if(!currentBucketID.equals(bucket_id)){
                    albumList.add(Function.mappingData(album, path, timestamp, time, count));
                    currentBucketID = bucket_id;
                }

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
            AlbumAdapter albumAdapter = new AlbumAdapter(MainActivity.this, albumList);
            galleryGridView.setAdapter(albumAdapter);
            galleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
                    intent.putExtra("albumName", albumList.get(position).get(Function.KEY_ALBUM));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String permissions[] = {Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
        } else {
            loadAlbumTask = new LoadAlbum();
            loadAlbumTask.execute();
        }
    }
}