package com.example.iain.broncoapps;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BuildingParser {
	BufferedReader br;
	ArrayList<Building> building;
	
	public BuildingParser(String fileName) throws IOException {
		building = new ArrayList<Building>();
		br = new BufferedReader(new FileReader(fileName));
		String currentLine = null;

		//discard the first 2 line of the text file
		br.readLine();br.readLine();
		// scan line by line to construct our building array list
		while((currentLine = br.readLine()) != null){
			addRecordToBuildingArray(currentLine);
		}
		br.close();
	}

	private void addRecordToBuildingArray(String currentLine) {
		String[] parts = currentLine.split("\t");
		if(parts.length == 4){
			Log.i("parserAddingRecord", parts[0] + "," + parts[1] + "," + parts[2] + "," + parts[3]);
		}
		else
		{
			Log.i("parserAddingRecord", parts[0] + "," + parts[1] + "," + parts[2]);
		}
		Building temp;
		//parse Id
		int id = Integer.parseInt(parts[0]);
		
		// parse Latlong
		String[] latlong = parts[1].split(",");
		double lat = Double.parseDouble(latlong[0]);
		double lon = Double.parseDouble(latlong[1]);
		LatLng location = new LatLng(lat, lon);
		
		//parse Building name
		String buildingName = parts [2];
		
		//parse event (if there is)
		if(parts.length ==4 ){
			String events = parts[3];
			temp = new Building(id, location, buildingName, events);
		}
		else
		{
			temp = new Building(id, location, buildingName);
		}
		
		// addd this record to building Array
		building.add(temp);
	}
	
	public ArrayList<Building> getBuildingArray(){
		return building;
	}
	
	//testing print to see if parser work or not
	public void testingPrint(){
		for(int i = 0; i< building.size(); i++){
			Log.i("buildingParser", building.get(i).getString());
		}
	}
}
