package com.wjs.common.search;	
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface SearchEngine {
	
	public <T> void addDoc(String id,T doc);
	
	public <T> List<T> get(String keyworld, Class<T> claz, Integer start, Integer limit);

	public <T> void removeDoc(String id);
	
	public <T> void updateDoc(String id,T doc);

}

