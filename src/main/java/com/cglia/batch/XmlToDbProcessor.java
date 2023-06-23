package com.cglia.batch;

import java.util.List;

import com.cglia.model.TwoCustomer;

import org.springframework.batch.item.ItemProcessor;

public class XmlToDbProcessor implements ItemProcessor<List<TwoCustomer>, TwoCustomer> {

    @Override
    public TwoCustomer process(List<TwoCustomer> items) throws Exception {
        // Assuming you want to process the first item in the list
        if (items != null && !items.isEmpty()) {
            return items.get(0);
        }
        return null; // Or handle the case when the list is empty
    }
}