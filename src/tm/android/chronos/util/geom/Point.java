/*
 * Point
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util.geom;

/**
 * This class represent a point in a 2D plane.<br>
 * <p></p>Cartesian coordinates and polar coordinate are provided and linked. Change in one update the other.
 * You can instantiate in cartesian, and polar degrees or radian.
 * You can set how the toString() method render, default is cartesian.</p>
 * There are methods to change coordinate, rotate, and do symmetries<br>
 * <p>There's a method <code>asCanvasPoint()</code> that is just a convenient to have two float (x,y) for use in Canvas.drawSomething(...)</p>
 * <p>Good usage is to Think and do calculation in a convenient local geometric coordinate. Then when you're ready, make change coordinate to real screen.
 * For example on android, screen ref(0,0) is upper left corner, x axis to the right and positive,y axis down to the bottom and negative.
 * Decide for example that your local coordinate are from bottom right corner, and both axis positive(to the left and to the top).
 * You'll have to do a central symmetry centered on the center of the screen to change your coordinate.
 * If Point p(X,Y) is from screen, p.getSymmetrical(SYMETRIE_TYPE.CENTRAL,centerScreen) is in your local coordinates.Idem for local to screen.
 * Of course one can directly do things on screen coordinate.</p>
 */
public class Point {

	/**
	 * Type of parameters in constructor and also type for toString() output format.<br>
	 * Coordinates are based on point(0,0).
	 * <ul>
	 *     <li>XY : cartesian coordinates </li>*
	 *     <li>POLAR_DEGREE : polar coordinates, with angle given in degrees</li>
	 *     <li>POLAR_RADIANT : polar coordinates, with angle given in radian</li>
	 * </ul>
	 */
	public enum TYPE {
		POLAR_DEGREE, POLAR_RADIANT, XY}

	/**
	 * Type of symmetry.
	 * <ul>
	 *     <li>CENTRAL : symmetry by a point</li>
	 *     <li>X_AXIS : symmetry by an axe parallel to X axis</li>
	 *     <li>Y_AXIS : symmetry by an axe parallel to Y axis</li>
	 * </ul>
	 */
	public enum SYMMETRY_TYPE {CENTRAL, X_AXIS, Y_AXIS}
	public static final double PIs4 = Math.PI/4;
	public static final Point ZERO = new Point();


	private double X;
	private double Y;
	private double radius;
	private double angle;// trigonometric, always positive anti clockwise.
	private TYPE typeForToString = TYPE.XY;


	/**
	 * Construct a new Point
	 * @param a X coordinate or radius.
	 * @param b Y coordinate or angle.
	 * @param type type of coordinate @see{TYPE}
	 */
	public Point(double a, double b, TYPE type) {
		init(a,b,type);
	}

	/**
	 * Default constructor, define a point (0,0).
	 */
	public Point(){
		init(0, 0, TYPE.XY);
	}

	private void init(double a, double b, TYPE type) {
		switch (type) {
			case XY:
				X = a;
				Y = b;
				XYtoPolar();
				break;
			case POLAR_RADIANT:
				radius = a;
				angle = b;
				polarToXY();
				break;
			case POLAR_DEGREE:
				radius =a;
				angle = degreesToRadiant(b);
				polarToXY();
				break;
		}
	}

	private void XYtoPolar(){
		 radius = (float)Math.sqrt(X*X+Y*Y);
		 angle= 0;
		if (X==0) {
			angle =  (Y==0?0:(Y > 0?(float)Math.PI/2:(float)Math.PI*3/4));
		} else {
				angle = (float) Math.atan(Y / X);
				if (X>0 && Y >0)
					return;
				if (X<0)
					angle = (float) Math.PI + angle;
				else if (X > 0 && Y < 0)
					angle = 2 * (float) Math.PI + angle;

		}
	}

	private void polarToXY(){
		if (angle==0){
			X=radius;
			Y=0;
		} else {
			X=radius*Math.cos(angle);
			Y=radius*Math.sin(angle);
		}


	}

	/**
	 * Return the symmetrical point of this point.
	 * @param stype type of symmetry @see{SYMMETRY_TYPE}
	 * @param center "center" of the symmetry, if X_AXIS, it is symmetry from the parallel line to x axis with equation y = center.Y. If Y_AXIS
	 *               it is symmetry from the parallel line to y axis with equation x = center.X.
	 * @return the point obtain by the requested transform.
	 */
	public Point getSymmetrical(SYMMETRY_TYPE stype, Point center){
		switch (stype) {
			case CENTRAL:
				return new Point(2*center.X-X, 2*center.Y-Y, TYPE.XY);
			case X_AXIS:
				return new Point(X , 2*center.Y-Y, TYPE.XY);
			case Y_AXIS:
				return new Point(2*center.X-X , Y, TYPE.XY);
		}
		return null;
	}

	/**
	 * Return the new coordinates in the new origin newRef
	 * @param newRef new origin
	 * @return new coordinates.
	 */
	public Point getChangedCoordinates(Point newRef){
				return new Point(X-newRef.X,Y-newRef.Y,TYPE.XY);
	}


	/**
	 * Rotate this point, it's coordinates are changed.
	 * @param center of the rotation
	 * @param angleRadiant angle of the rotation. Warning positive angle are anti clockwise.
	 */
	public void rotate(Point center,double angleRadiant){
		getChangedCoordinates(center);
		setAngle((angle+angleRadiant)%(2*Math.PI));
		getChangedCoordinates(center.getSymmetrical(SYMMETRY_TYPE.CENTRAL, ZERO));
		XYtoPolar();
	}

	/**
	 * Rotate this point, it's coordinates are NOT changed.
	 * @param center of the rotation
	 * @param angleRadiant angle of the rotation. Warning positive angle are anti clockwise.
	 * @return a new point witch is the image of the original (this) by the defined rotation.
	 */
	public Point getRotated(Point center, double angleRadiant){
		Point p = new Point(X,Y,TYPE.XY);
		p.rotate(center,angleRadiant);
		return p;


	}

	/**
	 * Convert to radian
	 * @param degrees param in degrees to convert.
	 * @return radian
	 */
	public static double degreesToRadiant(double degrees){
		return (degrees*Math.PI/180)%(2*Math.PI);
	}

	/**
	 * Convert to degrees
	 * @param radiant parameter in radian to convert.
	 * @return degrees
	 */
	public static double radiantToDegrees(double radiant) {
		return (radiant * 180 / Math.PI) % 360;
	}

	/**
	 * Return float[x,y] from this point(x,y).
	 * @return float[x,y]
	 */
	public float[] asCanvasPoint(){
		float[] ret = new float[2];
		ret[0]=(float)X;
		ret[1]=(float)Y;
		return ret;
	}

	@Override
	public String toString() {
		if (typeForToString == TYPE.XY)
			return "XY("+X+","+Y+")";
		else if (typeForToString == TYPE.POLAR_RADIANT)
			return "polar("+ radius +","+angle+" rad)";
		else
			return "polar("+radius+","+radiantToDegrees(angle)+" degrees)";


	}

	public void setTypeForToString(TYPE typeForToString) {
		this.typeForToString = typeForToString;
	}

	public void setX(double x) {
		X = x;
		XYtoPolar();
	}

	public void setY(double y) {
		Y = y;
		XYtoPolar();
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
		polarToXY();
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
		polarToXY();
	}

	public double getX() {
		return X;
	}

	public double getY() {
		return Y;
	}
}
