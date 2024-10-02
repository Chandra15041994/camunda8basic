package com.example.workflow.config;

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaDatasourceConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultDatasourceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.workflow.model.Invoice;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.sql.DataSource;


@Configuration
public class HandlerConfiguration {

  protected static final Logger LOG = LoggerFactory.getLogger(HandlerConfiguration.class);

  @Bean(name="camundaBpmDataSource")
  public DataSource primaryDatabase(){
    return DataSourceBuilder.create().driverClassName("org.h2.Driver").url("jdbc:h2:file:./camunda-h2-database").username("sa").password("password").build();
  }


  @Bean(name="camundaBpmTransactionManager")
  public PlatformTransactionManager camundaTransactionManager(DataSource dataSource) {
       return new DataSourceTransactionManager(dataSource);
  }


  @Bean
  @ExternalTaskSubscription(
    topicName="invoiceCreator",
    autoOpen = false
    )
  public ExternalTaskHandler invoiceCreatorHandler() {
    return (externalTask, externalTaskService) -> {
      // instantiate an invoice object
      Invoice invoice = new Invoice();
      invoice.setId(UUID.randomUUID().toString());
      
      // create an object typed variable with the serialization format XML
      ObjectValue invoiceValue = ClientValues
          .objectValue(invoice)
          .serializationDataFormat("application/xml")
          .create();

      // add the invoice object and its id to a map
      Map<String, Object> variables = new HashMap<>();
      variables.put("invoiceId", invoice.getId());
      variables.put("invoice", invoiceValue);

      // select the scope of the variables
      boolean isRandomSample = Math.random() <= 0.5;
      if (isRandomSample) {
        externalTaskService.complete(externalTask, variables);
      } else {
        externalTaskService.complete(externalTask, null, variables);
      }

      LOG.info("The External Task {} has been completed!", externalTask.getId());
      

    };
  }

  @Bean
  @ExternalTaskSubscription(
      topicName = "invoiceArchiver",
      autoOpen = false
  )
  public ExternalTaskHandler invoiceArchiverHandler() {
    return (externalTask, externalTaskService) -> {
      TypedValue typedInvoice = externalTask.getVariableTyped("invoice");
      Invoice invoice = (Invoice) typedInvoice.getValue();
      LOG.info("Invoice on process scope archived: {}", invoice);
      externalTaskService.complete(externalTask);
    };
  }

}
