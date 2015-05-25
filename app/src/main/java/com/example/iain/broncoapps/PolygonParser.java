package com.example.iain.broncoapps;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Iain on 5/25/2015.
 */
public class PolygonParser {
    BufferedReader br;
    Map<Integer, PolygonOptions> polygonoptions;


    public PolygonParser(String file) throws IOException {
        polygonoptions = new HashMap<Integer, PolygonOptions>();
        br = new BufferedReader(new FileReader(file));
        String currentLine = null;

        while((currentLine = br.readLine()) != null){
            addToPolygonMap(currentLine);
        }
        br.close();
    }

    public void addToPolygonMap(String line){
        ArrayList<LatLng> latlnglist = new ArrayList<LatLng>();
        String[] parts = line.split("\t");
        for(int i = 3; i < parts.length; i++){
            String[] latlong = parts[i].split(",");
            latlnglist.add(new LatLng(Double.parseDouble(latlong[0]), Double.parseDouble(latlong[1])));
        }
        PolygonOptions polygon = new PolygonOptions().strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000);
        for(int i = 0 ; i < latlnglist.size(); i++){
            polygon.add(latlnglist.get(i));
        }
        polygonoptions.put(Integer.parseInt(parts[1]), polygon);
    }
}
