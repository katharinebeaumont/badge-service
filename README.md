## Badge-Service

### Requirements:
- Java 1.8 and up
- Maven

### Installation and tests:
From the badge-service folder, run `mvn install`

### Start application:
From the badge-service folder, run  `java -jar target/badge-service-0.0.1-SNAPSHOT.jar`

### Add csv file of attendees:
Add file, using event-report.csv format as an example, to src/main/resources.
Then amend application.properties eventReport.path so it points to to the file.

### Startup issues:
Check the mapping in application.properties matches the headers in the CSV file. See event-report.csv for an example.
