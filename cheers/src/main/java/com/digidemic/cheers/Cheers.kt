/**
 * Cheers v1.0.0 - https://github.com/Digidemic/Cheers
 * (c) 2023 DIGIDEMIC, LLC - All Rights Reserved
 * Cheers developed by Adam Steinberg of DIGIDEMIC, LLC
 * License: Apache License 2.0
 *
 * ====
 *
 * Copyright 2023 DIGIDEMIC, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digidemic.cheers

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Android Toast wrapper to queue and show multiple messages one at a time in sequential order.
 *
 * Standard Toast tends to overwrites the prior message if more than one is called too quickly.
 * Cheers instead queues each message, if one is already being displayed, then automatically displays each in order as they become available.
 *
 * Main functionality is in calling the constructor.
 * For other configurable settings, see [Cheers.GlobalConfig].
 *
 * @param activity active Activity. Needed for displaying Toast and finding if app is running in debug.
 * @param message message to display in Toast when available. Value calls [toString] before being passed as Toast text.
 * @param duration optional, defaults to [Cheers.LENGTH_SHORT] if undefined. How long the Toast displays for. Can be either [Cheers.LENGTH_SHORT] or [Cheers.LENGTH_LONG].
 * @param onlyShowIfDebugging optional, defaults to null if undefined. If true, Toast will only queue or show if running in debug. If false, Toast will always queue or show. If null (or undefined) will observe [GlobalConfig.onlyShowIfDebuggingAndNullConstructorParam] which by default is set to false (Toast will always queue or show), if true will only queue or show Toast if debugging.
 */
class Cheers(
    private val activity: Activity,
    message: Any,
    private val duration: CheerDuration = GlobalConfig.defaultCheerDuration,
    onlyShowIfDebugging: Boolean? = null,
) {
    /** How long the Toast should show for. See public variables in companion object */
    enum class CheerDuration(val toastLength: Int, val milliseconds: Long) {
        SHORT(Toast.LENGTH_SHORT, 2800),
        LONG(Toast.LENGTH_LONG, 4300),
    }

    /** Properties that can be updated at anytime which update the entirety of Cheers. See [defaultCheerDuration] and [onlyShowIfDebuggingAndNullConstructorParam] */
    object GlobalConfig {
        /**
         * If Cheers constructor argument [duration] is undefined, the following will be the default Toast duration.
         * This will affect every Cheer that does not pass [duration] into the constructor.
         */
        @Volatile
        var defaultCheerDuration: CheerDuration = LENGTH_SHORT

        /**
         * Global setting for if every Cheer should show only when debugging.
         *
         * Cheers constructor argument [onlyShowIfDebugging] MUST be null (or undefined) for variable to have any effect.
         * If so, when true, calling Cheers will only show Toast if app is debugging.
         * When false, calling Cheers will always show Toast, regardless if debugging or release.
         */
        @Volatile
        var onlyShowIfDebuggingAndNullConstructorParam = false
    }

    init {
        // Setup condition called only once per lifecycle.
        if (firstCallFromLifecycle) {
            firstCallFromLifecycle = false
            setFlagIfRunningInDebug()
        }

        // [onlyShowIfDebugging] if passed in constructor supersedes GlobalConfig.onlyShowIfDebuggingAndNullConstructorParam.
        if (when (onlyShowIfDebugging) {
                true -> isDebugging
                false -> true
                null -> !GlobalConfig.onlyShowIfDebuggingAndNullConstructorParam || isDebugging
            }
        ) {
            messageQueue.add(message.toString())

            // Double-check locking to reduce overhead of synchronization
            // https://www.baeldung.com/kotlin/singleton-classes#4-double-locking
            if (!queueRunning) {
                synchronized(syncLock) {
                    if (!queueRunning) {
                        queueRunning = true
                        GlobalScope.launch {
                            runningQueue()
                        }
                    }
                }
            }
        }
    }

    /** When queue is active, function called recursively until queue is empty. Initially only called by [init]. */
    private suspend fun runningQueue(failedAttempts: Int = 0) {
        if (failedAttempts >= MAX_FAILED_ATTEMPTS_TO_CHEER) {
            clearQueue()
        }

        if (messageQueue.isEmpty()) {
            queueRunning = false
            return
        }

        try {
            val nextMessage = messageQueue[0]
            messageQueue.removeAt(0)
            shout(nextMessage)
            runningQueue()
        } catch (e: Exception) {
            // Volatile variables updated here. Unlikely, but do not crash for exception.
            runningQueue(failedAttempts + 1)
        }
    }

    /** Shows new Toast message and starts proper delay before continuing */
    private suspend fun shout(message: String) {
        try {
            activity.runOnUiThread {
                toast?.cancel()
                toast = Toast.makeText(activity, message, duration.toastLength)
                toast?.show()
            }
            delay(duration.milliseconds)
        } catch (e: Exception) {
            // Volatile variables updated here. Unlikely, but do not crash for exception.
        }
    }

    /** Sets variable flag if application is running in debug. Only called by [init] */
    private fun setFlagIfRunningInDebug() {
        isDebugging = try {
            activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: Exception) {
            // Possible for ApplicationInfo to throw a RuntimeException.
            false
        }
    }

    companion object {

        /** Equivalent to [Toast.LENGTH_SHORT] */
        val LENGTH_SHORT = CheerDuration.SHORT

        /** Equivalent to [Toast.LENGTH_LONG] */
        val LENGTH_LONG = CheerDuration.LONG

        private const val MAX_FAILED_ATTEMPTS_TO_CHEER = 3

        private val syncLock = Any()

        @Volatile
        private var toast: Toast? = null

        @Volatile
        private var queueRunning = false

        @Volatile
        private var messageQueue = arrayListOf<String>()

        @Volatile
        private var firstCallFromLifecycle = true

        @Volatile
        private var isDebugging = false

        /**
         * Clears all pending messages. Function can be called at any time.
         */
        fun clearQueue() = messageQueue.clear()
    }
}