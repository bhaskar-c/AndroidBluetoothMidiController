package com.freakk.midibluetooth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

class Pads extends View {

	int width, height;
	static final int num_pads = 12;
	static final int num_rows = 2;
	static final int num_cols = num_pads / num_rows;
	int rect_width;
	int rect_height;

	float touchX, touchY;
	boolean[] touched;

	Paint paint;
	Rect rect;
	Paint pad_surface;
	Paint pad_border;
	Paint pad_hit;
	
	public Pads(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		width = getContext().getResources().getDisplayMetrics().widthPixels;
		height = getContext().getResources().getDisplayMetrics().heightPixels;
		rect_width = (int) width / 6;
		rect_height = (int) height / 2;
		paint = new Paint();
		touched = new boolean[num_pads];
		int i;
		for (i = 0; i < num_pads; i++)
			touched[i] = false;
			
		// PADs
		pad_surface = new Paint();
		pad_border = new Paint();
		pad_hit = new Paint();
		pad_hit.setStyle(Paint.Style.FILL);
		//pad_hit.setColor(Color.YELLOW);
		pad_hit.setColor(Color.rgb(254,173,0));
		// fill
		pad_surface.setStyle(Paint.Style.FILL);
		pad_surface.setColor(Color.DKGRAY);

		// border
		pad_border.setStyle(Paint.Style.STROKE);
		pad_border.setStrokeWidth(2.0f);
		pad_border.setColor(Color.BLACK);

		rect = new Rect();
	}

	public void draw(Canvas canvas) {
		int i;
		for (i = 0; i < num_pads; i++) {
			//rect = new Rect();
			int x = (i % num_cols) * rect_width;
			int y = (i / num_cols) * rect_height;
			rect.set(x, y, x + rect_width, y + rect_height);
			canvas.drawRect(rect, pad_border);
			if (touched[i]) {
				canvas.drawRect(rect, pad_hit);
				//touched[i] = false;
				//invalidate();
			} else {
				canvas.drawRect(rect, pad_surface);
			}	
		}

	}

	public int getPadWidth() {
		return this.rect_width;
	}

	public int getPadHeight() {
		return this.rect_height;
	}

	// Highlight key hit
	public void setTouchedKey(int padNum) {
		touched[padNum] = true;
		invalidate();
	}
	public void setReleasedKey(int padNum) {
		touched[padNum] = false;
		invalidate();
	}
};