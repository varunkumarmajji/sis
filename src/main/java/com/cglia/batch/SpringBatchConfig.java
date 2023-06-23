package com.cglia.batch;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.cglia.model.Customer;
import com.cglia.repo.CustomerRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private CustomerRepository customerRepository;
    
	@Autowired
	public DataSource dataSource;

    @Autowired
    public SpringBatchConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
			CustomerRepository customerRepository) {
		super();
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.customerRepository = customerRepository;
	}

	@Bean
    public FlatFileItemReader<Customer> reader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;

    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

 
    
   //CSV to XML WRITER 
@Bean
public StaxEventItemWriter<Customer> writerXml()
{
	 StaxEventItemWriter<Customer> writerXmlFile=new  StaxEventItemWriter<>();
	 writerXmlFile.setResource(new FileSystemResource("D://varun/customer.xml"));
	 Map<String,String> aliasMap=new HashMap<>();
	 aliasMap.put("Customer", "com.cglia.model.Customer");
	 XStreamMarshaller marsh=new XStreamMarshaller();
	 marsh.setAliases(aliasMap);
	 writerXmlFile.setMarshaller(marsh);
	 writerXmlFile.setRootTagName("customers");
	 writerXmlFile.setOverwriteOutput(true);
	 return writerXmlFile;
}
    
    @Bean
    public RepositoryItemWriter<Customer> writer() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("csv-step").<Customer, Customer>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomers")
                .flow(step1()).end().build();

    }
  
    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }
    
    @Bean
    public Step stepXml() {
        return stepBuilderFactory.get("xml-step").<Customer, Customer>chunk(10)
                .reader(readerXml())
              .processor(new XmlProcessor())
                .writer(writerXml())
               // .taskExecutor(taskExecutor())
                .build();
    }
    @Bean
   // @Primary
    public Job runJobXml() {
        return jobBuilderFactory.get("importCustomers").incrementer(new RunIdIncrementer() )
                .flow(stepXml()).end().build();

    }
    
    @Bean
    public JdbcCursorItemReader<Customer> readerXml() {
    	JdbcCursorItemReader<Customer> itemReader = new JdbcCursorItemReader<>();
        itemReader.setDataSource(dataSource);
        itemReader.setSql("SELECT CUSTOMER_ID,FIRST_NAME FROM CUSTOMERS_INFO");
        itemReader.setRowMapper(new RowMapper<Customer>() {
			
			@Override
			public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
				Customer cust=null;
				
				 cust=new Customer();
				cust.setId(rs.getInt("CUSTOMER_ID"));
				cust.setFirstName(rs.getString("FIRST_NAME"));
			
				
				return cust;
			}
		});
        itemReader.setVerifyCursorPosition(false);
        return itemReader;
    }
   
}