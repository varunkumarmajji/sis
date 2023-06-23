package com.cglia.batch;



import com.cglia.model.Customer;

import org.springframework.batch.item.ItemProcessor;

public class XmlProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item) throws Exception {
       
       
        return item;
    }
}
