/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.examples.spring.batch;

import org.jboss.examples.spring.batch.multiline.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.HashMap;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step1(AggregateItemReader itemReader, FlatFileItemReader fileItemReaderStream,
                      FlatFileItemWriter itemWriter) {
        return stepBuilderFactory.get("step1")
                .<AggregateItem,AggregateItem>chunk(1)
                    .reader(itemReader)
                    .writer(itemWriter)
                    .stream(fileItemReaderStream)
                .build();
    }

    @Bean
    public Job job(Step step1) throws Exception {
        return jobBuilderFactory.get("multilineJob")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }


    @Bean
    public AggregateItemReader<AggregateItem<Trade>> reader(FlatFileItemReader flatFileItemReader){
        AggregateItemReader rc = new AggregateItemReader();
        rc.setItemReader(flatFileItemReader);
        return rc;
    }

    @Bean
    public FlatFileItemWriter<AggregateItem<Trade>> writer() {
        FlatFileItemWriter rc = new FlatFileItemWriter();
        rc.setLineAggregator(new PassThroughLineAggregator());
        rc.setResource(new FileSystemResource("./target/multistep.txt"));
        return rc;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<AggregateItem> flatFileItemReader(@Value("#{jobParameters[inputFile]}")String fileName, LineTokenizer lineTokenizer) {
        FlatFileItemReader<AggregateItem> rc = new FlatFileItemReader<>();
        rc.setResource(new FileSystemResource(fileName));
        DefaultLineMapper<AggregateItem> mapper = new DefaultLineMapper<>();

        // tokenizer
        mapper.setLineTokenizer(lineTokenizer);

        // field mapper
        AggregateItemFieldSetMapper fieldSetMapper = new AggregateItemFieldSetMapper();
        fieldSetMapper.setDelegate(new TradeFieldSetMapper());
        mapper.setFieldSetMapper(fieldSetMapper);

        rc.setLineMapper(mapper);
        return rc;
    }

    @Bean
    public LineTokenizer fixedFileDescriptor() {
        PatternMatchingCompositeLineTokenizer rc = new PatternMatchingCompositeLineTokenizer();

        HashMap<String, LineTokenizer> matchers = new HashMap<>();
        matchers.put("BEGIN*", beginRecordTokenizer());
        matchers.put("END*", endRecordTokenizer());
        matchers.put("*", tradeRecordTokenizer());

        rc.setTokenizers(matchers);
        return rc;
    }

    private LineTokenizer tradeRecordTokenizer() {
        FixedLengthTokenizer rc = new FixedLengthTokenizer();
        String[] names = new String[]{
                "ISIN",
                "Quantity",
                "Price",
                "Customer"
        };
        rc.setNames(names);
        Range[] ranges = new Range[]{
                new Range(1,12),
                new Range(13,15),
                new Range(16,20),
                new Range(21,29)
        };
        rc.setColumns(ranges);
        return rc;
    }

    private LineTokenizer endRecordTokenizer() {
        FixedLengthTokenizer rc = new FixedLengthTokenizer();
        Range[] ranges = new Range[]{new Range(1,3)};
        rc.setColumns(ranges);
        return rc;
    }

    private LineTokenizer beginRecordTokenizer() {
        FixedLengthTokenizer rc = new FixedLengthTokenizer();
        Range[] ranges = new Range[]{new Range(1,5)};
        rc.setColumns(ranges);
        return rc;
    }

}
