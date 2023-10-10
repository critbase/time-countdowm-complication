/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.complicationsdatasource

import android.content.ComponentName
import android.support.wearable.complications.ComplicationText
import android.support.wearable.complications.ComplicationText.TimeDifferenceBuilder
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountDownTimeReference
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.android.wearable.complicationsdatasource.data.TAP_COUNTER_PREF_KEY
import com.example.android.wearable.complicationsdatasource.data.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Example watch face complication data source provides a number that can be incremented on tap.
 *
 * Note: This class uses the suspending variation of complication data source service to support
 * async calls to the data layer, that is, to the DataStore saving the persistent values.
 */
class CustomComplicationDataSourceService : SuspendingComplicationDataSourceService() {

    /*
     * Called when a complication has been activated. The method is for any one-time
     * (per complication) set-up.
     *
     * You can continue sending data for the active complicationId until onComplicationDeactivated()
     * is called.
     */
    override fun onComplicationActivated(
        complicationInstanceId: Int,
        type: ComplicationType
    ) {
        Log.d(TAG, "onComplicationActivated(): $complicationInstanceId")
    }

    /*
     * A request for representative preview data for the complication, for use in the editor UI.
     * Preview data is assumed to be static per type. E.g. for a complication that displays the
     * date and time of an event, rather than returning the real time it should return a fixed date
     * and time such as 10:10 Aug 1st.
     *
     * This will be called on a background thread.
     */
    override fun getPreviewData(type: ComplicationType): ComplicationData {
        return RangedValueComplicationData.Builder(
            value = 4f,
            min = 0f,
            //max = ComplicationTapBroadcastReceiver.MAX_NUMBER.toFloat(),
            max = 10f,
            contentDescription = PlainComplicationText
                .Builder(text = "Ranged Value version of Number.").build()
        ).build()
    }

    /*
     * Called when the complication needs updated data from your data source. There are four
     * scenarios when this will happen:
     *
     *   1. An active watch face complication is changed to use this data source
     *   2. A complication using this data source becomes active
     *   3. The period of time you specified in the manifest has elapsed (UPDATE_PERIOD_SECONDS)
     *   4. You triggered an update from your own class via the
     *       ComplicationDataSourceUpdateRequester.requestUpdate method.
     */
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        return when (request.complicationType) {

            ComplicationType.RANGED_VALUE -> {
                // TODO: set time of day on watch somehow, per complication?
                val curvalue = Calendar.getInstance().timeInMillis
                var mincal = Calendar.getInstance()
                mincal.set(Calendar.HOUR_OF_DAY, 22)
                mincal.set(Calendar.MINUTE, 0)
                // check if the mincal time is greater than curvalue; if so, set the day back one
                if (mincal.timeInMillis > curvalue) {
                    mincal.add(Calendar.DATE, -1)
                }
                var maxcal = Calendar.getInstance()
                maxcal.set(Calendar.HOUR_OF_DAY, 22)
                maxcal.set(Calendar.MINUTE, 0)
                // check if the curvalue is greater than maxcal time; if so, set the maxcal forward one day
                if (curvalue > maxcal.timeInMillis) {
                    maxcal.add(Calendar.DATE, 1)
                }
                val minnum = mincal.timeInMillis
                val maxnum = maxcal.timeInMillis
                return RangedValueComplicationData.Builder(
                    value = curvalue.toFloat(),
                    min = minnum.toFloat(),
                    //max = ComplicationTapBroadcastReceiver.MAX_NUMBER.toFloat(),
                    max = maxnum.toFloat(),
                    contentDescription = PlainComplicationText
                        .Builder(text = "Ranged Value version of Number.").build()
                )
                    .setText(TimeDifferenceComplicationText.Builder(
                        style = TimeDifferenceStyle.SHORT_DUAL_UNIT,
                        countDownTimeReference = CountDownTimeReference(Instant.ofEpochMilli(maxnum))
                    ).build())
                    .build()
            }

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                null
            }
        }
    }

    /*
     * Called when the complication has been deactivated.
     */
    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        Log.d(TAG, "onComplicationDeactivated(): $complicationInstanceId")
    }

    companion object {
        private const val TAG = "CompDataSourceService"
    }
}
