package com.cglia.batch;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.cglia.model.TwoCustomer;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

public class CustomerItemPrpearedSetter implements ItemPreparedStatementSetter<TwoCustomer> {

	@Override
	public void setValues(TwoCustomer item, PreparedStatement ps) throws SQLException {
	ps.setInt(1, item.getId());
	ps.setString(2, item.getFirstname());

	}

}
