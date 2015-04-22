# MyAnkle-MetaWear
**Thesis:** Added wearable interface and feedback system in MyAnkle app

The changes made to existing MyAnkle source files are shown below:

**Directory:** src/com/ece1778/project/myAnkle/

**Change:** Added features to allow device selection and storage of device information
	
	Filename: ActivityCalibration.java
	Filename: ActivityMain.java
	Filename: ActivityMeasure.java
	Filename: FragmentTitle.java

**Change:** Added functionality to use MetaWear accelerometer for calibration and to store calibration readings for different devices in different files

    Filename: FragmentCalibration.java

**Change:** Added functionality to use MetaWear accelerometer and feedback system while performing a balance exercise
    
    Filename: FragmentExerciseMeasure.java

**Change:** Added user-interface features to request setup parameters for feedback system
    
    Filename: FragmentExerciseInstruction.java

**Change:** Application fails with EasyTracker dependencies so EasyTracker related code has been commented in the files below.
    
    Filename: ActivityInformation.java 
    Filename: ActivityProfile.java
    Filename: ActivitySettings.java
    Filename: ActivityTutorial.java
  	
**Directory:** src/com/ece1778/project/myAnkle/Helpers

**Change:** Added functionality to evaluate Aggregate BN and Transient BN every 500 milliseconds
    
    Filename: Samples.java

**Change:** Added feedback system that plays sounds of different tones based upon user’s stability	
	
	Filename: SoundPoolHelper.java

New files added to allow MetaWear support are listed below:

The purpose of the new files mentioned below is to establish connection and allow communication with a MetaWear anklet. 
The new files have been taken from the sample Android application provided by MetaWear developers. 
The files have been appropriately modified to support MyAnkle.


**Directory:** src/com/ece1778/project/myAnkle/metawear

**Role:** To generate and show a list of scanned MetaWear devices
	
	Filename: DeviceListAdapter.java

**Role:** A wrapper for storing MetaWear device name and MAC address
	
	Filename: ExtendedBluetoothDevice.java

**Role:** To propagate MetaWear information to MyAnkle Activity classes
	
	Filename: ModuleActivity.java

**Role:** To propagate MetaWear information to MyAnkle Fragment classes				
	
	Filename: ModuleFragment.java

**Role:** To scan for MetaWear (Bluetooth) devices 
	
	Filename: ScannerFragment.java

**Role:** To process scanned devices by extracting their information
	
	Filename: ScannerServiceParser.java
