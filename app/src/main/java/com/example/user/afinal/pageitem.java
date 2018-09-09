package com.example.user.afinal;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class pageitem extends AppCompatActivity {

    private ListView listView;
    private ListAdapter listAdapter;
    private List<HashMap<String , String>> list;
    private AlertDialog dialog;
    static private String[] item = new String[20];
    static private String[] tag = new String[20];

    private int currentPosition;
    private int linenew;

    private String filename = "DataFile.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pageitem);

        //        // Home 按鈕喚頁
        Button buttonhome = (Button) findViewById(R.id.buttonback);
        Button buttonnew = (Button) findViewById(R.id.buttonnew);
        Button buttondel = (Button) findViewById(R.id.buttondel);

        buttonhome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(pageitem.this,MainActivity.class);
                startActivity(intent);
            }
        });
        buttonnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newAlertDialog(view);
                dialog.show();
            }
        });
        buttondel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item=new String[20];
                tag=new String[20];
                list.clear();
                listView.setAdapter(listAdapter);
                ((BaseAdapter)listAdapter).notifyDataSetChanged();
                writeData();
            }
        });
        if(readData()){
        }
        else {
            item[0]="物品1";item[1]="物品2";item[2]="物品3";
            tag[0]="tag001";tag[1]="tag002";tag[2]="tag003";
        }
        //List 使用
        listView = (ListView) findViewById(R.id.List);
        list = new ArrayList<>();
        //使用List存入HashMap，用來顯示ListView上面的文字。
        linenew=0;
        for(int i = 0 ; i < item.length ; i++){
            if (item[i]==null)continue;
            linenew++;
            HashMap<String , String> hashMap = new HashMap<>();
            hashMap.put("item" , item[i]);
            hashMap.put("tag" , tag[i]);
            //把title , text存入HashMap之中
            list.add(hashMap);
            //把HashMap存入list之中
        }
        listAdapter = new SimpleAdapter(
                this,
                list,
                android.R.layout.simple_list_item_2 ,
                new String[]{"item" , "tag"} ,
                new int[]{android.R.id.text1 , android.R.id.text2});
        // 5個參數 : context , List , layout , key1 & key2 , text1 & text2

        listView.setAdapter(listAdapter);
        //列表長按監聽器
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                currentPosition= position;
                setAlertDialog(view);
                dialog.show();
                return false;
            }
        });
    }
    // 外部XML 呼叫
    private void setAlertDialog(final View view) {
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        View contview = factory.inflate(R.layout.modifylist, null);
        contview.setBackgroundColor(Color.WHITE);
        final EditText edit = (EditText) contview.findViewById(R.id.edit_dialog);
        final EditText edit2 = (EditText) contview.findViewById(R.id.edit_dialog2);
        Button btOK = (Button) contview.findViewById(R.id.btOK_dialog);
        btOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String newitemName = edit.getText().toString();
                String newtagName = edit2.getText().toString();
                item[currentPosition] = newitemName;
                tag[currentPosition] = newtagName;
                writeData();
                HashMap<String , String> hashMap = new HashMap<>();
                hashMap.put("item" , newitemName);
                hashMap.put("tag" , newtagName);
                list.remove(currentPosition);
                list.add(currentPosition,hashMap);
                listView.setAdapter(listAdapter);
                ((BaseAdapter)listAdapter).notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog = new AlertDialog.Builder(pageitem.this).setView(contview)
                .create();
    }
    // 外部XML 呼叫
    private void newAlertDialog(final View view) {
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        View contview = factory.inflate(R.layout.modifylist, null);
        contview.setBackgroundColor(Color.WHITE);
        final EditText edit = (EditText) contview.findViewById(R.id.edit_dialog);
        final EditText edit2 = (EditText) contview.findViewById(R.id.edit_dialog2);
        Button btOK = (Button) contview.findViewById(R.id.btOK_dialog);
        btOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String newitemName = edit.getText().toString();
                String newtagName = edit2.getText().toString();
                item[linenew] = newitemName;
                tag[linenew] = newtagName;
                writeData();
                linenew++;
                HashMap<String , String> hashMap = new HashMap<>();
                hashMap.put("item" , newitemName);
                hashMap.put("tag" , newtagName);
                //list.remove(currentPosition);
                list.add(hashMap);
                listView.setAdapter(listAdapter);
                ((BaseAdapter)listAdapter).notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog = new AlertDialog.Builder(pageitem.this).setView(contview)
                .create();
    }
    // 資料寫入
    private void writeData(){
        try {
            FileOutputStream outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i = 0 ; i < item.length ; i++){
                if (item[i]==null)continue;
                //SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                //String date = sDateFormat.format(new java.util.Date());
                outputStream.write(item[i].getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.write(tag[i].getBytes());
                outputStream.write("\r\n".getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //資料讀取
    private boolean readData() {
        if (getFilesDir().exists()) {
            try {
                FileInputStream inputStream = openFileInput(filename);
                byte[] readBytes = new byte[inputStream.available()];
                inputStream.read(readBytes);
                String readString = new String(readBytes);
                String[] content = readString.split("\r\n");
                for (int i = 0, j = 0; i < content.length - 1; i++) {
                    if (content[i] == "") continue;
                    if (i % 2 == 0) item[j] = content[i];
                    else {
                        tag[j] = content[i];
                        j++;
                    }
                }
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else return false;
    }

}
