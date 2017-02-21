package com.example.finaltest1;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import static android.graphics.BitmapFactory.decodeFile;

public class MainActivity extends AppCompatActivity {
	public enum State{ NOT_SET , IN_PROCESS , SET };
	public static final int radius = 5;
	public static final int thickness = -1;

	State rectState, lblsState;
	static State grabState;
	public boolean isInitialized;

	Rect rect = new Rect();
	Rect rect2 = new Rect();
	Vector<Point> fgdPxls = new Vector<Point>();
	Vector<Point> bgdPxls = new Vector<Point>();
	int iterCount, getiterCount, newIterCount;
	Mat img, img2, dst, res2, temp, binMask;
	Mat bgmask, firstMask;
	Mat bgModel, fgModel;
	Mat undoDst, undoDst2;
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	static final int REQUEST_OPEN_IMAGE = 1;

	String mCurrentPhotoPath;
	String uploadFilePath;

	private imageV mImageView;
	TextView mTextView;
	Button unDo;
	Point tl, br, bg, tl_coor, br_coor, bg_coor;
	ProgressDialog dlg;
	int scaleFactor = 8;
	float blank = 0, rate = 0;
	float photoW = 0, photoH = 0;
	float targetW = 0, targetH = 0, viewImgW = 0, viewImgH = 0;
	boolean width = false;

	Bitmap mBitmap;
	Bitmap mBitmap2;
	Bitmap showBitmap;
	long nStart = 0;
	long nEnd = 0;

	public static RadioGroup radio;
	int i=0, j = 1;
	//public static GCApplication gc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mImageView = (imageV) findViewById(R.id.imgDisplay);
		mTextView = (TextView) findViewById(R.id.text);
		radio = (RadioGroup) findViewById(R.id.radioGroup1);
		unDo = (Button) findViewById(R.id.undo);

		dlg = new ProgressDialog(this);
		tl = new Point();
		br = new Point();
		bg = new Point();
		tl_coor = new Point();
		br_coor = new Point();
		bg_coor = new Point();
		verifyStoragePermissions(this);
		if (!OpenCVLoader.initDebug()) {
			// Handle initialization error
		}
		unDo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				undo();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	public void setPic() {
		targetW = mImageView.getWidth();
		targetH = mImageView.getHeight();

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		decodeFile(mCurrentPhotoPath, bmOptions);
		photoW = bmOptions.outWidth;
		photoH = bmOptions.outHeight;

		if((targetW * photoH / photoW) > targetH) {  //왼쪽 항이 더 크면 이미지가 세로로 고정
			viewImgW = targetH * photoW / photoH;
			blank = (targetW - viewImgW) / 2;
			rate = photoH / targetH;
			width = false;
		}
		if((targetH * photoW / photoH) > targetW) {   //왼쪽 항이 더 크면 이미지가 가로로 고정
			viewImgH = targetW * photoH / photoW;
			blank = (targetH - viewImgH) / 2;
			rate = photoW / targetW;
			width = true;
		}

		mTextView.setText(targetW+","+targetH+"/"+photoW+","+photoH);

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		mBitmap = decodeFile(mCurrentPhotoPath, bmOptions);
		mBitmap2 = decodeFile(mCurrentPhotoPath);
		mImageView.setImageBitmap(mBitmap2);

		img = new Mat();
		Mat tmpImage = new Mat();
		Utils.bitmapToMat(mBitmap, tmpImage);
		Imgproc.cvtColor(tmpImage, img, Imgproc.COLOR_RGBA2RGB);

		img2 = new Mat();
		Mat tmpImage2 = new Mat();
		Utils.bitmapToMat(mBitmap2, tmpImage2);
		Imgproc.cvtColor(tmpImage2, img2, Imgproc.COLOR_RGBA2RGB);

		bgmask = new Mat(img.size(), CvType.CV_8UC1);
		bgmask.setTo(Scalar.all(Imgproc.GC_BGD));
		temp = new Mat(img.size(), CvType.CV_8UC1);
		temp.setTo(Scalar.all(1));
		binMask = new Mat(img.size(), CvType.CV_8UC1);
		//for(int i=0; i<30; i++) {
			//undoMask = new Mat[i];
			//undoMask = new Mat(img.size(), CvType.CV_8UC1);
			//undoMask.setTo(Scalar.all(Imgproc.GC_BGD));
		//}
		firstMask = new Mat();
		bgModel = new Mat();
		fgModel = new Mat();
		undoDst = new Mat();
		undoDst2 = new Mat();
		reset();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_OPEN_IMAGE:
				if (resultCode == RESULT_OK) {
					Uri imgUri = data.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };
					Cursor cursor = getContentResolver().query(imgUri, filePathColumn,
							null, null, null);
					cursor.moveToFirst();

					int colIndex = cursor.getColumnIndex(filePathColumn[0]);
					mCurrentPhotoPath = cursor.getString(colIndex);
					cursor.close();
					uploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/mirror/";
					setPic();
				}
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.action_open_img:
				Intent getPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
				getPictureIntent.setType("image/*");
				Intent pickPictureIntent = new Intent(Intent.ACTION_PICK,
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				Intent chooserIntent = Intent.createChooser(getPictureIntent, "Select Image");
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {
						pickPictureIntent
				});
				startActivityForResult(chooserIntent, REQUEST_OPEN_IMAGE);
				return true;

			case R.id.action_cut_image:
				if (mCurrentPhotoPath != null) {
					new ProcessImageTask().execute();
				}
				return true;
			case R.id.action_save_image:
				rectState = State.NOT_SET;

				showBitmap = Bitmap.createBitmap(mBitmap2);
				Utils.matToBitmap(res2, showBitmap);	//비트맵이 널값 -고침
				File dir = new File(uploadFilePath);
				if(!dir.exists())
					dir.mkdir();
				File files = new File(uploadFilePath+"1.png");
				while(files.exists()==true) {
					j++;
					files = new File(uploadFilePath+j+".png");
				}
				SaveBitmapToFileCache(showBitmap, uploadFilePath + j + ".png");
				this.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(files)) );	//갤러리 refresh
				//sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						//Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
				Toast.makeText(this, "image saved.", Toast.LENGTH_SHORT).show();
				mImageView.setImageBitmap(showBitmap);

				firstMask.release();
				//mBitmap.recycle(); mBitmap2.recycle();
				img.release(); img2.release();
				dst.release(); temp.release();
				binMask.release(); bgmask.release();
				bgModel.release(); fgModel.release();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
	private final void getBinMask(Mat comMask, Mat binMask )
	{
		if( comMask.empty() || comMask.type()!= CvType.CV_8UC1 )
//      CV_Error( CV_StsBadArg, "comMask is empty or has incorrect type (not CV_8UC1)" );
			Log.e("error","comMask is empty or has incorrect type (not CV_8UC1)");
		if( binMask.empty() || binMask.rows() != comMask.rows() || binMask.cols() != comMask.cols() )
			binMask.create( comMask.size(), CvType.CV_8UC1 );
//  binMask = comMask & 1;
		Core.bitwise_and(comMask, temp, binMask);
	}

	public Point calCoordinate(Point co){
		float mv_coorX = 0;
		float mv_coorY = 0;
		Point mv_coor = new Point();
		if(width){
			mv_coorX = (float)co.x * rate;
			mv_coorY = photoH * (((float)co.y - blank) / viewImgH);

		}
		else {
			mv_coorX = photoW * (((float)co.x - blank) / viewImgW);
			mv_coorY = (float)co.y * rate;
		}
		mv_coor.x = mv_coorX;
		mv_coor.y = mv_coorY;
		return mv_coor;
	}
	public void ontouch( int event, int x, int y ,int flag)
	{
		switch( event )
		{
			case 0: // Touch Down
			{
				i=0;
				if( flag == 0 && rectState == State.NOT_SET )
				{
					rectState = State.IN_PROCESS;
					rect = new Rect( x, y, 1, 1 );
					rect2 = new Rect( x / scaleFactor, y / scaleFactor, 1, 1);
				}
				if ( (flag == 1 || flag == 2) && rectState == State.SET )
					lblsState = State.IN_PROCESS;

			}
			break;
			case 1:  //Touch Up
				if( rectState == State.IN_PROCESS )
				{
					rect = new Rect( new Point(rect.x, rect.y), new Point(x,y) );
					rect2 = new Rect(new Point(rect2.x, rect2.y), new Point(x/scaleFactor, y/scaleFactor));
					rectState = State.SET;
					setRectInMask();
					assert( bgdPxls.isEmpty() && fgdPxls.isEmpty());
				}
				if( lblsState == State.IN_PROCESS )
				{
					setLblsInMask(flag, new Point(x/scaleFactor, y/scaleFactor));
					lblsState = State.SET;
				}

				break;
			case 2:  //Touch Move
				if( rectState == State.IN_PROCESS )
				{
					rect = new Rect( new Point(rect.x, rect.y), new Point(x,y) );
					rect2 = new Rect( new Point(rect2.x, rect2.y), new Point(x/scaleFactor, y/scaleFactor));
					assert( bgdPxls.isEmpty() && fgdPxls.isEmpty());// && prBgdPxls.isEmpty() &&prFgdPxls.isEmpty() );
				}
				else if( lblsState == State.IN_PROCESS )
				{
					setLblsInMask(flag, new Point( x/scaleFactor, y/scaleFactor));//, false);
				}
				break;
		}
	}
	private void setLblsInMask(int flag, Point p) {
		Vector<Point> bpxls, fpxls;
		int bvalue, fvalue;
		bpxls = bgdPxls;
		fpxls = fgdPxls;
		bvalue = Imgproc.GC_BGD;
		fvalue = Imgproc.GC_FGD;

		if( flag == 1 )
		{
			fpxls.add(p);
			Imgproc.circle( bgmask, p, radius, new Scalar(fvalue), thickness );
		}
		if( flag == 2 )
		{
			bpxls.add(p);
			Imgproc.circle( bgmask, p, radius, new Scalar(bvalue), thickness );
		}
	}
	private void setRectInMask(){
		assert( !bgmask.empty() );
		bgmask.setTo( new Scalar(Imgproc.GC_BGD));
		Mat res = new Mat();
		Utils.bitmapToMat(mBitmap, res);
		rect2.x = Math.max(0, rect2.x);
		rect2.y = Math.max(0, rect2.y);
		rect2.width = Math.min(rect2.width, res.cols()-rect2.x);
		rect2.height = Math.min(rect2.height, res.rows()-rect2.y);
		(bgmask.submat(rect2)).setTo( new Scalar(Imgproc.GC_PR_FGD) );
	}

	private class ProcessImageTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getiterCount = getIterCount();

		}

		@Override
		protected Integer doInBackground(Integer... params) {
			if( isInitialized )
				grabcut();
			else
			{
				if( rectState != State.SET )
					return iterCount;

				if( lblsState == State.SET )
					grabcut();
				else
					grabcut();

				isInitialized = true;
			}
			iterCount++;

			bgdPxls.clear(); fgdPxls.clear();

			return iterCount;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			newIterCount = result;
			showImage();
		}
	}
	public void grabcut() {
		iterCount++;
		nStart = System.currentTimeMillis();
		Mat background = new Mat(img2.size(), CvType.CV_8UC3,
				new Scalar(1, 1, 1));
		Mat mask;
		Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));

		if(isInitialized) {
			//bgmask.copyTo(undoMask);
			Mat dstemp = new Mat();
			dst.copyTo(dstemp);
			Imgproc.resize(dstemp, dstemp, img.size());
			Imgproc.grabCut(dstemp, bgmask, rect2, bgModel, fgModel, 1);
			i++;
		}
		else {
			undoDst = img2;
			Imgproc.grabCut(img, firstMask, rect2, bgModel, fgModel,
					1, Imgproc.GC_INIT_WITH_RECT);
			if(lblsState == State.SET) {
				Imgproc.grabCut(img, bgmask, rect2, bgModel, fgModel, 1, Imgproc.GC_INIT_WITH_MASK);
				Core.bitwise_or(firstMask, bgmask, firstMask);
			}
			Core.compare(firstMask, source, firstMask, Core.CMP_EQ);

			Mat foreground = new Mat(img2.size(), CvType.CV_8UC3,
					new Scalar(255, 255, 255));
			Imgproc.resize(firstMask, firstMask, img2.size());

			img2.copyTo(foreground, firstMask);  //이부분에서 이미지의 전경이 foreground에 저장됨.
			dst = new Mat();
			Mat tmp = new Mat();
			Imgproc.resize(background, tmp, img2.size());
			background = tmp;
			mask = new Mat(foreground.size(), CvType.CV_8UC1,
					new Scalar(255, 255, 255));

			Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY);
			Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY_INV);
			System.out.println();
			Mat vals = new Mat(1, 1, CvType.CV_8UC3, new Scalar(0.0));
			background.copyTo(dst);

			background.setTo(vals, mask);
			Core.add(background, foreground, dst, mask);

			vals.release();
			mask.release();
			foreground.release();
			tmp.release();
			grabState = State.SET;
		}
		nEnd = System.currentTimeMillis();
		System.out.println("실행시간 : " + (nEnd - nStart));
		source.release();
		background.release();

	}
	public int getIterCount() { return iterCount; }
	public void showImage() {
		if (newIterCount > getiterCount) {
			res2 = new Mat();

			getBinMask( bgmask, binMask );
			Imgproc.resize(binMask, binMask, img2.size());
			dst.copyTo( res2, binMask);	//dst사이즈가 img사이즈라 에러남 - 고침

			Utils.matToBitmap(res2, mBitmap2);
			res2.copyTo(undoDst2);
		}
		mImageView.setImageBitmap(mBitmap2);

	}
	public void reset(){
		if( !bgmask.empty() )
			bgmask.setTo(Scalar.all(Imgproc.GC_BGD));
		bgdPxls.clear(); fgdPxls.clear();

		isInitialized = false;
		rectState = State.NOT_SET;
		lblsState = State.NOT_SET;
		grabState = State.NOT_SET;
		iterCount = 0;
		getiterCount = 0;
	}
	public void undo(){
		if(iterCount==1) {
			Utils.matToBitmap(undoDst, mBitmap2);
			mImageView.setImageBitmap(mBitmap2);
			reset();
		}
		else {
			Utils.matToBitmap(undoDst2, mBitmap2);
			mImageView.setImageBitmap(mBitmap2);
			i--;
		}
	}

	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//persmission method.
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have read or write permission
		int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

		if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					activity,
					PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE
			);
		}
	}
	public static void SaveBitmapToFileCache(Bitmap bitmap, String filename) {

		File fileCacheItem = new File(filename);
		OutputStream out = null;
		try {
			fileCacheItem.createNewFile();
			out = new FileOutputStream(fileCacheItem);

			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}