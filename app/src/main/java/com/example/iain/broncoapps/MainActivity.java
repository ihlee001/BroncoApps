package com.example.iain.broncoapps;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void mapGraph(View v){
        new JSONAsyncTask().execute("http://broncomaps.com/edit/events/data/");
        Intent intent = new Intent(this, GMaps.class);
        startActivity(intent);
    }

    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls){
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost("http://broncomaps.com/edit/events/data/");
            httppost.setHeader("Content-type", "application/json");

            String path = Environment.getExternalStorageDirectory().getPath() + "/Download";

            File file = new File(path, "BuildingList2.txt");

            InputStream inputStream = null;
            String result;
            JSONArray jArray;

            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                // json is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();
                reader.close();

                FileOutputStream stream = new FileOutputStream(file);
                jArray = new JSONArray(result);
                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        org.json.JSONObject jobject = jArray.getJSONObject(i);
                        String towrite = jobject.getString("title") + "\t" +
                                jobject.getString("description") + "\t" +
                                jobject.getString("location") + "\t" +
                                jobject.getString("location_details") + "\t" +
                                jobject.getString("start_date") + "\t" +
                                jobject.getString("end_date") + "\t" +
                                jobject.getString("start_time") + "\t" +
                                jobject.getString("end_time") + "\t" +
                                jobject.getString("Lon") + ", " +
                                jobject.getString("Lat") + "\n";
                        stream.write(towrite.getBytes());
                        /*String oneObjectsItem2 = oneObject.getString("description");*/
                        Log.d("json", towrite);
                    } catch (org.json.JSONException e) {
                        // Oops
                    }
                }

                stream.close();
                Log.d("Finisher", "Try Method Complete");
            } catch (FileNotFoundException e){
                Log.e("login activiyt", "file not found");
            } catch (IOException e) {
                Log.e("login activity", "Can not read file");
            } catch (org.json.JSONException e){
                // Oops
            }
            finally {
                try{
                    if(inputStream != null) inputStream.close();
                }catch(Exception squish){}
            }
            return false;
        }
    }
}
