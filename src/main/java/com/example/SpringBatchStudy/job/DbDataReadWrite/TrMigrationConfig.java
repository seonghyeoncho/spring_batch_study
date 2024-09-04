package com.example.SpringBatchStudy.job.DbDataReadWrite;

import com.example.SpringBatchStudy.core.domain.accounts.Accounts;
import com.example.SpringBatchStudy.core.domain.accounts.AccountsRepository;
import com.example.SpringBatchStudy.core.domain.orders.Orders;
import com.example.SpringBatchStudy.core.domain.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

/**
 * 주문 테이블 -> 정산 테이블
 */
@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {
    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Bean
    public Job trMigrationJob(JobRepository jobRepository, Step trMigrationStep) {
        return new JobBuilder("trMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(trMigrationStep)
                .build();
    }

    @JobScope
    @Bean
    public Step trMigrationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader trOrdersReader,
            ItemProcessor trOrderProcessor,
            ItemWriter trOrderWriter
    ) {
        return new StepBuilder("trMigrationStep", jobRepository)
                // 어떤 데이터를 가져와서 어떤 데이터로 사용할 것인지, 한 번에 몇개를 처리할 것인지
                .<Orders, Accounts>chunk(5, transactionManager)
                // item reader 작성
                .reader(trOrdersReader)
//                .writer(
//                        new ItemWriter() {
//                    @Override
//                    public void write(Chunk chunk) throws Exception {
//                        chunk.forEach(System.out::println);
//                    }
//                }
//                )
                // Accounts로 저장할 수 있도록 설정
                .processor(trOrderProcessor)
                .writer(trOrderWriter)
                .build();
    }
    @StepScope
    @Bean
    public RepositoryItemWriter<Accounts> trOrderWriter() {
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository)
                .methodName("save")
                .build();
    }

    /**
     * ItemWriter을 직접 사용해서 작성할 수도 있음
     *
     */
//    @StepScope
//    @Bean
//    public ItemWriter<Accounts> trOrderWriter() {
//        return new ItemWriter<Accounts>() {
//            @Override
//            public void write(Chunk<? extends Accounts> chunk) throws Exception {
//                chunk.forEach(item -> accountsRepository.save(item));
//            }
//        };
//    }

    @StepScope
    @Bean
    public ItemProcessor<Orders, Accounts> trOrderProcessor() {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    @Bean
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader")
                // 사용할 레포지토리
                .repository(ordersRepository)
                .methodName("findAll")
                // 대부분은 청크 사이즈와 페이지 사이즈를 동일하게 유지
                .pageSize(5)
                // 입력 파라미터
                .arguments(Arrays.asList())
                // todo ?? 이게 뭐임
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }
}
