package com.litbikes.util;

import java.awt.geom.Line2D;

public class GeometryUtil {

	public static Vector getLineOrientation(Line2D line) {
		boolean noXChange = line.getX1() == line.getX2();
		boolean noYChange = line.getY1() == line.getY2();
		
		assert noXChange != noYChange;
		return new Vector(noXChange ? 0 : 1, noYChange ? 0 : 1);
	}
	
}
