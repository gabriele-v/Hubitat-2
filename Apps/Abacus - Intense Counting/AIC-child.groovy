/**
 *  ****************  Abacus - Intense Counting Child ****************
 *
 *  Design Usage:
 *  Count how many times a Device is triggered. Displays Daily, Weekly, Monthly and Yearly counts!
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *-------------------------------------------------------------------------------------------------------------------
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
 *  V1.0.8 - 02/16/19 - Big maintenance release. Reworked a lot of code as I continue to learn new things.
 *  V1.0.7 - 01/25/19 - Create a tile so counts can be used on Dashboard
 *  V1.0.6 - 01/15/19 - Updated footer with update check and links
 *  V1.0.5 - 01/04/19 - Removed some left over code causing an error.
 *  V1.0.4 - 01/03/19 - Bug fixes and a much better way to remove a device and it's stats.
 *  V1.0.3 - 01/02/19 - Changed name. Cleaned up code.
 *  V1.0.2 - 01/01/19 - Fixed a typo in the countReset modules. Added in ability to count Thermostats! Again, wipe is recommended.
 *  V1.0.1 - 12/31/18 - Major rewrite to how the app finds new devices and sets them up for the first time. You will need to 
 *						delete any lines that have null in them or delete the child app and start over. Sorry.
 *  V1.0.0 - 12/30/18 - Initial release.
 *
 */

def setVersion() {
	state.version = "v1.0.8"
}

definition(
    name: "Abacus - Intense Counting Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Count how many times a Device is triggered. Displays Daily, Weekly, Monthly and Yearly counts!",
    category: "Useless",
	parent: "BPTWorld:Abacus - Intense Counting",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
	page(name: "pageCounts")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Intense Counting</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {
	display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Information</b>"
			paragraph "Daily counts are reset each morning.<br>Weekly counts are reset each Sunday.<br>Monthly counts are reset at on the 1st of each month.<br>Yearly counts get reset on Jan 1st.<br>All count resets happen between 12:05am and 12:10am"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageCounts", title: "Abacus - Intense Counting Report", description: "Click here to view the Abacus Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Most Common Devices")) {
			input(name: "switchEvent", type: "capability.switch", title: "Switch Device(s) to count", submitOnChange: true, required: false, multiple: true)
			input(name: "motionEvent", type: "capability.motionSensor", title: "Motion sensor(s) to count", submitOnChange: true, required: false, multiple: true)
			input(name: "contactEvent", type: "capability.contactSensor", title: "Contact Sensor(s) to count", submitOnChange: true, required: false, multiple: true)
			input(name: "thermostatEvent", type: "capability.thermostat", title: "Thermostat(s) to count", submitOnChange: true, required: false, multiple: true)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your counts on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Abacus - Counting Tile'<br> - Use our 'Abacus - Counting Tile' as the Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Abacus - Counting Tile<br>- Pick a template = attribute<br>- 3rd box = abacusMotion, abacusContact, abacusSwitch or abacusThermostat"
			}
		section() {
			input(name: "countTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Counts to:", submitOnChange: true, required: false, multiple: false)
			input("updateTime", "number", title: "How long between updates (in minutes)", required:true, defaultValue: 15)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
			input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	}
		display2()
	}
}

def pageCounts(params) {
	dynamicPage(name: "pageStatus", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Intense Counting</h2>", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		if(state.motionMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Motion Sensors")) {
				if(state.motionMap) {
					LOGDEBUG("In pageCounts...Motion Sensors")
					paragraph "${state.motionMap}"
				} else {
					LOGDEBUG("In pageCounts...Motion Sensors")
					paragraph "No Motion data to display."
				}
			}
		}
		if(state.contactMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Contact Sensors")) {
				if(state.contactMap) {
					LOGDEBUG("In pageCounts...Contact Sensors")
					paragraph "${state.contactMap}"
				} else {
					LOGDEBUG("In pageCounts...Contact Sensors")
					paragraph "No Contact data to display."
				}
			}
		}
		if(state.switchMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Switch Events")) {
				if(state.switchMap) {
					LOGDEBUG("In pageCounts...Switch Events")
					paragraph "${state.switchMap}"
				} else {
					LOGDEBUG("In pageCounts...Switch Events")
					paragraph "No Switch data to display."
				}
			}
		}
		if(state.thermostatMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Thermostat Events")) {
				if(state.thermostatMap) {
					LOGDEBUG("In pageCounts...Thermostat Events")
					paragraph "${state.thermostatMap}"
				} else {
					LOGDEBUG("In pageCounts...Thermostat Events")
					paragraph "No Thermostat data to display."
				}
			}
		}
		section() {
			if(state.motionMap == null && state.contactMap == null && state.switchMap == null && state.thermostatMap) {
				paragraph "No data to display."
			}
		}
		section() {
			paragraph getFormat("line")
			def rightNow = new Date()
			paragraph "<div style='color:#1A77C9'>Report generated: ${rightNow}</div>"
		}
	}
}

def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOGDEBUG("Updated with settings: ${settings}")
	unsubscribe()
	logCheck()
	initialize()
}

def initialize() {
	LOGDEBUG("In initialize...")
	setDefaults()
	subscribe(motionEvent, "motion.active", motionHandler)
	subscribe(contactEvent, "contact.open", contactHandler)
	subscribe(switchEvent, "switch.on", switchHandler)
	subscribe(thermostatEvent, "thermostatOperatingState", thermostatHandler)

	schedule("0 5 0 * * ? *", resetMotionCountHandler)
	schedule("0 6 0 * * ? *", resetContactCountHandler)
	schedule("0 7 0 * * ? *", resetSwitchCountHandler)
	schedule("0 8 0 * * ? *", resetThermostatCountHandler)
	
	if(countTileDevice) schedule("0 */${updateTime} * ? * *", countMapHandler)  	// send new Counts every XX minutes
}

def countMapHandler(evt) {
	def rightNow = new Date()
	
	def motionMap = "${state.motionMap}<br>${rightNow}"
	LOGDEBUG("In countMapHandler...Sending new Abacus Motion Counts to ${countTileDevice}")
    countTileDevice.sendMotionMap(motionMap)
	
	def contactMap = "${state.contactMap}<br>${rightNow}"
	LOGDEBUG("In countMapHandler...Sending new Abacus Contact Counts to ${countTileDevice}")
	countTileDevice.sendContactMap(contactMap)
	
	def switchMap = "${state.switchMap}<br>${rightNow}"
	LOGDEBUG("In countMapHandler...Sending new Abacus Switch Counts to ${countTileDevice}")
	countTileDevice.sendSwitchMap(switchMap)
	
	def thermostatMap = "${state.thermostatMap}<br>${rightNow}"
	LOGDEBUG("In countMapHandler...Sending new Abacus Thermostat Counts to ${countTileDevice}")
	countTileDevice.sendThermostatMap(thermostatMap)
}

def setupNewStuff() {
	LOGDEBUG("In setupNewStuff...")
	
	// ********** Starting Motion Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Motion Maps")
	
	if(state.motionMap == null) resetMotionMapHandler()
	if(state.motionMapD == null) resetMotionMapHandler()
	if(state.motionMapW == null) resetMotionMapHandler()
	if(state.motionMapM == null) resetMotionMapHandler()
	if(state.motionMapY == null) resetMotionMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Motion devices")
	motionEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.motionMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.motionMapD.put(it.displayName, 0)
		}
		if(state.motionMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.motionMapW.put(it.displayName, 0)
		}
		if(state.motionMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.motionMapM.put(it.displayName, 0)
		}
		if(state.motionMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.motionMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Motion Devices **********
	
	// ********** Starting Contact Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Contact Maps")
	
	if(state.contactMap == null) resetContactMapHandler()
	if(state.contactMapD == null) resetContactMapHandler()
	if(state.contactMapW == null) resetContactMapHandler()
	if(state.contactMapM == null) resetContactMapHandler()
	if(state.contactMapY == null) resetContactMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Contact devices")
	contactEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.contactMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.contactMapD.put(it.displayName, 0)
		}
		if(state.contactMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.contactMapW.put(it.displayName, 0)
		}
		if(state.contactMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.contactMapM.put(it.displayName, 0)
		}
		if(state.contactMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.contactMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Contact Devices **********
	
	// ********** Starting Switch Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Switch Maps")
	
	if(state.switchMap == null) resetSwitchMapHandler()
	if(state.switchMapD == null) resetSwitchMapHandler()
	if(state.switchMapW == null) resetSwitchMapHandler()
	if(state.switchMapM == null) resetSwitchMapHandler()
	if(state.switchMapY == null) resetSwitchMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Switch devices")
	switchEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.switchMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.switchMapD.put(it.displayName, 0)
		}
		if(state.switchMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.switchMapW.put(it.displayName, 0)
		}
		if(state.switchMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.switchMapM.put(it.displayName, 0)
		}
		if(state.switchMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.switchMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Switch Devices **********
	
	// ********** Starting Thermostat Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Thermostat Maps")
	
	if(state.thermostatMap == null) resetThermostatMapHandler()
	if(state.thermostatMapD == null) resetThermostatMapHandler()
	if(state.thermostatMapW == null) resetThermostatMapHandler()
	if(state.thermostatMapM == null) resetThermostatMapHandler()
	if(state.thermostatMapY == null) resetThermostatMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Thermostat devices")
	thermostatEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.thermostatMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.thermostatMapD.put(it.displayName, 0)
		}
		if(state.thermostatMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.thermostatMapW.put(it.displayName, 0)
		}
		if(state.thermostatMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.thermostatMapM.put(it.displayName, 0)
		}
		if(state.thermostatMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.thermostatMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Thermostat Devices **********
}

def motionHandler(evt) {
	LOGDEBUG("In motionHandler...")
	LOGDEBUG("In motionHandler: Device: $evt.displayName is $evt.value")
	state.motionMap = ""
	try {
		motionEvent.each { it -> 
			if(evt.displayName == it.displayName) {
				countD = state.motionMapD.get(evt.displayName)
   	 			newCountD = countD + 1
    			state.motionMapD.put(evt.displayName, newCountD)
	
				countW = state.motionMapW.get(evt.displayName)
   		 		newCountW = countW + 1
    			state.motionMapW.put(evt.displayName, newCountW)
	
				countM = state.motionMapM.get(evt.displayName)
   				newCountM = countM + 1
   	 			state.motionMapM.put(evt.displayName, newCountM)
	
    			countY = state.motionMapY.get(evt.displayName)
    			newCountY = countY + 1
    			state.motionMapY.put(evt.displayName, newCountY)
	
				LOGDEBUG("Adding NEW In - ${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
				state.motionMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
			} else {
				countD = state.motionMapD.get(it.displayName)
				countW = state.motionMapW.get(it.displayName)
				countM = state.motionMapM.get(it.displayName)
				countY = state.motionMapY.get(it.displayName)
				LOGDEBUG("Adding OLD In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
				state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			}
		}
	} 	
	catch (e) {
		
	}
}

def contactHandler(evt) {
	LOGDEBUG("In contactHandler...")
	LOGDEBUG("$evt.displayName: $evt.value")
	state.contactMap = ""
	try {
		contactEvent.each { it -> 
			if(evt.displayName == it.displayName) {
				countD = state.contactMapD.get(evt.displayName)
  			  	newCountD = countD + 1
    			state.contactMapD.put(evt.displayName, newCountD)
	
				countW = state.contactMapW.get(evt.displayName)
    			newCountW = countW + 1
    			state.contactMapW.put(evt.displayName, newCountW)
	
				countM = state.contactMapM.get(evt.displayName)
    			newCountM = countM + 1
    			state.contactMapM.put(evt.displayName, newCountM)
	
    			countY = state.contactMapY.get(evt.displayName)
    			newCountY = countY + 1
    			state.contactMapY.put(evt.displayName, newCountY)
	
				LOGDEBUG("Adding NEW In - ${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
				state.contactMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
			} else {
				countD = state.contactMapD.get(it.displayName)
				countW = state.contactMapW.get(it.displayName)
				countM = state.contactMapM.get(it.displayName)
				countY = state.contactMapY.get(it.displayName)
				LOGDEBUG("Adding OLD In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
				state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			}
		}
	} 	
	catch (e) {
		
	}
}

def switchHandler(evt) {
	LOGDEBUG("In switchHandler...")
	LOGDEBUG("$evt.displayName: $evt.value")
	state.switchMap = ""
	try {
		switchEvent.each { it -> 
			if(evt.displayName == it.displayName) {
				countD = state.switchMapD.get(evt.displayName)
    			newCountD = countD + 1
   				state.switchMapD.put(evt.displayName, newCountD)
	
				countW = state.switchMapW.get(evt.displayName)
    			newCountW = countW + 1
    			state.switchMapW.put(evt.displayName, newCountW)
	
				countM = state.switchMapM.get(evt.displayName)
    			newCountM = countM + 1
    			state.switchMapM.put(evt.displayName, newCountM)
	
    			countY = state.switchMapY.get(evt.displayName)
    			newCountY = countY + 1
    			state.switchMapY.put(evt.displayName, newCountY)
	
				LOGDEBUG("Adding NEW In - ${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
				state.switchMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
			} else {
				countD = state.switchMapD.get(it.displayName)
				countW = state.switchMapW.get(it.displayName)
				countM = state.switchMapM.get(it.displayName)
				countY = state.switchMapY.get(it.displayName)
				LOGDEBUG("Adding OLD In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
				state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			}
		}
	} 	
	catch (e) {
		
	}		
}

def thermostatHandler(evt) {
	state.tStat = evt.value
	LOGDEBUG("In thermostatHandler...Current Status: ${state.tStat}")
	if(state.tStat != "idle") {
		LOGDEBUG("In thermostatHandler...Starting to count: ${state.tStat}")
		state.thermostatMap = ""
		try {
			thermostatEvent.each { it -> 
				if(evt.displayName == it.displayName) {
					countD = state.thermostatMapD.get(evt.displayName)
    				newCountD = countD + 1
    				state.thermostatMapD.put(evt.displayName, newCountD)
	
					countW = state.thermostatMapW.get(evt.displayName)
   	 				newCountW = countW + 1
   	 				state.thermostatMapW.put(evt.displayName, newCountW)
	
					countM = state.thermostatMapM.get(evt.displayName)
    				newCountM = countM + 1
    				state.thermostatMapM.put(evt.displayName, newCountM)
	
   	 				countY = state.thermostatMapY.get(evt.displayName)
   	 				newCountY = countY + 1
    				state.thermostatMapY.put(evt.displayName, newCountY)
	
					LOGDEBUG("Adding NEW In - ${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
					state.thermostatMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
				} else {
					countD = state.thermostatMapD.get(it.displayName)
					countW = state.thermostatMapW.get(it.displayName)
					countM = state.thermostatMapM.get(it.displayName)
					countY = state.thermostatMapY.get(it.displayName)
					LOGDEBUG("Adding OLD In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
					state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
				}
			}
		} 	
		catch (e) {
		
		}
	} else {
		LOGDEBUG("In thermostatHandler...Nothing to do because it change to ${state.tStat}")
	}
}

def removingFromMap() {
	LOGDEBUG("In removingStuff...Time to Clean up the Maps")
	LOGDEBUG("In removingStuff...Checking Motion Map: ${state.motionMap}")
	if(state.motionMap) {
		try {
			state.motionMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(motionEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.motionMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.motionMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
	
	LOGDEBUG("In removingStuff...Checking Contact Map: ${state.contactMap}")
	if(state.contactMap) {
		try {
			state.contactMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(contactEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.contactMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.contactMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
	
	LOGDEBUG("In removingStuff...Checking Switch Map: ${state.switchMap}")
	if(state.switchMap) {
		try {
			state.switchMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(switchEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.switchMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.switchMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
	
	LOGDEBUG("In removingStuff...Checking Thermostat Map: ${state.thermostatMap}")
	if(state.thermostatMap) {
		try {
			state.thermostatMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(thermostatEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.thermostatMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.thermostaMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
}

def resetMotionMapHandler() {
	LOGDEBUG("In resetMotionMapHandler...")
	if(state.motionMap == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMap")
    	state.motionMap = [:]
		state.motionMap = ""
	}
	if(state.motionMapD == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapD")
    	state.motionMapD = [:]
		motionEvent.each { it -> state.motionMapD.put(it.displayName, 0)}
	}
	if(state.motionMapW == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapW")
    	state.motionMapW = [:]
		motionEvent.each { it -> state.motionMapW.put(it.displayName, 0)}
	}
	if(state.motionMapM == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapM")
    	state.motionMapM = [:]
		motionEvent.each { it -> state.motionMapM.put(it.displayName, 0)}
	}
	if(state.motionMapY == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapY")
    	state.motionMapY = [:]
		motionEvent.each { it -> state.motionMapY.put(it.displayName, 0)}
	}
}

def resetContactMapHandler() {
	LOGDEBUG("In resetContactMapHandler...")
	if(state.contactMap == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMap")
    	state.contactMap = [:]
		state.contactMap = ""
	}
	if(state.contactMapD == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapD")
    	state.contactMapD = [:]
		contactEvent.each { it -> state.contactMapD.put(it.displayName, 0)}
	}
	if(state.contactMapW == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapW")
    	state.contactMapW = [:]
		contactEvent.each { it -> state.contactMapW.put(it.displayName, 0)}
	}
	if(state.contactMapM == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapM")
    	state.contactMapM = [:]
		contactEvent.each { it -> state.contactMapM.put(it.displayName, 0)}
	}
	if(state.contactMapY == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapY")
    	state.contactMapY = [:]
		contactEvent.each { it -> state.contactMapY.put(it.displayName, 0)}
	}
}

def resetSwitchMapHandler() {
	LOGDEBUG("In resetSwitchMapHandler...")
	if(state.switchMap == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMap")
    	state.switchMap = [:]
		state.switchMap = ""
	}
	if(state.switchMapD == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapD")
    	state.switchMapD = [:]
		switchEvent.each { it -> state.switchMapD.put(it.displayName, 0)}
	}
	if(state.switchMapW == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapW")
    	state.switchMapW = [:]
		switchEvent.each { it -> state.switchMapW.put(it.displayName, 0)}
	}
	if(state.switchMapM == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapM")
    	state.switchMapM = [:]
		switchEvent.each { it -> state.switchMapM.put(it.displayName, 0)}
	}
	if(state.switchMapY == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapY")
    	state.switchMapY = [:]
		switchEvent.each { it -> state.switchMapY.put(it.displayName, 0)}
	}
}

def resetThermostatMapHandler() {
	LOGDEBUG("In resetThermostatMapHandler...")
	if(state.thermostatMap == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMap")
    	state.thermostatMap = [:]
		state.thermostatMap = ""
	}
	if(state.thermostatMapD == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapD")
    	state.thermostatMapD = [:]
		thermostatEvent.each { it -> state.thermostatMapD.put(it.displayName, 0)}
	}
	if(state.thermostatMapW == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapW")
    	state.thermostatMapW = [:]
		thermostatEvent.each { it -> state.thermostatMapW.put(it.displayName, 0)}
	}
	if(state.thermostatMapM == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapM")
    	state.thermostatMapM = [:]
		thermostatEvent.each { it -> state.thermostatMapM.put(it.displayName, 0)}
	}
	if(state.thermostatMapY == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapY")
    	state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
}

def resetMotionCountHandler() {
	LOGDEBUG("In resetMotionCountHandler...")
	// Resetting Daily Counter
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			newCountD = 0
			state.motionMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
		}
    	state.motionMapD = [:]
		motionEvent.each { it -> state.motionMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			newCountW = 0
			state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
		}
		state.motionMapW = [:]
		motionEvent.each { it -> state.motionMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			newCountM = 0
			state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
		}
		state.motionMapM = [:]
		motionEvent.each { it -> state.motionMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			newCountY = 0
			state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
		}
		state.motionMapY = [:]
		motionEvent.each { it -> state.motionMapY.put(it.displayName, 0)}
	}
}

def resetContactCountHandler() {
	LOGDEBUG("In resetContactCountHandler...")
	// Resetting Daily Counter
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			newCountD = 0
			state.contactMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
		}
    	state.contactMapD = [:]
		contactEvent.each { it -> state.contactMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	LOGDEBUG("In resetContactCountHandler...dayOfWeek: ${dayOfWeek}")
	if(dayOfWeek == 1) {
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			newCountW = 0
			state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
		}
		state.contactMapW = [:]
		contactEvent.each { it -> state.contactMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			newCountM = 0
			state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
		}
		state.contactMapM = [:]
		contactEvent.each { it -> state.contactMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			newCountY = 0
			state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
		}
		state.contactMapY = [:]
		contactEvent.each { it -> state.contactMapY.put(it.displayName, 0)}
	}
}

def resetSwitchCountHandler() {
	LOGDEBUG("In resetSwitchCountHandler...")
	// Resetting Daily Counter
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			newCountD = 0
			state.switchMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
		}
    	state.switchMapD = [:]
		switchEvent.each { it -> state.switchMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			newCountW = 0
			state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
		}
		state.switchMapW = [:]
		switchEvent.each { it -> state.switchMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			newCountM = 0
			state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
		}
		state.switchMapM = [:]
		switchEvent.each { it -> state.switchMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			newCountY = 0
			state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
		}
		state.switchMapY = [:]
		switchEvent.each { it -> state.switchMapY.put(it.displayName, 0)}
	}
}

def resetThermostatCountHandler() {
	LOGDEBUG("In resetThermostatCountHandler...")
	// Resetting Daily Counter
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			newCountD = 0
			state.thermostatMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
		}
    	state.thermostatMapD = [:]
		thermostatEvent.each { it -> state.thermostatMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			newCountW = 0
			state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
		}
		state.thermostatMapW = [:]
		thermostatEvent.each { it -> state.thermostatMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			newCountM = 0
			state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
		}
		state.thermostatMapM = [:]
		thermostatEvent.each { it -> state.thermostatMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			newCountY = 0
			state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
		}
		state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
}

def sendMessage(msg) {
	LOGDEBUG("${msg}")
    if (pushNotification) {
        sendPush(msg)
    }
}

// ********** Normal Stuff **********

def pauseOrNot(){							// Modified from @Cobra Code
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def setDefaults(){
	setVersion()
	setupNewStuff()
	removingFromMap()
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	
	editALine = "false"
	whichMap = ""
	lineToEdit = ""
	ecountD = 0
	ecountW = 0
	ecountM = 0
	ecountY = 0
}

def logCheck(){									// Modified from @Cobra Code
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){								// Modified from @Cobra Code
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {							// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){					// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Abacus - Intense Counting - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
