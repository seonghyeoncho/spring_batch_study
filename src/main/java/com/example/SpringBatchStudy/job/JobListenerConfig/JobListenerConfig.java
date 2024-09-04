package com.example.SpringBatchStudy.job.JobListenerConfig;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobListenerConfig {

    // 실행할 때 잡 이름을 파라미터로 넘겨줘야함
    @Bean
    public Job jobListenerJob (JobRepository jobRepository, Step jobListenerStep) {
        return new JobBuilder("jobListenerJob", jobRepository)
                // 시퀀스를 순차적으로 부여
                .incrementer(new RunIdIncrementer())
                // 잡 리스너 등록
                .listener(new JobLoggerListener())
                .start(jobListenerStep)
                .build();
    }

    @JobScope
    @Bean
    public Step jobListenerStep(
            JobRepository jobRepository,
            Tasklet jobListenerTasklet,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("jobListenerStep", jobRepository)
                .tasklet(jobListenerTasklet, transactionManager)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet jobListenerTasklet() {
        return ((contribution, chunkContext) -> {
            System.out.println("Job Listener Tasklet");
            return RepeatStatus.FINISHED;
        });
    }
}
