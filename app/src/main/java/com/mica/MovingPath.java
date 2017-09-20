package com.mica;

import java.util.ArrayList;

public class MovingPath {
	
	public MovingPath() {
	}
	public static ArrayList<CheckPoint> getAllCheckPoints() { ArrayList<CheckPoint> ret = new ArrayList<CheckPoint>();
CheckPoint point_0 = new CheckPoint("PSI-CENTER", 5.041108, 55.206192, 8); ret.add(point_0);
CheckPoint point_1 = new CheckPoint("PSI-DOOR1", 5.041108, 51.706192, 8); ret.add(point_1);
CheckPoint point_2 = new CheckPoint("PSI-DOOR2", 12.041108, 51.706192, 8); ret.add(point_2);
CheckPoint point_3 = new CheckPoint("LV8-STAIR1", 12.041108, 48.006192, 8); ret.add(point_3);
CheckPoint point_4 = new CheckPoint("806-DOOR2", 12.041108, 37.506192, 8); ret.add(point_4);
CheckPoint point_5 = new CheckPoint("805-DOOR2", 12.041108, 34.756192, 8); ret.add(point_5);
CheckPoint point_6 = new CheckPoint("MEETING-DOOR2", 12.041108, 27.756192, 8); ret.add(point_6);
CheckPoint point_7 = new CheckPoint("LV8-SQUARE1", 12.041108, 20.256192, 8); ret.add(point_7);
CheckPoint point_8 = new CheckPoint("LV8-SQUARE2", 5.041108, 20.256192, 8); ret.add(point_8);
CheckPoint point_9 = new CheckPoint("LV8-SQUARE3", 5.041108, 15.256192, 8); ret.add(point_9);
CheckPoint point_10 = new CheckPoint("LV8-SQUARE4", 12.041108, 15.256192, 8); ret.add(point_10);
CheckPoint point_11 = new CheckPoint("LV8-SQUARE5", 15.041108, 15.256192, 8); ret.add(point_11);
CheckPoint point_12 = new CheckPoint("LV8-SQUARE6", 15.041108, 5.256192, 8); ret.add(point_12);
CheckPoint point_13 = new CheckPoint("STAIR2-1", 15.041108, 2.256192, 8); ret.add(point_13);
CheckPoint point_14 = new CheckPoint("STAIR2-2", 16.541108, 2.256192, 8); ret.add(point_14);
CheckPoint point_15 = new CheckPoint("LV9-SQUARE6", 16.541108, 5.256192, 9); ret.add(point_15);
CheckPoint point_16 = new CheckPoint("LV9-SQUARE5", 15.041108, 15.256192, 9); ret.add(point_16);
CheckPoint point_17 = new CheckPoint("LV9-SQUARE4", 12.041108, 15.256192, 9); ret.add(point_17);
CheckPoint point_18 = new CheckPoint("LV9-SQUARE3", 5.041108, 15.256192, 9); ret.add(point_18);
CheckPoint point_19 = new CheckPoint("LV9-SQUARE2", 5.041108, 20.256192, 9); ret.add(point_19);
CheckPoint point_20 = new CheckPoint("LV9-SQUARE1", 12.041108, 20.256192, 9); ret.add(point_20);
CheckPoint point_21 = new CheckPoint("904-DOOR2", 12.041108, 27.756192, 9); ret.add(point_21);
CheckPoint point_22 = new CheckPoint("905-DOOR2", 12.041108, 37.506192, 9); ret.add(point_22);
CheckPoint point_23 = new CheckPoint("CAFE-DOOR2", 12.041108, 44.006192, 9); ret.add(point_23);
CheckPoint point_24 = new CheckPoint("LV9-STAIR1", 12.041108, 48.506192, 9); ret.add(point_24);
CheckPoint point_25 = new CheckPoint("LV9-OUT1", 12.041108, 52.006192, 9); ret.add(point_25);
CheckPoint point_26 = new CheckPoint("LV9-OUT2", 3.041108, 52.006192, 9); ret.add(point_26);
CheckPoint point_27 = new CheckPoint("LV9-OUT3", 3.041108, 57.006192, 9); ret.add(point_27);
CheckPoint point_28 = new CheckPoint("LV9-OUT4", 12.041108, 57.006192, 9); ret.add(point_28);
CheckPoint point_29 = new CheckPoint("LV9-OUT5", 12.041108, 44.006192, 9); ret.add(point_29);
CheckPoint point_30 = new CheckPoint("LV9-STAIR1", 12.041108, 48.506192, 9); ret.add(point_30);
CheckPoint point_31 = new CheckPoint("STAIR1-1", 3.041108, 48.506192, 8); ret.add(point_31);
CheckPoint point_32 = new CheckPoint("STAIR1-2", 3.041108, 46.506192, 8); ret.add(point_32);
CheckPoint point_33 = new CheckPoint("LV8-STAIR1", 12.041108, 48.006192, 8); ret.add(point_33);
CheckPoint point_34 = new CheckPoint("PSI-DOOR2", 12.041108, 51.706192, 8); ret.add(point_34);
CheckPoint point_35 = new CheckPoint("PSI-DOOR1", 5.041108, 51.706192, 8); ret.add(point_35);
CheckPoint point_36 = new CheckPoint("PSI-CENTER", 5.041108, 55.206192, 8); ret.add(point_36);
 return ret;}
public static ArrayList<CheckPoint> getPath(){
String[] listNames = {"PSI-CENTER","PSI-DOOR1","PSI-DOOR2","LV8-STAIR1","806-DOOR2","805-DOOR2","MEETING-DOOR2","LV8-SQUARE1","LV8-SQUARE2","LV8-SQUARE3","LV8-SQUARE4","LV8-SQUARE5","LV8-SQUARE6","STAIR2-1","STAIR2-2","LV9-SQUARE6","LV9-SQUARE5","LV9-SQUARE4","LV9-SQUARE3","LV9-SQUARE2","LV9-SQUARE1","904-DOOR2","905-DOOR2","CAFE-DOOR2","LV9-STAIR1","LV9-OUT1","LV9-OUT2","LV9-OUT3","LV9-OUT4","LV9-OUT5","LV9-STAIR1","STAIR1-1","STAIR1-2","LV8-STAIR1","PSI-DOOR2","PSI-DOOR1","PSI-CENTER"};
return getMovingPath(listNames);
}
	
	public static ArrayList<CheckPoint> getMovingPath(String[] listNames) {
		ArrayList<CheckPoint> ret = new ArrayList<CheckPoint>();
		
		ArrayList<CheckPoint> allPoints = getAllCheckPoints();
		
		for(String name: listNames) {
			String tmp = name.toUpperCase();
			
			int imin = -1;
			for(int i = 0; i < allPoints.size(); i++) {
				if (allPoints.get(i).name.compareTo(tmp) == 0) {
					imin = i;
					break;
				}
			}
			
			if (imin != -1)
			ret.add(allPoints.get(imin));
		}

		return ret;
	}
	
	//
	// This path is used for the scenario at following day:
	// 1.
	// 2.
	// It tooks 120s for complete.
	//
	public static ArrayList<CheckPoint> getPath1() {
		String[] listNames = {"PSI_ROOM", "8TH_STAIR_A",
							"9TH_STAIR_A", "TTAC_DOOR", "9TH_STAIR_B",
							"8TH_STAIR_B", "8TH_STAIR_A", "PSI_ROOM"};
		
		return getMovingPath(listNames);
	}

	
	public static ArrayList<CheckPoint> getPath2_user1() {
		String[] listNames = {"TTAC_ROOM", "TTAC_DOOR", "9TH_STAIR_A", "8TH_STAIR_A",
							"PSI_ROOM"};
		
		return getMovingPath(listNames);
	}

	public static ArrayList<CheckPoint> getPath2_user2() {
		String[] listNames = {"PSI_ROOM", "8TH_STAIR_A", "8TH_STAIR_B"};
		
		return getMovingPath(listNames);
	}

	public static ArrayList<CheckPoint> getPath3_user1() {
		String[] listNames = {"PSI_ROOM", "8TH_STAIR_A", "9TH_STAIR_A", "TTAC_DOOR", "9TH_STAIR_B", "8TH_STAIR_B", "8TH_STAIR_A",
							"PSI_ROOM"};
		
		return getMovingPath(listNames);
	}

	public static ArrayList<CheckPoint> getPath3_user2() {
		String[] listNames = {"PSI_ROOM", "8TH_STAIR_A", "8TH_STAIR_B", "8TH_STAIR_B", "8TH_STAIR_A",
							"PSI_ROOM"};
		
		return getMovingPath(listNames);
	}
}
