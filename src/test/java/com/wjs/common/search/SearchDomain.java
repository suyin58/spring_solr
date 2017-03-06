package com.wjs.common.search;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class SearchDomain {

	private Integer id;
	private long age;
	private String talk;
	private Date birthDay;
	
	
	
	
	public SearchDomain(Integer id, long age, String talk, Date birthDay) {
		super();
		this.id = id;
		this.age = age;
		this.talk = talk;
		this.birthDay = birthDay;
	}
	
	

	
	public Integer getId() {
	
		return id;
	}



	
	public void setId(Integer id) {
	
		this.id = id;
	}



	public long getAge() {
	
		return age;
	}
	
	public void setAge(long age) {
	
		this.age = age;
	}
	
	
	
	public String getTalk() {
	
		return talk;
	}

	
	public void setTalk(String talk) {
	
		this.talk = talk;
	}

	public Date getBirthDay() {
	
		return birthDay;
	}
	
	public void setBirthDay(Date birthDay) {
	
		this.birthDay = birthDay;
	}
	
	
	@Override
	public String toString() {
	
		return ReflectionToStringBuilder.toString(this);
	}
}

