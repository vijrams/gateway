package com.wkelms.ebilling.edsscheduler.service.util

import com.wkelms.ebilling.edsscheduler.repository.JobDefinitionRepository
import com.wkelms.ebilling.edsscheduler.job.IBaseJob
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationContext
import com.wkelms.ebilling.edsscheduler.job.ExecutorJob

@Service
class BeanUtil {

    @Autowired
    private ApplicationContext context

    @Autowired
    public JobDefinitionRepository jdr

    public Object getJobInstance(String name){
        try {
            GroovyClassLoader gcl = new GroovyClassLoader(this.class.getClassLoader())
            def factory = context.getAutowireCapableBeanFactory()
            def jClass = jdr.findByName(name)
            Class c = gcl.parseClass(jClass.groovyClass)
            final Object ins = c.newInstance()
            factory.autowireBean(ins)
            return ins
        }catch(Exception e){
            throw e
        }
    }

    public Class getGroovyClass(String name) {
        GroovyClassLoader gcl = new GroovyClassLoader(this.class.getClassLoader())
        def jClass = jdr.findByName(name)
        if (jClass != null) {
            Class cls = gcl.parseClass(jClass.groovyClass)
            if (cls != null && IBaseJob.isAssignableFrom(cls)) return ExecutorJob
        }
        try {
            Class cls = gcl.loadClass(name)
            if (cls != null && QuartzJobBean.isAssignableFrom(cls)) return cls
            else
                throw new Exception("Job Class invalid")
        }catch(Exception e){
            throw new Exception("Job Class invalid")
        }
    }
}
