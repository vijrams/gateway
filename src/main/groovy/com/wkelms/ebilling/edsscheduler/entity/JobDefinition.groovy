package com.wkelms.ebilling.edsscheduler.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "job_definition")
class JobDefinition implements Serializable {
    private static final long serialVersionUID = 1L

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id

    @Column(unique = true, nullable = false)
    private String name

    @Column(nullable = false)
    private String packageName

    @Column(nullable = false)
    private String className

    @Column(length = 8000, nullable = false)
    private String groovyClass

    Long getId() {
        return id
    }

    void setId(Long id) {
        this.id = id
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getPackageName() {
        return packageName
    }

    void setPackageName(String packageName) {
        this.packageName = packageName
    }

    String getClassName() {
        return className
    }

    void setClassName(String className) {
        this.className = className
    }

    String getGroovyClass() {
        return groovyClass
    }

    void setGroovyClass(String groovyClass) {
        this.groovyClass = groovyClass
    }

}
