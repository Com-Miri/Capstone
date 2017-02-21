package com.example.finaltest1;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.opencv.core.Point;

import static com.example.finaltest1.MainActivity.grabState;


public class imageV extends ImageView{
	private Context context;
	Point ad = new Point();
	Point au = new Point();
	Point am = new Point();
	Point ad_coor = new Point();
	Point au_coor = new Point();
	Point am_coor = new Point();

	public imageV(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	public imageV(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	public imageV(Context context,AttributeSet attrs,int defStyle) {
		super(context,attrs,defStyle);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public boolean onTouchEvent(final MotionEvent event) {

		int rbId = MainActivity.radio.getCheckedRadioButtonId();
		if(rbId < 0)
			return true;
		RadioButton rb = (RadioButton) ((Activity) context).findViewById(rbId);
		Log.e("button",(String) rb.getText());
		if(rb.getText().equals("분할영역"))
			rbId = 0;
		else if (rb.getText().equals("전경점"))
			rbId = 1;
		else if (rb.getText().equals("배경점"))
			rbId = 2;

		if(rbId < 0 || rbId > 2)
			return true;
		Log.e("rbid",""+rbId);

		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			ad.x = event.getX();
			ad.y = event.getY();
			ad_coor = ((MainActivity) context).calCoordinate(ad);
			((MainActivity) context).ontouch(0, (int) ad_coor.x, (int) ad_coor.y, rbId);
			//((MainActivity) context).showImage();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			au.x = event.getX();
			au.y = event.getY();
			au_coor = ((MainActivity) context).calCoordinate(au);
			((MainActivity) context).ontouch(1, (int) au_coor.x, (int) au_coor.y, rbId);
			//((MainActivity) context).showImage();
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			am.x = event.getX();
			am.y = event.getY();
			am_coor = ((MainActivity) context).calCoordinate(am);
			((MainActivity) context).ontouch(2, (int) am_coor.x, (int) am_coor.y, rbId);
			if(grabState == MainActivity.State.SET && (rbId == 1 || rbId == 2)) {
				((MainActivity) context).grabcut();
				((MainActivity) context).showImage();
			}
			//else
			//((MainActivity) context).showImage();
		}
		return true;
	}
}
