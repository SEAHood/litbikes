#!/bin/bash
cd client
npm install
gulp build+deploy
cd ../server
mvn package
cp target/LitBikesServer*.jar ./server.jar
echo "Finished build"
