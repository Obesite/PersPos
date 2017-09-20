package com.mica;

public class CheckPoint {
	
	public String name;
	public double xlocal;			// Coordinate in m
	public double ylocal;			// Coordinate in m
	public double zlocal;			// Coordinate in m
	

	public CheckPoint(String name, double xlocal, double ylocal, double zlocal) {
		this.name = name;
		this.xlocal = xlocal;
		this.ylocal = ylocal;
		this.zlocal = zlocal;
	}
	
	public int similarity(String name) {
		int s1 = this.name.length();
		int s2 = name.length();
		
		int S[][] = new int[s1+1][s2+1];
		
		for (int i = 0; i < s1; i++) S[i][0] = i;
		for (int j = 0; j < s2; j++) S[0][j] = j;
		
		for (int i = 1; i <= s1; i++)
			for (int j = 1; j <= s2; j++) {
				S[i][j] = S[i-1][j-1];
				S[i][j] = Math.min(S[i][j], S[i-1][j] + 1);
				S[i][j] = Math.min(S[i][j], S[i][j-1] + 1);
				
				if (this.name.charAt(i-1) == name.charAt(j-1)) 
					S[i][j] = Math.min(S[i][j], S[i-1][j-1]);
			}
		
		return S[s1][s2];
	}
	
	public String getName() {
		if (name.compareTo("8TH_STAIR_A") == 0) {
			return "Stair, floor 8th, near psi Room";
		}
		else if (name.compareTo("8TH_STAIR_B") == 0) {
			return "Stair, floor 8th, near show Room";
		}
		else if (name.compareTo("9TH_STAIR_A") == 0) {
			return "Stair, floor 9th, near psi Room";
		}
		else if (name.compareTo("9TH_STAIR_B") == 0) {
			return "Stair, floor 9th, near show Room";
		}
		else return name;
	}
}
