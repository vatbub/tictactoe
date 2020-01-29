if [ "$TRAVIS_PULL_REQUEST" = "false" ] && [ "$TRAVIS_BRANCH" = "master" ]
then
  mvn org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar --settings target/travis/settings.xml
else
    mvn package --settings target/travis/settings.xml
fi