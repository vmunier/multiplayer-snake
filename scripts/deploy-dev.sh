#!/bin/zsh

# http://askubuntu.com/questions/402832/how-do-i-allow-only-one-user-to-su-to-another-account
sudo -u vmunier zsh -c "/home/vmunier/install/sbt/bin/sbt -Dsbt.log.noformat=true update test dist"
