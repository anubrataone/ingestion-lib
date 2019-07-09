package com.att.ingestion.service;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;

import com.att.ingestion.config.CmsConfiguration;
import com.att.ingestion.model.Resource;
import com.att.ingestion.publish.IngestionHandlerService;

public class TestInjestionService {

	@Test
	public void test() {
		try {
			final File initialFile = new File("src/test/resources/OV_metadata_descriptor.xml");
		    final InputStream descriptorStream = new DataInputStream(new FileInputStream(initialFile));
		    CmsConfiguration cmsConfig = new CmsConfiguration();
		    cmsConfig.setCmsAdminHost("localhost");
		    cmsConfig.setCmsAdminPort(8983);
		    cmsConfig.setCmsClientHost("localhost");
		    cmsConfig.setCmsClientPort(8984);
		    cmsConfig.setOauthHost("localhost");
		    cmsConfig.setOauthPort(8111);
		    cmsConfig.setUser("test");
		    cmsConfig.setPassword("test");
		    cmsConfig.setTimeout(600);
		    IngestionHandlerService executor = new IngestionHandlerService();
			String offerJsonContent = new String(Files.readAllBytes(Paths.get("src/test/resources/offerTest.json")));
		    Map<String, Resource> resources = executor.publishToCMS(descriptorStream, offerJsonContent, "accessToken", cmsConfig);
		    for (Map.Entry<String, Resource> entry : resources.entrySet()) {
	            System.out.println(entry.getValue().getUrn());
	        }
		} catch(Exception ex) {
			fail("Exception while ingestion "+ex.getMessage());
		}
	}

}
