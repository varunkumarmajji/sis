package com.cglia.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.cglia.model.TwoCustomer;
import com.cglia.repo.CustomerRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

@Configuration
@EnableBatchProcessing
public class XmlToDatabase {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	public DataSource dataSource;

//	@Bean
//	public StaxEventItemReader<TwoCustomer> readXml() {
//		StaxEventItemReader<TwoCustomer> reader = new StaxEventItemReader<>();
//		reader.setResource(new ClassPathResource("SaveData.xml"));
//		reader.setFragmentRootElementName("twocustomers");
//		Map<String, String> aliasMap = new HashMap<>();
//		aliasMap.put("twocustomers", "com.cglia.model.TwoCustomer");
//		XStreamMarshaller marsh = new XStreamMarshaller();
//	
//		marsh.setAliases(aliasMap);
//		marsh.getXStream().allowTypes(new Class[] {TwoCustomer.class});
//		reader.setUnmarshaller(marsh);
//		return reader;
//	}

	@Bean
	public StaxEventItemReader<List<TwoCustomer>> readXml() {
	    StaxEventItemReader<List<TwoCustomer>> reader = new StaxEventItemReader<>();
	    reader.setResource(new ClassPathResource("SaveData.xml"));
	    reader.setFragmentRootElementName("twocustomers");
	    XStreamMarshaller marshaller = new XStreamMarshaller();
	    Map<String, Class<?>> aliases = new HashMap<>();
	    aliases.put("Customer", TwoCustomer.class);
	    marshaller.setAliases(aliases);
	    reader.setUnmarshaller(marshaller);
	    return reader;
	}

	@Bean
	public JdbcBatchItemWriter<TwoCustomer> writeSql() {
		JdbcBatchItemWriter<TwoCustomer> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setSql("insert into two_customer(id,first_name) values (?,?)");
		writer.setItemPreparedStatementSetter(new CustomerItemPrpearedSetter());
		return writer;
	}

//	@Bean
//	public Step stepXmlToDb() {
//		return stepBuilderFactory.get("xmltodb-step").<TwoCustomer, TwoCustomer>chunk(10).reader(readXml())
//				.processor(new XmlToDbProcessor())
//				.writer(writeSql())
//				// .taskExecutor(taskExecutor())
//				.build();
//	}
//	
	@Bean
	public Step stepXmlToDb() {
	    return stepBuilderFactory.get("xmltodb-step")
	            .<List<TwoCustomer>, TwoCustomer>chunk(10)
	            .reader(readXml())
	            .processor(new XmlToDbProcessor())
	            .writer(writeSql())
	            .build();
	}

	@Bean
	@Primary
	public Job runJobXmlToDb() {
		return jobBuilderFactory.get("importtodb").incrementer(new RunIdIncrementer()).flow(stepXmlToDb()).end()
				.build();

	}
}
