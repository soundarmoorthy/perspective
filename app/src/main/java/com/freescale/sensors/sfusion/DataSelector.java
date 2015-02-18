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

import com.freescale.sensors.sfusion.FlicqActivity.GuiState;

/**
 * This class acts as a funnel for collecting data from either local or remote sensors.
 * ALL sensor data should be called via this class.  Only one instance of this class is present in the application.
 *
 * @author Michael Stanley
 */
public class DataSelector {
    FlicqActivity activity;
    SensorsWrapper choice = null;
    SensorsWrapper stopped = null;
    SensorsWrapper fixed = null;
    FlicqQuaternion zeroPosition = null;
    FlicqQuaternion workingQuaternion1 = null;
    FlicqQuaternion workingQuaternion2 = null;
    FlicqQuaternion workingQuaternion3 = null;
    float myBoxRotation;

    DataSelector(FlicqActivity activity) {
        this.activity = activity;
        myBoxRotation = 0;
        zeroPosition = new FlicqQuaternion();
        stopped = new SensorsWrapper(activity);
        fixed = new SensorsWrapper(activity);
        choice = stopped;
        workingQuaternion1 = new FlicqQuaternion();
        workingQuaternion2 = new FlicqQuaternion();
        workingQuaternion3 = new FlicqQuaternion();
    }

    public void updateSelection() {
        switch (activity.dataSource) {
            case LOCAL:
                choice = activity.localSensors;
                break;
            case REMOTE:
                choice = activity.flicqDevice;
                break;
            case STOPPED:
                choice = this.stopped;
                break;
            case FIXED:
                choice = this.fixed;
                break;
            default:
                // no change
        }
    }

    synchronized void adjustForZero(RotationVector rv, FlicqQuaternion q) {
        if (activity.zeroPending) {
            zeroPosition.set(q);
            zeroPosition.reverse();
            activity.zeroPending = false;
            activity.zeroed = true;
            rv.setIdentity();
        } else if (activity.zeroed) {
            workingQuaternion1.eqPxQ(zeroPosition, q);
            rv.computeFromQuaternion(workingQuaternion1, FlicqUtils.AngleUnits.DEGREES);
        } else {
            rv.computeFromQuaternion(q, FlicqUtils.AngleUnits.DEGREES);
        }
    }

    synchronized void getData(RotationVector rv, TimedQuaternion q, int screenRotation) {
        updateSelection();
        switch (activity.dataSource) {
            case LOCAL:
                activity.localSensors.computeQuaternion(workingQuaternion2, activity.algorithm);
                rv.computeFromQuaternion(workingQuaternion2, FlicqUtils.AngleUnits.DEGREES);
                q.set(workingQuaternion2);
                break;
            case REMOTE:
                activity.flicqDevice.computeQuaternion(workingQuaternion2, activity.algorithm);
                if (activity.dualModeRequired()) {
                    workingQuaternion3.set(activity.localSensors.quaternion());
                    workingQuaternion3.reverse();
                    workingQuaternion1.eqPxQ(workingQuaternion3, workingQuaternion2);
                    adjustForZero(rv, workingQuaternion1);
                    q.set(workingQuaternion2);
                } else {
                    adjustForZero(rv, workingQuaternion2);
                    q.set(workingQuaternion2);
                }
                break;
            case STOPPED:
                if (activity.guiState == GuiState.DEVICE) {
                    rv.set(FlicqUtils.AngleUnits.DEGREES, 0, 0, 0, 1);
                } else {
                    rv.set(FlicqUtils.AngleUnits.DEGREES, 0, 0, 0, 1);
                }
                break;
            case FIXED:
            default:
                if (activity.guiState == GuiState.DEVICE) {
                    rv.set(FlicqUtils.AngleUnits.DEGREES, myBoxRotation, 0.1f, 0.1f, 0.1f);
                    myBoxRotation += 1.0f;
                } else {
                    if (screenRotation == 0) {
                        rv.set(FlicqUtils.AngleUnits.DEGREES, myBoxRotation, 0.0f, 1.0f, 0.0f);
                        myBoxRotation += 0.2f;
                    } else {
                        rv.set(FlicqUtils.AngleUnits.DEGREES, myBoxRotation, 1.0f, 0.0f, 0.0f);
                        myBoxRotation -= 0.2f;
                    }
                }
                break;
        }
    }
}
