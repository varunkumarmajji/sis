package com.cglia.batch;
import com.cglia.model.Customer;

import org.springframework.batch.item.ItemProcessor;

public class YourDataProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item) throws Exception {
        // Perform data processing/transformation on the item
    	Customer processedData = new Customer();
        processedData.setId(item.getId());
        processedData.setFirstName(item.getFirstName().toUpperCase());
       
        return processedData;
    }
}
