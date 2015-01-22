/*
 * ${NAME}
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util.geom;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class PointTest {
	double epsilon = 0.000001;
	@Test
	public void testGetSymmetrical() throws Exception {
			Point p = new Point(2,3, Point.TYPE.XY);
		Point center = new Point(4,-5, Point.TYPE.XY);
		Point result  = p.getSymmetrical(Point.SYMMETRY_TYPE.CENTRAL, center);
		assertEquals(6.0,result.getX());
		assertEquals(-13.0,result.getY());

		result = p.getSymmetrical(Point.SYMMETRY_TYPE.X_AXIS,center);
		assertEquals(2.0,result.getX());
		assertEquals(-13.0,result.getY());


		result = p.getSymmetrical(Point.SYMMETRY_TYPE.Y_AXIS,center);
		assertEquals(6.0,result.getX());
		assertEquals(3.0,result.getY());

	}

	@Test
	public void testGetChangedCoordinates() throws Exception {
		Point p = new Point(2,3, Point.TYPE.XY);
		Point center = new Point(4,-5, Point.TYPE.XY);
		Point result = p.getChangedCoordinates(center);
		assertEquals(result.getX(),-2.0);
		assertEquals(result.getY(),8.0);
	}

	@Test
	public void testRotate() throws Exception {
		Point p = new Point(20,Math.PI/2, Point.TYPE.POLAR_RADIANT);
		Point center = new Point(2,18, Point.TYPE.XY);
		p.rotate(center,Math.PI/2);

		assertTrue(Math.abs(p.getX())<epsilon);
		assertTrue(Math.abs(16.0-p.getY())<epsilon);

	}




	@Test
	public void testToString() throws Exception {
				Point p = new Point();
		p.setTypeForToString(Point.TYPE.XY);
		assertEquals("XY(0.0,0.0)",p.toString());
		p.setTypeForToString(Point.TYPE.POLAR_DEGREE);
		assertEquals("polar(0.0,0.0 degrees)",p.toString());
		p.setTypeForToString(Point.TYPE.POLAR_RADIANT);
		assertEquals("polar(0.0,0.0 rad)",p.toString());
	}




	@Test
	public void testSetXY() throws Exception {
		Point p = new Point();
		p.setX(100);
		p.setY(-0.25);
		assertEquals(100.0,p.getX());
		assertEquals(-0.25,p.getY());
	}


	@Test
	public void testSetAngle() throws Exception {
		Point p = new Point();
		p.setRadius(50);
		p.setAngle(Math.PI/2);
		assertEquals(50.0,p.getRadius());
		assertTrue(Math.abs(p.getAngle()-Math.PI/2)<epsilon);

	}

	@Test
	public void testGetRadius() throws Exception {
		Point p = new Point(20,20, Point.TYPE.XY);
		assertTrue(Math.abs(Point.degreesToRadiant(45) - p.getAngle()) <epsilon );
		assertTrue(Math.abs(Math.sqrt(800)-p.getRadius())<epsilon);
	}

	@Test
	public void testSetRadius() throws Exception {
		Point p = new Point(20,20, Point.TYPE.XY);
		p.setRadius(30);
		assertTrue(Math.abs(p.getRadius()*Math.cos(p.getAngle())-p.getX())<epsilon);
		assertTrue(Math.abs(p.getRadius()*Math.sin(p.getAngle())-p.getY())<epsilon);
	}

	@Test
	public void testGetXY() throws Exception {
		Point p = new Point(30, -80, Point.TYPE.XY);
		assertEquals(30.0, p.getX());
		assertEquals(-80.0, p.getY());
	}
}