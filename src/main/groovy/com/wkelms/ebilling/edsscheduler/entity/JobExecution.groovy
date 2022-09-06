package com.wkelms.ebilling.edsscheduler.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "job_execution")
class JobExecution implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id

    @Column(unique = true, nullable = false)
    private String jobName

    @Column(unique = true, nullable = false)
    private long dateTime

    @Column(unique = true, nullable = false)
    private long duration

    @Column(unique = true, nullable = false)
    private String status

    @Column(length = 8000, nullable = false)
    private String message

    Long getId() {
        return id
    }

    void setId(Long id) {
        this.id = id
    }

    String getJobName() {
        return jobName
    }

    void setJobName(String jobName) {
        this.jobName = jobName
    }

    long getDateTime() {
        return dateTime
    }

    void setDateTime(long dateTime) {
        this.dateTime = dateTime
    }

    long getDuration() {
        return duration
    }

    void setDuration(long duration) {
        this.duration = duration
    }

    String getStatus() {
        return status
    }

    void setStatus(String status) {
        this.status = status
    }

    String getMessage() {
        return message
    }

    void setMessage(String message) {
        this.message = message
    }

    public Map asMap() {
        this.class.declaredFields.findAll { !it.synthetic }.collectEntries {
            [ (it.name):this."$it.name" ]
        }
    }

}
