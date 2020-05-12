# Convert All Apache Geode Statistics to Micrometer Gauges
## Description

This project describes a way to convert all existing Geode Statistics to Micrometer Gauges and to push those Gauges to Wavefront using a Spring Boot Micrometer Wavefront registry.

The project provides:

* a **GetAllMetricsFunction** that returns a server's Statistics as a list of Metrics
* a Spring Boot client application defining a **MetricsProvider** that provides the Micrometer support

The **GetAllMetricsFunction**:

* iterates all the existing Statistics
* for each one, converts it to a Metric
* returns the list of all Metrics


The **MetricsProvider** provides the Micrometer support by:

* periodically executing the GetAllMetricsFunction on all servers
* for each returned Metric, creating and registering a Gauge in the MeterRegistry if one doesn't already exist
* for each returned Metric, updating the current value of the Metric in its map of current values
* providing a method to get the latest value for each Metric

## Initialization
Modify the **GEODE** environment variable in the *setenv.sh* script to point to a Geode installation directory.
## Build
Build the Spring Boot Client Application and GetAllMetricsFunction using the *gradlew* command like:

```
./gradlew clean jar bootJar
```
## Run Example
### Start and Configure Locator and Servers
Start and configure the locator and 3 servers using the *startandconfigure.sh* script like:

```
./startandconfigure.sh
```
### Start Client
Run the client to register Gauges and publish them to Wavefront using the *runclient.sh* script like:

```
./runclient.sh
```
### Shutdown Locator and Servers
Execute the *shutdownall.sh* script to shutdown the servers and locators like:

```
./shutdownall.sh
```
### Remove Locator and Server Files
Execute the *cleanupfiles.sh* script to remove the server and locator files like:

```
./cleanupfiles.sh
```
## Example Sample Output
### Start and Configure Locator and Servers
Sample output from the *startandconfigure.sh* script is:

```
./startandconfigure.sh 
1. Executing - start locator --name=locator

....
Locator in <working-directory>/locator on localhost[10334] as locator is currently online.
Process ID: 83445
Uptime: 6 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/locator/locator.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host= localhost, port=1099]

Cluster configuration service is up and running.

2. Executing - set variable --name=APP_RESULT_VIEWER --value=any

Value for variable APP_RESULT_VIEWER is now: any.

3. Executing - start server --name=server-1 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

....
Server in <working-directory>/server-1 on localhost[49689] as server-1 is currently online.
Process ID: 83468
Uptime: 4 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

4. Executing - start server --name=server-2 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

....
Server in <working-directory>/server-2 on localhost[49712] as server-2 is currently online.
Process ID: 83469
Uptime: 3 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

5. Executing - start server --name=server-3 --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

....
Server in <working-directory>/server-3 on localhost[49739] as server-3 is currently online.
Process ID: 83479
Uptime: 3 seconds
Geode Version: 1.9.2
Java Version: 1.8.0_121
Log File: <working-directory>/server-3/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

6. Executing - list members

  Name   | Id
-------- | --------------------------------------------------------------
locator  | localhost(locator:83445:locator)<ec><v0>:41000 [Coordinator]
server-1 | localhost(server-1:83468)<v1>:41001
server-2 | localhost(server-2:83469)<v2>:41002
server-3 | localhost(server-3:83479)<v3>:41003

7. Executing - deploy --jar=server/build/libs/server-0.0.1-SNAPSHOT.jar

 Member  |       Deployed JAR        | Deployed JAR Location
-------- | ------------------------- | --------------------------------------------------------------------------------------------------------
server-1 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-1/server-0.0.1-SNAPSHOT.v1.jar
server-2 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-2/server-0.0.1-SNAPSHOT.v1.jar
server-3 | server-0.0.1-SNAPSHOT.jar | <working-directory>/server-3/server-0.0.1-SNAPSHOT.v1.jar

8. Executing - list functions

 Member  | Function
-------- | ---------------------
server-1 | GetAllMetricsFunction
server-2 | GetAllMetricsFunction
server-3 | GetAllMetricsFunction
```
### Start Client
Sample output from the *runclient.sh* script shows Gauges being registered like:

```
./runclient.sh

> Task :client:bootRun

2020-05-11 16:38:05.927  INFO 83928 --- [           main] example.client.Client                    : Starting Client on localhost with PID 83928
2020-05-11 16:38:08.494  INFO 83928 --- [           main] example.client.Client                    : Started Client in 2.872 seconds (JVM running for 3.219)
2020-05-11 16:38:08.663  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Created metrics map for server=server-3
2020-05-11 16:38:08.663  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-3; category=DistributionStats; type=distributionStats; metric=sentMessages
2020-05-11 16:38:08.664  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-3; category=DistributionStats; type=distributionStats; metric=commitMessages
2020-05-11 16:38:08.664  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-3; category=DistributionStats; type=distributionStats; metric=commitWaits
2020-05-11 16:38:08.664  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-3; category=DistributionStats; type=distributionStats; metric=sentMessagesTime
2020-05-11 16:38:08.664  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-3; category=DistributionStats; type=distributionStats; metric=sentMessagesMaxTime
...
2020-05-11 16:38:08.704  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Created metrics map for server=server-2
2020-05-11 16:38:08.704  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-2; category=DistributionStats; type=distributionStats; metric=sentMessages
2020-05-11 16:38:08.704  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-2; category=DistributionStats; type=distributionStats; metric=commitMessages
2020-05-11 16:38:08.704  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-2; category=DistributionStats; type=distributionStats; metric=commitWaits
2020-05-11 16:38:08.704  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-2; category=DistributionStats; type=distributionStats; metric=sentMessagesTime
2020-05-11 16:38:08.704  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-2; category=DistributionStats; type=distributionStats; metric=sentMessagesMaxTime
...
2020-05-11 16:38:08.720  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Created metrics map for server=server-1
2020-05-11 16:38:08.720  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-1; category=DistributionStats; type=distributionStats; metric=sentMessages
2020-05-11 16:38:08.720  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-1; category=DistributionStats; type=distributionStats; metric=commitMessages
2020-05-11 16:38:08.720  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-1; category=DistributionStats; type=distributionStats; metric=commitWaits
2020-05-11 16:38:08.720  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-1; category=DistributionStats; type=distributionStats; metric=sentMessagesTime
2020-05-11 16:38:08.720  INFO 83928 --- [   scheduling-1] example.client.metrics.MetricsProvider   : Registered gauge for server=server-1; category=DistributionStats; type=distributionStats; metric=sentMessagesMaxTime
...
```
### Shutdown Locator and Servers
Sample output from the *shutdownall.sh* script is:

```
./shutdownall.sh 

(1) Executing - connect

Connecting to Locator at [host=localhost, port=10334] ..
Connecting to Manager at [host= localhost, port=1099] ..
Successfully connected to: [host= localhost, port=1099]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered
```
