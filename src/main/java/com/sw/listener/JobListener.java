package com.sw.listener;

import com.sw.config.ApplicationPropertiesConfig;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Objects;

import static com.sw.config.Constants.SWA_CONFLICT_FILE;
import static com.sw.config.Constants.SWW_EXCEPTION_FILE;

@Component
public class JobListener implements JobExecutionListener {

    private final JavaMailSender mailSender;
    private final ApplicationPropertiesConfig config;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public JobListener(ThreadPoolTaskExecutor threadPoolTaskExecutor, JavaMailSender mailSender, ApplicationPropertiesConfig config) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.mailSender = mailSender;
        this.config = config;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        if (!config.isEmailEnabled()) {
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(config.getEmailFrom());
        mail.setTo(config.getEmailTo());
        mail.setSubject(String.format("%s is starting", jobName));
        mail.setText(String.format("We are informing you that %s is starting", jobName));

        mailSender.send(mail);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        String jobName = jobExecution.getJobInstance().getJobName();

        if (!config.isEmailEnabled()) {
            threadPoolTaskExecutor.shutdown();
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper mail = new MimeMessageHelper(message, true);
            mail.setFrom(config.getEmailFrom());
            mail.setTo(config.getEmailTo());
            mail.setSubject(String.format("%s has completed", jobName));
            mail.setText(String.format("We are informing you that %s has completed", jobName));

            String path = (String) jobExecution.getJobParameters().getParameters().get("path").getValue();

            FileSystemResource swaConflictFile = new FileSystemResource(path + SWA_CONFLICT_FILE);
            mail.addAttachment(Objects.requireNonNull(swaConflictFile.getFilename()), swaConflictFile);

            FileSystemResource swwExceptionFile = new FileSystemResource(path + SWW_EXCEPTION_FILE);
            mail.addAttachment(Objects.requireNonNull(swwExceptionFile.getFilename()), swwExceptionFile);

        } catch (MessagingException e) {
            throw new MailParseException(e);
        }

        mailSender.send(message);
        threadPoolTaskExecutor.shutdown();
    }
}