#!/bin/bash
cd client
npm install
gulp build+deploy
cd ../server
mvn package
echo "Moving server.jar to server directory"
cp target/LitBikesServer*.jar ./server.jar
echo "Finished build"
