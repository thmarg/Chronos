/*
 * Watch
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import tm.android.chronos.util.geom.Point;
import tm.android.chronos.util.geom.Segment;

import static tm.android.chronos.util.geom.Point.SYMMETRY_TYPE.X_AXIS;


/**
 * Analogique watch to select a time or a duration.
 */
public class Watch extends SurfaceView implements  SurfaceHolder.Callback {

	protected float centerX;
	private float centerY;
	protected float radius;

	Point screenCoordInLocalCoord;
	Point localCoordInScreenCoord;
	Point localCenter;
	Point previousPoint;

	private Segment clockRepereSegment;
	protected Segment baseLineHour;
	protected Segment baseLineMinute;


	protected Paint paint;
	protected Paint paintNeedles;

	Rect rect = new Rect();

	public Watch(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.TRANSPARENT);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintNeedles = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintNeedles.setColor(Color.RED);
		getHolder().addCallback(this);
		localCenter = new Point(0, 0, Point.TYPE.XY);
	}

	protected void init(int i1, int i2) {

		centerX = i1/2;
		if (i1 > i2)
			centerX = i2/2;

		centerY = centerX;

		screenCoordInLocalCoord = new Point(-centerX, centerY, Point.TYPE.XY);
		localCoordInScreenCoord = new Point(centerX, -centerY, Point.TYPE.XY);
		radius = centerX - 100;

		Point E = new Point();
		E.setRadius(0.85 * radius);
		E.setAngle(0);
		Point F = new Point();
		F.setRadius(0.95 * radius);
		F.setAngle(0);
		clockRepereSegment = new Segment(E, F);

		Point A = new Point();
		A.setRadius(0.1 * radius);
		A.setAngle(3 * Math.PI / 2);
		Point B = new Point();
		B.setRadius(0.6 * radius);
		B.setAngle(Math.PI / 2);
		baseLineHour = new Segment(A, B);

		Point C = new Point();
		C.setX(0.1 * radius);
		C.setAngle(3 * Math.PI / 2);
		Point D = new Point();
		D.setRadius(0.82 * radius);
		D.setAngle(Math.PI / 2);

		baseLineMinute = new Segment(C, D);


	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
		init(i1,i2);
		Canvas canvas = surfaceHolder.lockCanvas();
		drawAll(canvas);
		surfaceHolder.unlockCanvasAndPost(canvas);

	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		// nothing to do here
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

	}


	protected void drawAll(Canvas canvas) {
		if (canvas==null)
			return ;
		rect = canvas.getClipBounds();
		paint.setColor(Color.BLACK);
		canvas.drawRect(rect, paint);
		drawBackground(canvas);
	}

	protected void drawCenterPoint(Canvas canvas) {
		paint.setColor(Color.BLACK);
		float x = (float) localCenter.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS,Point.ZERO).getX();
		float y = (float) localCenter.getChangedCoordinates(screenCoordInLocalCoord).getSymmetrical(X_AXIS,Point.ZERO).getY();
		canvas.drawCircle(x, y, 3, paint);
	}

	protected void drawBackground(Canvas canvas) {
		// All computation are made in local coordinate (based on 0,0) and computed to screen coordinate for use in canvas.drawSomething
		paint.setColor(Color.BLUE);
		canvas.drawCircle(centerX, centerY, radius, paint);
		paint.setColor(Color.DKGRAY);
		canvas.drawCircle(centerX, centerY, radius - 5, paint);

		paint.setColor(Color.YELLOW);
		for (int i = 0; i < 12; i += 1) {
			canvas.drawLines(clockRepereSegment.getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS,Point.ZERO).asCanvasLine(), paint);
			for (int j = 1; j < 5; j++) {
				canvas.drawLines(clockRepereSegment.getTranslatedParallel(j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paint);
				canvas.drawLines(clockRepereSegment.getTranslatedParallel(-j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paint);
			}
			clockRepereSegment.rotate(localCenter, Math.PI / 6);
		}
	}

	protected void drawNeedles(Segment segment, Canvas canvas, double rotationAngle) {
		segment.rotate(localCenter, rotationAngle);

		for (int j = 0; j < 3; j++) {
			canvas.drawLines(segment.getTranslatedParallel(j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
			canvas.drawLines(segment.getTranslatedParallel(-j).getChangedCoordinate(screenCoordInLocalCoord).getSymmetrical(X_AXIS, Point.ZERO).asCanvasLine(), paintNeedles);
		}
	}



}
