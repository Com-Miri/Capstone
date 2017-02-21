package com.example.finaltest1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by 태경원 on 2017-02-20.
 */

public class Select extends AppCompatActivity {
    Button display;
    Button edit;
    Button ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        display = (Button) findViewById(R.id.display);
        edit = (Button) findViewById(R.id.edit);
        ip = (Button) findViewById(R.id.ip);

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dis = new Intent(Select.this, GridView.class);
                startActivity(dis);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent edt = new Intent(Select.this, MainActivity.class);
                startActivity(edt);
            }
        });
        ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ip = new Intent(Select.this, MirrorIP.class);
                startActivity(ip);
            }
        });
    }

}
