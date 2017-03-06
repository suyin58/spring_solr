package com.wjs.common.solr;	
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ConfigSetAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.ConfigSetAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.cloud.ZkCLI;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;


public class SolrjApiTest {

	
	private static final String zkHost = "127.0.0.1:2181";
	static final String collection1 = "help_center";
	
	Gson gson = new GsonBuilder().create();
    
	
	@Test
	public void docHelpQuery(){

		
		try {
			CloudSolrClient cloudSolrClient = new CloudSolrClient(zkHost);

			cloudSolrClient.setDefaultCollection(collection1);
//	        cloudSolrClient.setZkClientTimeout(zkClientTimeout);  
//	        cloudSolrClient.setZkConnectTimeout(zkConnectTimeout);  
	        cloudSolrClient.connect();  
			
			SolrQuery params = new SolrQuery();
			// 设置高亮
//			params.setHighlight(true).setHighlightSimplePre("<span class='red'>")
//			        .setHighlightSimplePost("</span>");
//			query.setParam("hl.fl", "Content");//设置高亮字段
//			query.setParam("fl", "ID,Published");
		    // the common parameters for all search  
		    params.set("q", "content_sa:微积分是西红柿的调味");  
		    params.set("fl", "id,content_sa"); // field list 返回字段  
		    params.set("start", "0"); // 分页  
		    params.set("rows", "2");  
			 
			
			QueryResponse response  = cloudSolrClient.query(params);
			System.out.println(params);
			// 查询出来的结果都保存在SolrDocumentList�?
			SolrDocumentList results  = response.getResults();
			System.out.println("总数" + results .getNumFound());
			for (SolrDocument document  : results ) {
 
			    // 打印高亮信息
			    //
			    System.out.println(document .get("id").toString() + document .get("content_sa").toString());
			}

			 cloudSolrClient.close();  
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}
	
	@Test
	public void docHelpAdd(){

		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		

		solrClient.setDefaultCollection(collection1);
//        cloudSolrClient.setZkClientTimeout(zkClientTimeout);  
//        cloudSolrClient.setZkConnectTimeout(zkConnectTimeout);  
		solrClient.connect();  
        
		SolrInputDocument doc = new SolrInputDocument();  
		Long id = 1L;
		doc.addField("id", id);
		Map<String, String> map = new HashMap<String, String>();
		map.put("title", "震惊标题:"+id);
		map.put("title1", "震惊标题1:"+id);
		map.put("title2", "震惊标题2:"+id);
		map.put("desc", "没多久辛亥革命那些积分可购买哪些机构模板");
		doc.addField("content_sa", gson.toJson(map));
		try {
			UpdateResponse res = solrClient.add(doc);
			System.out.println(res);

			solrClient.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void docHelpDelete(){

		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		

		solrClient.setDefaultCollection(collection1);
//        cloudSolrClient.setZkClientTimeout(zkClientTimeout);  
//        cloudSolrClient.setZkConnectTimeout(zkConnectTimeout);  
		solrClient.connect();  
		try {
			solrClient.deleteById("5");

			solrClient.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void docCollection2Add(){

		System.out.println("1111");
		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		

		solrClient.setDefaultCollection("hello_world");
//        cloudSolrClient.setZkClientTimeout(zkClientTimeout);  
//        cloudSolrClient.setZkConnectTimeout(zkConnectTimeout);  
		solrClient.connect();  
		SolrInputDocument doc = new SolrInputDocument();  
		doc.addField("id", 2L);
		doc.addField("_root_", "源码分析标题大傻");
		doc.addField("_text_", "CloudSolrClient是solrj提供的客户端与solrCloud交互的类。该类的实例与zookeeper进行通信来确定solrCloud collections中的solr endpoint，然后使用LBHttpSolrClient发请求");
		try {
			solrClient.add(doc);
			solrClient.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void collectionAdd(){

		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		
		CollectionAdminRequest.Create collectionCreate = new CollectionAdminRequest.Create();
		collectionCreate.setCollectionName("test_admin");
		collectionCreate.setConfigName("help_center_config");
		collectionCreate.setNumShards(3);
		try {
			CollectionAdminResponse response = collectionCreate.process(solrClient);
			if(response.isSuccess()){
				System.out.println("create new collection success");
			}
			System.out.println(response);
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void collectionReload(){
		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		
		
		String collectionName = "test_admin";
		
		CollectionAdminRequest.Reload collectionReload = new CollectionAdminRequest.Reload();
		collectionReload.setCollectionName(collectionName);
		try {
			collectionReload.process(solrClient);
			System.out.println("collectionName:" + collectionName +" reload succed");
		} catch(RemoteSolrException e){
			System.out.println("collectionName:" + collectionName +" reload error");
//			e.printStackTrace();
		}catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void collectionStatus(){
		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		
		String collectionName = "test_admin1";
		
		CollectionAdminRequest.ClusterStatus collectionReload = new CollectionAdminRequest.ClusterStatus();
		collectionReload.setCollectionName(collectionName);
		
		try {
			CollectionAdminResponse response = collectionReload.process(solrClient);
			System.out.println(response);
		} catch(RemoteSolrException e){
			System.out.println("");
		}catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void collectionList(){

		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		
		CollectionAdminRequest.List collectionList = new CollectionAdminRequest.List();
		try {
			CollectionAdminResponse response = collectionList.process(solrClient);
			LinkedTreeMap<String, Object> responseMap = (LinkedTreeMap<String, Object>) gson.fromJson(response.toString(), Object.class);
//			for (Iterator<Entry<String, Object>> it = responseMap.entrySet().iterator(); it.hasNext();) {
//				Entry<String, Object> entry = it.next();
//				System.out.println(entry);
//			}
			ArrayList<String> collectionNames = (ArrayList<String>) responseMap.get("collections");
			for (String collectionName : collectionNames) {
				System.out.println("collectionName:" + collectionName);
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void configAdd(){
		

		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		
		
		ConfigSetAdminRequest.Create configCreate = new ConfigSetAdminRequest.Create();
		configCreate.setBaseConfigSetName("");
		configCreate.setConfigSetName("test_new_config");
		configCreate.setPath("D:/WorkSpace/common/wjs-common/src/test/resources/solr/");
		try {
			ConfigSetAdminResponse response = configCreate.process(solrClient);
			System.out.println(response.getStatus());
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void zkAddSolrConfig(){
//		org.apache.solr.cloud.ZkCLI -cmd upconfig -zkhost 127.0.0.1:2181 -confdir C:/Users/Silver/Desktop/apache-tomcat-8.5.11-10100/webapps/solr/solrhome/configsets/help_center_configs/conf/ -confname help_center_config
		
		String[] args = {"-cmd","upconfig"
						, "-zkhost" ,"127.0.0.1:2181"
						,"-confdir" ,"D:/WorkSpace/common/wjs-common/src/test/resources/solr/"
						,"-confname", "zkcli_solr_config1"};
		try {
			
			ZkCLI.main(args);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void getClassPath(){
		try {
//			File cfgFile = ResourceUtils.getFile("classpath*:logback.xml");
//
//			System.out.println(cfgFile.getAbsolutePath());
//			System.out.println(cfgFile.exists());
			
			
//			org.springframework.core.io.Resource fileRource = new ClassPathResource("solr_help_center_config/");
//			File rourceFile =  fileRource.getFile();
//			System.out.println(rourceFile.getAbsolutePath());
//			System.out.println(rourceFile.exists());
			PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();  
			Resource[] resources = patternResolver.getResources("classpath*:solr_help_center_config");  
			File rourceFile =  resources[0].getFile();
			System.out.println(rourceFile.getAbsolutePath());
			System.out.println(rourceFile.exists());
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

