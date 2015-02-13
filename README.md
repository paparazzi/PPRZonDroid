PPRZonDroid
===========

PPRZonDroid is an Android application for controlling [Paparazzi UAS] (http://paparazziuav.org).

The user documentation can be found at http://http://wiki.paparazziuav.org/wiki/PPRZonDroid


Released version
----------------

**Please download & compile latest app_server.c from paparazzi repository to be compatible for this version.**

PPRZonDroid is now available on Google Play


How to open in android studio
-----------------------------

* Clone the source on your local computer with [Android Studio] (http://developer.android.com/sdk/installing/studio.html) installed.
* Open Android Studio and choose "import project", then select your project folder.
* Synchronize your project with Gradle files.
* Run the application, if your android device is connected in debug mode, the newly compiled program can be directly uploaded.


Google Map API
--------------

Using Google Map API requires a key (in app/src/AndroidManifest.xml file) and a keystore file (in app/key/debug.keystore).
You can use the set of keys for debugging purpose but not for releasing the application.
You can also use your own keys by changing the relevant files (app/build.gradle holds the path to the local keystore file, remove the line in order to use your default key (usually in ~/.android folder)


How to install and run from Paparazzi
-------------------------------------

The GCS should run at least the server agent (and link/simulator to get data from aircraft) and App Server application from the tool menu

Connect your android device by Wifi to your ground station.
Copy the apk file to your android device and select it in your file browser to install it.
Run the newly installed application.
Set the network settings (IP of server, TCP/UDP ports, password), your aircraft should appear soon (left panel shows the list of aircraft, select one to center the aircraft).

In case of trouble, kill the android app and start again and/or restart the ground agent in verbose mode (option -v)

