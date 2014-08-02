#!/bin/zsh

/Users/vmunier/install/sbt/bin/sbt -Dsbt.log.noformat=true update test dist

zipArchive=`find . -name "play-game-*zip"`

mv -f $zipArchive deployments

cd deployments
git add -A
git commit -m "new zip archive"
git push origin multiplayer-snake
