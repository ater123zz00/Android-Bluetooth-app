package com.example.user.afinal;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {
    private String Data_filename = "DataFile.txt";
    private String Record_filename = "RecordFile.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView history =  (TextView) findViewById(R.id.history);
        inithistory(history);
        Button buttonitem = (Button) findViewById(R.id.buttonitem);
        buttonitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,pageitem.class);
                startActivity(intent);
            }
        });

        Button buttonbluetooth = (Button) findViewById(R.id.buttonbluetooth);
        buttonbluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent();
                intent2.setClass(MainActivity.this,connectbluetooth.class);
                startActivity(intent2);
            }
        });

        Button buttonclear = (Button) findViewById(R.id.Recordclear);
        buttonclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeclear();
                Intent intent2 = new Intent();
                intent2.setClass(MainActivity.this,MainActivity.class);
                startActivity(intent2);
                MainActivity.this.finish();
            }
        });

    }
    public void refresh() {
        onCreate(null);
    }
    private  void inithistory(TextView history){
        history.setMaxLines(200);
        if( getFilesDir().exists()) {
            try {
                FileInputStream inputStream = openFileInput(Record_filename);
                byte[] readBytes =new byte[inputStream.available()];
                inputStream.read(readBytes);
                String readString =new String(readBytes);
                String[] content = readString.split("\r\n");
                for (int i = 0; i < content.length; i++) {
                    history.append(content[i]+"\n");
                }
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void startAlarm(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (notification == null) return;
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }
    private void writeclear(){
        try {
            FileOutputStream outputStream = openFileOutput(Record_filename, Context.MODE_PRIVATE);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
