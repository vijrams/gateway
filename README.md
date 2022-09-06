# EDS Cron Service
[![pipeline status](https://gitlab.wkelms.com/collaboration-services/eds-scheduler/badges/master/pipeline.svg)](https://gitlab.wkelms.com/collaboration-services/eds-scheduler/commits/master)
[![version](https://img.shields.io/badge/Ver-1.0-blue.svg)]()

This project uses Spring Boot to implement the generic cron service that
takes in groovy class stored in database and runs in the job schedule created

## Features

- Uses embedded Tomcat 8
- Schedules new cron jobs
- unschedules / deletes cron jobs
- Gets a list of all scheduled jobs
- takes action in scheduled jobs (stop, start, pause, resume)
- rest endpoint for job definiton CRUD operation

## Endpoints
### Scheduler endpoints
---
* <code>GET</code> `/scheduler/jobs`

<small>Gets list of scheduled jobs</small>
```
Paramaters:NONE
```
---
* <code>GET</code> `/scheduler/jobs/{job_name}`

<small>Gets details of scheduled job for the specified job name</small>
```
Paramaters:
1. job_name		(required), Type = String, Path Parameter
```
---
* <code>GET</code> `/scheduler/jobs/history/{job_name}`

<small>Gets scheduled job execution history for the specified job name</small>
```
Paramaters:
1. job_name		(required), Type = String, Path Parameter
```
---
* <code>POST</code> `/scheduler/schedule`

<small>Schedules a new cron job with the given class and frequency</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
2. className 		 (required), Type = String, Request parameter
3. jobScheduleTime	 (required), Type = String, Request parameter (yyyy/MM/dd HH:mm)
4. cronExpression	 (required), Type = String, Request parameter
```
---
* <code>POST</code> `/scheduler/update`

<small>Updates cron job with the given job name with new frequency</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
2. jobScheduleTime	 (required), Type = String, Request parameter (yyyy/MM/dd HH:mm)
3. cronExpression	 (required), Type = String, Request parameter
```
---
* <code>GET</code> `/scheduler/unschedule`

<small>Unschedules cron job with the given job name</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
```
---
* <code>GET</code> `/scheduler/delete`

<small>Deletes cron job with the given job name</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
```
---
* <code>GET</code> `/scheduler/pause`

<small>Pauses cron job with the given job name</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
```
---
* <code>GET</code> `/scheduler/resume`

<small>Resumes cron job with the given job name</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
```
---
* <code>GET</code> `/scheduler/stop`

<small>Stops cron job with the given job name</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
```
---
* <code>GET</code> `/scheduler/start`

<small>Starts cron job with the given job name</small>
```
Paramaters:
1. jobName   		 (required), Type = String, Request parameter
```

### ReST endpoints
---
* <code>GET</code> `/api/jobDefinitions`

<small>Gets list of job definitions</small>
```
Paramaters: NONE
```
---

* <code>POST</code> `/api/jobDefinitions`

<small>Creates new job definition</small>
```
Paramaters:
1. name   		 (required), Type = String, Request parameter
2. packageName	 (required), Type = String, Request Parameter
3. className	 (required), Type = String, Request Parameter
4. groovyClass	 (required), Type = String, Request Parameter
```
- - -
* <code>PUT</code> `/api/jobDefinitions/{id}`

<small>Updates job definition for the given id</small>
```
Paramaters:
1. id			 (required), Type = Number, Path parameter
2. name   		 (required), Type = String, Request parameter
2. packageName	 (required), Type = String, Request Parameter
3. className	 (required), Type = String, Request Parameter
4. groovyClass	 (required), Type = String, Request Parameter
```
- - -
* <code>DELETE</code> `/api/jobDefinitions/{id}`

<small>Deletes job definition for the given id</small>
```
Paramaters: 
1. id			 (required), Type = Number, Path parameter
```

## Notes

This project require Gradle to build an run.

* To build the the project, from the root directory run: **gradle clean build**
* To upload the jar to nexus, from the root directory run : **gradle dist upload**
* To run junit tests , from the root directory run : **gradle test**
* To run the application, from the root directory run : **gradle bootrun**
* Prerequisite: run **/resources/data.sql** for creating postgres schema