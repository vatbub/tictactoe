if [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ "$TRAVIS_BRANCH" = "master" ]
then
  mvn deploy --settings target/travis/settings.xml -DskipTests=true
  cd server || return
  mvn heroku:deploy --settings ../target/travis/settings.xml -DskipTests=true
fi