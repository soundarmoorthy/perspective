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

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.DataSource;
import com.freescale.sensors.sfusion.A_FSL_Sensor_Demo.GuiState;
/**
 * The Statistics class is responsible for measuring basic statistics on sensor outputs.
 * Results are presented in the "Statistics" view of the application.  The user can 
 * determine sample size and mode (continuous or single pass).  Stats computed include:
 * current value, min, mean, max, standard deviation and noise in /rtHz terms.
 * An HTML dump of the statistics report can also be generated.
 * @author Michael Stanley
 *
 */
public class Statistics {
	private static A_FSL_Sensor_Demo demo = null;
	private static Activity context=null;
	private static View accStatsView, magStatsView, gyroStatsView, quatStatsView;
	private static Spinner accRateSpinner=null;
	private static Spinner magRateSpinner = null;
	private static Spinner gyroRateSpinner = null;
	private static Spinner quatRateSpinner = null;
	private static ProgressBar progressBar = null;
	
	private static TimedTriad acc = new TimedTriad();
	private static TimedTriad mag = new TimedTriad();
	private static TimedTriad gyro = new TimedTriad();
	private static TimedQuaternion quat = new TimedQuaternion();
	private static RotationVector rv = new RotationVector();
    private Handler periodicHandler = new Handler();
    private static final Object lock = new Object();
    private static long refreshInterval = 200; // ms
    private static boolean statsActive = false;

	Statistics(Activity context, A_FSL_Sensor_Demo demo) {
		Statistics.demo=demo;
		Statistics.context = context;
	}
	
	
	/**
	 * accelerometer, magnetometer and gyroscope data are displayed using a common screen "format", which
	 * is configured using this function.  The layout is defined in file xyz_stats.xml and then instantiated
	 * three times in activity_main.xml.  GUI items "below" the sensor level have common names defined in
	 * xyz_stats.xml.  You find them by first dereferencing the top level structure for the sensor.
	 * @param sensorDisplay 	The top level GUI structure for this sensor
	 * @param name				Sensor Name
	 * @param description		Sensor description
	 * @param units				Measumentment units
	 */
	static void configure_xyz(View sensorDisplay, String name, String description, String units) {
		TextView nameField = (TextView) sensorDisplay.findViewById(R.id.sensor_name);
		TextView descField = (TextView) sensorDisplay.findViewById(R.id.sensor_description);
		TextView xUnitsField = (TextView) sensorDisplay.findViewById(R.id.x_units);
		TextView yUnitsField = (TextView) sensorDisplay.findViewById(R.id.y_units);
		TextView zUnitsField = (TextView) sensorDisplay.findViewById(R.id.z_units);
		TextView magUnitsField = (TextView) sensorDisplay.findViewById(R.id.mag_units);
		nameField.setText(name);
		descField.setText(description);
		xUnitsField.setText(units);
		yUnitsField.setText(units);
		zUnitsField.setText(units);
		magUnitsField.setText(units);
	}
	/**
	 * all spinners on the Stats page have a common format for setting sensor sampling rates.
	 * This function expands the options for the spinners
	 * @param sensorDisplay	The top level GUI structure for each sensor
	 * @return				A pointer to the spinner
	 */
	static Spinner configureRateSpinner(View sensorDisplay) {
		ArrayAdapter<CharSequence> rate_adapter = ArrayAdapter.createFromResource(
				context, R.array.sample_rates_all, android.R.layout.simple_spinner_item );
		rate_adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		Spinner rate_spinner = (Spinner) sensorDisplay.findViewById( R.id.rate_spinner );
		rate_spinner.setAdapter( rate_adapter );
		return rate_spinner;
	}
	void setSpinnerVisible(View sensorDisplay, boolean visible) {
		Spinner rateSpinner = (Spinner) sensorDisplay.findViewById( R.id.rate_spinner );
		if (visible) {
			rateSpinner.setVisibility(View.VISIBLE);
		} else {
			rateSpinner.setVisibility(View.GONE);
		}
	}
	void setSpinnersVisible(boolean visible) {
		accStatsView = context.findViewById(R.id.acc_stats);
		magStatsView = context.findViewById(R.id.mag_stats);
		gyroStatsView = context.findViewById(R.id.gyro_stats);
		quatStatsView = context.findViewById(R.id.quat_stats);	
		setSpinnerVisible(accStatsView, visible);
		setSpinnerVisible(magStatsView, visible);
		setSpinnerVisible(gyroStatsView, visible);
		setSpinnerVisible(quatStatsView, visible);
	}
	public void updateStatsFields(View statsView, TimedTriad sensor) {
		
		TextView txtFldX = (TextView) statsView.findViewById(R.id.x_value);
		TextView txtFldMinX = (TextView) statsView.findViewById(R.id.min_x_value);
		TextView txtFldMeanX = (TextView) statsView.findViewById(R.id.mean_x_value);
		TextView txtFldMaxX = (TextView) statsView.findViewById(R.id.max_x_value);
		TextView txtFldStdDevX = (TextView) statsView.findViewById(R.id.stddev_x_value);
		TextView txtFldXRtHz = (TextView) statsView.findViewById(R.id.x_rtHz);

		TextView txtFldY = (TextView) statsView.findViewById(R.id.y_value);
		TextView txtFldMinY = (TextView) statsView.findViewById(R.id.min_y_value);
		TextView txtFldMeanY = (TextView) statsView.findViewById(R.id.mean_y_value);
		TextView txtFldMaxY = (TextView) statsView.findViewById(R.id.max_y_value);
		TextView txtFldStdDevY = (TextView) statsView.findViewById(R.id.stddev_y_value);
		TextView txtFldYRtHz = (TextView) statsView.findViewById(R.id.y_rtHz);

		TextView txtFldZ = (TextView) statsView.findViewById(R.id.z_value);
		TextView txtFldMinZ = (TextView) statsView.findViewById(R.id.min_z_value);
		TextView txtFldMeanZ = (TextView) statsView.findViewById(R.id.mean_z_value);
		TextView txtFldMaxZ = (TextView) statsView.findViewById(R.id.max_z_value);
		TextView txtFldStdDevZ = (TextView) statsView.findViewById(R.id.stddev_z_value);
		TextView txtFldZRtHz = (TextView) statsView.findViewById(R.id.z_rtHz);
		
		TextView txtFldMag = (TextView) statsView.findViewById(R.id.mag_value);
		TextView txtFldMinMag = (TextView) statsView.findViewById(R.id.min_mag_value);
		TextView txtFldMeanMag = (TextView) statsView.findViewById(R.id.mean_mag_value);
		TextView txtFldMaxMag = (TextView) statsView.findViewById(R.id.max_mag_value);
		TextView txtFldStdDevMag = (TextView) statsView.findViewById(R.id.stddev_mag_value);
		TextView txtFldMagRtHz = (TextView) statsView.findViewById(R.id.mag_rtHz);
		
		TextView txtFldRate = (TextView) statsView.findViewById(R.id.rate_value);
		TextView txtFldMinRate = (TextView) statsView.findViewById(R.id.min_rate_value);
		TextView txtFldMeanRate = (TextView) statsView.findViewById(R.id.mean_rate_value);
		TextView txtFldMaxRate = (TextView) statsView.findViewById(R.id.max_rate_value);
		TextView txtFldStdDevRate = (TextView) statsView.findViewById(R.id.stddev_rate_value);
		
		String x = String.format("%10.4f", sensor.x());
		String minX = String.format("%10.4f", sensor.xStats.min);
		String meanX = String.format("%10.4f", sensor.xStats.mean);
		String maxX = String.format("%10.4f", sensor.xStats.max);
		String stdDevX = String.format("%12.6f", sensor.xStats.stddev);
		String xRtHz = String.format("%12.6f", sensor.xStats.stddev/Math.sqrt(sensor.rateStats.mean));

		String y = String.format("%10.4f", sensor.y());
		String minY = String.format("%10.4f", sensor.yStats.min);
		String meanY = String.format("%10.4f", sensor.yStats.mean);
		String maxY = String.format("%10.4f", sensor.yStats.max);
		String stdDevY = String.format("%12.6f", sensor.yStats.stddev);
		String yRtHz = String.format("%12.6f", sensor.yStats.stddev/Math.sqrt(sensor.rateStats.mean));

		String z = String.format("%10.4f", sensor.z());
		String minZ = String.format("%10.4f", sensor.zStats.min);
		String meanZ = String.format("%10.4f", sensor.zStats.mean);
		String maxZ = String.format("%10.4f", sensor.zStats.max);
		String stdDevZ = String.format("%12.6f", sensor.zStats.stddev);
		String zRtHz = String.format("%12.6f", sensor.zStats.stddev/Math.sqrt(sensor.rateStats.mean));
		
		String mag = String.format("%10.4f", sensor.magnitude());
		String minMag = String.format("%10.4f", sensor.magStats.min);
		String meanMag = String.format("%10.4f", sensor.magStats.mean);
		String maxMag = String.format("%10.4f", sensor.magStats.max);
		String stdDevMag = String.format("%12.6f", sensor.magStats.stddev);
		String magRtHz = String.format("%12.6f", sensor.magStats.stddev/Math.sqrt(sensor.rateStats.mean));
		
		String rate = String.format("%10.4f", sensor.rate);
		String minRate = String.format("%10.4f", sensor.rateStats.min);
		String meanRate = String.format("%10.4f", sensor.rateStats.mean);
		String maxRate = String.format("%10.4f", sensor.rateStats.max);
		String stdDevRate = String.format("%12.6f", sensor.rateStats.stddev);

		txtFldX.setText(x);
		txtFldMeanX.setText(meanX);
		txtFldMinX.setText(minX);
		txtFldMaxX.setText(maxX);
		txtFldStdDevX.setText(stdDevX);
		txtFldXRtHz.setText(xRtHz);

		txtFldY.setText(y);
		txtFldMeanY.setText(meanY);
		txtFldMinY.setText(minY);
		txtFldMaxY.setText(maxY);
		txtFldStdDevY.setText(stdDevY);
		txtFldYRtHz.setText(yRtHz);

		txtFldZ.setText(z);
		txtFldMeanZ.setText(meanZ);
		txtFldMinZ.setText(minZ);
		txtFldMaxZ.setText(maxZ);
		txtFldStdDevZ.setText(stdDevZ);
		txtFldZRtHz.setText(zRtHz);
		
		txtFldMag.setText(mag);		
		txtFldMeanMag.setText(meanMag);
		txtFldMinMag.setText(minMag);
		txtFldMaxMag.setText(maxMag);
		txtFldStdDevMag.setText(stdDevMag);
		txtFldMagRtHz.setText(magRtHz);
		
		txtFldRate.setText(rate);
		txtFldMinRate.setText(minRate);
		txtFldMeanRate.setText(meanRate);
		txtFldMaxRate.setText(maxRate);
		txtFldStdDevRate.setText(stdDevRate);
	}
		
		public void updateQuatFields(View statsView, TimedQuaternion q) {
		
		TextView txtFldQ0 = (TextView) statsView.findViewById(R.id.q0_value);
		TextView txtFldMinQ0 = (TextView) statsView.findViewById(R.id.min_q0_value);
		TextView txtFldMeanQ0 = (TextView) statsView.findViewById(R.id.mean_q0_value);
		TextView txtFldMaxQ0 = (TextView) statsView.findViewById(R.id.max_q0_value);
		TextView txtFldStdDevQ0 = (TextView) statsView.findViewById(R.id.stddev_q0_value);
		TextView txtFldQ0RtHz = (TextView) statsView.findViewById(R.id.q0_rtHz);

		TextView txtFldQ1 = (TextView) statsView.findViewById(R.id.q1_value);
		TextView txtFldMinQ1 = (TextView) statsView.findViewById(R.id.min_q1_value);
		TextView txtFldMeanQ1 = (TextView) statsView.findViewById(R.id.mean_q1_value);
		TextView txtFldMaxQ1 = (TextView) statsView.findViewById(R.id.max_q1_value);
		TextView txtFldStdDevQ1 = (TextView) statsView.findViewById(R.id.stddev_q1_value);
		TextView txtFldQ1RtHz = (TextView) statsView.findViewById(R.id.q1_rtHz);
		
		TextView txtFldQ2 = (TextView) statsView.findViewById(R.id.q2_value);
		TextView txtFldMinQ2 = (TextView) statsView.findViewById(R.id.min_q2_value);
		TextView txtFldMeanQ2 = (TextView) statsView.findViewById(R.id.mean_q2_value);
		TextView txtFldMaxQ2 = (TextView) statsView.findViewById(R.id.max_q2_value);
		TextView txtFldStdDevQ2 = (TextView) statsView.findViewById(R.id.stddev_q2_value);
		TextView txtFldQ2RtHz = (TextView) statsView.findViewById(R.id.q2_rtHz);

		TextView txtFldQ3 = (TextView) statsView.findViewById(R.id.q3_value);
		TextView txtFldMinQ3 = (TextView) statsView.findViewById(R.id.min_q3_value);
		TextView txtFldMeanQ3 = (TextView) statsView.findViewById(R.id.mean_q3_value);
		TextView txtFldMaxQ3 = (TextView) statsView.findViewById(R.id.max_q3_value);
		TextView txtFldStdDevQ3 = (TextView) statsView.findViewById(R.id.stddev_q3_value);
		TextView txtFldQ3RtHz = (TextView) statsView.findViewById(R.id.q3_rtHz);
		
		TextView txtFldMag = (TextView) statsView.findViewById(R.id.mag_value);
		TextView txtFldMinMag = (TextView) statsView.findViewById(R.id.min_mag_value);
		TextView txtFldMeanMag = (TextView) statsView.findViewById(R.id.mean_mag_value);
		TextView txtFldMaxMag = (TextView) statsView.findViewById(R.id.max_mag_value);
		TextView txtFldStdDevMag = (TextView) statsView.findViewById(R.id.stddev_mag_value);
		TextView txtFldMagRtHz = (TextView) statsView.findViewById(R.id.mag_rtHz);
		
		TextView txtFldAngle = (TextView) statsView.findViewById(R.id.angle_value);
		TextView txtFldMinAngle = (TextView) statsView.findViewById(R.id.min_angle_value);
		TextView txtFldMeanAngle = (TextView) statsView.findViewById(R.id.mean_angle_value);
		TextView txtFldMaxAngle = (TextView) statsView.findViewById(R.id.max_angle_value);
		TextView txtFldStdDevAngle = (TextView) statsView.findViewById(R.id.stddev_angle_value);
		TextView txtFldAngleRtHz = (TextView) statsView.findViewById(R.id.angle_rtHz);

		TextView txtFldRate = (TextView) statsView.findViewById(R.id.rate_value);
		TextView txtFldMinRate = (TextView) statsView.findViewById(R.id.min_rate_value);
		TextView txtFldMeanRate = (TextView) statsView.findViewById(R.id.mean_rate_value);
		TextView txtFldMaxRate = (TextView) statsView.findViewById(R.id.max_rate_value);
		TextView txtFldStdDevRate = (TextView) statsView.findViewById(R.id.stddev_rate_value);
		
		String q0 = String.format("%10.4f", q.q0);
		String minQ0 = String.format("%10.4f", q.q0Stats.min);
		String meanQ0 = String.format("%10.4f", q.q0Stats.mean);
		String maxQ0 = String.format("%10.4f", q.q0Stats.max);
		String stdDevQ0 = String.format("%12.6f", q.q0Stats.stddev);
		String q0RtHz = String.format("%12.6f", q.q0Stats.stddev/Math.sqrt(q.rateStats.mean));

		String q1 = String.format("%10.4f", q.q1);
		String minQ1 = String.format("%10.4f", q.q1Stats.min);
		String meanQ1 = String.format("%10.4f", q.q1Stats.mean);
		String maxQ1 = String.format("%10.4f", q.q1Stats.max);
		String stdDevQ1 = String.format("%12.6f", q.q1Stats.stddev);
		String q1RtHz = String.format("%12.6f", q.q1Stats.stddev/Math.sqrt(q.rateStats.mean));

		String q2 = String.format("%10.4f", q.q2);
		String minQ2 = String.format("%10.4f", q.q2Stats.min);
		String meanQ2 = String.format("%10.4f", q.q2Stats.mean);
		String maxQ2 = String.format("%10.4f", q.q2Stats.max);
		String stdDevQ2 = String.format("%12.6f", q.q2Stats.stddev);
		String q2RtHz = String.format("%12.6f", q.q2Stats.stddev/Math.sqrt(q.rateStats.mean));

		String q3 = String.format("%10.4f", q.q3);
		String minQ3 = String.format("%10.4f", q.q3Stats.min);
		String meanQ3 = String.format("%10.4f", q.q3Stats.mean);
		String maxQ3 = String.format("%10.4f", q.q3Stats.max);
		String stdDevQ3 = String.format("%12.6f", q.q3Stats.stddev);
		String q3RtHz = String.format("%12.6f", q.q3Stats.stddev/Math.sqrt(q.rateStats.mean));
		
		String mag = String.format("%10.4f", q.magnitude);
		String minMag = String.format("%10.4f", q.magStats.min);
		String meanMag = String.format("%10.4f", q.magStats.mean);
		String maxMag = String.format("%10.4f", q.magStats.max);
		String stdDevMag = String.format("%12.6f", q.magStats.stddev);
		String magRtHz = String.format("%12.6f", q.magStats.stddev/Math.sqrt(q.rateStats.mean));
		
		String angle = String.format("%10.4f", q.angle);
		String minAngle = String.format("%10.4f", q.angleStats.min);
		String meanAngle = String.format("%10.4f", q.angleStats.mean);
		String maxAngle = String.format("%10.4f", q.angleStats.max);
		String stdDevAngle = String.format("%12.6f", q.angleStats.stddev);
		String angleRtHz = String.format("%12.6f", q.angleStats.stddev/Math.sqrt(q.rateStats.mean));
		
		String rate = String.format("%10.4f", q.rate);
		String minRate = String.format("%10.4f", q.rateStats.min);
		String meanRate = String.format("%10.4f", q.rateStats.mean);
		String maxRate = String.format("%10.4f", q.rateStats.max);
		String stdDevRate = String.format("%12.6f", q.rateStats.stddev);

		txtFldQ0.setText(q0);
		txtFldMeanQ0.setText(meanQ0);
		txtFldMinQ0.setText(minQ0);
		txtFldMaxQ0.setText(maxQ0);
		txtFldStdDevQ0.setText(stdDevQ0);
		txtFldQ0RtHz.setText(q0RtHz);
		
		txtFldQ1.setText(q1);
		txtFldMeanQ1.setText(meanQ1);
		txtFldMinQ1.setText(minQ1);
		txtFldMaxQ1.setText(maxQ1);
		txtFldStdDevQ1.setText(stdDevQ1);
		txtFldQ1RtHz.setText(q1RtHz);

		txtFldQ2.setText(q2);
		txtFldMeanQ2.setText(meanQ2);
		txtFldMinQ2.setText(minQ2);
		txtFldMaxQ2.setText(maxQ2);
		txtFldStdDevQ2.setText(stdDevQ2);
		txtFldQ2RtHz.setText(q2RtHz);
		
		txtFldQ3.setText(q3);
		txtFldMeanQ3.setText(meanQ3);
		txtFldMinQ3.setText(minQ3);
		txtFldMaxQ3.setText(maxQ3);
		txtFldStdDevQ3.setText(stdDevQ3);
		txtFldQ3RtHz.setText(q3RtHz);

		txtFldMag.setText(mag);		
		txtFldMeanMag.setText(meanMag);
		txtFldMinMag.setText(minMag);
		txtFldMaxMag.setText(maxMag);
		txtFldStdDevMag.setText(stdDevMag);
		txtFldMagRtHz.setText(magRtHz);
		
		txtFldAngle.setText(angle);		
		txtFldMeanAngle.setText(meanAngle);
		txtFldMinAngle.setText(minAngle);
		txtFldMaxAngle.setText(maxAngle);
		txtFldStdDevAngle.setText(stdDevAngle);
		txtFldAngleRtHz.setText(angleRtHz);
		
		txtFldRate.setText(rate);
		txtFldMinRate.setText(minRate);
		txtFldMeanRate.setText(meanRate);
		txtFldMaxRate.setText(maxRate);
		txtFldStdDevRate.setText(stdDevRate);
	}
		
	private void updateStatsDisplay() {
		float percent2;
		demo.dataSelector.getSensorsSnapshot(acc, mag, gyro, quat, rv);
		updateStatsFields(accStatsView, acc);
		updateStatsFields(magStatsView, mag);
		updateStatsFields(gyroStatsView, gyro);
		updateQuatFields(quatStatsView, quat);
		float accPercent = acc.percentDone();
		float magPercent = mag.percentDone();
		float gyroPercent = gyro.percentDone();
		float quatPercent = quat.percentDone();
		float percent1 = Math.min(accPercent, magPercent);
		if (gyro.enabled) {
			percent2 = Math.min(gyroPercent, quatPercent);
//			gyroStatsView.setVisibility(View.VISIBLE);
		} else {
			percent2 = quatPercent;
//			gyroStatsView.setVisibility(View.GONE);
		}
		int totalPercent = (int) (100.0 * Math.min(percent1,  percent2));
		progressBar.setProgress(totalPercent);
	}
	private Runnable updaterTask = new Runnable() {
		public void run() {
			synchronized(lock) {
				if (statsActive) {
					updateStatsDisplay();
					periodicHandler.postDelayed(updaterTask,  refreshInterval);
				}
			}
		}
	};

	void onCreate() {		
		accStatsView = context.findViewById(R.id.acc_stats);
		magStatsView = context.findViewById(R.id.mag_stats);
		gyroStatsView = context.findViewById(R.id.gyro_stats);
		quatStatsView = context.findViewById(R.id.quat_stats);
		progressBar = (ProgressBar) context.findViewById(R.id.progress_bar);

		accRateSpinner = configureRateSpinner(accStatsView);
		magRateSpinner = configureRateSpinner(magStatsView);
		gyroRateSpinner = configureRateSpinner(gyroStatsView);
		quatRateSpinner = configureRateSpinner(quatStatsView);		
		accRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int ratePosition = accRateSpinner.getSelectedItemPosition()-1;
				if (ratePosition>=0) // This spinner really starts at 1, so ignore 1st location
					demo.localSensors.setSensorRateBySensorType(SensorsWrapper.SensorType.ACCEL, ratePosition);
				accRateSpinner.setSelection(0); // Back to general label
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		magRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int ratePosition = magRateSpinner.getSelectedItemPosition()-1;
				if (ratePosition>=0) // This spinner really starts at 1, so ignore 1st location
					demo.localSensors.setSensorRateBySensorType(SensorsWrapper.SensorType.MAG, ratePosition);
				magRateSpinner.setSelection(0); // Back to general label
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		gyroRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int ratePosition = gyroRateSpinner.getSelectedItemPosition()-1;
				if (ratePosition>=0) // This spinner really starts at 1, so ignore 1st location
					demo.localSensors.setSensorRateBySensorType(SensorsWrapper.SensorType.GYRO, ratePosition);
				gyroRateSpinner.setSelection(0); // Back to general label
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		quatRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int ratePosition = quatRateSpinner.getSelectedItemPosition()-1;
				if (ratePosition>=0) // This spinner really starts at 1, so ignore 1st location
					demo.localSensors.setSensorRateBySensorType(SensorsWrapper.SensorType.QUAT, ratePosition);
				quatRateSpinner.setSelection(0); // Back to general label
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		updateSpinnerValues();
	}
	void updateSpinnerValues() {
		accRateSpinner.setSelection(0);
		magRateSpinner.setSelection(0);
		gyroRateSpinner.setSelection(0);
		quatRateSpinner.setSelection(0);
	}
	public void show(boolean show) {
		final HorizontalScrollView statsFrame = (HorizontalScrollView) demo.findViewById(R.id.statsFrame);
		final Button statsSampleSizePopup = (Button) demo.findViewById(R.id.stats_sample_size_popup);
		final Button statsCalcModePopup = (Button) demo.findViewById(R.id.stats_calc_mode_popup);
		if (show) {
			statsFrame.setVisibility(View.VISIBLE);	
			statsSampleSizePopup.setVisibility(View.VISIBLE);	
			statsCalcModePopup.setVisibility(View.VISIBLE);	
			// Start updating the fields on a periodic basis
			if (!statsActive) {
				periodicHandler.postDelayed(updaterTask,  refreshInterval);				
			}
			statsActive=true;
		} else {
			statsFrame.setVisibility(View.GONE);
			statsSampleSizePopup.setVisibility(View.GONE);	
			statsCalcModePopup.setVisibility(View.GONE);	
			// Stop updating the fields on a periodic basis
			statsActive=false;
		}
	}
	static void configureStatsPage() {
		accStatsView = context.findViewById(R.id.acc_stats);
		magStatsView = context.findViewById(R.id.mag_stats);
		gyroStatsView = context.findViewById(R.id.gyro_stats);
		quatStatsView = context.findViewById(R.id.quat_stats);
		configure_xyz(accStatsView, demo.dataSelector.getAccelName(), demo.dataSelector.getAccelDescription(), "gravity");
		configure_xyz(magStatsView, demo.dataSelector.getMagName(), demo.dataSelector.getMagDescription(), "microTeslas ");
		if (demo.dataSelector.gyroEnabled()) {
			gyroStatsView.setVisibility(View.VISIBLE);
			configure_xyz(gyroStatsView, demo.dataSelector.getGyroName(), demo.dataSelector.getGyroDescription(), "radians/sec");
		} else {
			gyroStatsView.setVisibility(View.GONE);
		}
		TextView quatName = (TextView) quatStatsView.findViewById(R.id.sensor_name);
		TextView quatDesc = (TextView) quatStatsView.findViewById(R.id.sensor_description);
		quatName.setText(demo.dataSelector.getQuatName());
		quatDesc.setText(demo.dataSelector.getQuatDescription());
	}
	public void configureStatsGathering(int statsSampleSize, boolean oneShot, boolean resetStats) {
		switch (demo.guiState) {
		case STATS:
			if (demo.dataSource==DataSource.REMOTE) {
				demo.localSensors.enableLogging(false, statsSampleSize, oneShot, resetStats);
				if (demo.imu!=null) demo.imu.enableLogging(true, statsSampleSize, oneShot, resetStats);
				demo.wigo.enableLogging(false, statsSampleSize, oneShot, resetStats);
			} else if (demo.dataSource==DataSource.LOCAL) {
				demo.localSensors.enableLogging(true, statsSampleSize, oneShot, resetStats);
				if (demo.imu!=null) demo.imu.enableLogging(false, statsSampleSize, oneShot, resetStats);				
				demo.wigo.enableLogging(false, statsSampleSize, oneShot, resetStats);
			} else if (demo.dataSource==DataSource.WIGO) {
				demo.localSensors.enableLogging(false, statsSampleSize, oneShot, resetStats);
				if (demo.imu!=null) demo.imu.enableLogging(false, statsSampleSize, oneShot, resetStats);				
				demo.wigo.enableLogging(true, statsSampleSize, oneShot, resetStats);
			} else {
				demo.localSensors.enableLogging(false, statsSampleSize, oneShot, resetStats);
				if (demo.imu!=null) demo.imu.enableLogging(false, statsSampleSize, oneShot, resetStats);				
				demo.wigo.enableLogging(false, statsSampleSize, oneShot, resetStats);
			}
			break;
		default:
			demo.localSensors.enableLogging(false, statsSampleSize, oneShot, resetStats);
			if (demo.imu!=null) demo.imu.enableLogging(false, statsSampleSize, oneShot, resetStats);
			demo.wigo.enableLogging(false, statsSampleSize, oneShot, resetStats);
		}
		if (demo.guiState==GuiState.STATS) {
			String str = String.format("(%d/", statsSampleSize);
			if (oneShot) {
				str += "single pass)";
			} else {
				str += "continuous)";
			}
			demo.setSts(str);
		}
	}
}
