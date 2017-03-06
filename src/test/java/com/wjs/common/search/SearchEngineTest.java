package com.wjs.common.search;	
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring/test-spring-common-search.xml"})
public class SearchEngineTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchEngineTest.class);
	
	@Autowired
	SearchEngine searchEngine;
	
	@Test
	public void testGet(){
		List<SearchDomain> results = searchEngine.get("女下属的巧克力和奶茶都不如西红柿，吃了还咳", SearchDomain.class, 0, 10);
		for (SearchDomain searchDomain : results) {
			System.out.println(searchDomain);
		}
	}
	
	@Test
	public void testAdd(){
		String[] talks = {"先缴纳好像见过库很开心女下属"
						,"普乐迪西南风哈哈我继续看刚看下女很费劲阿克苏"
						,"可单独发哈我心甘阿勒颇返回非"
						,"上看新广告法我个卡伊的fiance"
						,"西红柿就按剪短发哈问空格等你发给还咳"
						,"跑去洗头发能够开小金啊可接受性，阿里扎激光焊接我复活小魔"
						,"破琴绝弦预估卡近些年噶厦折扣卡会更好哪些机构"
						,"老西的那个看数据大家先看时间为我行拿过来卡卡"};
		for (Long i = 0L ; i < talks.length ; i++) {
			
			SearchDomain domain = new SearchDomain(i.intValue(), 10+ i, talks[i.intValue()], DateUtils.addYears(new Date(), -i.intValue()));
			searchEngine.addDoc(i + "", domain);
			
		}
	}
	
	@Test
	public void testDelete(){
		searchEngine.removeDoc("1");
	}
}



