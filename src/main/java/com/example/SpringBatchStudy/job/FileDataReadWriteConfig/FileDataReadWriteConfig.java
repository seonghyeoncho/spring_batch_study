package com.example.SpringBatchStudy.job.FileDataReadWriteConfig;

import com.example.SpringBatchStudy.job.FileDataReadWriteConfig.dto.Player;
import com.example.SpringBatchStudy.job.FileDataReadWriteConfig.dto.PlayerYears;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class FileDataReadWriteConfig {
    @Bean
    public Job fileReadWriteJob(JobRepository jobRepository, Step fileReadWriteStep) {
        return new JobBuilder("fileReadWriteJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fileReadWriteStep)
                .build();
    }

    @JobScope
    @Bean
    public  Step fileReadWriteStep(
            JobRepository jobRepository,
            ItemReader playerFlatFileItemReader,
            ItemProcessor playerItemProcessor,
            ItemWriter playerFlatFileItemWriter,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("fileReadWriteStep", jobRepository)
                .<Player, PlayerYears>chunk(5, transactionManager)
                .reader(playerFlatFileItemReader)
//                .writer(
//                        new ItemWriter() {
//                            @Override
//                            public void write(Chunk chunk) throws Exception {
//                                chunk.forEach(System.out::println);
//                            }
//                        }
//                )
                .processor(playerItemProcessor)
                .writer(playerFlatFileItemWriter)
                .build();
    }
    @StepScope
    @Bean
    public ItemProcessor<Player, PlayerYears> playerItemProcessor() {
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }
    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerYears> playerFlatFileItemWriter() {
        // 어떤 필드를 사용하는지 명시하기 위해서 필요
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID", "lastName", "position", "yearsExperience"});
        //새로운 파일 생성
        fieldExtractor.afterPropertiesSet();

        // 어떤 기준으로 파일을 생성하는지 알려주기 위해 사용
        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
        // csv로 다시 만들기 때문이 , 로 구별
        lineAggregator.setDelimiter("");
        // 어떤 필드를 추출할건지
        lineAggregator.setFieldExtractor(fieldExtractor);

        FileSystemResource outputResource = new FileSystemResource("players_output.txt");

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playerItemWriter")
                .resource(outputResource)
                .lineAggregator(lineAggregator)
                .build();

    }

    @StepScope
    @Bean
    public FlatFileItemReader<Player> playerFlatFileItemReader() {
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerFileItemReader")
                .resource(new FileSystemResource("Player.csv"))
                // 데이터를 어떤 기준으로 나눌지
                .lineTokenizer(new DelimitedLineTokenizer())
                // 읽어온 데이터를 객체로 변경할 수 있도로 mapper 함수
                .fieldSetMapper(new PlayerFieldSetMapper())
                // 헤더 무시
                .linesToSkip(1)
                .build();
    }
}
