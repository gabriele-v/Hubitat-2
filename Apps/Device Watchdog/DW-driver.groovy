/**
 *  ****************  Device Watchdog Tile Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the Device Watchdog data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.1 - 02/25/19 - Added Device Status attribute
 *  V1.0.0 - 01/28/19 - Initial release
 */

metadata {
	definition (name: "Device Watchdog Tile", namespace: "BPTWorld", author: "Bryan Turcotte") {
   		capability "Actuator"

		command "sendWatchdogActivityMap", ["string"]
		command "sendWatchdogBatteryMap", ["string"]
		command "sendWatchdogStatusMap", ["string"]
		
    	attribute "watchdogActivity", "string"
		attribute "watchdogBattery", "string"
		attribute "watchdogStatus", "string"
	}
	preferences() {    	
        section(""){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
            input "debugMode", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
//received new counts from Abacus - Intense Counting 
def sendWatchdogActivityMap(activityMap) {
    LOGDEBUG("In Device Watchdog Tile - Received new Activity data!")
	state.activityDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.activityDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${activityMap}</div>"
	state.activityDevice+= "</td></tr></table>"
	
	sendEvent(name: "watchdogActivity", value: state.activityDevice, displayed: true)
}

def sendWatchdogBatteryMap(batteryMap) {
    LOGDEBUG("In Device Watchdog Tile - Received new Battery data!")
	state.batteryDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.batteryDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${batteryMap}</div>"
	state.batteryDevice+= "</td></tr></table>"
	
	sendEvent(name: "watchdogBattery", value: state.batteryDevice, displayed: true)
}

def sendWatchdogStatusMap(statusMap) {
    LOGDEBUG("In Device Watchdog Tile - Received new Status data!")
	state.statusDevice = "<table width='100%'><tr><td width='10'><td align='left'>"
	state.statusDevice+= "<div style='line-height=50%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${statusMap}</div>"
	state.statusDevice+= "</td></tr></table>"
	
	sendEvent(name: "watchdogStatus", value: state.statusDevice, displayed: true)
}
	
def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
	
