package com.example.iain.broncoapps;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Iain on 1/14/2015.
 */
public class Event {
    private LatLng location;
    private String title;
    private String description;
    private String location_type;
    private String start_time;
    private String end_time;
    private MarkerOptions marker;
    int building_number;

    public Event(String title, String description, String location_type, LatLng location, String start_time, String end_time, String building_number_str){
        this.title = title;
        this.description = description;
        this.location = location;
        this.location_type = location_type;
        this.start_time = start_time;
        this.end_time = end_time;
        building_number = Integer.parseInt(building_number_str);
        if(building_number == 0){
            marker = new MarkerOptions();
            marker.visible(true);
            marker.position(location);
            marker.title(title);
            marker.snippet(description);
            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }

    }


    public LatLng getLocation(){
        return location;
    }

    public int get_building_number(){ return building_number;}

    public String getTitle(){
        return title;
    }

    public String getDescription(){
        return description;
    }

    public MarkerOptions getMarker(){
        return marker;
    }

    public String getString(){
        return title + "\t" + description + "\t" + location.latitude + location.longitude + "\t" + building_number;
    }

    public void setMarker(boolean set){
        marker.visible(set);
    }

    public boolean visible(){
        return marker.isVisible();
    }

}
