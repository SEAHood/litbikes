#🔥litbikes🔥

![Lit Bikes](http://i.imgur.com/iZvD9D5.png "Lit Bikes")

##Deployment
1. Download/clone repo
2. Run build.sh or follow steps 3-5 below - running build.sh results in 'server.jar' inside the server directory
3. Run ```npm install``` inside client directory
4. Run ```gulp build+deploy``` inside client directory - if a 'file not found' error happens, just run this again
5. Run ```mvn package``` inside server directory - creates server JAR inside 'target' directory (LitBikesServer\*.jar)
6. Start web server by running ```java -cp <server JAR> com.litbikes.server.Launcher [-b <bot count>] [-s <game width> <game height>]```
7. Litbikes is now running on port 8080
