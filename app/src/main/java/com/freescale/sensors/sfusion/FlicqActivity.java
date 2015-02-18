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

package com.freescale.sensors.sfusion;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class FlicqActivity extends Activity implements OnMenuItemClickListener {
    static public String LOG_TAG = null;            // This string is used to uniquely identify Android log messages
    public LocalSensors localSensors = null;        // Pointer to object for managing input from sensors local to your Android device
    public DataSelector dataSelector = null;        // Pointer to object which selects one of several different sensor sources
    public FlicqUtils myUtils = null;                  // This class is used as a "home" for misc. utility functions
    public FlicqDevice flicqDevice = null;                          // The FlicqDevice class encapsulates sensor boards which communicate via Bluetooth

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

    static public TextView tv1 = null;               // TextView variables are global pointers to fields in the GUI which can be
    static private TextView numMsgsField = null;

    private boolean absoluteRemoteView = false;     // set to true to NOT take into account Android device view when using remote FlicqDevice

    static public boolean hexDumpEnabled = false;   // set to true to enable hex display of BT input

    public boolean zeroPending = false;             // handshaking control for "Zero" function in the device view
    public boolean zeroed = false;

    // Options Menu definitions
    private final int SET_PREFERENCES_MENU_ITEM = Menu.FIRST;

    public final String PREF_NAME = "FlicqActivity";  // String for retrieving shared preferences
    public SharedPreferences myPrefs;                     // Structure for preferences
    private float filterCoefficient = 0;                  // Used to control low pass filtering

    static public FlicqActivity self = null;
    // The self pointer is used in the body of one of the
    // listener functions, where "this" points to the
    // listener, not the emo itself.

    public String imuName;
    // class variables, but need this early
    // in onCreate().

    // Definition for graphics files used to render various 3D displays
    public TextureCubeRenderer pcbRenderer = null;
    final int pcbSurfaces[] = {R.drawable.pcb_sides, R.drawable.pcb_sides,
            R.drawable.pcb_sides, R.drawable.pcb_sides, R.drawable.rev5_pcb_top,
            R.drawable.rev5_pcb_bottom};

    final float pcbDimensions[] = {0.96f, 1.5f, 0.05f, -2.5f};

    public boolean dualModeRequired() {
        return ((dataSource == DataSource.REMOTE))
                && (guiState == GuiState.DEVICE) && (!absoluteRemoteView);
    }

    static private class MyHandler extends Handler {
        @SuppressWarnings("unused")
        private final WeakReference<FlicqActivity> myActivity;

        public MyHandler(FlicqActivity activity) {
            myActivity = new WeakReference<FlicqActivity>(activity);
            // this construct is used to help the JAVA garbage collector.
            // making the handler static and using a weak reference to the
            // activity is supposed to make it easier to recycle objects
            // that are no longer needed.
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) { // the Logging Window needs an update
                tv1.setText(msg.obj.toString());
            }
            long numLogged = 1024 * msg.arg1 + msg.arg2;
        }
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

    /**
     * top level function used to configure visibility of various GUI
     * components.
     * <p/>
     * The application is state-based, and what is visible or not is a function
     * of those states.
     *
     * @param gui_state the state of the application which we are about to configure.
     */
    public void configureApplicationViews(GuiState guiState, boolean resetStats) {
        CheckBox absolute = (CheckBox) findViewById(R.id.absolute);
        CheckBox zeroCheckBox = (CheckBox) findViewById(R.id.zeroed);

        LinearLayout graphicFrame = (LinearLayout) findViewById(R.id.graphicFrame);
        GLSurfaceView pcbGlview = (GLSurfaceView) findViewById(R.id.pcb_glview);
        Button dataSourcePopup = (Button) findViewById(R.id.data_source_popup);
        this.guiState = guiState;
        switch (guiState) {
            case DEVICE:
                pcbGlview.setVisibility(View.VISIBLE);
                graphicFrame.setVisibility(View.VISIBLE);
                if (this.dataSource == DataSource.REMOTE) {
                    absolute.setVisibility(View.VISIBLE);
                    zeroCheckBox.setVisibility(View.VISIBLE);
                } else {
                    zeroCheckBox.setVisibility(View.GONE);
                    absolute.setVisibility(View.GONE);
                    disableZeroFunction(zeroCheckBox);
                }
                dataSourcePopup.setVisibility(View.VISIBLE);
                break;
        }
        updateSensors();
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

    /**
     * Update both local and remote sensors based upon the current state of the
     * application. May also be called when returning from STOP mode.
     */
    public void updateSensors() {
        switch (dataSource) {
            case LOCAL:
                localSensors.run();
                if (flicqDevice != null) flicqDevice.stop(false);
                break;
            case REMOTE:
                localSensors.stop();
                if (flicqDevice != null) {
                    flicqDevice.start();
                }
                break;
            case STOPPED:
            case FIXED:
                localSensors.stop();
                if (flicqDevice != null)
                    flicqDevice.stop(false);
                break;
        }
        dataSelector.updateSelection();
    }

    /**
     * provides callback capability for menus residing on the "Source" selector.
     * This works because we have used "implements OnMenuItemClickListener" in
     * the definition of the FlicqActivity class. The callback was
     * previously set back in showDataSelector().
     */
    public boolean onMenuItemClick(MenuItem item) {
        boolean sts = true;
        Button menuButton = (Button) findViewById(R.id.data_source_popup);

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
            if (flicqDevice != null) flicqDevice.useQ9();
        } else if (itemId == R.id.fixed) {
            dataSource = DataSource.FIXED;
            algorithm = Algorithm.NONE;
            menuButton.setText("FIXED");
        } else {
            sts = false;
        }
        updateSensors();
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
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (currentOrientation != 2) {
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
        if (loadImu) flicqDevice = FlicqDevice.getInstance(this, imuName);

        myUtils = new FlicqUtils(this); // Register utility class
        localSensors = new LocalSensors(this);
        dataSelector = new DataSelector(this);

        setContentView(R.layout.activity_main);
        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);

        Display display = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenRotation = display.getRotation();

        GLSurfaceView pcbGlview = (GLSurfaceView) findViewById(R.id.pcb_glview);
        pcbRenderer = new TextureCubeRenderer(this, screenRotation, pcbSurfaces, pcbDimensions, "Rev5 board");

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

        View v1 = findViewById(R.id.absolute);
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
        configureApplicationViews(guiState, true); // Override default set in
        // activity_main.xml
        // previously used imu_names include "Gen5" and "Motorola"
        if ((flicqDevice != null) && (!flicqDevice.isListening())) {
            flicqDevice.startBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FlicqDevice.requestCode) {
            if (resultCode == RESULT_OK) {
                flicqDevice.getPairedDevice();
                flicqDevice.initializeConnection();
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
        //Log.v(LOG_TAG, "begin onStop() from FlicqActivity");
        //dumpStates("calling onStop().");
        localSensors.stop();
        if (flicqDevice != null) flicqDevice.stop(false);
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
        if (flicqDevice != null) {
            flicqDevice.stop(true); // release BT threads
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
