package com.kaniblu.naver.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpHeaders extends HashMap<String, List<String>>
{
	public void put(String key, String value) {
		// TODO Auto-generated method stub
		List<String> valueList = new ArrayList<String>();
		valueList.add(value);
		this.put(key, valueList);
	}
}

	