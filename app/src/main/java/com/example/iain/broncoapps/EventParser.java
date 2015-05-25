package com.example.iain.broncoapps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Iain on 1/14/2015.
 */
public class EventParser {
    BufferedReader br;
    ArrayList<Event> events;

    public EventParser(String file) throws IOException {
        events = new ArrayList<Event>();
        br = new BufferedReader(new FileReader(file));
        String currentLine = null;

        while((currentLine = br.readLine()) != null){
            addToEventArray(currentLine);
        }
        br.close();
    }

    private void addToEventArray(String currentLine){
        String[] parts = currentLine.split("\t");
        String[] latlong = parts[4].split(",");
        double lat = Double.parseDouble(latlong[0]);
        double lon = Double.parseDouble(latlong[1]);
        LatLng location = new LatLng(lat, lon);

        events.add(new Event(parts[0], parts[1], parts[2], location, parts[6], parts[7], parts[11]));
    }

    public ArrayList<Event> getEvents(){
        return events;
    }

    public void printTest(){
        for(int i = 0; i < events.size(); i++){
            Log.i("EventParser", events.get(i).getString());
        }
    }
}
