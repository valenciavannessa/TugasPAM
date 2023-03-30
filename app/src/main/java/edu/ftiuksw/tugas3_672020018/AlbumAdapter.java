package edu.ftiuksw.tugas3_672020018;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumAdapter extends BaseAdapter{

    Activity activity;
    ArrayList<HashMap<String, String>> data;

    public AlbumAdapter(Activity a, ArrayList<HashMap<String, String>> d){
        activity = a;
        data = d;
    }

    @Override
    public int getCount() { //menghitung jml data
        return data.size();
    }

    @Override
    public Object getItem(int position) { //mengambil item keberapa
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //mengambil view yg tersedia
        AlbumViewHolder holder = null;
        if(convertView == null) {
            holder = new AlbumViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.album_row, parent, false);

            holder.galleryImage = convertView.findViewById(R.id.galleryImage);
            holder.gallery_count = convertView.findViewById(R.id.gallery_count);
            holder.gallery_title = convertView.findViewById(R.id.gallery_title);

            convertView.setTag(holder);
        } else {
            holder = (AlbumViewHolder) convertView.getTag();
        }
        holder.galleryImage.setId(position);
        holder.gallery_count.setId(position);
        holder.gallery_title.setId(position);

        HashMap<String, String> image = new HashMap<>();
        image = data.get(position);
        try {
            holder.gallery_count.setText(image.get(Function.KEY_COUNT));
            holder.gallery_title.setText(image.get(Function.KEY_ALBUM));

            Glide.with(activity).load(new File(image.get(Function.KEY_PATH))).into(holder.galleryImage);
        } catch(Exception ex) {

        }

        return convertView;
    }
}

class AlbumViewHolder {
    ImageView galleryImage;
    TextView gallery_count, gallery_title;
}