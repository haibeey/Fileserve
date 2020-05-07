## Fileserver  
This is an android application that embeds a custom minimal file server. It can serves files over http through the browser. See the screenshot folder for an example  

### Use Case   
Say you are on a VPN or peer network such as WLAN. you can use this app to serve files to other connected devices.   
It has no UI. just launch the app and head to your browser on port 6646 i.e localhost:6646. you can share your IP address with users on the same network so they could acess your files. 

## Issues 
Currently the app can't serve files in the root directory on andriod 10 . This is because of the strict files permission in such device. Although if you have root access this won't be a problem.  


