#!/bin/zsh

deploymentsBranch="multiplayer-snake"
zipFolder="scalajvm/target/universal"

scriptPath=$(dirname "$0")
cd $scriptPath/..

/Users/vmunier/install/sbt/bin/sbt -Dsbt.log.noformat=true update test dist

zipArchive=$(find $zipFolder -name "*.zip")
mv -f $zipArchive deployments

cd deployments
git pull origin $deploymentsBranch
git add -A
git commit -m "new zip archive"
git push origin $deploymentsBranch
