/*Copyright (c) 2013, 2014, Freescale Semiconductor, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the name of Freescale Semiconductor, Inc. nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL FREESCALE SEMICONDUCTOR, INC. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * A_FSL_Sensor_Demo = top level class definition for Freescale sensor fusion
 * demo program for Android. This application provides the user an intuitive
 * user interface for experimenting with various combinations of sensors and
 * sensor fusion algorithms. Both local and remote (via Bluetooth) sensors are
 * supported.
 * <p>
 * Program functions include:
 * <UL>
 * <LI>Graphical views affected by orientation calculations
 * <LI>Data logging capability
 * <LI>Integrated documentation
 * </UL>
 * 
 * @author Michael Stanley
 */
package com.freescale.sensors.sfusion;

import java.io.File;
import java.lang.ref.WeakReference;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class A_FSL_Sensor_Demo extends Activity implements OnMenuItemClickListener {
	static public String LOG_TAG = null;            // This string is used to uniquely identify Android log messages
	public LocalSensors localSensors = null;        // Pointer to object for managing input from sensors local to your Android device
	public DataSelector dataSelector = null;        // Pointer to object which selects one of several different sensor sources
	public Statistics statistics = null;            // Pointer to object which creates the "Status screen"
	static private DataLogger dataLogger = null;    // Messages to the transcript window are logged through this object
	private MyWebView browser = null;               // We use the browser for in-app help
	static public String[] urls = new String[16];   // List of HTML pages for the help
	public MyUtils myUtils = null;                  // This class is used as a "home" for misc. utility functions
	public IMU imu = null;                          // The IMU class encapsulates sensor boards which communicate via Bluetooth
	public MyCanvas canvasApplet = null;            // Used to demonstrate air mouse features
	public ViewGroup canvasFrame;                   
	public enum GuiState {                          // GuiState defines the different states that the user interface can take on
		DEVICE
	}

	public enum DataSource {                        // DataSource defines the various "sources" of sensor data
		STOPPED, LOCAL, REMOTE, FIXED
	}

	public enum Algorithm {                         // These are the choices of algorithms that are supported by the embedded code
		NONE, NINE_AXIS
	}
	
	public enum DevelopmentBoard {                  // List of supported development boards.  Note that KL16Z is a placeholder.
		REV5, KL25Z, K20D50M, KL26Z, K64F, KL16Z, KL46Z, KL46Z_Standalone
	}

	// Centralized state variables
	public GuiState guiState = GuiState.DEVICE;
	public DataSource dataSource = DataSource.STOPPED;
	public Algorithm algorithm = Algorithm.NONE;
	public DevelopmentBoard developmentBoard = DevelopmentBoard.REV5;
	
	// some handy constants
	private final String GUI_STATE = "GuiState";
	private final String DATA_SOURCE = "DataSource";
	private final String ALGORITHM = "Algorithm";
	private final String DEVBOARD = "DevtBoard";

	public int statsSampleSize=100;                  // This is the number of sensor samples used to calculate sensor statistics
	public boolean statsOneShot=false;               // control variable for the stats view
	static public TextView tv1 = null;               // TextView variables are global pointers to fields in the GUI which can be 
	static private TextView stsTextView = null;      // access via various utility functions.
	static private TextView console2 = null;
	static private TextView console3 = null;
	static private TextView console4 = null;
	static private TextView numMsgsField = null;

	static public ScrollView loggingWindow = null;
	static private int fileLoggingEnabled = 0;      // functional a boolean, but must be int for messaging purposes
	private boolean absoluteRemoteView = false;     // set to true to NOT take into account Android device view when using remote IMU
	
	private MenuItem legacyDumpMenuItem = null;     // variables for controlling logging functions
	private MenuItem hexDumpMenuItem = null;
	private MenuItem csvDumpMenuItem = null;
	public boolean legacyDumpEnabled = true;        // set to true to enable legacy dump
	static public boolean hexDumpEnabled = false;   // set to true to enable hex display of BT input
	public boolean csvDumpEnabled = false;          // set to true to enable CSV dump of remote (BT and WiFi) IMUs
	
	static public boolean console2IsStale = true;   // used to throttle console2 updates to protect performance - debug
	static public boolean console3IsStale = true;   // used to throttle console3 updates to protect performance - virtual gyro
	static public boolean console4IsStale = true;   // used to throttle console4 updates to protect performance - roll pitch compass
	public boolean zeroPending = false;             // handshaking control for "Zero" function in the device view
	public boolean zeroed = false;
	public boolean splitScreen = false;             // Boolean for turning on/off the split screen mode
	public boolean dumpScreen = false;              // Set to true to request render to create a screen dump. Will be reset
										            // to false by render after done.
	
	int currentDocPage = 0;                         // Index pointer to the current help page
	
	// Options Menu definitions
	private final int SET_PREFERENCES_MENU_ITEM = Menu.FIRST;

	public final String PREF_NAME = "A_FSL_Sensor_Demo";  // String for retrieving shared preferences
	public SharedPreferences myPrefs;                     // Structure for preferences
	private float filterCoefficient = 0;                  // Used to control low pass filtering
	public ToneGenerator toneGenerator = null;            // for sound effects

	static public A_FSL_Sensor_Demo self = null; 
	// The self pointer is used in the body of one of the
	// listener functions, where "this" points to the
	// listener, not the demo itself.
	
	public String imuName;
	private Spinner docSpinner = null; // I don't normally make UI elements
										// class variables, but need this early
										// in onCreate().

    // Definition for graphics files used to render various 3D displays
	public TextureCubeRenderer pcbRenderer = null;
	final int roomSurfaces[] = { R.drawable.front_wall,
			R.drawable.left_wall_with_door, R.drawable.back_wall,
			R.drawable.right_wall_with_door, R.drawable.roof, R.drawable.floor };
	final int pcbSurfaces[] = { R.drawable.pcb_sides, R.drawable.pcb_sides,
			R.drawable.pcb_sides, R.drawable.pcb_sides, R.drawable.rev5_pcb_top,
			R.drawable.rev5_pcb_bottom };

    final int k20d50mMultiSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
            R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.sensor_shield_top,
            R.drawable.k20d50m_bottom };

	final float pcbDimensions[] = { 0.96f, 1.5f, 0.05f, -2.5f };
	final float freedomDimensions[] = { 1.05f, 1.55f, 0.05f, -2.5f }; 

	/**
	 * Initializes an array of strings which define the documentation pages for
	 * this app. The order of each file in the array should match the order used
	 * in the "Page" spinner definition. Other functionn can obtain this list
	 * (once initialized) by calling the urls() function.
	 */
	static final private int FINAL_PAGE_IN_ROTATION = 13;
	final private int FREESCALE_PAGE = 14;
	final private int WWW_PAGE = 15;

	static void loadUrls() {
	}

	/**
	 * used to make sure that the value displayed for the "Page" spinner on the
	 * GUI matches the currently displayed page.
	 * 
	 * @param docSpinner
	 *            a pointer to the Spinner being configured
	 * @param url
	 *            the URL currently being displayed in the browser
	 */
	public void setDocSpinner(String url) {
		int currentDocPage = docSpinner.getSelectedItemPosition();
		String currentDocSpinnerValue = urls[currentDocPage];
		Log.i(LOG_TAG, "setDocSpinner url = " + url + " currentSpinner = " + currentDocSpinnerValue + "\n");
		if (url.startsWith(currentDocSpinnerValue)) {
			// we're good!
			return;
		}
		for (int i = 0; i <= FINAL_PAGE_IN_ROTATION; i++) {
			if (url.equalsIgnoreCase(urls[i])||url.startsWith(urls[i])) {
				docSpinner.setSelection(i);
				Log.i(LOG_TAG, "Found url match at index " + i + "\n");
				return;
			}
		}
		if (url.startsWith("http://www.freescale.com")) {
			docSpinner.setSelection(FREESCALE_PAGE);
			Log.i(LOG_TAG, "Matched against http://www.freescale.com\n");
			return;
		} 
		Log.i(LOG_TAG, "Default URL match to general web\n");
		docSpinner.setSelection(WWW_PAGE);
	};
	/**
	 * returns the list of documentation pages for this application
	 */
	static public String[] urls() {
		return (urls);
	}
	static public int finalPageInRotation() {
		return(FINAL_PAGE_IN_ROTATION);
	}

	/**
	 * utility library used to quickly determine whether or not we should be
	 * sampling sensor data.
	 * 
	 * @return true unless the current data source is specified to be FIXED or
	 *         STOPPED.
	 */
	public boolean dataIsLive() {
		return ((dataSource != DataSource.FIXED) && (dataSource != DataSource.STOPPED));
	}
	public boolean dualModeRequired() {
		boolean sts = ((dataSource == DataSource.REMOTE))
				&& (guiState==GuiState.DEVICE) && (absoluteRemoteView == false);
		return(sts);
	}
	public boolean absoluteModeRequired() {
		boolean sts = (dataSource == DataSource.REMOTE)&& (absoluteRemoteView == true);
		return(sts);
	}

	/**
	 * Updates the status field.
	 * 
	 * @param msg
	 *            String to be written to the status field in the App-specific
	 *            settings bar
	 */
	public void setSts(String msg) {
		if (stsTextView!=null) {
			stsTextView.setText(msg);
		}
	}
	public void setConsole2(String msg) {
		if (guiState==GuiState.DEVICE) {
			if (console2IsStale && (console2!=null)) {
				console2.setText(msg);
				console2IsStale = false;
			}
		}
	}
	public void clearConsole2() {
		if (console2!=null) {
			console2.setText("");
		}
	}
	public void setConsole3(String msg) {
		if (guiState==GuiState.DEVICE) {
			// only visible in device view
			if (console3IsStale && (console3!=null)) {
				console3.setText(msg);
				console3IsStale = false;
			}
		}
	}
	public void clearConsole3() {
		if (console3!=null) {
			console3.setText("");
		}
	}
	public void setConsole4(String msg) {
		if (guiState==GuiState.DEVICE) {
			// only visible in device view
			if (console4IsStale && (console4!=null)) {
				console4.setText(msg);
				console4IsStale = false;
			}
		}
	}
	public void clearConsole4() {
		if (console4!=null) {
			console4.setText("");
		}
	}
	public void makeConsolesStale() {
		console2IsStale=true;
		console3IsStale=true;
		console4IsStale=true;
	}
	public void clearConsoles234() {
		clearConsole2();
		clearConsole3();
		clearConsole4();
	}

	/**
	 * The App-specific settings bar contains a text field which is used to
	 * display how many messages have been written to date. This function is
	 * used to update that field.
	 * 
	 * @param num
	 */
	 static public void setNumMsgs(Long num) {
		numMsgsField.setText(String.format(" (%d) ", num));
	 }

	/**
	 * Used for debug purposes only to dump application state variables to the
	 * log window.
	 * 
	 * @param str
	 *            a string indicating the current state transition
	 */
	public void dumpStates(String str) {
		Log.i(LOG_TAG, str);
		Log.i(LOG_TAG, "  guiState=" + guiState.toString());
		Log.i(LOG_TAG, "  algorithm=" + algorithm.toString());
		Log.i(LOG_TAG, "  dataSource=" + dataSource.toString());
	}

	/**
	 * Writes a message to the log window and (if file logging is enabled) the
	 * log file.
	 * <P>
	 * Author's Note: In an effort to speed up textual output to the logging
	 * window, I have rewritten the log function several (at least four) times.
	 * The current implementation does all the list management and concatenation
	 * in a separate thread created using the DataLogger class. Regardless of
	 * the technique used, it appears that the log window is always prone to
	 * slowing down once the length of displayed text reaches a certain value.
	 * In other words, the tv1.setText() seems to be the limiting case, and that
	 * must (because it controls the GUI) be located in this thread. I've kept
	 * the separate DataLogger class-based version because it is a nice example
	 * of how to communicate between two threads and I can add conveniently add
	 * file I/O in the same thread. Performance using the DataLogger class seems
	 * a bit better, but you can apparently get reasonable performance by doing
	 * everything locally if you want.
	 * <P>
	 * Currently the DataLogger class limits the display to 100 messages, which
	 * seems to be a reasonable compromise of performance vs function.
	 * 
	 * @param always
	 *            true if message should always print, false if it should print
	 *            only when the log window is displayed
	 * @param str
	 *            String to be printed.
	 * */
	static public void write(Boolean always, String str) {
	}

	static private class MyHandler extends Handler {
		@SuppressWarnings("unused")
		private final WeakReference<A_FSL_Sensor_Demo> myActivity;
		public MyHandler(A_FSL_Sensor_Demo activity) {
			myActivity= new WeakReference<A_FSL_Sensor_Demo>(activity);
			// this construct is used to help the JAVA garbage collector.
			// making the handler static and using a weak reference to the
			// activity is supposed to make it easier to recycle objects
			// that are no longer needed.
		}
		public void handleMessage(Message msg) {
			if (msg.what == 1) { // the Logging Window needs an update
				tv1.setText(msg.obj.toString());
				loggingWindow.scrollTo(0, tv1.getBottom());
			}
			// arg1 = int upper = (int) numMsgsLoggedToFile/1024;
			// arg2 = int lower = (int) numMsgsLoggedToFile - 1024*upper;
			long numLogged = 1024 * msg.arg1 + msg.arg2;
			setNumMsgs(numLogged);
		}
	}

	public MyHandler logHandler = new MyHandler(this);
	/**
	 * clears the logging window. Also sends a message to the dataLogger class
	 * telling it to close the log file (if open).
	 * 
	 * @return tv1 pointer to the console TextView
	 */
	 public TextView cls() {
		Message msg = Message.obtain(dataLogger.getHandler(), 3, 0, 0, null);
		dataLogger.getHandler().sendMessage(msg); // clear the ArrayList backing
													// the log window
		if (tv1!=null) {
			tv1.setText("");
			loggingWindow.scrollTo(0, 0);
		}
		return (tv1);
	}

	/**
	 * Computes a string with the app name, copyright and program version.
	 * 
	 * @return msg containing app name, copyright and program version.
	 */
	public String prompt() {
		return ("It works, really");
	}
	public String appVersion() {
		String versionName = "";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(),
					0).versionName;
		} catch (Throwable t) {
			versionName = "unknown";
		}
		return (versionName);
	}

	/**
	 * Configures visibility of GUI components related to viewing documentation
	 * 
	 * @param includeDocs
	 *            equal true if documentation controls should be visible.
	 *            Otherwise false.
	 */
	private void configureDocOptions(boolean includeDocs) {
		LinearLayout webframe = (LinearLayout) findViewById(R.id.webframe);
		// final MyWebView browser = (MyWebView) findViewById(R.id.webview);
		Button backButton = (Button) findViewById(R.id.back_button);
		Button forwardButton = (Button) findViewById(R.id.forward_button);
		Spinner docSpinner = (Spinner) findViewById(R.id.doc_spinner);
		if (includeDocs) {
			browser.setVisibility(View.VISIBLE);
			webframe.setVisibility(View.VISIBLE);
			backButton.setVisibility(View.VISIBLE);
			forwardButton.setVisibility(View.VISIBLE);
			docSpinner.setVisibility(View.VISIBLE);
		} else {
			browser.setVisibility(View.GONE);
			webframe.setVisibility(View.GONE);
			backButton.setVisibility(View.GONE);
			forwardButton.setVisibility(View.GONE);
			docSpinner.setVisibility(View.GONE);
		}
		browser.loadUrl(urls[currentDocPage]);
	}

	/**
	 * Configure visibility of GUI components related to use of sensor low pass
	 * filters.
	 */
	private void configureLpfOptions() {
		CheckBox lpfEnable = (CheckBox) findViewById(R.id.lpf_enable);
		LinearLayout filterCoefWrapper = (LinearLayout) findViewById(R.id.filter_coef_wrapper);
		//;
		boolean lpfAllowed = true;
		if (lpfAllowed) {
			lpfEnable.setVisibility(View.VISIBLE);
			if (lpfEnable.isChecked()) {
				filterCoefWrapper.setVisibility(View.VISIBLE);
				localSensors.enableLowPassFilters(true);
			} else {
				filterCoefWrapper.setVisibility(View.GONE);
				localSensors.enableLowPassFilters(false);
			}
		} else {
			lpfEnable.setVisibility(View.GONE);
			filterCoefWrapper.setVisibility(View.GONE);
			localSensors.enableLowPassFilters(false);
		}
	}
	void disableZeroFunction(CheckBox zeroCheckBox) {
		zeroCheckBox.setChecked(false);
		zeroed = false;
		zeroPending = false;
	}

	// Configure visibility for text fields above the DEVICE view
	public void configureConsoles(boolean show) {
    	TextView console2 = (TextView) findViewById(R.id.console2); // console2 = debug
     	TextView console3 = (TextView) findViewById(R.id.console3); // console3 = virtual gyro
       	TextView console4 = (TextView) findViewById(R.id.console4); // console4 = roll pitch compass
		boolean debugEnabled = myPrefs.getBoolean("enable_device_debug", false);
		boolean virtualGyroEnabled=myPrefs.getBoolean("enable_virtual_gyro", false);
		boolean rpcEnabled = myPrefs.getBoolean("enable_rpc", false);
		if (show && debugEnabled) console2.setVisibility(View.VISIBLE);
		else console2.setVisibility(View.GONE);
		if (show && virtualGyroEnabled) console3.setVisibility(View.VISIBLE);
		else console3.setVisibility(View.GONE);
		if (show && rpcEnabled) console4.setVisibility(View.VISIBLE);
		else console4.setVisibility(View.GONE);
	}
	/**
	 * top level function used to configure visibility of various GUI
	 * components.
	 * <P>
	 * The application is state-based, and what is visible or not is a function
	 * of those states.
	 * 
	 * @param gui_state
	 *            the state of the application which we are about to configure.
	 */
	public void configureApplicationViews(GuiState guiState, boolean resetStats) {
		CheckBox flEnable = (CheckBox) findViewById(R.id.fl_enable);
		CheckBox absolute = (CheckBox) findViewById(R.id.absolute);
		CheckBox zeroCheckBox = (CheckBox) findViewById(R.id.zeroed);

		LinearLayout graphicFrame = (LinearLayout) findViewById(R.id.graphicFrame);
		LinearLayout canvasFrame = (LinearLayout) findViewById(R.id.canvasFrame);
		GLSurfaceView pcbGlview = (GLSurfaceView) findViewById(R.id.pcb_glview);
		GLSurfaceView roomGlview = (GLSurfaceView) findViewById(R.id.room_glview);
		Button dataSourcePopup = (Button) findViewById(R.id.data_source_popup);
		TextView numMsgsField = (TextView) findViewById(R.id.num_msgs);
		setSts(""); // clear status field
		clearConsole2();
		this.guiState = guiState;
		switch (guiState) {
		case DEVICE:
			pcbGlview.setVisibility(View.VISIBLE);
			roomGlview.setVisibility(View.GONE);
			graphicFrame.setVisibility(View.VISIBLE);
			canvasFrame.setVisibility(View.GONE);
			flEnable.setVisibility(View.GONE);
			if (this.dataSource==DataSource.REMOTE) {
				absolute.setVisibility(View.VISIBLE);				
				zeroCheckBox.setVisibility(View.VISIBLE);
			} else {
				zeroCheckBox.setVisibility(View.GONE);
				absolute.setVisibility(View.GONE);
				disableZeroFunction(zeroCheckBox);
			}
			numMsgsField.setVisibility(View.GONE);
			loggingWindow.setVisibility(View.GONE);
			dataSourcePopup.setVisibility(View.VISIBLE);
			configureDocOptions(splitScreen);
			statistics.show(false);
			configureConsoles(this.dataSource==DataSource.REMOTE);
			break;
		}
		updateSensors();
		statistics.configureStatsGathering(statsSampleSize, statsOneShot, resetStats);
		configureLpfOptions();
	}


	/**
     * Add the source options menu to the dialog
	 */
	public void showDataSelector(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.data_source_options, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}
	public void showSampleSizeSelector(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.stats_sample_size, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}
	public void showStatsModeSelector(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.stats_calc_mode, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		popup.show();
	}


	/**
	 * Update both local and remote sensors based upon the current state of the
	 * application. May also be called when returning from STOP mode.
	 */
	public void updateSensors() {
		switch (dataSource) {
		case LOCAL:
			localSensors.run();
			if (imu!=null) imu.stop(false);
			pcbRenderer.selectCube(0);
			break;
		case REMOTE:
			if (dualModeRequired()) {
				localSensors.run();
			} else {
				localSensors.stop();
			}
			if (imu!=null) imu.start();
			pcbRenderer.selectCube(0);
			break;
		case STOPPED:
		case FIXED:
			localSensors.stop();
			if (imu!=null) imu.stop(false);
			pcbRenderer.selectCube(0);
			break;
		}
		dataSelector.updateSelection();
		Statistics.configureStatsPage();
	}

	/**
	 * provides callback capability for menus residing on the "Source" selector.
	 * This works because we have used "implements OnMenuItemClickListener" in
	 * the definition of the A_FSL_Sensor_Demo class. The callback was
	 * previously set back in showDataSelector().
	 */
	public boolean onMenuItemClick(MenuItem item) {
		boolean sts = true;
		Button menuButton = (Button) findViewById(R.id.data_source_popup);

		setSts(""); // clear status field
		int itemId = item.getItemId();
		if (itemId == R.id.stopped) {
			dataSource = DataSource.STOPPED;
			algorithm = Algorithm.NONE;
			menuButton.setText("STOPPED");
		} else if (itemId == R.id.local_9_axis) {
			dataSource = DataSource.LOCAL;
			algorithm = Algorithm.NINE_AXIS;
			menuButton.setText("LOCAL 9 AXIS");
		} else if (itemId == R.id.remote_9_axis) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.NINE_AXIS;
			menuButton.setText("REMOTE 9 AXIS");
			if (imu!=null) imu.useQ9();
		} else if (itemId == R.id.fixed) {
			dataSource = DataSource.FIXED;
			algorithm = Algorithm.NONE;
			menuButton.setText("FIXED");
		} else if (itemId == R.id.stats_sample_size_10) {
			statsSampleSize=10;
		} else if (itemId == R.id.stats_sample_size_100) {
			statsSampleSize=100;
		} else if (itemId == R.id.stats_sample_size_500) {
			statsSampleSize=500;
		} else if (itemId == R.id.stats_sample_size_1000) {
			statsSampleSize=1000;
		} else if (itemId == R.id.stats_sample_size_5000) {
			statsSampleSize=5000;
		} else if (itemId == R.id.stats_one_shot) {
			statsOneShot=true;
			MyUtils.waitALittle(2000);
		} else if (itemId == R.id.stats_continuous) {
			statsOneShot=false;
		} else {
			sts = false;
		}
		updateSensors();
		statistics.setSpinnersVisible(dataSource==DataSource.LOCAL);
		configureApplicationViews(guiState, true);
		return (sts);
	}

	/**
	 * "Standard" Android onCreate function responsible for creating of the main
	 * GUI. This function can also initialize the Bluetooth connection.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean loadImu = true;
		Log.i(LOG_TAG, "calling onCreate()");
		myPrefs = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
		int orientationChoice = myPrefs.getInt("orientation", 0);
		if (orientationChoice == 1) {
			int currentOrientation = getResources().getConfiguration().orientation;
			//Log.i(LOG_TAG, "Current orientation = " + currentOrientation + " desired " + orientationChoice);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			if (currentOrientation != 2) {
				// this piece of code ensures that we initialize the IMU only the 2nd time
				// through onCreate for the case where we are in landscale mode.
				loadImu = false;
			}
		}
		if (savedInstanceState != null) {
			guiState = GuiState.values()[savedInstanceState.getInt(GUI_STATE)];
			dataSource = DataSource.values()[savedInstanceState
					.getInt(DATA_SOURCE)];
			algorithm = Algorithm.values()[savedInstanceState.getInt(ALGORITHM)];
			developmentBoard = DevelopmentBoard.values()[savedInstanceState.getInt(DEVBOARD)];
		}

		self = this;

		imuName = myPrefs.getString("btPrefix", getString(R.string.btPrefix));
		if (loadImu) imu = IMU.getInstance(this, imuName);

		myUtils = new MyUtils(this); // Register utility class
		dataLogger = new DataLogger(this, logHandler);
		dataLogger.setPriority(3); // slightly lower than default
		dataLogger.setDaemon(true); // will cause thread to be killed when the
									// main app thread is killed
		dataLogger.start();
		localSensors = new LocalSensors(this);
		dataSelector = new DataSelector(this);

		LOG_TAG = getString(R.string.log_tag);
		loadUrls();
		setContentView(R.layout.activity_main);
		ActionBar bar = getActionBar();
		bar.setDisplayShowTitleEnabled(false);
		// XML files set the background and Logo,
		// onCreateOptionsMenu() creates the options menu.

		loggingWindow = (ScrollView) findViewById(R.id.listingScrollView); // must
																				// be
																				// before
																				// 1st
																				// cls()
		numMsgsField = (TextView) findViewById(R.id.num_msgs);
		tv1 = (TextView) findViewById(R.id.console1);
		stsTextView = (TextView) findViewById(R.id.sts);
		console2 = (TextView) findViewById(R.id.console2);
		console3 = (TextView) findViewById(R.id.console3);
		console4 = (TextView) findViewById(R.id.console4);

		this.statistics = new Statistics(this, self); 
		this.statistics.onCreate();
		this.toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 33);

		canvasApplet = new MyCanvas(this);
		canvasFrame = (ViewGroup) findViewById(R.id.canvasFrame);
		canvasFrame.addView(canvasApplet);
		
		boolean enableJavascript = myPrefs.getBoolean("enable_javascript", true);
		LinearLayout webframe = (LinearLayout) findViewById(R.id.webframe);
		browser = new MyWebView(this, enableJavascript);
		WebViewClient webClient = new WebViewClient() {
			@Override
			public synchronized boolean shouldOverrideUrlLoading(WebView view,
					String url) {
				if (url.startsWith("http://youtu.be")) {
					startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
					return true;
				}
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.i(LOG_TAG, "Loaded: " + url);
				setDocSpinner(url);
			}
		};
		browser.setWebViewClient(webClient);
		webframe.addView(browser);

		// browser web client is set at the end of onCreate (needed docSpinner
		// for that function).
		// Adding the web client forces the browser to properly handle links
		// (like freescale.com)
		// on the external web. If removed, a standard browser session will be
		// started instead when
		// loading those pages. Plus the web client will keep the docSpinner
		// consistent

		Button backButton = (Button) findViewById(R.id.back_button);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public synchronized void onClick(View view) {
				if (browser.canGoBack())
					browser.goBack();
			}
		});

		Button forwardButton = (Button) findViewById(R.id.forward_button);
		forwardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public synchronized void onClick(View view) {
				if (browser.canGoForward())
					browser.goForward();
			}
		});

		Display display = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int screenRotation = display.getRotation();

		GLSurfaceView pcbGlview = (GLSurfaceView) findViewById(R.id.pcb_glview);
//		pcbGlview.setRenderer(new TextureCubeRenderer(this, screenRotation,
//				pcbSurfaces, pcbDimensions));

		
		pcbRenderer = new TextureCubeRenderer(this, screenRotation);
		pcbRenderer.addCube(pcbSurfaces, pcbDimensions, "Rev5 board")	;	
		pcbRenderer.addCube(k20d50mMultiSurfaces, freedomDimensions, "K20D50M with MULTI-sensor board")	;
		pcbRenderer.addCube(pcbSurfaces, pcbDimensions, "Rev5 board")	;	// This is a dummy for the space reserved for KL16Z

		pcbGlview.setRenderer(pcbRenderer);

		Button exitButton = (Button) findViewById(R.id.exit_button);
		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				localSensors.stop();
				finish();
				onStop();
				System.exit(0);
			}
		});

		CheckBox flEnable = (CheckBox) findViewById(R.id.fl_enable);
		flEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					fileLoggingEnabled = 1;
				} else {
					fileLoggingEnabled = 0;
				}
			}
		});
		CheckBox zeroCheckBox = (CheckBox) findViewById(R.id.zeroed);
		zeroCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					zeroPending = true;
					zeroed = false;
				} else {
					zeroPending = false;
					zeroed = false;
					Log.i(LOG_TAG, "Turning off display zero function.\n");
				}
			}
		});

		CheckBox lpfEnable = (CheckBox) findViewById(R.id.lpf_enable);
		final LinearLayout filterCoefWrapper = (LinearLayout) findViewById(R.id.filter_coef_wrapper);
		lpfEnable.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					filterCoefWrapper.setVisibility(View.VISIBLE);
					localSensors.enableLowPassFilters(true);
				} else {
					filterCoefWrapper.setVisibility(View.GONE);
					localSensors.enableLowPassFilters(false);
				}
			}
		});
		
		View v1 =  findViewById(R.id.absolute);
		CheckBox absolute = (CheckBox) v1;
		//CheckBox absolute = (CheckBox) findViewById(R.id.absolute);
		absolute.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					absoluteRemoteView = true;
				} else {
					absoluteRemoteView = false;
					CheckBox zeroCheckBox = (CheckBox) findViewById(R.id.zeroed);
					disableZeroFunction(zeroCheckBox);
				}
			}
		});

		SeekBar filterCoef = (SeekBar) findViewById(R.id.filter_coef);
		filterCoef.setMax(99);
		filterCoef.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int progress,
					boolean arg2) {
				filterCoefficient = (float) progress / 100.0f;
				setSts("  (filter coef = " + filterCoefficient + ")");
				localSensors.enableLowPassFilters(true);
				localSensors.setFilterCoef(filterCoefficient);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		ArrayAdapter<CharSequence> docAdapter = ArrayAdapter
				.createFromResource(this, R.array.doc_arrays,
						android.R.layout.simple_spinner_item);
		docAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		docSpinner = (Spinner) findViewById(R.id.doc_spinner);
		docSpinner.setAdapter(docAdapter);

		docSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.i(LOG_TAG, "docSpinner onItemSelected has been called " + arg2 + " " + arg3);
				currentDocPage = docSpinner.getSelectedItemPosition();
				if (currentDocPage < WWW_PAGE) {
					browser.loadUrl(urls[currentDocPage]);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Not needed for this application
			}
		});
	}

	/**
	 * Standard Android function which initializes Action Bar menus
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SET_PREFERENCES_MENU_ITEM, 0, "Preferences");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Supplies callback functionality for Android Action Bar menus.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case SET_PREFERENCES_MENU_ITEM:
			startActivity(new Intent(this, Settings.class));
			return true;
            default:
			configureApplicationViews(GuiState.DEVICE, false);
			return true;
		}
	}

	/**
	 * "Standard" Android life cycle function. In this case, we configure the
	 * visibility of GUI components and (if previously enabled in Preferences)
	 * start the Bluetooth connection.
	 */
	@Override
	public void onStart() {
		super.onStart();
		//dumpStates("calling onStart()");
		configureApplicationViews(guiState, true); // Override default set in
		// activity_main.xml
		// previously used imu_names include "Gen5" and "Motorola"
			if ((imu!=null) && (!imu.isListening())) {
				imu.startBluetooth();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == IMU.requestCode) {
			if (resultCode == RESULT_OK) {
				IMU.setBtSts(IMU.BtStatus.ENABLED, "Bluetooth enabled from onActivityResult().");
				imu.getPairedDevice();
				imu.initializeConnection();
			}
		}
	}
	/**
	 * "Standard" Android life cycle function. We did not really need to
	 * override it for this application, but doing so allows us to monitor
	 * application life cycle events in the debugger.
	 */
	@Override
	public void onRestart() {
		//dumpStates("calling onRestart()");
		super.onRestart();
	}

	/**
	 * "Standard" Android life cycle function. Saves primary application states.
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		//dumpStates("calling onSaveInstanceState()");
		savedInstanceState.putInt(GUI_STATE, guiState.ordinal());
		savedInstanceState.putInt(DATA_SOURCE, dataSource.ordinal());
		savedInstanceState.putInt(ALGORITHM, algorithm.ordinal());
		savedInstanceState.putInt(DEVBOARD, developmentBoard.ordinal());
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * "Standard" Android life cycle function. Disables Bluetooth if the
	 * preferences have been set for that option. Also disables local (to the
	 * Android device) sensors.
	 */
	@Override
	public void onStop() {
		//Log.v(LOG_TAG, "begin onStop() from A_FSL_Sensor_Demo");	
		//dumpStates("calling onStop().");
		localSensors.stop();
		if (imu!=null) imu.stop(false);
			// Threads are NOT stopped, as the settings shown keep things working after
			// accessing the preferences screen.
		super.onStop();
	}

	/**
	 * "Standard" Android life cycle function. Stops Bluetooth if not already
	 * done. Destroys the application instance.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (imu!=null) {
			imu.stop(true); // release BT threads 
		}
	}

	/**
	 * "Standard" Android life cycle function. We did not really need to
	 * override it for this application, but doing so allows us to monitor
	 * application life cycle events in the debugger.
	 */
	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * "Standard" Android life cycle function. The call to updateSensors() in
	 * this function is important to maintain continuity when returning from the
	 * Preferences screen.
	 */
	@Override
	public void onResume() {
		updateSensors();
		super.onResume();
	}

	/**
	 * "Standard" Android life cycle function. Restores previously saved
	 * application states.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			guiState = GuiState.values()[savedInstanceState.getInt(GUI_STATE)];
			dataSource = DataSource.values()[savedInstanceState.getInt(DATA_SOURCE)];
			algorithm = Algorithm.values()[savedInstanceState.getInt(ALGORITHM)];
			developmentBoard = DevelopmentBoard.values()[savedInstanceState.getInt(DEVBOARD)];
		}
	}
}
