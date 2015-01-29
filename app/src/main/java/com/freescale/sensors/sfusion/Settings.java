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
package com.freescale.sensors.sfusion;
// preferences code based on example found at: http://www.javacodegeeks.com/2011/01/android-quick-preferences-tutorial.html
// This class saves and restores application preferences from device storage.

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * This class encapsulates the "Preferences" screen, which is a second "Activity" within the application.
 * Only one instance of this class is used.
 *
 * @author Michael Stanley
 */
public class Settings extends Activity {

    // Usage to read settings:
    // public static final String PREF_NAME = "A_FSL_Sensor_Demo";
    // SharedPreferences myPrefs = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE); OR
    // SharedPreferences myPrefs = getSharedPreferences("A_FSL_Sensor_Demo", Activity.MODE_PRIVATE);
    // String my_email = myPrefs.getString("my_email", "")
    // boolean remote_enable = myPrefs.getBoolean("remote_enable", false)
    // boolean scroll_disable = myPrefs.getBoolean("scroll_disable", false)
    // boolean local_lpf_enable = myPrefs.getBoolean("local_lpf_enable", false)
    // boolean local_use_6_axis = myPrefs.getBoolean("local_use_6_axis", false)
    // Int sample_rate_option = myPrefs.getInt("sample_rate", 0)

    public static final String PREF_NAME = "A_FSL_Sensor_Demo";
    public static final String defaultMaxFileMsgs = new String("10000");

    public SharedPreferences myPrefs;
    //	myPrefs = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    String LOG_TAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        myPrefs = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);

        LOG_TAG = getString(R.string.log_tag);

        Button cancel_button = (Button) findViewById(R.id.cancel_prefs_button);
        Button exit_prefs_button = (Button) findViewById(R.id.exit_prefs_button);

        ArrayAdapter<CharSequence> orientation_adapter = ArrayAdapter.createFromResource(
                this, R.array.orientations, android.R.layout.simple_spinner_item);
        orientation_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner orientation_spinner = (Spinner) findViewById(R.id.orientation_spinner);
        orientation_spinner.setAdapter(orientation_adapter);

        ArrayAdapter<CharSequence> acc_rate_adapter = ArrayAdapter.createFromResource(
                this, R.array.sample_rates, android.R.layout.simple_spinner_item);
        acc_rate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner acc_rate_spinner = (Spinner) findViewById(R.id.acc_rate_spinner);
        acc_rate_spinner.setAdapter(acc_rate_adapter);

        ArrayAdapter<CharSequence> mag_rate_adapter = ArrayAdapter.createFromResource(
                this, R.array.sample_rates, android.R.layout.simple_spinner_item);
        mag_rate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mag_rate_spinner = (Spinner) findViewById(R.id.mag_rate_spinner);
        mag_rate_spinner.setAdapter(mag_rate_adapter);

        ArrayAdapter<CharSequence> gyro_rate_adapter = ArrayAdapter.createFromResource(
                this, R.array.sample_rates, android.R.layout.simple_spinner_item);
        gyro_rate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner gyro_rate_spinner = (Spinner) findViewById(R.id.gyro_rate_spinner);
        gyro_rate_spinner.setAdapter(gyro_rate_adapter);

        ArrayAdapter<CharSequence> rotation_vector_rate_adapter = ArrayAdapter.createFromResource(
                this, R.array.sample_rates, android.R.layout.simple_spinner_item);
        rotation_vector_rate_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner rotation_vector_rate_spinner = (Spinner) findViewById(R.id.rotaton_vector_rate_spinner);
        rotation_vector_rate_spinner.setAdapter(rotation_vector_rate_adapter);

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        exit_prefs_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This function is where we store preferences to non-volatile memory
                TextView myEmail = (TextView) findViewById(R.id.my_email);
                TextView fileName = (TextView) findViewById(R.id.fileName);
                TextView statsFileName = (TextView) findViewById(R.id.statsFileName);
                TextView maxFileMsgs = (TextView) findViewById(R.id.maxFileMsgs);
                TextView btPrefix = (TextView) findViewById(R.id.btPrefix);
                TextView ipCode = (TextView) findViewById(R.id.ipCode);
                TextView ipPort = (TextView) findViewById(R.id.ipPort);
                CheckBox remoteEnable = (CheckBox) findViewById(R.id.remote_enable);
                CheckBox enableJavascript = (CheckBox) findViewById(R.id.enable_javascript);
                CheckBox enableDeviceDebug = (CheckBox) findViewById(R.id.enable_device_debug);
                CheckBox enableRpc = (CheckBox) findViewById(R.id.enable_rpc);
                CheckBox enableVirtualGyro = (CheckBox) findViewById(R.id.enable_virtual_gyro);
                Spinner accRateSpinner = (Spinner) findViewById(R.id.acc_rate_spinner);
                Spinner magRateSpinner = (Spinner) findViewById(R.id.mag_rate_spinner);
                Spinner gyroRateSpinner = (Spinner) findViewById(R.id.gyro_rate_spinner);
                Spinner rotationVectorRateSpinner = (Spinner) findViewById(R.id.rotaton_vector_rate_spinner);
                Spinner orientationSpinner = (Spinner) findViewById(R.id.orientation_spinner);

                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putString("my_email", myEmail.getText().toString());
                editor.putString("fileName", fileName.getText().toString());
                editor.putString("statsFileName", statsFileName.getText().toString());
                editor.putString("maxFileMsgs", maxFileMsgs.getText().toString());
                editor.putString("btPrefix", btPrefix.getText().toString());
                editor.putString("ipCode", ipCode.getText().toString());
                editor.putString("ipPort", ipPort.getText().toString());
                editor.putBoolean("remote_enable", remoteEnable.isChecked());
                editor.putBoolean("enable_javascript", enableJavascript.isChecked());
                editor.putBoolean("enable_device_debug", enableDeviceDebug.isChecked());
                editor.putBoolean("enable_rpc", enableRpc.isChecked());
                editor.putBoolean("enable_virtual_gyro", enableVirtualGyro.isChecked());
                editor.putInt("acc_sample_rate", accRateSpinner.getSelectedItemPosition());
                editor.putInt("mag_sample_rate", magRateSpinner.getSelectedItemPosition());
                editor.putInt("gyro_sample_rate", gyroRateSpinner.getSelectedItemPosition());
                editor.putInt("rotation_vector_sample_rate", rotationVectorRateSpinner.getSelectedItemPosition());
                editor.putInt("orientation", orientationSpinner.getSelectedItemPosition());

                editor.commit();
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // This function restores preferences
        TextView myEmail = (TextView) findViewById(R.id.my_email);
        TextView fileName = (TextView) findViewById(R.id.fileName);
        TextView statsFileName = (TextView) findViewById(R.id.statsFileName);
        TextView maxFileMsgs = (TextView) findViewById(R.id.maxFileMsgs);
        TextView btPrefix = (TextView) findViewById(R.id.btPrefix);
        TextView ipCode = (TextView) findViewById(R.id.ipCode);
        TextView ipPort = (TextView) findViewById(R.id.ipPort);
        CheckBox remoteEnable = (CheckBox) findViewById(R.id.remote_enable);
        CheckBox enableJavascript = (CheckBox) findViewById(R.id.enable_javascript);
        CheckBox enableDeviceDebug = (CheckBox) findViewById(R.id.enable_device_debug);
        CheckBox enableRpc = (CheckBox) findViewById(R.id.enable_rpc);
        CheckBox enableVirtualGyro = (CheckBox) findViewById(R.id.enable_virtual_gyro);
        Spinner accRateSpinner = (Spinner) findViewById(R.id.acc_rate_spinner);
        Spinner magRateSpinner = (Spinner) findViewById(R.id.mag_rate_spinner);
        Spinner gyroRateSpinner = (Spinner) findViewById(R.id.gyro_rate_spinner);
        Spinner rotationVectorRateSpinner = (Spinner) findViewById(R.id.rotaton_vector_rate_spinner);
        Spinner orientationSpinner = (Spinner) findViewById(R.id.orientation_spinner);

        String feedbackEmailAddr = A_FSL_Sensor_Demo.self.getString(R.string.feedbackEmailAddr);
        myEmail.setText(myPrefs.getString("my_email", feedbackEmailAddr));
        String defaultFileName = A_FSL_Sensor_Demo.self.getString(R.string.defaultOutputFile);
        String defaultStatsFileName = A_FSL_Sensor_Demo.self.getString(R.string.statsOutputFile);
        String defaultIpCode = A_FSL_Sensor_Demo.self.getString(R.string.ipCode);
        String defaultIpPort = A_FSL_Sensor_Demo.self.getString(R.string.ipPort);
        String defaultBtPrefix = A_FSL_Sensor_Demo.self.getString(R.string.btPrefix);
        fileName.setText(myPrefs.getString("fileName", defaultFileName));
        statsFileName.setText(myPrefs.getString("statsFileName", defaultStatsFileName));
        ipCode.setText(myPrefs.getString("ipCode", defaultIpCode));
        ipPort.setText(myPrefs.getString("ipPort", defaultIpPort));
        btPrefix.setText(myPrefs.getString("btPrefix", defaultBtPrefix));

        maxFileMsgs.setText(myPrefs.getString("maxFileMsgs", defaultMaxFileMsgs));
        remoteEnable.setChecked(myPrefs.getBoolean("remote_enable", false));
        enableJavascript.setChecked(myPrefs.getBoolean("enable_javascript", true));
        enableDeviceDebug.setChecked(myPrefs.getBoolean("enable_device_debug", false));
        enableRpc.setChecked(myPrefs.getBoolean("enable_rpc", false));
        enableVirtualGyro.setChecked(myPrefs.getBoolean("enable_virtual_gyro", false));
        // Possible rate spinner values are (in this order):
        //<item>SENSOR_DELAY_NORMAL</item>
        //<item>SENSOR_DELAY_UI</item>
        //<item>SENSOR_DELAY_GAME</item>
        //<item>SENSOR_DELAY_FASTEST</item>
        accRateSpinner.setSelection(myPrefs.getInt("acc_sample_rate", 0));
        magRateSpinner.setSelection(myPrefs.getInt("mag_sample_rate", 0));
        gyroRateSpinner.setSelection(myPrefs.getInt("gyro_sample_rate", 0));
        rotationVectorRateSpinner.setSelection(myPrefs.getInt("rotation_vector_sample_rate", 0));
        orientationSpinner.setSelection(myPrefs.getInt("orientation", 0));
    }
}
