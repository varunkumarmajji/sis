package com.cglia.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Entity

public class TwoCustomer {
	@Id
	private int id;
	private String firstName;
	
	public TwoCustomer() {
		super();
	}
	public TwoCustomer(int id, String firstname) {
		super();
		this.id = id;
		this.firstName = firstname;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstname() {
		return firstName;
	}
	public void setFirstname(String firstname) {
		this.firstName = firstname;
	}
	@Override
	public String toString() {
		return "TwoCustomer [id=" + id + ", firstname=" + firstName+ "]";
	}
	

}
