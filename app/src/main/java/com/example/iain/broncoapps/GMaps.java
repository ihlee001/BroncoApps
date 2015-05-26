package com.example.iain.broncoapps;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Iain on 3/24/2015.
 */
public class GMaps extends FragmentActivity{

    private Handler uiCallback;
    private GoogleMap googleMap;
    private MarkerOptions markerOptions;
    private Location location;
    private Map<Integer, PolygonHolder> polygonholder_list;
    private ArrayList<Building> buildings;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private Map<Integer, Marker> polygon_markers = new HashMap<Integer, Marker>();
    private Thread plotUserMovement;
    private Location previous_location;
    private Polygon[] polygon_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        try {
            //Starts to load things into Maps
            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        button_listener();

        try{
            //Initialize Building drop down list Spinner
            initializeBuildings();
            createPolygons();
            initializeEvents();
            initializeGo();
        } catch(IOException e){
            e.printStackTrace();
        }
    }



///////////////////////////////////////Map Initialization //////////////////////////////////////
    private void initializeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
        }
        centerMapOnMyLocation();
        googleMap.setMyLocationEnabled(false);
    }

    private void centerMapOnMyLocation() {
        googleMap.setMyLocationEnabled(false);
        location = getLocation();
        LatLng myLocation;
        if (location != null) {
            myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
            markerOptions = new MarkerOptions();
            markerOptions.position(myLocation);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
            markerOptions.title("you");

            googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                    (float) 16.0));
        }
    }

//////////////////////////////Building Initialization//////////////////////////////////////
    private void initializeBuildings() throws IOException{
        buildings = new BuildingParser(Environment.getExternalStorageDirectory()
                .getPath() + "/Download/buildingList.txt").getBuildingArray();

        /*//Tester
        for( int i  =0 ; i < buildings.size(); i++){
            Log.i("Building", buildings.get(i).getString());
        }*/

        add_building_drop_down_menu();
    }

    private void add_building_drop_down_menu(){
        ArrayList<String> buildingName = new ArrayList<>();
        for(int i = 0; i < buildings.size(); i++){
            buildingName.add(Integer.toString(buildings.get(i).getID()) + ": "+buildings.get(i).getName());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, buildingName);

        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        Spinner building = (Spinner) findViewById(R.id.building);
        building.setAdapter(dataAdapter);
    }

/////////////////////////////////////Event Initializer///////////////////////////////////////////
    public void initializeEvents() throws IOException{
        ArrayList<Event> events = new ArrayList<Event>();

        EventParser parser = new EventParser(Environment.getExternalStorageDirectory().getPath() + "/Download/buildingList2.txt");

        events = parser.getEvents();
        Log.d("initializeEvents", "" + events.size());
        for(int i = 0; i < events.size(); i++){
            Log.i("Events", events.get(i).getString());
        }

        for(int i = 0; i < events.size(); i++){
            int bn = events.get(i).building_number;
            if(bn == 0){
                Marker mark = googleMap.addMarker(events.get(i).getMarker());
                markers.add(mark);
            }
            else{
                try{
                    polygonholder_list.get(bn).add_event(events.get(i));
                }
                catch(Exception e){}
            }
        }
        for(PolygonHolder ph : polygonholder_list.values()){
            Log.d("initializeEvents", "goes here");
            Marker mark = googleMap.addMarker(ph.getMarker());
            polygon_markers.put(ph.id, mark);
        }
    }

    //Refreshes the initialized events list in case there is something that went amiss
    void button_listener(){
        Button button = (Button) findViewById(R.id.refresh_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JSONAsyncTask().execute("http://broncomaps.com/edit/events/data/");
                try{
                    initializeEvents();
                    initializeBuildings();
                }
                catch(IOException e){
                    Log.e("intializeEvents", "failed");
                }
            }
        });
    }

///////////////////////////////////Polygon Initializer/////////////////////////////////////////////
    public void createPolygons() throws IOException{
        polygonholder_list = new HashMap<Integer, PolygonHolder>();
        PolygonParser parser = new PolygonParser(Environment.getExternalStorageDirectory().getPath() + "/Download/buildingList1.txt");
        for(int index: parser.polygonoptions.keySet()){

            polygonholder_list.put(index, new PolygonHolder(googleMap.addPolygon(parser.polygonoptions.get(index)), index));
            //googleMap.addPolygon(new PolygonOptions().addAll(parser.polygonoptions.get(index).getPoints()).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000));
            Log.d("works", "" + parser.polygonoptions.get(index).getPoints() + ": " + index);

        }
/*        polygonholder_list.put(5, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.057886045422,-117.82490104437),
                new LatLng(34.057886045422,-117.82490104437), new LatLng(34.057886045422,-117.82490104437),
                new LatLng(34.057886045422,-117.82490104437)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 5));*/
/*        polygonholder_list.put(8, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.05871267583157, -117.82521486282349),
                new LatLng(34.05891266584845, -117.8247481584549), new LatLng(34.05830380588406, -117.82436728477478),
                new LatLng(34.058094925910545, -117.82487154006958)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 8));

        polygonholder_list.put(5, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.057886045422, -117.82490104437),
                new LatLng(34.058126035726, -117.82434314489), new LatLng(34.057659387289, -117.82404541969),
                new LatLng(34.057414951368, -117.82460868359)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 5));

        polygonholder_list.put(97, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.057748272904, -117.82376646996),
                new LatLng(34.05808603739, -117.82296985388), new LatLng(34.057743828625, -117.82275795937),
                new LatLng(34.057408284923, -117.82354921103)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 97));

        polygonholder_list.put(6, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.058768228661, -117.82310664654),
                new LatLng(34.058874889993, -117.82284110785), new LatLng(34.058392690823, -117.82254606485),
                new LatLng(34.058274918259, -117.8228276968)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 6));

        polygonholder_list.put(94, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.059408194635, -117.82354384661),
                new LatLng(34.059545964455, -117.82320320606), new LatLng(34.058979329083, -117.82284110785),
                new LatLng(34.058832669898, -117.82317638397)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 94));

        polygonholder_list.put(1, new PolygonHolder(googleMap.addPolygon(new PolygonOptions().add(new LatLng(34.059494856322, -117.82485276461),
                new LatLng(34.059832613848, -117.82400786877), new LatLng(34.059588184194, -117.82386034727),
                new LatLng(34.059248203594, -117.82471060753)).strokeColor(Color.BLACK).strokeWidth(4).fillColor(0x3F000000)), 1));*/

    }

    public void temp_grab_events(){
        String path = Environment.getExternalStorageDirectory().getPath() + "/Download";
        File file = new File(path, "BuildingList2.txt");
        try{
            FileOutputStream stream = new FileOutputStream(file);
            String towrite = "College of Sci event\tscience experiments\t8\tout in front\t2015-01-21\t2016-02-05\t00:00:00\t00:00:00\t-117.824643552303, 34.0587193421731\n";
            stream.write(towrite.getBytes());
            towrite = "Active Sock Fundraiser\tAmerican Marketing Association\t0\tOutdoor Spaces\t2015-01-21\t2016-02-21\t00:00:00\t00:00:00\t-117.823745012283, 34.0585926815949\n";
            stream.write(towrite.getBytes());
            Log.d("temp_events", "yay it worked");
        }
        catch(IOException e){
            Log.e("temp_events", "failed");
        }

    }

//////////////////////////////////////Go Button Initialization////////////////////////////////////
    private void initializeGo(){
        Log.d("initializeGo", "refreshes here");
        plotUserMovement = new DynamicLocation();
        Button go_btn = (Button) findViewById(R.id.go_button);
        View.OnClickListener goClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapDirections();
            }
        };
        go_btn.setOnClickListener(goClickListener);
    }

    private void mapDirections(){
        Log.d("mapDirections", "refreshes here");
        plotUserMovement.run();
        location = getLocation();
        final LatLng startingLocation = new LatLng(location.getLatitude(), location.getLongitude());
        final Thread t = new Thread(){

            @Override
            public void run(){
                try{
                    Log.i("sleep", "sleep");
                    sleep(10000);


                }catch(InterruptedException exception){
                    exception.printStackTrace();
                }
                if(startingLocation == null){
                    Log.e("mapdirection", "no starting location");
                    run();
                }
                else{
                    //Log.e("mapDireciton", "goes into else");
                    //progress.dismiss();
                    PlotDirectionTask task = new PlotDirectionTask();
                    //Log.e("mapDireciton", "goes into plotdirectiontask");
                    LatLng[] arrayLatLngs = new LatLng[2];

                    arrayLatLngs[0] = startingLocation;
                    arrayLatLngs[1] = getEndingLocation();
                    if(arrayLatLngs[1] == null)
                    {
                        //Log.e("mapdirection", "no ending location");
                    }

                    task.execute(arrayLatLngs);
                    //Log.e("mapDireciton", "goes into task");
                    int period = 1000;

                    Timer timer = new Timer();

                    timer.scheduleAtFixedRate(new TimerTask(){
                        public void run(){
                            updateDistanceToUser();
                            updateMap();
                        }



                        private void updateDistanceToUser() {
                            for(int i =0; i<buildings.size(); i++){
                                buildings.get(i).setDistance(calCulateDistance(startingLocation, buildings.get(i).getLocation(),'K'));
                            }

                        }

                        private double calCulateDistance(LatLng start, LatLng end, char unit){
                            double theta = start.longitude - end.longitude;
                            double dist = Math.sin(deg2rad(start.latitude)) * Math.sin(deg2rad(end.latitude)) + Math.cos(deg2rad(start.latitude)) * Math.cos(deg2rad(end.latitude)) * Math.cos(deg2rad(theta));
                            dist = rad2deg(Math.acos(dist)) * 60 * 1.1515;
                            if (unit == 'K') {
                                dist = dist * 1.609344;
                            } else if (unit == 'N') {
                                dist = dist * 0.8684;
                            }
                            return (dist);
                        }

                        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    					    /*::  This function converts decimal degrees to radians             :*/
    					    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
                        private double deg2rad(double deg) {
                            return (deg * Math.PI / 180.0);
                        }

                        /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    					    /*::  This function converts radians to decimal degrees             :*/
    					    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
                        private double rad2deg(double rad) {
                            return (rad * 180.0 / Math.PI);
                        }

                        private void updateMap() {
                            for(int i = 0; i< buildings.size(); i++){
                                if(buildings.get(i).getDistance()<0.094697 && buildings.get(i).getMarker().getSnippet()!=null){
                                    Message message = new Message();
                                    message.arg1 = i;
                                    uiCallback.sendMessage(message);
                                }
                            }

                        }
                    }, 0, period);
                    //Log.e("mapDireciton", "timer");
                }
            }
        };

        t.start();
        Log.d("t", "thread ends");
    }

    public LatLng getEndingLocation(){
        Spinner building = (Spinner) findViewById(R.id.building);
        int position = building.getSelectedItemPosition();
//    	Log.e("Ending location", )
        return buildings.get(position).getLocation();
    }

    class PlotDirectionTask extends AsyncTask<LatLng, Void, ArrayList<LatLng>>{

        @Override
        protected ArrayList<LatLng> doInBackground(LatLng... latlong) {
            Log.e("Plotdirectiontask", "doinbackground");
            GMapV2Direction md = new GMapV2Direction();
            LatLng start;
            LatLng end;

            start = latlong[0];

            end  =latlong[1];
            Document doc = md.getDocument(start, end, GMapV2Direction.MODE_WALKING);
            Log.e("Direction", doc.toString());
            ArrayList<LatLng> directionPoint = md.getDirection(doc);

            return directionPoint;
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> directionPoint) {
            Log.e("Plotdirectiontask", "onpostexecute");
            PolylineOptions rectLine = new PolylineOptions().width(7).color(
                    Color.BLUE);
            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            Polyline polylin = googleMap.addPolyline(rectLine);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0),
                    (float) 15.0));
        }

    }

    class DynamicLocation extends Thread{
        public void run(){
            Log.e("dynamiclocation", "run");
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location){
                    updateMapUI(location);
                }

                private void updateMapUI(Location location) {
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                    float[] result = new float[1];
                    if(previous_location != null){
                        Location.distanceBetween(previous_location.getLatitude(), previous_location.getLongitude(), location.getLatitude(), location.getLongitude(), result);
                        Log.d("starting location", "it reaches here3");
                        updateGPSCamera();
                        if(result[0] > 3 ){
                            Log.d("starting location", "it reaches here");
                            GMaps.this.googleMap.addMarker(marker);
                            updateGPSCamera();
                        }
                    }
                    else{
                        previous_location = location;
                        GMaps.this.googleMap.addMarker(marker);
                        Log.d("starting location", "it reaches here2");
                        updateGPSCamera();
                    }
                    previous_location = location;
                    GMaps.this.location = location;
                    //Log.d("starting location", "it reaches here");
                    LatLng startingLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    event_visibility(location, startingLocation, result);
                    polygon_visibility(result, startingLocation);
                    //Log.d("starting location", "it creates it");


                }

                private void event_visibility(Location location, LatLng startingLocation, float[] result){
                    for(int i = 0; i < markers.size(); i++){
                        Marker mark = markers.get(i);
                        LatLng event_loc = mark.getPosition();
                        Location.distanceBetween(event_loc.latitude, event_loc.longitude, startingLocation.latitude, startingLocation.longitude, result);
                        if(result[0] <= 40) {
                            mark.setVisible(true);
                        }
                        else {
                            mark.setVisible(false);
                        }
                    }
                }

                private void polygon_visibility(float[] result, LatLng startingLocation){
                    for(PolygonHolder ph : polygonholder_list.values()){
                        Location.distanceBetween(ph.center.latitude, ph.center.longitude, startingLocation.latitude, startingLocation.longitude, result);
                        if(result[0] <= 40 + ph.radius){
                            ph.polygon.setVisible(true);
                            polygon_markers.get(ph.id).setVisible(true);

                        }
                        else{
                            ph.polygon.setVisible(false);
                            polygon_markers.get(ph.id).setVisible(false);

                        }
                    }
                }


                @Override
                public void onProviderDisabled(String paramString) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProviderEnabled(String paramString) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onStatusChanged(String paramString,
                                            int paramInt, Bundle paramBundle) {
                    // TODO Auto-generated method stub

                }

            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void updateGPSCamera(){
        LatLng loc =new LatLng(location.getLatitude(), location.getLongitude());
        float zoom= (float) 17.00;
        //Set bearing here maybe?
        float bearing= location.getBearing();
        float tilt= (float) 90.00;
        CameraPosition cameraPosition = new CameraPosition(loc, zoom, tilt, bearing);
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                cameraPosition));

    }

    public Location getLocation(){
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            public void onLocationChanged(Location location){
                makeUseOfNewLocation(location);

            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                // TODO Auto-generated method stub

            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,40*1000,2, locationListener);

        if(location != null){
            return location;
        }
        else {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }

    private void makeUseOfNewLocation(Location location){
        this.location = location;
    }

    ///////////////////////////////////////JSON///////////////////////////////////////////////
    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls){
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost("http://ec2-52-10-224-162.us-west-2.compute.amazonaws.com/edit/events/data/");
            httppost.setHeader("Content-type", "application/json");
            HttpPost httpost2 = new HttpPost("http://ec2-52-10-224-162.us-west-2.compute.amazonaws.com/edit/locations/data/");

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
                                jobject.getString("location_type") + "\t" +
                                jobject.getString("location") + "\t" +
                                jobject.getString("lat") + ", " +
                                jobject.getString("lon") + "\t" +
                                jobject.getString("location_details") + "\t" +
                                jobject.getString("start_date") + "\t" +
                                jobject.getString("end_date") + "\t" +
                                jobject.getString("start_time") + "\t" +
                                jobject.getString("end_time") + "\t" +
                                jobject.getString("id") + "\t" +
                                jobject.getString("bNumber") + "\n";
                        stream.write(towrite.getBytes());
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
