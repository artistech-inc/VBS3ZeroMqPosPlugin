#vbs3-pos-subscriber
This is a demo application for acting as a sink for listening to multiple VBS3ZeroMqPosPlugin instances and then publishing to multiple web-based clients.

##Requirements:
###Utilizing libjzmq.so under Ubuntu 14.04/14.10/15.04
 1. sudo apt-get install libzmq3 libzmq3-dev
 2. Install libjzmq - Java Binding for Ubuntu 14.04/14.10
   1. git clone https://github.com/zeromq/jzmq.git
   2. cd jzmq
   3. git checkout v3.1.0
   4. ./autogen.sh
   5. ./configure
   6. make
   7. sudo make install
   8. sudo ln -s /usr/local/lib/libjzmq.so /usr/lib/libjzmq.so
 3. Install libjzmq - Java Binding for Ubuntu 15.04
   1. sudo apt-get install libzmq-java
   2. If using Oracle Java, the libjzmq library must be able to be found in /usr/lib
   3. [REQUIRED] sudo ln -s /usr/lib/jni/libjzmq.so /usr/lib/libjzmq.so
   4. [OPTIONAL] sudo ln -s /usr/lib/jni/libjzmq.so.0 /usr/lib/libjzmq.so.0
   5. [OPTIONAL] sudo ln -s /usr/lib/jni/libjzmq.so.0.0.0 /usr/lib/libjzmq.so.0.0.0

### Windows and OS X
The default for OS X and Winows is to use jeromq intead of jzmq for ZeroMQ.

##Compile
1. git clone https://github.com/artistech-inc/VBS3ZeroMqPosPlugin.git
2. cd VBS3ZeroMqPosPlugin
3. cd vbs3-pos-subscriber
4. mvn clean package

##Run
By default, the JETTY_PORT_VALUE is 8888 unless otherwise specified on the command line. At least one VBS3 ZeroMQ publisher must be specified.  New to version 1.1 is the ability to convert from x/y coordinates to lon/lat.  To do this, a max/min value for each lon/lat must be provided on the command line.  If none is provided, the entire world is assumed using -180 to 180 and -90 to 90 for lon/lat respectfully.
```shell
java -jar target/vbs3-pos-subscriber-1.1.jar -z VBS3ZeroMqPosPlugin_SERVER_1:5551 -z VBS3ZeroMqPosPlugin_SERVER_2:5551 [-j JETTY_PORT_VALUE]
```
###To Specify Min/Max Lon/Lat
The command line arguments q, w, e, r are used.
 1. -q &lt;min_latitude_value&gt;
 2. -w &lt;min_longitude_value&gt;
 3. -e &lt;max_latitude_value&gt;
 4. -r &lt;max_longitude_value&gt;

##Viewing in Web-Browser
 1. Using the latest versions of Firefox and Google Chrome, view: http://vbs3-pos-subscriber_IP:JETTY_PORT_VALUE/vbs3_map.jsp
 2. Click "Start Receiving Data"

###Viewing in [sdt3d] (http://downloads.pf.itd.nrl.navy.mil/docs/sdt/sdt.html) using KML
 1. In a Web-Browser, download the KML file from: http://vbs3-pos-subscriber_IP:JETTY_PORT_VALUE/vbs3.kml
 2. Start sdt3d
 3. File -> KML -> Load KML File
 4. Open the downloaded file from #1

###Viewing in [sdt3d] (http://downloads.pf.itd.nrl.navy.mil/docs/sdt/sdt.html) using UDP
By default, vbs3-pos-subscriber uses multicast address 228.5.6.7 and port 5000, these values can be change on the command line if desired.
```shell
java -jar target/vbs3-pos-subscriber-1.1.jar -z VBS3ZeroMqPosPlugin_SERVER_1:5551 -z VBS3ZeroMqPosPlugin_SERVER_2:5551 [-p UDP_PORT] [-i UDP_ADDRESS]
```
 1. Start sdt3d
 2. File -> Listen to UDP Port...
 3. Specify UDP_PORT/UDP_ADDRESS (ex: 228.5.6.7/5000)

