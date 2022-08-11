package com.soapp.global;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.soapp.R;

import androidx.appcompat.app.AppCompatActivity;

public class ImageVideoViewList extends AppCompatActivity {

    GridView imageVideo_List;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagevideo_grid_list);

        imageVideo_List = findViewById(R.id.imageVideo_List);
        imageVideo_List.setAdapter(new ImageVideo_Adapter(this));

        imageVideo_List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {


            }
        });
    }
}
