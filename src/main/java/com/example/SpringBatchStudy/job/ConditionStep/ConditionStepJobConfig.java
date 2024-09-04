package com.example.SpringBatchStudy.job.ConditionStep;

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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ConditionStepJobConfig {
    @Bean
    public Job conditionalStepJob(
            JobRepository jobRepository,
            Step conditionalStartStep,
            Step conditionalAllStep,
            Step conditionalFailStep,
            Step conditionalCompletedStep) {
        return new JobBuilder("conditionalStepJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(conditionalStartStep)
                .on("FAILED").to(conditionalFailStep)
                .from(conditionalStartStep)
                .on("COMPLETED").to(conditionalCompletedStep)
                .from(conditionalStartStep)
                .on("*").to(conditionalAllStep)
                .end()
                .build();
    }

    @JobScope
    @Bean
    public Step conditionalStartStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("conditionalStartStep", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//                        System.out.println("conditional Start Step");
//                        return RepeatStatus.FINISHED;
                        throw new Exception("Exception!!");
                    }
                }, platformTransactionManager)
                .build();
    }

    @JobScope
    @Bean
    public Step conditionalAllStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("conditionalAllStep", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("conditional All Step");
                        return RepeatStatus.FINISHED;
                    }
                }, platformTransactionManager)
                .build();
    }

    @JobScope
    @Bean
    public Step conditionalFailStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("conditionalFailStep", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("conditional Fail Step");
                        return RepeatStatus.FINISHED;
                    }
                }, platformTransactionManager)
                .build();
    }

    @JobScope
    @Bean
    public Step conditionalCompletedStep(
            JobRepository jobRepository,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("conditionalCompletedStep", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("conditional Completed Step");
                        return RepeatStatus.FINISHED;
                    }
                }, platformTransactionManager)
                .build();
    }
}
