package com.mecart.gbsmeetingrooms;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private static final String TAG = "GBSMap";

	private String sala;

	private ArrayList<Room> rooms;
	private ArrayList<String> roomNames;

	rotatingMap map;

	// Sensors & SensorManager
	private Sensor accelerometer;
	private Sensor magnetometer;
	private SensorManager mSensorManager;

	// Storage for Sensor readings
	private float[] mGravity = null;
	private float[] mGeomagnetic = null;

	// Rotation around the Z axis

	private static final int nSamples = 10;
	

	private Bitmap floor1;
	private Bitmap floor2;
	private FloorMapBitmap floorMap1;
	private FloorMapBitmap floorMap2;
	private ArrayList<FloorMapBitmap> floorMapCollection;
	
	
	final int HPBLUE = Color.rgb(0,150,214);
	final int HPGREEN = Color.rgb(0,139,43);
	final int HPORANGE = Color.rgb(240, 83, 50);
	final int HPPURPLE = Color.rgb(130, 41, 128);
	final int HPLIGHTGRAY = Color.rgb(229, 232, 232);
	final int HPMEDIUMGRAY = Color.rgb(185,184,187);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		log("onCreate()");
		// load XML
		// http://www.sitepoint.com/learning-to-parse-xml-data-in-your-android-app/
		XmlPullParserFactory pullParserFactory;
		try {
			//log("reading xml...");
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();

			InputStream in_s = getApplicationContext().getAssets().open(
					"rooms.xml");
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in_s, null);
			rooms = parseXML(parser);
			//log("Read XML. Found " + rooms.size() + " rooms.");

		} catch (XmlPullParserException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		//log("Populating arraylist roomnames");
		roomNames = new ArrayList<String>();
		for (int i = 0; i < rooms.size(); i++) {
			//log("name " + i + ": " + rooms.get(i).roomName);
			roomNames.add(rooms.get(i).roomName);
		}
		//log("Populated roomnames with " + roomNames.size() + " names");
		//log("setting up bitmaps:");
		//
		floor1 = BitmapFactory.decodeResource(getResources(),R.drawable.pb);
		floor2 = BitmapFactory.decodeResource(getResources(),R.drawable.pa);
		//log("setting up floors:");
		floorMap1 = new FloorMapBitmap(floor1, "PB");
		floorMap2 = new FloorMapBitmap(floor2, "PA");
		
		//log("floorMapCollection = new ArrayList<FloorMapBitmap>();");
		floorMapCollection = new ArrayList<FloorMapBitmap>();
		//log("adding floors to collection:");
		floorMapCollection.add(floorMap1);
		floorMapCollection.add(floorMap2);
		
		//log("floors set. Getting intent...");
		// Get intent, action and MIME type
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		//log("Received intent: Action:" + action.toString() + " . Data string:"+ intent.getDataString());

		if (Intent.ACTION_VIEW.equals(action)) {
			//log("ACTION_VIEW.equals(action)");
			sala = processIntentText(intent.getDataString());
			//log("Received room " + sala);
			

		} else {
			// Handle other intents, such as being started from the home screen
			sala = "No intent";
			//log("No intent");
		}

		super.onCreate(savedInstanceState);
		log("setContentView");
		setContentView(R.layout.activity_main);
		log("Created");

		LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.LinearLayout1);
		//log("LinearLayout ok, creating map");
		map = new rotatingMap(getApplicationContext(), floorMapCollection);
		//log("map ok");

		int initialRoomID=0;

		if(sala!="No intent"){
				//sala = "suecia";
				//log("Requesting to locate room " + sala);
				
				for(int i=0;i<rooms.size();i++){
					//log("Room " + i + ": "+ rooms.get(i).roomName);
				}
				
				initialRoomID = findRoomIdInListByName(rooms,sala);
				//log("initialRoomID = " + initialRoomID);
				
		}
		
		relativeLayout.addView(map);

		TextView textViewLabel = (TextView) findViewById(R.id.textViewSearchFor);
		Typeface hpSimplified = Typeface.createFromAsset(getAssets(), "HPSimplified_Rg.ttf");
		textViewLabel.setTypeface(hpSimplified);
		
		//begin autocompletetextview
		//http://www.tutorialspoint.com/android/android_auto_complete.htm
		log("AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1); ");
		AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1); 
		log("ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,roomNames);");
		//ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,roomNames);
		AutoCompleteAdapter acAdapter = new AutoCompleteAdapter(this,R.layout.my_spinner_layout,R.id.text_room_name,roomNames);
		log("actv.setAdapter(acAdapter);");
		actv.setAdapter(acAdapter);
		actv.setTypeface(hpSimplified);
		
		actv.setOnItemClickListener(new OnItemClickListener() {
			
		

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				sala = parent.getItemAtPosition(position).toString();
				log("actv selected: "+sala );
				map.locateRoom(findRoomInListByName(rooms, sala));
				hideKeyboard();
				
			}

	
		

			
			
		});
				//end autocompletetextview
		
		  //log("Setting up spinner..."); 
			Spinner spinner1 = (Spinner)  findViewById(R.id.spinner1); // Create an ArrayAdapter using the  string array and a default spinner layout 
		  //ArrayAdapter<String>  roomsAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, roomNames);
		  //spinner1.setAdapter(roomsAdapter);
		  
		  MySpinnerAdapter roomsAdapter = new MySpinnerAdapter(getApplicationContext(),R.layout.my_spinner_layout,roomNames);
		  spinner1.setAdapter(roomsAdapter);
		  
		  
		  
		  spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
	
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				
				sala = parent.getItemAtPosition(position).toString();
				//log("Spinner selected: "+sala );
				map.locateRoom(findRoomInListByName(rooms, sala));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
			
		  });
		
		  if(0!=initialRoomID){
			 // log("initialRoomID was " + initialRoomID);
			 // log("Trying to set spinner...");
			  spinner1.setSelection(initialRoomID, true);
		  }

		//TextView spinnerTextView = (TextView) spinner1.findViewById(R.id.text_room_name);
		//spinnerTextView.setTypeface(hpSimplified);

	}
	
	@Override
	protected void onStart(){
		log("onStart()");
		super.onStart();
		// Get a reference to the SensorManager
				mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

				// Get a reference to the accelerometer
				accelerometer = mSensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

				// Get a reference to the magnetometer
				magnetometer = mSensorManager
						.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

				// Exit unless both sensors are available
				if (null == accelerometer || null == magnetometer)
					finish();
				//log("Sensors ok");
	}

	@Override
	protected void onResume() {
		log("onResume()");
		super.onResume();

		//if (map.isAlive()) {
			// Register for sensor updates
			//log("onResume: registering listener");

			mSensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);

			mSensorManager.registerListener(this, magnetometer,
					SensorManager.SENSOR_DELAY_NORMAL);
			//log("...Listeners registered");
		//}
	}

	@Override
	protected void onPause() {
		log("onPause()");
		super.onPause();



	}
	
	@Override
	protected void onStop(){
		log("onStop()");
		super.onStop();

	}
	
	@Override
	protected void onDestroy(){
		log("onDestroy()");
		super.onDestroy();
		// Unregister all sensors
		mSensorManager.unregisterListener(this);
		//log("unregistered");
		// map.invalidate();
		map.surfaceDestroyed(null);
	}
	
	@Override
	protected void onRestart(){
		log("onRestart()");
		super.onRestart();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		// Acquire accelerometer event data
		// log("SensorChanged");
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// log("TYPE_ACCELEROMETER");
			mGravity = new float[3];
			System.arraycopy(event.values, 0, mGravity, 0, 3);

		}

		// Acquire magnetometer event data

		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			// log("TYPE_MAGNETIC_FIELD");
			mGeomagnetic = new float[3];
			System.arraycopy(event.values, 0, mGeomagnetic, 0, 3);

		}

		// If we have readings from both sensors then
		// use the readings to compute the device's orientation
		// and then update the display.

		if (mGravity != null && mGeomagnetic != null) {
			// log("Both Sensors present");
			float rotationMatrix[] = new float[9];

			// Users the accelerometer and magnetometer readings
			// to compute the device's rotation with respect to
			// a real world coordinate system

			boolean success = SensorManager.getRotationMatrix(rotationMatrix,
					null, mGravity, mGeomagnetic);

			if (success) {
				// log("Got rotation matrix success");
				float orientationMatrix[] = new float[3];

				// Returns the device's orientation given
				// the rotationMatrix

				SensorManager.getOrientation(rotationMatrix, orientationMatrix);

				// Get the rotation, measured in radians, around the Z-axis
				// Note: This assumes the device is held flat and parallel
				// to the ground

				float rotationInRadians = orientationMatrix[0];

				/**
				 * // log("Rotation calculated: " + mRotationInDegrees); // //
				 * Request redraw float rotationSum=0;
				 * 
				 * for (int i = 0; i < nSamples - 1; i++) { log("" +
				 * rotationHistory[i]); rotationHistory[i] = rotationHistory[i +
				 * 1]; rotationSum += rotationHistory[i]; }
				 * 
				 * rotationHistory[nSamples - 1] = -rotationInRadians;
				 * rotationSum += rotationHistory[nSamples - 1];
				 * 
				 * map.setTarget((float) (rotationSum / nSamples));
				 * log("Set target " + rotationSum / nSamples);
				 **/

				//map delta offset with respect to real north: 23° = 0.4014rad
				map.setTarget(0.4014f-rotationInRadians);
				// Reset sensor event data arrays
				mGravity = mGeomagnetic = null;

			}

		
			
		
		}
		


	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// N/A
	}

	
	private Room findRoomInListByName(ArrayList<Room> list, String name){
		
		
		for(Room r: list){
			if(stringsMatch(r.roomName,name)){
				return r;
			}
		}
		
		return null;
				
	}
	
	private int findRoomIdInListByName(ArrayList<Room> list, String name){
		
		
		for(int i = 0; i< list.size();i++){
			//log(i+": checking if " + list.get(i).toString() + " equals " + name);
			if(stringsMatch(list.get(i).toString(),name)){
				//log("it does!!");
				return i;
			}
		}
		
		return 0;
				
	}
	
	private String removeAccentedChars(String s){
		s = s.toLowerCase();
		s = s.replace("á", "a");
		s = s.replace("é", "e");
		s = s.replace("í", "i");
		s = s.replace("ó", "o");
		s = s.replace("ú", "u");
		s = s.replace("ñ", "n");
		return s;
		
	}
	
	private Boolean stringsMatch(String s1, String s2){
		return (removeAccentedChars(s1).equalsIgnoreCase(removeAccentedChars(s2)) || (removeAccentedChars(s1) + " room").equalsIgnoreCase(removeAccentedChars(s2)) || removeAccentedChars(s1).equalsIgnoreCase(removeAccentedChars(s2) + " room"));
	}
	
	
	private class rotatingMap extends SurfaceView implements
			SurfaceHolder.Callback {

		//private MapBitmap mapBitmap;
		private int mBitmapWidth, mBitmapHeight;
		//private Bitmap mBitmap;
		private FloorMapBitmap activeFloor;
		private ArrayList<FloorMapBitmap> allFloors;
		//private final DisplayMetrics mDisplay;
		//private final int mDisplayWidth, mDisplayHeight;
		private float mX, mY;
		private float thetaTarget;
		private float thetaStep;
		private final SurfaceHolder mSurfaceHolder;
		private final Paint mPainter = new Paint();
		private final Paint textPainter = new Paint();
		private Thread mDrawingThread;
		private int room;
		private float scale;
		private String texto;
		private int markerX, markerY, markerSize, markerSizeMin, markerSizeMax;
		private float markerSizeGrowth;
		private boolean markerShown;
		private float surfaceSizeToBitmapSize;

		private float d, ds, ds1, ds2, alpha, beta, gamma, theta, beta2;
		private float thetaBefore, thetaSpeed;

		private static final float kpr = 0.5f;
		private static final float kdr = 0.1f;
		private final double minDeltaToMove = Math.toRadians(5);

		private volatile boolean alive = true;
		private volatile int surfaceWidth = 0;
		private volatile int surfaceHeight = 0;
		
		private Room activeRoom ;
		private Boolean surfaceSizeIsKnown;
		private Boolean bitmapSizeIsKnown;

		public rotatingMap(Context context, ArrayList<FloorMapBitmap> floorCollection) {

			super(context);
			//log("Creating rotatingMap");

			allFloors = floorCollection;
			
			//mBitmap = bm;
			activeFloor = allFloors.get(0);
		
		
			
			//this.mBitmap = Bitmap.createScaledBitmap(bitmap, mBitmapWidth,mBitmapHeight, false);
			mBitmapWidth = activeFloor.getWidth();
			mBitmapHeight = activeFloor.getHeight();
			bitmapSizeIsKnown = true;
			
			//log("mBitmapHeight Width ok");
			//mDisplay = new DisplayMetrics();
			//MainActivity.this.getWindowManager().getDefaultDisplay()					.getMetrics(mDisplay);
			//log("getMetrics ok");
			//mDisplayWidth = mDisplay.widthPixels;
			//mDisplayHeight = mDisplay.heightPixels;
			//log("displaywidth = " + mDisplayWidth + "     displayheight = "					+ mDisplayHeight);
			
			
			
			room = 0;
			mX = 0;
			mY = 0;
			scale = 1;
			markerX = 0;
			markerY = 0;
			markerSize = 30;
			markerSizeGrowth = 1f;
			markerShown = false;
			activeRoom = new Room();
			

			mPainter.setAntiAlias(true);

			textPainter.setColor(HPORANGE);
			textPainter.setTextSize(50);
			surfaceSizeIsKnown = false;


			mSurfaceHolder = getHolder();
			mSurfaceHolder.addCallback(this);
			//log("trying trigonometrics...");
			alpha = (float) Math.atan(mBitmapWidth / mBitmapHeight);
			d = (float) Math.hypot(mBitmapHeight, mBitmapWidth);
			//log("Created rotatingMap");

		}

		private void drawMap(Canvas canvas) {
			// log("DrawingMap");
			// log("mX: " + mX + "   mY: " + mY);

			
			if (!surfaceSizeIsKnown || !bitmapSizeIsKnown) {
				mBitmapWidth = activeFloor.getWidth();
				mBitmapHeight = activeFloor.getHeight();
				surfaceWidth = this.getWidth();
				surfaceHeight = this.getHeight();
				mX = (surfaceWidth - mBitmapWidth) / 2;
				mY = (surfaceHeight - mBitmapHeight) / 2;
				//log("Surface size: " + surfaceWidth + " x " + surfaceHeight);
				//log("Bitmap size:" + mBitmapWidth + " x " + mBitmapHeight);
				surfaceSizeToBitmapSize = ((float)surfaceHeight)/((float)mBitmapHeight) ;
				//log("surfaceSizeToBitmpSize = " + surfaceSizeToBitmapSize);
				
				surfaceSizeIsKnown = true;
				bitmapSizeIsKnown = true;
				
				markerSizeMin = mBitmapWidth / 40;
				markerSizeMax = mBitmapWidth / 10;
				markerSize = (markerSizeMin+markerSizeMax)/2;
				markerSizeGrowth = Math.max(markerSizeMax/markerSizeMin/70,1); //at least 1
				
				//refresh coordinates which might have been created before:
				if(markerShown) refreshRoomCoordinates();
				rescale();
			}

			canvas.drawColor(HPLIGHTGRAY);

			// canvas.scale( scale, scale,mBitmapWidth/2,mBitmapHeight/2);
			// canvas.rotate((float)Math.toDegrees(theta));//,mBitmapWidth/2,mBitmapHeight/2);

			canvas.scale(scale, scale, surfaceWidth / 2, surfaceHeight / 2);
			canvas.rotate((float) Math.toDegrees(theta), surfaceWidth / 2,
					surfaceHeight / 2);

			canvas.drawBitmap(activeFloor.getBitmap(), mX, mY, mPainter);

			//canvas.drawText(activeRoom.roomName , 0,mY+markerY, textPainter);

			mPainter.setColor(HPORANGE);

			if(markerShown) canvas.drawCircle(mX+markerX, mY+markerY, markerSize, mPainter);
			
			// log("Drew Map");
		}

		private boolean move() {
			
			if(markerShown) animateMarker();

			thetaSpeed = theta - thetaBefore;
			thetaBefore = theta;

			float thetaDelta = thetaTarget - theta;
			//log("thetaDelta: " + thetaDelta);
			if (Math.abs(thetaDelta) < minDeltaToMove) {
				//log("too small delta. " +thetaDelta + " < " + minDeltaToMove);
				return true;
			}
			if (thetaDelta > Math.PI) {
				thetaDelta -= 2 * Math.PI;
			} else if (thetaDelta < -Math.PI) {
				thetaDelta += 2 * Math.PI;
			}

			thetaStep = (kpr * thetaDelta) - (kdr * thetaSpeed);

			theta += thetaStep;
			// log("delta: " + thetaDelta + "  step: " +thetaStep);

			// theta = thetaTarget;

			rescale();
			return true;

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			//log("Creating surface");
			mDrawingThread = new Thread(new Runnable() {
				public void run() {
					//log("Running thread");
					Canvas canvas = null;
					while (!Thread.currentThread().isInterrupted() && alive
							&& move()) {
						canvas = mSurfaceHolder.lockCanvas();
						//log("Rotating " + theta + "  Alive:  " + alive);
						if (null != canvas && alive) {
							drawMap(canvas);
							mSurfaceHolder.unlockCanvasAndPost(canvas);
						}

					}

					accelerometer = null;
					magnetometer = null;

					return;
				}
			});
			mDrawingThread.start();

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			//log("SurfaceDestroyed");
			if (null != mDrawingThread) {
				//log("interrupting thread");
				mDrawingThread.interrupt();
				alive = false;
				mDrawingThread = null;

			}

		}


		public void locateRoom(Room roomToDisplay){
			
			
			if(roomToDisplay!=null){
				
				//log("Room " + roomToDisplay.roomName + " requested.");

				//check if I need to change floor
				if(!roomToDisplay.floor.equalsIgnoreCase(activeFloor.getFloorName())){
					//log("Changing floor");
					FloorMapBitmap newFloor;
					newFloor = findFloorByName(roomToDisplay.floor);
					if(null!=newFloor){
						activeFloor = newFloor;
						bitmapSizeIsKnown = false;
					}
				}
				
				markerShown = true;
				activeRoom = roomToDisplay;
				refreshRoomCoordinates();
			} else {
				markerShown = false;
			}
					
		}
		
		private void refreshRoomCoordinates(){
			if(null!=activeRoom ){
				markerX = activeRoom.x;
				markerY = activeRoom.y;
				texto = activeRoom.roomName;
				//log("Real Coordinates: " + markerX + ", " + markerY);
				//log("Scaled Coordinates: " + surfaceSizeToBitmapSize*markerX + ", " + surfaceSizeToBitmapSize*markerY);
				//markerX *= surfaceSizeToBitmapSize;
				//markerY *=surfaceSizeToBitmapSize;
				
			}
		}
		

		public void setTarget(float tTarget) {
			//log("Receiving target " + tTarget);
			thetaTarget = (float) (tTarget);
		}


		public void rescale() {
			/**
			 * // log("rescaling"); beta = alpha + thetaTarget; beta2 = beta - 2
			 * * alpha;
			 * 
			 * ds1 = (float) Math.abs(mDisplayWidth / Math.sin(beta)); if
			 * (Double.isInfinite(ds1)) { ds1 = 999; }
			 * 
			 * ds2 = (float) Math.abs(mDisplayHeight / Math.sin(beta2)); if
			 * (Double.isInfinite(ds2)) { ds2 = 999; }
			 * 
			 * ds = Math.min(ds1, ds2); if (ds > mDisplayHeight) { ds =
			 * mDisplayHeight; }
			 * 
			 * scale = ds / d;
			 **/
			// scale = 0.75+0.25*Math.cos(2*Math.PI*mRotation/180);
			/**
			 * scale = (float)((mDisplayHeight / mBitmapWidth)*
			 * Math.abs(Math.cos(Math.PI * theta / 180)) - (mDisplayWidth /
			 * mBitmapHeight)* Math.abs(Math.sin(Math.PI * theta / 180))); if
			 * (scale<0.5){ scale = 0.5f; }
			 **/
			// log("rescaled ok");

			/**
			 * 				|
			 *  			|
			 *    surfHeight+--.             .---. 
			 *    			|   \           /     \ 
			 *    		   .|    \         /       \ 
			 *    		   '|     \       /         \ 
			 *    			|      \     /           \
			 *     surfWidth+       '---'             '-- 
			 *     			| 
			 *     			| 
			 *     			+---------+--------+--------+-------- 
			 *     			0		 180      360
			 * 
			 */

			float a = (surfaceHeight - surfaceWidth) / 2;
			//log("a= " +a);
			float y0 = (surfaceHeight + surfaceWidth) / 2;
			//log("y0= "  + y0);

			double currHeight = a * Math.cos(2*theta) + y0;
			//log("currHeight = "+ currHeight);

			scale = (float) (0.95*currHeight/mBitmapHeight); //0.95 para darle un margencito
			//log("scale = " + scale);
			


		}

		
		private void animateMarker(){
			if(markerSize>markerSizeMin && markerSize<markerSizeMax){
				markerSize += markerSizeGrowth;
				
			} else {
				markerSizeGrowth *=-1;
				markerSize += markerSizeGrowth;
				
			}
			//log("Marker Size: " + markerSize);
		}
		
		public boolean isAlive() {
			return alive;
		}
		
		private FloorMapBitmap findFloorByName(String name){
			for(int i=0; i<allFloors.size();i++){
				if(allFloors.get(i).floorName.equalsIgnoreCase(name)){
					return allFloors.get(i);
				}
			}
			return null;
			
		}
		
	}

	
	
	private class FloorMapBitmap {
		private final Bitmap bitmap;
		private final String floorName;
		
		public FloorMapBitmap(Bitmap bitmap, String floorName){
			//log("Creating new FloorMapBitmap "+ floorName);
			this.bitmap = bitmap;
			//log("bitmap ok");
			this.floorName = floorName;
			//log("floorname ok");
		}
		
		public Bitmap getBitmap(){
			return bitmap;
		}
		
		public String getFloorName(){
			return floorName;
		}
		
		public int getWidth(){
			return bitmap.getWidth();
		}
		
		public int getHeight(){
			return bitmap.getHeight();
		}
		
		
		@Override
		public boolean equals(Object obj){
			if(obj instanceof FloorMapBitmap){
				return obj == this;
			} else if(obj instanceof String){
				return obj == this.floorName;
			} else{
				return false;
			}
		}
	}
	
	private static void log(String message) {
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		Log.i(TAG, message);
	}

	private static String processIntentText(String text) {
		String query = text.substring(text.indexOf("q=") + 2).toLowerCase();
		String exchangeNameIdentifier = "CONF-".toLowerCase();
		String nombre;

		if (query.contains(exchangeNameIdentifier)) {
			nombre = query.substring(
					query.indexOf(exchangeNameIdentifier)
							+ exchangeNameIdentifier.length()).replace(")", "");

		} else {

			nombre = query.replace("sala ", "").replace(" room", "").replace("%20"," ");
		}

		return nombre.trim();

	}

	public static class Room {
		public String roomName;
		public int x;
		public int y;
		public String floor;

		
		public Room(String roomName, int x, int y, String floor){
			this.roomName = roomName;
			this.x = x;
			this.y = y;
			this.floor = floor;
			
		}
		
		public Room(){
			this("",0,0,"");
		}
		
		
		public boolean equals(String s) {
			//log("Using room equals string:" + this.roomName + ".equals('" + s + "');");
			return this.roomName.equalsIgnoreCase(s.trim());
		}
		
		public boolean equals(Room r){
			//log("Using room equals room:" + this.roomName + ".equals('" + r.roomName + "');");
			return this.roomName.trim().equalsIgnoreCase(r.roomName.trim());
		}
		
		
		@Override
		public boolean equals(Object obj) {
			
			
		    if (obj == null) {
		        return false;
		    }
		   // log("Using room equals Object:" + this.roomName + ".equals('" + ((Room)obj).roomName + "');");
		    if (getClass() != obj.getClass()) {
		        return false;
		    }
		    final Room other = (Room) obj;
		    if ((this.roomName == null) ? (other.roomName != null) : !this.roomName.equalsIgnoreCase(other.roomName)) {
		        return false;
		    }
		    
		    return true;
		}

		
		@Override
		public int hashCode() {
			//log("Using hashcode");
		    int hash = 3;
		    hash = 53 * hash + (this.roomName != null ? this.roomName.hashCode() : 0);
		     return hash;
		}
		
		@Override
		public String toString(){
			return roomName;
		}
	}

	private ArrayList<Room> parseXML(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		//log("Beginning parseXML");
		ArrayList<Room> rooms = null;
		int eventType = parser.getEventType();
		Room currentRoom = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			// log("Reading line " + parser.getLineNumber());
			String tagName = null;
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:
				// log("New document");
				rooms = new ArrayList<Room>();
				break;
			case XmlPullParser.START_TAG:
				tagName = parser.getName();
				// log("New tag '" + tagName + "'");
				if (tagName.equalsIgnoreCase("room")) {
					// log("New room");
					currentRoom = new Room();
				} else if (currentRoom != null) {
					// log("Adding attributes to current room");
					if (tagName.equalsIgnoreCase("roomname")) {
						currentRoom.roomName = parser.nextText();
						// log("set room name = " + currentRoom.roomName);
					} else if (tagName.equalsIgnoreCase("floor")) {
						currentRoom.floor = parser.nextText();
						// log("set floor = " + currentRoom.floor);
					} else if (tagName.equalsIgnoreCase("x")) {
						currentRoom.x = Integer.parseInt(parser.nextText());
						// log("set x = " + currentRoom.x);
					} else if (tagName.equalsIgnoreCase("y")) {
						currentRoom.y = Integer.parseInt(parser.nextText());
						// log("set y = " + currentRoom.y);
					}
				}
				break;
			case XmlPullParser.END_TAG:
				tagName = parser.getName();
				// log("End tag " + tagName);
				if (tagName.equalsIgnoreCase("room") && currentRoom != null) {
					// log("Adding room " + currentRoom.roomName);
					rooms.add(currentRoom);
				}
			}
			eventType = parser.next();
		}
		return rooms;
	}
	

	public class MySpinnerAdapter extends ArrayAdapter<String> {
		public MySpinnerAdapter(Context ctx, int txtViewResourceId, ArrayList<String> strings) {
			super(ctx, txtViewResourceId, strings); 
			}
		
		@Override 
		public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
			return getCustomView(position, cnvtView, prnt); 
			}
		
		@Override
		public View getView(int pos, View cnvtView, ViewGroup prnt) { 
			
			return getCustomView(pos, cnvtView, prnt); 
		}
		
		public View getCustomView(int position, View convertView, ViewGroup parent) { 
			LayoutInflater inflater = getLayoutInflater(); 
			View mySpinner = inflater.inflate(R.layout.my_spinner_layout, parent, false); 
			TextView main_text = (TextView) mySpinner .findViewById(R.id.text_room_name); 
			main_text.setText((CharSequence)roomNames.get(position));
			
			Typeface hpSimplified = Typeface.createFromAsset(getAssets(), "HPSimplified_Rg.ttf");
			main_text.setTypeface(hpSimplified);
			
			
			Typeface hpSimplifiedBold = Typeface.createFromAsset(getAssets(), "HPSimplified_Bd.ttf");
			TextView bullet = (TextView) mySpinner.findViewById(R.id.bullet);
			bullet.setTypeface(hpSimplifiedBold);
		 
			return mySpinner;
		}

	
	}

	private void hideKeyboard() {
		log("Trying to hide keyboard...");
	    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

	    // check if no view has focus:
	    View view = this.getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	
}
