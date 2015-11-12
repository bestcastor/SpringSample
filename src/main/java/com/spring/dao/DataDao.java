package com.spring.dao;

import java.util.List;

import com.spring.orm.Person;

public interface DataDao {

	public void save(Person p);
	     
	public List<Person> list();
	    
}
