package com.example.SpringBatchStudy.job.MultipleStep;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MultipleStepJobConfig {

    @Bean
    public Job multipleStepJob(
            JobRepository jobRepository,
            Step multipleStep1,
            Step multipleStep2,
            Step multipleStep3
    ) {
        return new JobBuilder("multipleStepJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(multipleStep1)
                .next(multipleStep2)
                .next(multipleStep3)
                .build();
    }

    @JobScope
    @Bean
    public Step multipleStep1(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {

        return new StepBuilder("multipleStep1", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("step1");
                        return RepeatStatus.FINISHED;
                    }
                }, platformTransactionManager)
                .build();
    }

    @JobScope
    @Bean
    public Step multipleStep2(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("multipleStep2", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("step2");

                        ExecutionContext executionContext = chunkContext
                                .getStepContext()
                                .getStepExecution()
                                .getJobExecution()
                                .getExecutionContext();

                        executionContext.put("someKey", "hello!!");

                        return RepeatStatus.FINISHED;
                    }
                }, platformTransactionManager)
                .build();
    }

    @JobScope
    @Bean
    public Step multipleStep3(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("multipleStep2", jobRepository)
//                .tasklet(new Tasklet() {
//                    @Override
//                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                        System.out.println("step3");
//
//                        ExecutionContext executionContext = chunkContext
//                                .getStepContext()
//                                .getStepExecution()
//                                .getJobExecution()
//                                .getExecutionContext();
//
//                        executionContext.get("someKey");
//
//                        return RepeatStatus.FINISHED;
//                    }
//                }, platformTransactionManager)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step3");

                    ExecutionContext executionContext = chunkContext
                            .getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext();

                    System.out.println(executionContext.get("someKey"));

                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
}
