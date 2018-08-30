/*
 * Segment
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util.geom;


import android.graphics.RectF;

/**
 * Represent a segment ( a bounded line)
 */
public class Segment {
    private Point A;
    private Point B;


    public Segment(Point A, Point B) {
        this.A = A;
        this.B = B;
    }

    public float[] asCanvasLine() {
        float[] ret = new float[4];
        ret[0] = (float) A.getX();
        ret[1] = (float) A.getY();
        ret[2] = (float) B.getX();
        ret[3] = (float) B.getY();
        return ret;
    }


    public RectF asRectF() {
        return new RectF((float) A.getX(), (float) A.getY(), (float) B.getX(), (float) B.getY());
    }

    /**
     * <p>Return a new segment parallel to this at the given distance (distance in a perpendicular direction).
     * The final position depends on distance sign and position of A et B.
     * Assuming that A.x<B.x  the new segment is ("above" if (y diff not too big) or right if A.y<B.x else left.</p>
     *
     * @param distance a distance positive or negative. If zero return a clone of this.
     * @return new Segment // to this at the given distance
     */
    public Segment getTranslatedParallel(double distance) {
        Vector v = new Vector(A, B);
        v = v.getOrthogonal().getNormalized();
        return new Segment(v.translate(A, distance), v.translate(B, distance));

    }

    /**
     * Rotate this segment. coordinates are changed.
     *
     * @param center       of the rotation
     * @param angleRadiant of the rotation
     */
    public void rotate(Point center, double angleRadiant) {
        A.rotate(center, angleRadiant);
        B.rotate(center, angleRadiant);
    }

    /**
     * Rotate this segment. coordinates are NOT changed.
     *
     * @param center       of the rotation
     * @param angleRadiant of the rotation
     * @return a new Segment rotated from this by the defined rotation.
     */
    public Segment getRotated(Point center, double angleRadiant) {
        return new Segment(A.getRotated(center, angleRadiant), B.getRotated(center, angleRadiant));

    }

    /**
     * Change the coordinate
     *
     * @param p new origin
     * @return new segment with coordinates in new system with origin at p
     */
    public Segment getChangedCoordinate(Point p) {
        return new Segment(new Point(A.getX(), A.getY(), Point.TYPE.XY).getChangedCoordinates(p), new Point(B.getX(), B.getY(), Point.TYPE.XY).getChangedCoordinates(p));

    }

    /**
     * Symmetrie
     *
     * @param stype  type
     * @param center center of symmetry
     * @return a new segment symmetric of this by <code>stype</code>
     */
    public Segment getSymmetrical(Point.SYMMETRY_TYPE stype, Point center) {
        return new Segment(new Point(A.getX(), A.getY(), Point.TYPE.XY).getSymmetrical(stype, center), new Point(B.getX(), B.getY(), Point.TYPE.XY).getSymmetrical(stype, center));

    }

    /**
     * Special getter used for segment passing at (0,0) and to know there angle (same as B point).
     *
     * @return Point.
     */
    public Point getB() {
        return B;
    }

    public Point getA() {
        return A;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "A=" + A +
                ", B=" + B +
                '}';
    }
}
