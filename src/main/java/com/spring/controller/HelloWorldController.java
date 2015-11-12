package com.spring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.dao.DataDao;
import com.spring.orm.Person;

@Controller
public class HelloWorldController {
	
		@Autowired
		private DataDao dataDao;

		@RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
		public @ResponseBody
		  Object  hello(@PathVariable String name) {
			
			return name +",  hello!";
		}
		
		@RequestMapping(value = "/person/{name}", method = RequestMethod.PUT)
		public @ResponseBody 
		Object savePerson(@PathVariable String name) {
			//ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");	         
	        //DataDao dataDao = context.getBean(DataDao.class);
	        Person person = new Person();
	        person.setName(name); person.setCountry("China");
	         
	        dataDao.save(person);
	        
	        String result = "Saved [Person::"+person + "]\n";
	        
	        result += "\n Now print out the items in the database: \n";
	        List<Person> list = dataDao.list();
	         
	        for(Person p : list){
	            result += "[Person::"+ p + "]\n";
	        }
	        //close resources
	        //context.close();   	
	        return result;
		}
		
	}
