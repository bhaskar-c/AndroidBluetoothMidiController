package com.freakk.midibluetooth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.view.View;

class PianoKeys extends View {

	int width, height;
	static final int num_white_keys = 15;
	static final int num_rows = 1;
	static final int num_cols = num_white_keys / num_rows;
	int white_width, white_height;
	int black_width, black_height;
	boolean[] touchedW;
	boolean[] touchedB;
	int touchX, touchY;
	Paint key_surface, key_border;
	Paint key_hit;
	Rect rect;

	public PianoKeys(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		width = getContext().getResources().getDisplayMetrics().widthPixels;
		height = getContext().getResources().getDisplayMetrics().heightPixels;

		white_width = (int) width / num_white_keys;
		white_height = (int) height;
		black_width = (int) (white_width * 0.55);
		black_height = (int) (white_height * 0.6);

		touchedW = new boolean[num_white_keys];
		touchedB = new boolean[num_white_keys]; // it's convenient to use the same size, non existent black keys will simply be ignored
	}

	public void draw(Canvas canvas) {
		key_surface = new Paint();
		key_border = new Paint();

		// White Keys
		int i;

		// fill
		Shader shader = new LinearGradient(0, 0, 0, white_height * 0.4f,0xffC9C9C9, Color.WHITE, TileMode.CLAMP);
		key_surface.setShader(shader);

		// border
		key_border.setStyle(Paint.Style.STROKE);
		key_border.setStrokeWidth(2.0f);
		key_border.setColor(Color.BLACK);

		// Draw Keys

		for (i = 0; i < num_white_keys; i++) {
			rect = new Rect();
			int x = (i % num_cols) * white_width;
			int y = (i / num_cols) * white_height;
			rect.set(x, y, x + white_width, y + white_height);
			if(!touchedW[i]) canvas.drawRect(rect, key_surface);
			else {
				key_hit = new Paint();
				key_hit.setStyle(Paint.Style.FILL);
				key_hit.setColor(Color.YELLOW);
				canvas.drawRect(rect, key_hit);
				//touchedW[i] = false;
			}
			canvas.drawRect(rect, key_border);
		}

		// Black Keys

		// fill
		// key_surface.setStyle(Paint.Style.FILL);
		key_surface.setColor(Color.BLACK);
		shader = new LinearGradient(0, 0, 0, black_height * 0.7f, Color.DKGRAY,
				Color.BLACK, TileMode.CLAMP);
		key_surface.setShader(shader);

		// border
		key_border.setStyle(Paint.Style.STROKE);
		key_border.setStrokeWidth(4.0f);
		key_border.setColor(Color.BLACK);

		// Draw Keys
		key_surface.setShadowLayer(8.0f, 2.0f, 1.0f, 0xFF000000);
		// in onDraw(Canvas)

		for (i = 0; i < num_white_keys; i++) {
			if ((i % 7) == 2 || (i % 7) == 6)
				continue;
			rect = new Rect();
			int x = (i % num_cols + 1) * white_width - black_width / 2;
			int y = (i / num_cols) * black_height;
			rect.set(x, y, x + black_width, y + black_height);
			if(!touchedB[i]){ 
				canvas.drawRect(rect, key_surface);
				canvas.drawRect(rect, key_border);
				//invalidate();
			}
			else {
				key_hit = new Paint();
				key_hit.setStyle(Paint.Style.FILL);
				key_hit.setColor(Color.YELLOW);
				key_hit.setShadowLayer(8.0f, 2.0f, 1.0f, 0xFF000000);
				canvas.drawRect(rect, key_hit);
				//touchedB[i] = false;
				//invalidate();
			}
			
		}
	}

	// Highlight key hit
	public void setTouchedKey(int k, boolean isBlack) {
		if(isBlack)touchedB[k] = true;
		else touchedW[k] = true;
		invalidate();
	}
	public void setReleasedKey(int k, boolean isBlack) {
		if(isBlack)touchedB[k] = false;
		else touchedW[k] = false;
		invalidate();
	}

	// Getters
	public int getWhiteKeyWidth() {
		return white_width;
	}

	public int getWhiteKeyHeight() {
		return white_height;
	}

	public int getBlackKeyWidth() {
		return black_width;
	}

	public int getBlackKeyHeight() {
		return black_height;
	}

};