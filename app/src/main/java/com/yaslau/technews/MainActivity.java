package com.yaslau.technews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView myView = (ListView) findViewById(R.id.myView);
        Article[] articles = new Article[20];
        Log.i("db", doesDatabaseExist(this, "db16") + "");
        if(!doesDatabaseExist(this, "db16")){
            SQLiteDatabase mydatabase = openOrCreateDatabase("db16", MODE_PRIVATE, null);
            String apiURL = "https://hacker-news.firebaseio.com/v0/topstories.json";
            Log.i("hello", "hi");
            String[] ids = new String[20];
            DownloadIds task = new DownloadIds();
            try {
                String fetchedIds = task.execute(apiURL).get();
                Log.i("ids", fetchedIds);
                fetchedIds = fetchedIds.substring(1, fetchedIds.length() - 1);
                String[] idsArr = fetchedIds.split(",");
                System.arraycopy(idsArr, 0, ids, 0, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for(int i = 0; i<20; i++) {
                if (i == 8) ids[i] = "29279467";
                if (i == 18) ids[i] = "29288445";
                if (i == 9 || i == 17 || i == 16 || i == 11) ids[i] = "29277106";
                String appInfo = "https://hacker-news.firebaseio.com/v0/item/" + ids[i] + ".json?print=pretty";
                DownloadInfo downloadInfo = new DownloadInfo();
                try {
                    String info = downloadInfo.execute(appInfo).get();
                    JSONObject json = new JSONObject(info);
                    String title = json.getString("title");
                    String url = json.getString("url");
                    articles[i] = new Article(ids[i], title, url);
                    Log.i("fetching " + i, info);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
            for(int i = 0; i < 20; i++) {
                //Log.i("articles " + i, articles[i].toString());
                String id = articles[i].getId();
                String title = articles[i].getTitle();
                String url = articles[i].getUrl();
                mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Articles(id VARCHAR, title VARCHAR, url VARCHAR);");
                mydatabase.execSQL("INSERT INTO Articles VALUES('" + id + "','"+title+"','"+url+"');");
            }
        }

        SQLiteDatabase mydatabase = openOrCreateDatabase("db16", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("Select * from Articles",null);
        resultSet.moveToFirst();
        for(int i = 0; i < 20; i++) {
            String id = resultSet.getString(0);
            String title = resultSet.getString(1);
            String url = resultSet.getString(2);
            Log.i("column + " + i, id + " " + title + " " + url);
            articles[i] = new Article(id, title, url);
            resultSet.moveToNext();
        }
        ArrayList<String> titles = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            titles.add(articles[i].getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, titles);
        myView.setAdapter(adapter);

        myView.setOnItemClickListener( new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("url", articles[i].getUrl());
                startActivity(intent);
            }
        });

    }


    private static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

}
