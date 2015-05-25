package com.example.iain.broncoapps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iain on 3/4/2015.
 */
public class PolygonHolder {
    Polygon polygon;
    ArrayList<Event> event_list = new ArrayList<Event>();
    MarkerOptions marker;
    LatLng center;
    LatLng below_center;
    float radius;
    String number;
    String snippet = "";
    int id;
    EnglishNumberToWords entw = new EnglishNumberToWords();


    public PolygonHolder(Polygon polygon, int id){
        number = entw.convertLessThanOneThousand(id);
        this.id = id;
        this.polygon = polygon;
        this.center = centroid(polygon.getPoints());
        this.radius = largest_distance(polygon.getPoints());
        marker = new MarkerOptions();
        marker.visible(true);
        marker.position(below_center);
        marker.title("Events:");
        //marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        marker.icon(BitmapDescriptorFactory.fromResource(getResId(number, R.drawable.class)));
    }

    public void add_event(Event event){
        event_list.add(event);
        snippet += event.getTitle() + ": " + event.getDescription() + "\n";
        marker.snippet(snippet);
    }

    public LatLng centroid(List<LatLng> points) {
        double[] centroid = { 0.0, 0.0 };

        for (int i = 0; i < points.size(); i++) {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        below_center = new LatLng(centroid[0]  - 0.0001, centroid[1] + 0.00005);
        center = new LatLng(centroid[0], centroid[1]);
        return center;
    }

    public float largest_distance(List<LatLng> points){
        float longest = 0;
        float[] result = new float[1];
        for(int i = 0; i < points.size(); i++){
            Location.distanceBetween(center.latitude, center.longitude, points.get(i).latitude, points.get(i).longitude, result);
            if(result[0] > longest) longest = result[0];
        }
        return longest;
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public MarkerOptions getMarker(){return marker;}
}
