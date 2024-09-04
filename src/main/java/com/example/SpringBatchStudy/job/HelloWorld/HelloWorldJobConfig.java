package com.example.SpringBatchStudy.job.HelloWorld;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HelloWorldJobConfig {

// 실행할 때 잡 이름을 파라미터로 넘겨줘야함
    @Bean
    public Job helloWorldJob (JobRepository jobRepository, Step helloWorldStep) {
        return new JobBuilder("helloWorldJob", jobRepository)
                // 시퀀스를 순차적으로 부여
                .incrementer(new RunIdIncrementer())
                .start(helloWorldStep)
                .build();
    }

    @JobScope
    @Bean
    public Step helloWorldStep(
            JobRepository jobRepository,
            Tasklet helloWorldTasklet,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("helloWorldStep", jobRepository)
                // 단순한 방식 todo Task 더 알아보기
                .tasklet(helloWorldTasklet, transactionManager)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet helloWorldTasklet() {
        // todo 뭔지 더 알아보기
        return ((contribution, chunkContext) -> {
            System.out.println("Hello World Spring Batch");
            return RepeatStatus.FINISHED;
        });
    }

}
