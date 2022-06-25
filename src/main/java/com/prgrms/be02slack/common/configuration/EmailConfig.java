package com.prgrms.be02slack.common.configuration;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@PropertySource(value = "classpath:email.properties")
public class EmailConfig {

  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Value("${spring.mail.properties.mail.smtp.auth}")
  private boolean auth;

  @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
  private boolean enable;

  @Value("${spring.mail.properties.mail.smtp.starttls.required}")
  private boolean required;

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
    javaMailSender.setHost(host);
    javaMailSender.setPort(port);
    javaMailSender.setUsername(username);
    javaMailSender.setPassword(password);
    javaMailSender.setDefaultEncoding("UTF-8");
    javaMailSender.setJavaMailProperties(getMailProperties());

    return javaMailSender;
  }

  private Properties getMailProperties() {
    Properties properties = new Properties();
    properties.put("mail.smtp.auth", auth);
    properties.put("mail.transport.protocol", "smtp");
    properties.put("mail.smtp.starttls.enable", enable);
    properties.put("mail.smtp.starttls.required", required);

    return properties;
  }
}
