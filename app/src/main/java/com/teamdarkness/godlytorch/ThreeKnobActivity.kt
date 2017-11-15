/*
 * This file is part of Godly Torch.
 *
 *     Godly Torch is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Godly Torch is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Godly Torch. If not, see <http://www.gnu.org/licenses/>.
 */

package com.teamdarkness.godlytorch

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.chrisplus.rootmanager.RootManager
import com.chrisplus.rootmanager.container.Result
import com.sdsmdg.harjot.crollerTest.Croller
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper


class ThreeKnobActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false
    private var whiteOn = false
    private var yellowOn = false

    private var yellowValue = 0
    private var whiteValue = 0
    private var yellowLocation = "led:torch_0/brightness"
    private var whiteLocation = "led:torch_1/brightness"
    private var yellowProgress = 0
    private var whiteProgress = 0
    private var masterProgress = 0
    private var isUnsupported = true
    var deviceId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_knob)

        val bothCroller: Croller = findViewById(R.id.bothCroller)
        val whiteCroller: Croller = findViewById(R.id.whiteCroller)
        val yellowCroller: Croller = findViewById(R.id.yellowCroller)

        val deviceId = this.intent?.getStringExtra("device_id")
        val supportedDevices = resources.getStringArray(R.array.supported_devices)
        val whiteLedList = resources.getStringArray(R.array.device_white_led)
        val yellowLedList = resources.getStringArray(R.array.device_yellow_led)

        if (deviceId != null) {
            if (deviceId.isNotEmpty()) {
                for ((i, device) in supportedDevices.withIndex()) {
                    if (device == deviceId) {
                        isUnsupported = false
                        whiteLocation = whiteLedList[i]
                        yellowLocation = yellowLedList[i]
                    }
                }
            }
        }

        bothCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {

                if (progress != masterProgress) {
                    Log.i("onProgressChanged", progress.toString())
                    if (progress == 1) {
                        whiteCroller.isEnabled = true
                        yellowCroller.isEnabled = true
                        whiteValue = 0
                        yellowValue = 0
                        whiteOn = false
                        yellowOn = false
                    } else {
                        whiteCroller.isEnabled = false
                        yellowCroller.isEnabled = false
                        yellowOn = true
                        whiteOn = true
                        whiteValue = (255 / 20) * (progress - 1)
                        if (whiteValue > 225)
                            whiteValue = 225
                        yellowValue = (255 / 20) * (progress - 1)
                        if (yellowValue > 225)
                            yellowValue = 225
                        Log.i("onProgressChanged", "${whiteValue.toString()} | ${yellowValue.toString()}")
                    }
                    masterProgress = progress
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                Log.i("onStopTrackingTouch", "${whiteValue.toString()} | ${yellowValue.toString()}")
                if (yellowOn)
                    controlLed(whiteValue, yellowValue, true)
                else
                    controlLed(whiteValue, yellowValue, false)
            }
        });

        whiteCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {
                if (progress != whiteProgress) {
                    if (progress == 1) {
                        if (yellowCroller.progress == 1)
                            bothCroller.isEnabled = true
                        whiteValue = 0
                        whiteOn = false
                    } else {
                        bothCroller.isEnabled = false
                        whiteOn = true
                        whiteValue = (255 / 20) * (progress - 1)
                        if (whiteValue > 225)
                            whiteValue = 225
                    }
                    whiteProgress = progress
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                when {
                    whiteOn || yellowOn -> controlLed(whiteValue, yellowValue, true)
                    else -> controlLed(whiteValue, yellowValue, false)
                }
            }
        });

        yellowCroller.setOnCrollerChangeListener(object : OnCrollerChangeListener {
            override fun onProgressChanged(croller: Croller?, progress: Int) {
                if (progress != yellowProgress) {
                    if (progress == 1) {
                        if (whiteCroller.progress == 1)
                            bothCroller.isEnabled = true
                        yellowValue = 0
                        yellowOn = false
                    } else {
                        bothCroller.isEnabled = false
                        yellowOn = true
                        yellowValue = (255 / 20) * (progress - 1)
                        if (yellowValue > 225)
                            yellowValue = 225
                    }
                    yellowProgress = progress
                }
            }

            override fun onStartTrackingTouch(croller: Croller?) {
            }

            override fun onStopTrackingTouch(croller: Croller?) {
                when {
                    yellowOn || whiteOn -> controlLed(whiteValue, yellowValue, true)
                    else -> controlLed(whiteValue, yellowValue, false)
                }
            }
        });
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            val alertDialogBuilder = ProgressDialog.show(this, "Quit", "Please wait...")

            if (isUnsupported) {
                alertDialogBuilder.cancel()
                finishAffinity()
                return
            } else {
                alertDialogBuilder.setCancelable(false)
                alertDialogBuilder.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                alertDialogBuilder.show()
            }

            controlLed(0, 0, false)

            Handler().postDelayed({
                alertDialogBuilder.dismiss()
                finishAffinity()
            }, 1000)

            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)

    }

    private fun controlLed(whiteLed: Int = 0, yellowLed: Int = 0, torchState: Boolean = false) {

        runAsyncTask(@SuppressLint("StaticFieldLeak")
        object : AsyncTask<Void, Void, Result>() {
            override fun doInBackground(vararg p0: Void?): Result? {
                var torch = 0
                if (torchState)
                    torch = 255

                val command: String = String.format(getString(R.string.cmd_echo), "0", "led:switch/brightness") +
                        getString(R.string.cmd_sleep) +
                        String.format(getString(R.string.cmd_echo), whiteLed, whiteLocation) +
                        String.format(getString(R.string.cmd_echo), yellowLed, yellowLocation) +
                        String.format(getString(R.string.cmd_echo), torch, "led:switch/brightness")
                return RootManager.getInstance().runCommand(command)
            }

            override fun onPostExecute(result: Result?) {
                super.onPostExecute(result)
            }

        })
    }

    private fun <T> runAsyncTask(asyncTask: AsyncTask<T, *, *>, vararg params: T) {
        asyncTask.execute(*params)
    }
}