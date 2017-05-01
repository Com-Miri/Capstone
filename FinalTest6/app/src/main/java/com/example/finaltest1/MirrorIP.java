package com.example.finaltest1;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 태경원 on 2017-02-20.
 */

public class MirrorIP extends Activity {

    private EditText et_ip;
    private TextView tv_currentIP;
    private Button btn_setIP;

    private String current_ip;
    private String smartmirror_ip;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        tv_currentIP=(TextView) findViewById(R.id.tv_currentIP);
        et_ip = (EditText) findViewById(R.id.et_ip);
        btn_setIP = (Button) findViewById(R.id.btn_setIP);
        prefs = getSharedPreferences("login", 0);
        editor = prefs.edit();

        current_ip = prefs.getString("SMARTMIRROR_IP", "0.0.0.0");
        tv_currentIP.setText(current_ip);
        btn_setIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smartmirror_ip = et_ip.getText().toString();
                editor.putString("SMARTMIRROR_IP", smartmirror_ip);
                editor.commit();
                Toast.makeText(MirrorIP.this, "IP가" + smartmirror_ip + "로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                tv_currentIP.setText(smartmirror_ip);
            }
        });

    }
}
