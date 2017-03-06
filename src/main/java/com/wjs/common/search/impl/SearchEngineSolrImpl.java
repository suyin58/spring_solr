package com.wjs.common.search.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.cloud.ZkCLI;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.wjs.common.search.SearchEngine;

public class SearchEngineSolrImpl implements SearchEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchEngineSolrImpl.class);

	/**
	 * SolrCloud 配置中心地址
	 */
	private String zkHost;

	/**
	 * SolrCloud collection名称
	 */
	private String searchCore;

	/**
	 * 业务环境
	 */
	private String appEnv;

	/**
	 * 操作超时时间
	 */
	private Integer timeOut = 1000;

	/**
	 * collection 配置文件目录
	 */
	private String cfgPath;

	/**
	 * collection 分片数量（仅仅用于创建collection中时使用）
	 */
	private Integer numShards;

	private static String collectionName = null;

	private CloudSolrClient cloudSolrClient = null;

	private Gson gson = new GsonBuilder().create();

	@PostConstruct
	public void init() {

		collectionName = appEnv + "_" + searchCore;
		// 查看collection是否存在，如果不存在需要构建collection
		createCollectionWhenNotExist();
		cloudSolrClient = new CloudSolrClient(zkHost);
		cloudSolrClient.setDefaultCollection(collectionName);
		cloudSolrClient.setZkClientTimeout(timeOut);
		cloudSolrClient.setZkConnectTimeout(3000);
		try {
			cloudSolrClient.connect();
		} catch (Exception e) {
			LOGGER.error("solr connect error:{}", e.getMessage(), e);
		}
	}

	private void createCollectionWhenNotExist() {
		/**
		 * 查看配置的collection是否存在，如果不存在，1.上传solr配置.2.创建collection
		 */

		CloudSolrClient solrClient = new CloudSolrClient(zkHost);
		solrClient.connect();
		// 通过查询Collection的分布式情况，判断是否存在collection
		CollectionAdminRequest.List collectionList = new CollectionAdminRequest.List();
		ArrayList<String> collectionNames = new ArrayList<String>();
		try {
			CollectionAdminResponse response = collectionList.process(solrClient);
			LinkedTreeMap<String, Object> responseMap = (LinkedTreeMap<String, Object>) gson.fromJson(response.toString(), Object.class);
			collectionNames = (ArrayList<String>) responseMap.get("collections");
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (collectionNames.contains(collectionName)) {
			return;
		}

		String configName = "solr_" + collectionName + "_config";

		try {

			PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = patternResolver.getResources(cfgPath);
			File rourceFile = resources[0].getFile();

			// 上传配置
			String[] args = { "-cmd", "upconfig", "-zkhost", zkHost, "-confdir", rourceFile.getAbsolutePath(), "-confname", configName };
			ZkCLI.main(args);
			// 创建collection
			CollectionAdminRequest.Create collectionCreate = new CollectionAdminRequest.Create();
			collectionCreate.setCollectionName(collectionName);
			collectionCreate.setConfigName(configName);
			collectionCreate.setNumShards(numShards);
			collectionCreate.process(solrClient);
		} catch (Exception e) {
			LOGGER.error("init solr collection error,collectionName:{} , errmsg:{}", collectionName, e.getMessage(), e);
		}

	}

	@Override
	public <T> void addDoc(String id, T doc) {

		SolrInputDocument document = new SolrInputDocument();
		document.addField("id", id);
		try {
			Field field = doc.getClass().getDeclaredField("id");
			field.setAccessible(true);
			field.set(doc, "");
		} catch (Exception e) {
			// ignore
		}
		document.addField("content_sa", gson.toJson(doc));
		try {
			UpdateResponse res = cloudSolrClient.add(document);
			LOGGER.info("solr add object success:{}", res);
			cloudSolrClient.commit();
		} catch (SolrServerException e) {
			LOGGER.error("solr add object error,collection:{} doc:{} , errmsg:{}", collectionName, doc, e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("solr add object error,collection:{} doc:{} , errmsg:{}", collectionName, doc, e.getMessage(), e);
		}

	}

	@Override
	public <T> List<T> get(String keyworld, Class<T> claz, Integer start, Integer limit) {

		List<T> results = new ArrayList<T>();
		try {
			SolrQuery params = new SolrQuery();
			// 设置高亮
			//			params.setHighlight(true).setHighlightSimplePre("<span class='red'>")
			//			        .setHighlightSimplePost("</span>");
			//			query.setParam("hl.fl", "Content");//设置高亮字段
			//			query.setParam("fl", "ID,Published");
			// the common parameters for all search  
			params.set("q", "content_sa:" + keyworld);
			params.set("fl", "id,content_sa"); // field list 返回字段  
			params.set("start", start == null ? "0" : String.valueOf(start)); // 分页  
			params.set("rows", limit == null ? "10" : String.valueOf(limit));

			QueryResponse response = cloudSolrClient.query(params);
			//			System.out.println(params);
			// 查询出来的结果都保存在SolrDocumentList中
			SolrDocumentList res = response.getResults();
			for (SolrDocument document : res) {
				// 打印高亮信息
				LOGGER.debug("solr query result:" + document.get("id").toString() + document.get("content_sa").toString());
				T t = gson.fromJson(document.get("content_sa").toString(), claz);
				try {
					String id = document.get("id").toString();
					Field field = claz.getDeclaredField("id");
					field.setAccessible(true);
					field.set(t, id);
				} catch (Exception e) {
					// ignore
				}
				results.add(t);
			}

		} catch (SolrServerException e) {
			LOGGER.error("solr query error, collection :{} , errmsg{}", collectionName, e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("solr query error, collection :{} , errmsg{}", collectionName, e.getMessage(), e);
		}

		return results;
	}

	@Override
	public <T> void removeDoc(String id) {

		try {
			cloudSolrClient.deleteById(String.valueOf(id));
			cloudSolrClient.commit();
		} catch (SolrServerException e) {
			LOGGER.error("solr delete error:{}", e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("solr delete error:{}", e.getMessage(), e);
		}
	}

	@Override
	public <T> void updateDoc(String id, T doc) {

		addDoc(id, doc);
	}

	public String getZkHost() {

		return zkHost;
	}

	public void setZkHost(String zkHost) {

		this.zkHost = zkHost;
	}

	public String getSearchCore() {

		return searchCore;
	}

	public void setSearchCore(String searchCore) {

		this.searchCore = searchCore;
	}

	public Integer getTimeOut() {

		return timeOut;
	}

	public void setTimeOut(Integer timeOut) {

		this.timeOut = timeOut;
	}

	public String getAppEnv() {

		return appEnv;
	}

	public void setAppEnv(String appEnv) {

		this.appEnv = appEnv;
	}

	public String getCfgPath() {

		return cfgPath;
	}

	public void setCfgPath(String cfgPath) {

		this.cfgPath = cfgPath;
	}

	public Integer getNumShards() {

		return numShards;
	}

	public void setNumShards(Integer numShards) {

		this.numShards = numShards;
	}

}
