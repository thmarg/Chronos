/*
 * SeekBarEvent
 *
 * Copyright (c) 2015 Thierry Margenstern under MIT license
 * http://opensource.org/licenses/MIT
 */

package tm.android.chronos.uicomponent.event;

/**
 * Event fired by MinMaxSeekBar when max or min value change.
 */
public class SeekBarEvent {
	public  static enum TYPE {MIN,MAX}
	private TYPE type;
	private double value;
	public SeekBarEvent(TYPE type, double value) {
		this.type = type;
		this.value=value;
	}

	public TYPE getType() {
		return type;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
