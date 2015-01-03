/*
Copyright (c) 2013, 2014, Freescale Semiconductor, Inc.
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
	public WiGo wigo = null;                        // The WiGo class encapsulates WiGo boards from Avnet, plus the WiFi communications for same
	public MyCanvas canvasApplet = null;            // Used to demonstrate air mouse features
	public ViewGroup canvasFrame;                   
	public enum GuiState {                          // GuiState defines the different states that the user interface can take on
		LOGGING, DEVICE, PANORAMA, STATS, DOCS, CANVAS
	}

	public enum DataSource {                        // DataSource defines the various "sources" of sensor data
		STOPPED, LOCAL, REMOTE, WIGO, FIXED
	}

	public enum Algorithm {                         // These are the choices of algorithms that are supported by the embedded code
		NONE, ACC_ONLY, MAG_ONLY, GYRO_ONLY, ACC_MAG, ACC_GYRO, NINE_AXIS
	}
	
	public enum DevelopmentBoard {                  // List of supported development boards.  Note that KL16Z is a placeholder.
		REV5, KL25Z, K20D50M, WIGO, KL26Z, K64F, KL16Z, KL46Z, KL46Z_Standalone
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
	private final int VIEW_STATS_REPORT_MENU_ITEM = SET_PREFERENCES_MENU_ITEM + 1;
	private final int TOGGLE_MENU_ITEM = VIEW_STATS_REPORT_MENU_ITEM + 1;
	private final int TOGGLE_HEX_DISPLAY_ITEM = TOGGLE_MENU_ITEM + 1;
	private final int TOGGLE_LEGACY_DISPLAY_ITEM = TOGGLE_HEX_DISPLAY_ITEM + 1;
	private final int TOGGLE_CSV_DISPLAY_ITEM = TOGGLE_LEGACY_DISPLAY_ITEM + 1;
	private final int TOGGLE_VIRTUAL_GYRO_ENABLE_ITEM = TOGGLE_CSV_DISPLAY_ITEM + 1;
	private final int TOGGLE_RPC_ENABLE_ITEM = TOGGLE_VIRTUAL_GYRO_ENABLE_ITEM + 1;
	private final int CLS_MENU_ITEM = TOGGLE_RPC_ENABLE_ITEM + 1;
	private final int DUMP_CONFIG_MENU_ITEM = CLS_MENU_ITEM + 1;
	private final int ABOUT_MENU_ITEM = DUMP_CONFIG_MENU_ITEM + 1;
	private final int HELP_MENU_ITEM = ABOUT_MENU_ITEM + 1;
	private final int FEEDBACK_MENU_ITEM = HELP_MENU_ITEM + 1;
	private final int UNIT_TEST_MENU_ITEM = FEEDBACK_MENU_ITEM + 1;
	
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
	final int wigoSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.wigo_top,
			R.drawable.wigo_bottom };
	final int kl25zMultiSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.sensor_shield_top,
			R.drawable.kl25z_bottom };
	final int kl26zMultiSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.sensor_shield_top,
			R.drawable.kl26z_bottom };
	final int kl46zMultiSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.sensor_shield_top,
			R.drawable.kl46z_bottom };
	final int kl46zSingleSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.kl46z_top,
			R.drawable.kl46z_bottom };
	final int k64fMultiSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.sensor_shield_top,
			R.drawable.k64f_bottom };
	final int k20d50mMultiSurfaces[] = { R.drawable.wigo_sides, R.drawable.wigo_sides,
			R.drawable.wigo_sides, R.drawable.wigo_sides, R.drawable.sensor_shield_top,
			R.drawable.k20d50m_bottom };
	
	// define standard dimensions for the graphics files above
	final float roomDimensions[] = { 4.8f, 4.8f, 1.456f, 0.0f }; // width, length,
															// height and Z
															// offset. Room
															// dimensions are
															// twice these
															// numbers
	final float pcbDimensions[] = { 0.96f, 1.5f, 0.05f, -2.5f }; 
	final float freedomDimensions[] = { 1.05f, 1.55f, 0.05f, -2.5f }; 
	final float wigoDimensions[] = { 1.05f, 1.875f, 0.05f, -2.5f }; 

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
		urls[0] = "file:///android_asset/index.html";	// Overview
		urls[1] = "file:///android_asset/OrientationPart1.html"; 	// Rotations and Orientation: Part 1
		urls[2] = "file:///android_asset/OrientationPart2.html";	// Rotations and Orientation: Part 2
		urls[3] = "file:///android_asset/algorithms.html";	// Algorithms
		urls[4] = "file:///android_asset/statistics.html";	// Gathering Statistics
		urls[5] = "file:///android_asset/hardware.html";	// Hardware & Software Requirements
		urls[6] = "file:///android_asset/packet_structure.html";	// Bluetooth Packet Structure
		urls[7] = "file:///android_asset/logging.html";		// Data Logger
		urls[8] = "file:///android_asset/device.html";		// Device View
		urls[9] = "file:///android_asset/panorama.html";	// Panorama View
		urls[10] = "file:///android_asset/canvas.html";			// Canvas View
		urls[11] = "file:///android_asset/credits.html";		// Credits
		urls[12] = "file:///android_asset/preferences.html";	// Setting Preferences
		urls[13] = "file:///android_asset/change_log.html";		// Change Log
		urls[14] = "http://www.freescale.com/sensors";			// Freescale Semiconductor Web Site
		urls[15] = "http://www.google.com";						// General WWW
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
		boolean sts = ((dataSource == DataSource.REMOTE)||(dataSource == DataSource.WIGO)) 
				&& (guiState==GuiState.DEVICE) && (absoluteRemoteView == false);
		return(sts);
	}
	public boolean absoluteModeRequired() {
		boolean sts = ((dataSource == DataSource.REMOTE)||(dataSource == DataSource.WIGO)) 
				&& (guiState==GuiState.CANVAS) && (absoluteRemoteView == true);
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
		Message msg = Message.obtain();
		if (always) {
			msg.what = 1; // unconditional
		} else {
			msg.what = 2; // only posts if log window is visible
		}
		msg.obj = str;
		msg.arg1 = fileLoggingEnabled; // 0=no, 1=yes
		if (dataLogger!=null) {
			dataLogger.getHandler().sendMessage(msg);
		} else {
		   Log.e(LOG_TAG, "Null datalogger pointer found in write() function.\n");
		}
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
	static public Handler alertHandler = new Handler() {
		// Messages to this handler are sent by class WiGo.
		public void handleMessage(Message msg) {
			if (msg.what == 1) { // the Logging Window needs an update
				MyUtils.alert("External WiFi not visible!", msg.obj.toString()); 
			}
		}
	};

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
		String msg = "Xtrinsic Sensor Fusion Demo\n"
				+ "Copyright 2013 by Freescale Semiconductor\n"
				+ "Program Version = " + appVersion() + "\n\n";
		return (msg);
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
	 * Function uses Android email intent to share the contents of the current
	 * output file.
	 * <P>
	 * The user must have an Android email client that supports the standard
	 * "intent" mechanism installed in order for this feature to work properly.
	 * <P>
	 * The "to" field of the email is pre-filled based on the Preferences
	 * "my_email" field.
	 */
	public void shareLogFile() {
		File f = MyUtils.getFile(DataLogger.fileName);
		if (f.exists()) {
			write(true, "\n\nThe absolute pathname for the log file is " + f.getAbsolutePath() + "\n");
			Uri uri = Uri.fromFile(f);
			String prefName = "A_FSL_Sensor_Demo";
			SharedPreferences myPrefs = getSharedPreferences(prefName,
					Activity.MODE_PRIVATE);
			String feedbackEmailAddr  = A_FSL_Sensor_Demo.self.getString(R.string.feedbackEmailAddr);
			String[] recipients = new String[] { myPrefs.getString("my_email", feedbackEmailAddr) };

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT,
					"Freescale Sensor Fusion Log File");
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			intent.putExtra(Intent.EXTRA_EMAIL, recipients);
			intent.putExtra(android.content.Intent.EXTRA_TEXT,
					"Created by Freescale");
			startActivity(Intent.createChooser(intent, "Email:"));
		} else {
			String msg = "Output file = " + f.getAbsolutePath()
					+ " does not exist.";
			MyUtils.alert("File does not exist", msg);
		}
	}
	public void shareStatsReport() {
		if (dataSelector.statsReady()) {
			HtmlGenerator html = dataSelector.dumpStatsAsHtml();
			if (html.ok) {
				Uri uri = html.uri;
				String prefName = "A_FSL_Sensor_Demo";
				SharedPreferences myPrefs = getSharedPreferences(prefName,
						Activity.MODE_PRIVATE);
				String feedbackEmailAddr  = A_FSL_Sensor_Demo.self.getString(R.string.feedbackEmailAddr);
				String[] recipients = new String[] { myPrefs.getString("my_email", feedbackEmailAddr) };

				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/html");
				intent.putExtra(Intent.EXTRA_SUBJECT,
						"Freescale Sensor Fusion Statistics File");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.putExtra(Intent.EXTRA_EMAIL, recipients);
				intent.putExtra(android.content.Intent.EXTRA_TEXT,
						"Created by Freescale");
				startActivity(Intent.createChooser(intent, "Email:"));
			} else {
				String msg = "HTML statistics file = " + html.fullFileName
						+ " could not be created.";
				MyUtils.alert("File does not exist", msg);
			}
		} else {
			String msg = "You must be displaying the Statistic page and sufficient time must have " +
					"elapsed to collect required samples before calling this function.";
			MyUtils.alert("Too soon!", msg);
		}
	}
	public void viewStatsReport() {
		if (dataSelector.statsReady()) {
			HtmlGenerator html = dataSelector.dumpStatsAsHtml();
			if (html.ok) {
				Uri uri = html.uri;
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, "text/html");
				startActivity(Intent.createChooser(intent, "View HTML"));
			} else {
				String msg = "HTML statistics file = " + html.fullFileName
						+ " could not be created.";
				MyUtils.alert("File does not exist", msg);
			}
		} else {
			String msg = "You must be displaying the Statistic page and sufficient time must have " +
					"elapsed to collect required samples before calling this function.";
			MyUtils.alert("Too soon!", msg);
		}
	}

	/**
	 * Function uses Android email intent to share the a dump of the graphic
	 * screen.
	 * <P>
	 * The user must have an Android email client that supports the standard
	 * "intent" mechanism installed in order for this feature to work properly.
	 * <P>
	 * The "to" field of the email is pre-filled based on the Preferences
	 * "my_email" field.
	 * 
	 * @param uri
	 *            a URI pointing to the previously created graphics file
	 * @param description
	 *            a string describing the item to be shared (used for subject
	 *            line of the email).
	 */
	public void sharePng(Uri uri, String description) {
		String prefName = "A_FSL_Sensor_Demo";
		SharedPreferences myPrefs = getSharedPreferences(prefName,
				Activity.MODE_PRIVATE);
		String feedbackEmailAddr  = A_FSL_Sensor_Demo.self.getString(R.string.feedbackEmailAddr);
		String[] recipients = new String[] { myPrefs.getString("my_email", feedbackEmailAddr) };

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/png");
		intent.putExtra(Intent.EXTRA_SUBJECT, description);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.putExtra(Intent.EXTRA_EMAIL, recipients);
		intent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Created by Freescale");
		startActivity(Intent.createChooser(intent, "Email:"));
	}

	/**
	 * Function uses Android email intent to share the a dump of the log window.
	 * <P>
	 * The user must have an Android email client that supports the standard
	 * "intent" mechanism installed in order for this feature to work properly.
	 * <P>
	 * The "to" field of the email is pre-filled based on the Preferences
	 * "my_email" field.
	 * 
	 * @param description
	 *            a string describing the item to be shared (used for subject
	 *            line of the email).
	 */
	public void shareTranscript(String description) {
		// Long timestamp = System.currentTimeMillis();
		String prefName = "A_FSL_Sensor_Demo";
		SharedPreferences myPrefs = getSharedPreferences(prefName,
				Activity.MODE_PRIVATE);
		String feedbackEmailAddr  = A_FSL_Sensor_Demo.self.getString(R.string.feedbackEmailAddr);
		String[] recipients = new String[] { myPrefs.getString("my_email", feedbackEmailAddr) };

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, description);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, tv1.getText());
		if (!recipients[0].equals("")) {
			// this is conditional because otherwise we get a "," in the email
			// "to" field when
			// there is no predefined email field in the preferences
			intent.putExtra(Intent.EXTRA_EMAIL, recipients);
		}
		startActivity(Intent.createChooser(intent, "Email:"));
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
		boolean b1 = (dataSource==DataSource.LOCAL) && (algorithm == Algorithm.ACC_MAG);
		boolean b2 = (dataSource==DataSource.LOCAL) && (algorithm == Algorithm.ACC_ONLY);
		boolean b3 = (algorithm == Algorithm.ACC_ONLY) && (dataSource == DataSource.WIGO);
		boolean lpfAllowed = (guiState != GuiState.DOCS) && (b1 || b2 || b3);
		if (lpfAllowed) {
			lpfEnable.setVisibility(View.VISIBLE);
			if (lpfEnable.isChecked()) {
				filterCoefWrapper.setVisibility(View.VISIBLE);
				localSensors.enableLowPassFilters(true);
				wigo.enableLowPassFilters(true);
			} else {
				filterCoefWrapper.setVisibility(View.GONE);
				localSensors.enableLowPassFilters(false);
				wigo.enableLowPassFilters(false);
			}
		} else {
			lpfEnable.setVisibility(View.GONE);
			filterCoefWrapper.setVisibility(View.GONE);
			localSensors.enableLowPassFilters(false);
			wigo.enableLowPassFilters(false);
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
		case LOGGING:
			pcbGlview.setVisibility(View.GONE);
			roomGlview.setVisibility(View.GONE);
			graphicFrame.setVisibility(View.GONE);
			canvasFrame.setVisibility(View.GONE);
			absolute.setVisibility(View.GONE);
			flEnable.setVisibility(View.VISIBLE);
			zeroCheckBox.setVisibility(View.GONE);
			disableZeroFunction(zeroCheckBox);
			numMsgsField.setVisibility(View.VISIBLE);
			loggingWindow.setVisibility(View.VISIBLE);
			dataSourcePopup.setVisibility(View.VISIBLE);
			statistics.show(false);
			configureDocOptions(splitScreen);
			if (localSensors != null) {
				localSensors.clear(); // zero out acc, mag & gyro settings
			}
			configureConsoles(false);
			break;
		case DEVICE:
			pcbGlview.setVisibility(View.VISIBLE);
			roomGlview.setVisibility(View.GONE);
			graphicFrame.setVisibility(View.VISIBLE);
			canvasFrame.setVisibility(View.GONE);
			flEnable.setVisibility(View.GONE);
			if ((this.dataSource==DataSource.REMOTE)||(this.dataSource==DataSource.WIGO)) {
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
		case PANORAMA:
			absolute.setVisibility(View.GONE);
			zeroCheckBox.setVisibility(View.GONE);
			disableZeroFunction(zeroCheckBox);
			pcbGlview.setVisibility(View.GONE);
			roomGlview.setVisibility(View.VISIBLE);
			graphicFrame.setVisibility(View.VISIBLE);
			canvasFrame.setVisibility(View.GONE);
			flEnable.setVisibility(View.GONE);
			numMsgsField.setVisibility(View.GONE);
			loggingWindow.setVisibility(View.GONE);
			dataSourcePopup.setVisibility(View.VISIBLE);
			configureDocOptions(splitScreen);
			statistics.show(false);
			configureConsoles(false);
			break;
		case STATS:
			absolute.setVisibility(View.GONE);
			zeroCheckBox.setVisibility(View.GONE);
			disableZeroFunction(zeroCheckBox);
			pcbGlview.setVisibility(View.GONE);
			roomGlview.setVisibility(View.GONE);
			graphicFrame.setVisibility(View.GONE);
			canvasFrame.setVisibility(View.GONE);
			flEnable.setVisibility(View.GONE);
			numMsgsField.setVisibility(View.GONE);
			loggingWindow.setVisibility(View.GONE);
			dataSourcePopup.setVisibility(View.VISIBLE);
			configureDocOptions(splitScreen);
			Statistics.configureStatsPage();
			statistics.show(true);
			configureConsoles(false);
			break;
		case CANVAS:
			absolute.setVisibility(View.VISIBLE);
			zeroCheckBox.setVisibility(View.GONE);
			disableZeroFunction(zeroCheckBox);
			pcbGlview.setVisibility(View.GONE);
			roomGlview.setVisibility(View.GONE);
			graphicFrame.setVisibility(View.GONE);
			canvasFrame.setVisibility(View.VISIBLE);
			flEnable.setVisibility(View.GONE);
			numMsgsField.setVisibility(View.GONE);
			loggingWindow.setVisibility(View.GONE);
			dataSourcePopup.setVisibility(View.VISIBLE);
			configureDocOptions(splitScreen);
			statistics.show(false);
			configureConsoles(false);
			canvasApplet.center();
			break;
		case DOCS:
			absolute.setVisibility(View.GONE);
			zeroCheckBox.setVisibility(View.GONE);
			disableZeroFunction(zeroCheckBox);
			pcbGlview.setVisibility(View.GONE);
			roomGlview.setVisibility(View.GONE);
			flEnable.setVisibility(View.GONE);
			numMsgsField.setVisibility(View.GONE);
			graphicFrame.setVisibility(View.GONE);
			canvasFrame.setVisibility(View.GONE);
			dataSourcePopup.setVisibility(View.GONE);
			loggingWindow.setVisibility(View.GONE);
			configureDocOptions(true);
			statistics.show(false);
			configureConsoles(false);
			break;
		}
		updateSensors();
		statistics.configureStatsGathering(statsSampleSize, statsOneShot, resetStats);
		configureLpfOptions();
	}


	/**
	 * creates and configures the popup for the "Source" button. Only options
	 * supported by the current hardware are enabled
	 * <P>
	 * This callback is specified in activity_main.xml on the data_source_popup
	 * button.
	 * 
	 * @param v
	 *            a pointer to the object associated with this callback (in this
	 *            case, the "Source" button)
	 */
	public void showDataSelector(View v) {
		PopupMenu popup = new PopupMenu(this, v);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.data_source_options, popup.getMenu());
		popup.setOnMenuItemClickListener(this);
		if (!localSensors.hasLocalGyro()) {
			popup.getMenu().getItem(3).setEnabled(false); // disable 9-axis
															// local option on
															// menu
		}
		if ((imu!=null) && (!imu.isReady())) {
			popup.getMenu().getItem(4).setEnabled(false); // disable IMU options
			popup.getMenu().getItem(5).setEnabled(false);
			popup.getMenu().getItem(6).setEnabled(false);
			popup.getMenu().getItem(7).setEnabled(false);
			popup.getMenu().getItem(8).setEnabled(false);
			popup.getMenu().getItem(9).setEnabled(false);
		}
		if ((imu!=null) && (developmentBoard == DevelopmentBoard.KL46Z_Standalone)) {
			popup.getMenu().getItem(6).setEnabled(false); // disable non-gyro options
			popup.getMenu().getItem(8).setEnabled(false);
			popup.getMenu().getItem(9).setEnabled(false);
		}
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
			wigo.stop();
			pcbRenderer.selectCube(0);
			break;
		case REMOTE:
			if (dualModeRequired()) {
				localSensors.run();
			} else {
				localSensors.stop();
			}
			if (imu!=null) imu.start();
			wigo.stop();
			pcbRenderer.selectCube(0);
			break;
		case WIGO:
			if (dualModeRequired()) {
				localSensors.run();
			} else {
				localSensors.stop();
			}
			if (imu!=null) imu.stop(false);
			wigo.run();
			pcbRenderer.selectCube(3);
			break;
		case STOPPED:
		case FIXED:
			localSensors.stop();
			if (imu!=null) imu.stop(false);
			wigo.stop();
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
			menuButton.setText("Source = STOPPED");
		} else if (itemId == R.id.local_accel) {
			dataSource = DataSource.LOCAL;
			algorithm = Algorithm.ACC_ONLY;
			menuButton.setText("Source = Local Accel");
		} else if (itemId == R.id.local_mag_accel) {
			dataSource = DataSource.LOCAL;
			algorithm = Algorithm.ACC_MAG;
			menuButton.setText("Source = Local Mag/Accel");
		} else if (itemId == R.id.local_9_axis) {
			dataSource = DataSource.LOCAL;
			algorithm = Algorithm.NINE_AXIS;
			menuButton.setText("Source = Local 9-Axis");
		} else if (itemId == R.id.remote_acc) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.ACC_ONLY;
			menuButton.setText("Source = Remote Accel");
			if (imu!=null) imu.useQ3();
		} else if (itemId == R.id.remote_mag) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.MAG_ONLY;
			menuButton.setText("Source = Remote Mag (2D algorithm)");
			if (imu!=null) imu.useQ3M();
		} else if (itemId == R.id.remote_gyro) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.GYRO_ONLY;
			menuButton.setText("Source = Remote Gyro");
			if (imu!=null) imu.useQ3G();
		} else if (itemId == R.id.remote_accel_mag) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.ACC_MAG;
			menuButton.setText("Source = Remote Accel/Mag");
			if (imu!=null) imu.useQ6MA();
		} else if (itemId == R.id.remote_accel_gyro) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.ACC_GYRO;
			menuButton.setText("Source = Remote Accel/Gyro");
			if (imu!=null) imu.useQ6AG();
		} else if (itemId == R.id.remote_9_axis) {
			dataSource = DataSource.REMOTE;
			algorithm = Algorithm.NINE_AXIS;
			menuButton.setText("Source = Remote 9-Axis");
			if (imu!=null) imu.useQ9();
		} else if (itemId == R.id.wigo_acc) {
			dataSource = DataSource.WIGO;
			algorithm = Algorithm.ACC_ONLY;
			menuButton.setText("Source = WiGo Accel");
		} else if (itemId == R.id.wigo_mag_accel) {
			dataSource = DataSource.WIGO;
			algorithm = Algorithm.ACC_MAG;
			menuButton.setText("Source = WiGo Mag/Accel");
		} else if (itemId == R.id.fixed) {
			dataSource = DataSource.FIXED;
			algorithm = Algorithm.NONE;
			menuButton.setText("Source = Fixed Rotation");
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
			MyUtils.beep();
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
		//dumpStates("calling onCreate()"); // Just a bunch of Log.i's
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
		wigo = new WiGo(this);
		
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
		GLSurfaceView roomGlview = (GLSurfaceView) findViewById(R.id.room_glview);
//		pcbGlview.setRenderer(new TextureCubeRenderer(this, screenRotation,
//				pcbSurfaces, pcbDimensions));
		
		pcbRenderer = new TextureCubeRenderer(this, screenRotation);
		pcbRenderer.addCube(pcbSurfaces, pcbDimensions, "Rev5 board")	;	
		pcbRenderer.addCube(kl25zMultiSurfaces, freedomDimensions, "KL25Z with MULTI-sensor board")	;	
		pcbRenderer.addCube(k20d50mMultiSurfaces, freedomDimensions, "K20D50M with MULTI-sensor board")	;	
		pcbRenderer.addCube(wigoSurfaces, wigoDimensions, "WiGo board")	;	
		pcbRenderer.addCube(kl26zMultiSurfaces, freedomDimensions, "KL26Z with MULTI-sensor board")	;	
		pcbRenderer.addCube(k64fMultiSurfaces, freedomDimensions, "K64F with MULTI-sensor board")	;	
		pcbRenderer.addCube(pcbSurfaces, pcbDimensions, "Rev5 board")	;	// This is a dummy for the space reserved for KL16Z
		pcbRenderer.addCube(kl46zMultiSurfaces, freedomDimensions, "KL46Z with MULTI-sensor board")	;	
		pcbRenderer.addCube(kl46zSingleSurfaces, freedomDimensions, "Standalone KL46Z board")	;	
		
		pcbGlview.setRenderer(pcbRenderer);

		TextureCubeRenderer roomRenderer = new TextureCubeRenderer(this, screenRotation);
		roomRenderer.addCube(roomSurfaces, roomDimensions, "Fusion Board Room")	;	
		roomGlview.setRenderer(roomRenderer);

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
					wigo.enableLowPassFilters(true);
				} else {
					filterCoefWrapper.setVisibility(View.GONE);
					localSensors.enableLowPassFilters(false);
					wigo.enableLowPassFilters(false);
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
				wigo.enableLowPassFilters(true);
				wigo.setFilterCoef(filterCoefficient);
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
		menu.add(0, VIEW_STATS_REPORT_MENU_ITEM, 0, "View Statistics Report");
		menu.add(0, TOGGLE_MENU_ITEM, 0, "Toggle Split Screen");
		
		hexDumpMenuItem = menu.add(0, TOGGLE_HEX_DISPLAY_ITEM, 0, "Toggle Hex Dump (Remote/WiGo only)");
		legacyDumpMenuItem = menu.add(0, TOGGLE_LEGACY_DISPLAY_ITEM, 0, "Toggle Legacy Dump");
		csvDumpMenuItem = menu.add(0, TOGGLE_CSV_DISPLAY_ITEM, 0, "Toggle CSV Dump (Remote/WiGo only)");
		changeHexMenuLabel(hexDumpEnabled);
		changeLegacyMenuLabel(legacyDumpEnabled);
		changeCsvMenuLabel(csvDumpEnabled);
		
		menu.add(0, CLS_MENU_ITEM, 0, "Clear Log Window");
		menu.add(0, DUMP_CONFIG_MENU_ITEM, 0, "Dump Android Configuration");
		menu.add(0, ABOUT_MENU_ITEM, 0, "About");
		menu.add(0, HELP_MENU_ITEM, 0, "Help");
		menu.add(0, FEEDBACK_MENU_ITEM, 0, "Feedback");
		/*
		menu.add(0, UNIT_TEST_MENU_ITEM, 0, "Unit Test A");
		menu.add(0, UNIT_TEST_MENU_ITEM+1, 0, "Unit Test D");
		menu.add(0, UNIT_TEST_MENU_ITEM+2, 0, "Unit Test Q");
		menu.add(0, UNIT_TEST_MENU_ITEM+3, 0, "Unit Test R");
		*/
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.navigator, menu);
		return super.onCreateOptionsMenu(menu);
	}
	void changeLegacyMenuLabel(boolean enabled) {
		if (enabled) {
			legacyDumpMenuItem.setTitle("Disable legacy dump");
		} else {
			legacyDumpMenuItem.setTitle("Enable legacy dump");			
		}
	}
	void changeHexMenuLabel(boolean enabled) {
		if (enabled) {
			hexDumpMenuItem.setTitle("Disable hex dump (Remote/Wi-Go only)");
		} else {
			hexDumpMenuItem.setTitle("Enable hex dump (Remote/Wi-Go only)");			
		}
	}
	void changeCsvMenuLabel(boolean enabled) {
		if (enabled) {
			csvDumpMenuItem.setTitle("Disable csv dump (Remote/Wi-Go only)");
		} else {
			csvDumpMenuItem.setTitle("Enable csv dump (Remote/Wi-Go only)");			
		}
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
		case VIEW_STATS_REPORT_MENU_ITEM:
			viewStatsReport();
			return true;
		case TOGGLE_MENU_ITEM:
			splitScreen = !splitScreen;
			configureApplicationViews(guiState, false);
			return true;
		case TOGGLE_HEX_DISPLAY_ITEM:
			hexDumpEnabled = ! hexDumpEnabled;
			changeHexMenuLabel(hexDumpEnabled);
			return true;
		case TOGGLE_LEGACY_DISPLAY_ITEM:
			legacyDumpEnabled = ! legacyDumpEnabled;
			changeLegacyMenuLabel(legacyDumpEnabled);
			return true;
		case TOGGLE_CSV_DISPLAY_ITEM:
			csvDumpEnabled = ! csvDumpEnabled;
			changeCsvMenuLabel(csvDumpEnabled);
			return true;
		case CLS_MENU_ITEM:
			cls();
			return true;
		case DUMP_CONFIG_MENU_ITEM:
			localSensors.dump();
			return true;
		case ABOUT_MENU_ITEM: // still need to add ABOUT screen
			String title = "About";
			String versionName = "";
			try {
				versionName = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionName;
			} catch (Throwable t) {
				versionName = "unknown";
			}
			String msg = "Copyright 2013 by Freescale Semiconductor\nProgram Version = "
					+ versionName
					+ "\n"
					+ "This sensor fusion demo is provided as is for the convenience of the sensor design "
					+ "community and Freescale does not warrant the accuracy of the results or their "
					+ "suitability for any specific purpose. Users are responsible for validating the "
					+ "accuracy and appropriateness of the results before using them in their designs and analyses.";
			MyUtils.popupAlert(title, msg);
			break;
		case HELP_MENU_ITEM:
			currentDocPage = 0;
			configureApplicationViews(GuiState.DOCS, false);
			return true;
		case FEEDBACK_MENU_ITEM: // This is just a convenient spot to add unit
									// tests during development
			configureApplicationViews(GuiState.LOGGING, false);
			cls();
			write(true, "-------------------------\n");
			write(true, " I like:\n");
			write(true, " I don't like:\n");
			write(true, " Could you add:\n");
			write(true, "-------------------------\n");
			write(true, "My Android configuration is:\n\n");
			localSensors.dump();
			// now wait for the print queue to clear
			Handler h = new Handler();
			h.postDelayed(new Runnable() {
				public void run() {
					shareTranscript("Feedback on Freescale Sensor Fusion App for Android");
				}
			}, 2 * DataLogger.refreshInterval);
			return true;
		case UNIT_TEST_MENU_ITEM:
			if (imu!=null) imu.sendTo("A   ");
			MyUtils.beep();
			return true;
		case UNIT_TEST_MENU_ITEM+1:
			if (imu!=null) imu.sendTo("D   ");
			MyUtils.beep();
			return true;
		case UNIT_TEST_MENU_ITEM+2:
			if (imu!=null) imu.sendTo("Q   ");
			MyUtils.beep();
			return true;
		case UNIT_TEST_MENU_ITEM+3:
			if (imu!=null) imu.sendTo("R   ");
			MyUtils.beep();
			return true;
		case R.id.logger:
			configureApplicationViews(GuiState.LOGGING, false);
			return true;
		case R.id.canvas:
			configureApplicationViews(GuiState.CANVAS, false);
			return true;
		case R.id.dev:
			configureApplicationViews(GuiState.DEVICE, false);
			return true;
		case R.id.pan:
			// Panorama View
			configureApplicationViews(GuiState.PANORAMA, false);
			return true;
		case R.id.stats:
			// Statistics Screen
			configureApplicationViews(GuiState.STATS, true);
			return true;
		case R.id.doc:
			// Documentation View
			configureApplicationViews(GuiState.DOCS, false);
			return true;
		case R.id.share_screen:
			// Share
			dumpScreen = true; // Flag to request render to dump a PNG file of
								// the graphic screen
			return true;
		case R.id.share_transcript:
			// Share Transcript
			shareTranscript("Freescale demo transcript");
			return true;
		case R.id.share_output_file:
			// Share output file
			shareLogFile();
			return true;
		case R.id.share_stats:
			// Share output file
			shareStatsReport();
			return true;
		}
		return false;
	}
	void unitTest() {
		write(true, "Unit test currently tests NOTHING\n");
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
		boolean remote_enable = myPrefs.getBoolean("remote_enable", false);
		if (remote_enable) {
			if ((imu!=null) && (!imu.isListening())) {
				imu.startBluetooth();
			}
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
		wigo.stop();
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
		//Log.v(LOG_TAG, "begin onDestroy() from A_FSL_Sensor_Demo");	
		//dumpStates("calling onDestroy()");
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
		//dumpStates("calling onPause()");
		super.onPause();
	}

	/**
	 * "Standard" Android life cycle function. The call to updateSensors() in
	 * this function is important to maintain continuity when returning from the
	 * Preferences screen.
	 */
	@Override
	public void onResume() {
		//dumpStates("calling onResume(), updating sensors.");
		updateSensors(); // sensors are disabled onStop(). This gets them back.
		super.onResume();
	}

	/**
	 * "Standard" Android life cycle function. Restores previously saved
	 * application states.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		//dumpStates("calling onRestoreInstanceState()");

		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			guiState = GuiState.values()[savedInstanceState.getInt(GUI_STATE)];
			dataSource = DataSource.values()[savedInstanceState.getInt(DATA_SOURCE)];
			algorithm = Algorithm.values()[savedInstanceState.getInt(ALGORITHM)];
			developmentBoard = DevelopmentBoard.values()[savedInstanceState.getInt(DEVBOARD)];
		}
	}

}
