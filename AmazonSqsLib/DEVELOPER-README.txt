The included gradle build tool and configuration can be used to automatically download the aws-java-sdk-sqs-*.jar and dependecies too keep up with bundled CloverDX versions of AWS SDK.

Usage:
  1. edit build.gradle line 
	   com.amazonaws:aws-java-sdk-bom:desired-version-number
  2. run the lib task using the gradle wrapper
	   gradlew.bat lib
  3. compare downloaded .jars with what is bundled with clover and delete duplicates
  4. update .classpath 
  5. test the library