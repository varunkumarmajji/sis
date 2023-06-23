package com.cglia.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.cglia.model.Customer;
import com.cglia.repo.CustomerRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;

@Configuration
@EnableBatchProcessing
public class DataBaseToCsv {

	private JobBuilderFactory jobBuilderFactory;

	private StepBuilderFactory stepBuilderFactory;

	private CustomerRepository customerRepository;

	@Autowired
	public javax.sql.DataSource dataSource;

	public DataBaseToCsv(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
			CustomerRepository customerRepository) {
		super();
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
		this.customerRepository = customerRepository;
	}

	@Bean
	public ItemReader<Customer> reader1() {
		JdbcCursorItemReader<Customer> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql("SELECT CUSTOMER_ID,FIRST_NAME FROM CUSTOMERS_INFO");
		reader.setRowMapper(new YourDataClassRowMapper());
		return reader;
	}

	// Define the row mapper to map the database row to your data class
	private class YourDataClassRowMapper implements RowMapper<Customer> {
		@Override
		public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			Customer data = new Customer();
			// Map the resultSet columns to the fields of YourDataClass
			data.setId(resultSet.getInt("CUSTOMER_ID"));
			data.setFirstName(resultSet.getString("FIRST_NAME"));
			// data.setLastName(resultSet.getString("LAST_NAME"));
			// data.setEmail(resultSet.getString("EMAIL"));
			// data.setContactNo(resultSet.getString("CONTACT"));
			// data.setDob(resultSet.getString("DOB"));

			return data;
		}
	}

	@Bean
	public YourDataProcessor processor1() {
		return new YourDataProcessor();
	}

	@Bean
	public ItemWriter<Customer> writer1() {
		return new FlatFileItemWriterBuilder<Customer>().name("yourDataClassWriter")
				.resource(new FileSystemResource("D://output1.csv")) // Specify the output file path
				.delimited().delimiter(",") // Set the delimiter for CSV file
				.names("id", "firstName") // Specify the column names
				.build();
		// return writer;
	}

	@Bean
	public Step executeStep() {
		return stepBuilderFactory.get("executeStep").<Customer, Customer>chunk(10).reader(reader1()).processor(processor1()).processor(new XmlProcessor()).writer(writer1())
				.build();
	}

	@Bean

//	@Primary
	public Job processJob() {
		return jobBuilderFactory.get("/convert/csv").incrementer(new RunIdIncrementer()).flow(executeStep()).end()
				.build();
	}

}
