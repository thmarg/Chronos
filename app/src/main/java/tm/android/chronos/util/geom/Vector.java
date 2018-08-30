/*
 * Vector
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.util.geom;


/**
 * Represent a Vector in geometry.
 * Methods to normalized, get an orthogonal from this, and translate a point.
 */
@SuppressWarnings("SuspiciousNameCombination")
public class Vector {
    private double X;
    private double Y;

    public Vector(Point A, Point B) {
        X = B.getX() - A.getX();
        Y = B.getY() - A.getY();
    }

    private Vector(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }

    public Vector getNormalized() {
        double m = Math.sqrt(X * X + Y * Y);
        return new Vector(X / m, Y / m);
    }

    public Vector getOrthogonal() {
        return new Vector(-Y, X);

    }

    public Point translate(Point p, double distance) {
        Vector v = getNormalized();
        return new Point(p.getX() + distance * v.X, p.getY() + distance * v.Y, Point.TYPE.XY);


    }
}
