#🔥litbikes🔥

![Lit Bikes](http://i.imgur.com/iZvD9D5.png "Lit Bikes")

##Deployment
1. Download/clone repo
2. Run ```npm install``` inside client directory
3. Run ```gulp build+deploy``` inside client directory - if a 'file not found' error happens, just run this again
4. Run ```mvn package``` inside server directory - creates server JAR inside target folder (LitBikesServer\*.jar)
5. The above steps can also be achieved my running build.sh - results in 'server.jar' inside the server directory
6. Start web server by running ```java -cp \<server JAR\> com.litbikes.server.WebServer```
7. Start web server by running ```java -cp \<server JAR\> com.litbikes.server.GameServer```
8. Litbikes is now running on port 8080
