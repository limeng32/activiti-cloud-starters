package org.activiti.cloud.starter.configuration;

import org.activiti.services.audit.AuditConsumerChannels;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@EnableJpaRepositories("org.activiti.services.audit")
@EntityScan(basePackages = {"org.activiti.services.audit.events","org.activiti.services.audit.events.model"})
@EnableBinding(AuditConsumerChannels.class)
@Inherited
@EnableDiscoveryClient
public @interface EnableActivitiAudit {

}