package com.example.SpringBatchStudy.job.ValidationParam;

import com.example.SpringBatchStudy.job.ValidationParam.Validator.FileParamValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;


@Configuration
@RequiredArgsConstructor
public class ValidatedParamJobConfig {

    // 실행할 때 잡 이름을 파라미터로 넘겨줘야함
    @Bean
    public Job validatedParamJob (JobRepository jobRepository, Step validatedParamStep) {
        return new JobBuilder("validatedParamJob", jobRepository)
                // 시퀀스를 순차적으로 부여
                .incrementer(new RunIdIncrementer())
                .validator(multipleValidator())
                .start(validatedParamStep)
                .build();
    }

    // 여러 개의 검증 로직 추가
    private CompositeJobParametersValidator multipleValidator() {
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(new FileParamValidator()));

        return validator;
    }

    @JobScope
    @Bean
    public Step validatedParamStep(
            JobRepository jobRepository,
            Tasklet validatedParamTasklet,
            PlatformTransactionManager transactionManager
    ) {

        return new StepBuilder("validatedParamStep", jobRepository)
                // 단순한 방식 todo Task 더 알아보기
                .tasklet(validatedParamTasklet, transactionManager)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet validatedParamTasklet(@Value("#{jobParameters['fileName']}") String fileName) {
        // todo 뭔지 더 알아보기
        return ((contribution, chunkContext) -> {
            System.out.println("validated Param Tasklet");
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    public static BeanDefinitionRegistryPostProcessor jobRegistryBeanPostProcessorRemover() {
        return registry -> registry.removeBeanDefinition("jobRegistryBeanPostProcessor");
    }

}
