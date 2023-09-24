/**
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

package com.digidemic.examplecheers

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.digidemic.cheers.Cheers
import com.digidemic.examplecheers.ui.theme.CheersTheme

val buttonModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 4.dp)

val textModifier = Modifier.padding(4.dp)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sample(this@MainActivity)
        }
    }
}

@Composable
fun Sample(activity: MainActivity?) {

    var isShortDuration by remember { mutableStateOf(true) }
    var isOnlyShowIfDebuggingAndNullConstructorParam by remember { mutableStateOf(false) }
    var withDefaultsCount by remember { mutableStateOf(1) }
    var alwaysShortCount by remember { mutableStateOf(1) }
    var alwaysLongCount by remember { mutableStateOf(1) }
    var onlyDebugCount by remember { mutableStateOf(1) }
    var anyBuildCount by remember { mutableStateOf(1) }

    CheersTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                SectionTitle("Cheers Demo App")
                SectionTitle("Global Settings (defaults)")
                Text(
                    text = "Toggling does not alter current queue",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = textModifier
                )
                Button(
                    onClick = {
                        isShortDuration = !isShortDuration
                        Cheers.GlobalConfig.defaultCheerDuration =
                            if (isShortDuration) Cheers.CheerDuration.SHORT
                            else Cheers.CheerDuration.LONG
                    }, modifier = buttonModifier
                ) {
                    Text(
                        "Cheers.GlobalConfig.defaultCheerDuration = " +
                                if (isShortDuration) "Cheers.LENGTH_SHORT"
                                else "Cheers.LENGTH_LONG"
                    )
                }
                Button(
                    onClick = {
                        isOnlyShowIfDebuggingAndNullConstructorParam = !isOnlyShowIfDebuggingAndNullConstructorParam
                        Cheers.GlobalConfig.onlyShowIfDebuggingAndNullConstructorParam = isOnlyShowIfDebuggingAndNullConstructorParam
                    }, modifier = buttonModifier
                ) {
                    Text(
                        "Cheers.GlobalConfig.defaultCheerDuration = " +
                                if (isOnlyShowIfDebuggingAndNullConstructorParam) "true"
                                else "false"
                    )
                }
                SectionTitle("Cheers Actions")
                CheersButton(
                    activity = activity!!,
                    message = "Cheers with defaults | Click $withDefaultsCount",
                    onCounterIncrease = { withDefaultsCount += 1 }
                )
                CheersButton(
                    activity = activity,
                    message = "Cheers always short duration | Click $alwaysShortCount",
                    duration = Cheers.LENGTH_SHORT,
                    onCounterIncrease = { alwaysShortCount += 1 }
                )
                CheersButton(
                    activity = activity,
                    message = "Cheers always long duration | Click $alwaysLongCount",
                    duration = Cheers.LENGTH_LONG,
                    onCounterIncrease = { alwaysLongCount += 1 }
                )
                CheersButton(
                    activity = activity,
                    message = "Cheers only for debug builds | Click $onlyDebugCount",
                    onlyShowIfDebugging = true,
                    onCounterIncrease = { onlyDebugCount += 1 }
                )
                CheersButton(
                    activity = activity,
                    message = "Cheers for any build regardless of defaults | Click $anyBuildCount",
                    onlyShowIfDebugging = false,
                    onCounterIncrease = { anyBuildCount += 1 }
                )
                SectionTitle("Other")
                Button(onClick = {
                    Cheers.clearQueue()
                }, modifier = buttonModifier
                ) {
                    Text(text = "Clear queue")
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = textModifier
    )
}

@Composable
fun CheersButton(
    activity: Activity,
    message: String,
    duration: Cheers.CheerDuration? = null,
    onlyShowIfDebugging: Boolean? = null,
    onCounterIncrease: () -> Unit
) {
    Button(onClick = {
        onCounterIncrease()
        if(duration != null) {
            Cheers(
                activity = activity,
                message = message,
                duration = duration,
                onlyShowIfDebugging = onlyShowIfDebugging
            )
        } else {
            Cheers(
                activity = activity,
                message = message,
                onlyShowIfDebugging = onlyShowIfDebugging
            )
        }
    }, modifier = buttonModifier
    ) {
        Text(message)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    Sample(activity = null)
}