package com.example.springbatch.config;

import com.example.springbatch.model.Person;
import com.example.springbatch.repository.PersonRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

/**
 * Spring Batch設定クラス
 */
@Configuration
public class BatchConfig {

    @Autowired
    private PersonRepository personRepository;

    /**
     * データリーダー - データベースから未処理のPersonデータを読み取り
     */
    @Bean
    public RepositoryItemReader<Person> reader() {
        RepositoryItemReader<Person> reader = new RepositoryItemReader<>();
        reader.setRepository(personRepository);
        reader.setMethodName("findByProcessedFalse");
        reader.setPageSize(10);
        reader.setSort(Collections.singletonMap("id", Sort.Direction.ASC));
        return reader;
    }

    /**
     * データプロセッサー - Personデータを処理
     */
    @Bean
    public ItemProcessor<Person, Person> processor() {
        return new ItemProcessor<Person, Person>() {
            @Override
            public Person process(Person person) throws Exception {
                // 処理ロジックをシミュレート：名前を大文字に変換
                String firstName = person.getFirstName().toUpperCase();
                String lastName = person.getLastName().toUpperCase();
                
                person.setFirstName(firstName);
                person.setLastName(lastName);
                person.setProcessed(true);
                
                // 処理時間をシミュレート
                Thread.sleep(1000);
                
                System.out.println("処理完了: " + person);
                return person;
            }
        };
    }

    /**
     * データライター - 処理後のデータをデータベースに書き戻し
     */
    @Bean
    public RepositoryItemWriter<Person> writer() {
        RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
        writer.setRepository(personRepository);
        writer.setMethodName("save");
        return writer;
    }

    /**
     * Stepを定義
     */
    @Bean
    public Step processPersonStep(JobRepository jobRepository, 
                                  PlatformTransactionManager transactionManager,
                                  ItemReader<Person> reader,
                                  ItemProcessor<Person, Person> processor,
                                  ItemWriter<Person> writer) {
        return new StepBuilder("processPersonStep", jobRepository)
                .<Person, Person>chunk(5, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /**
     * Jobを定義
     */
    @Bean
    public Job processPersonJob(JobRepository jobRepository, Step processPersonStep) {
        return new JobBuilder("processPersonJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(processPersonStep)
                .build();
    }
}