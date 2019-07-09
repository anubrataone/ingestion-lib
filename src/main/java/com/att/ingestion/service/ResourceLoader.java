package com.att.ingestion.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.att.ingestion.model.Resource;


public class ResourceLoader {
    interface IRunWithRetries {
        void run() throws Exception;
    }

    private static Logger log = LoggerFactory.getLogger(ResourceLoader.class);

    private IngestionService server;
    ObjectMapper mapper = new ObjectMapper();
    private int batchSize;
    private Deque<Resource> resourceUpdateQue = new ConcurrentLinkedDeque<Resource>();
    private Deque<Resource> resourceCreateQue = new ConcurrentLinkedDeque<Resource>();
    private Map<String, Long> checkResourceMap = new HashMap<String, Long>();
    private Integer retryWaitMs = 5000;
    private Integer retryNum = 2;

    public ResourceLoader(IngestionService server, int batchSize, int pageSize) {
        this.server = server;
        this.batchSize = batchSize;
    }

    public Integer getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(Integer retryNum) {
        this.retryNum = retryNum;
    }

    private void runWithRetries(int nRetries, long retryWaitMs, IRunWithRetries runner) throws Exception {
        int attempt = 1;
        while (true) {

            try {
                runner.run();
                break;
            } catch (Exception e) {
                if (attempt > nRetries) {
                    throw new Exception(String.format("encountered error, attempt %d, error %s", attempt, e.getMessage()), e);
                }

                log.info(String.format("encountered error, attempt %d, next retry in %s ms, error ", attempt, retryWaitMs, e.getMessage()), e);
                try {
                    Thread.sleep(retryWaitMs);
                } catch (InterruptedException e1) {
                    break; // break the loop
                }
                attempt++;
            }
        }
    }

    private void sendCreateBatch(final List<Resource> list) throws Exception {
        runWithRetries(retryNum, retryWaitMs, new IRunWithRetries()  {
            public void run() throws Exception {
                server.createIngestionResourceBatch(list);
            }
        });

    }

    private void sendUpdateBatch(final List<Resource> list) throws Exception {
        runWithRetries(retryNum, retryWaitMs, new IRunWithRetries()  {
            public void run() throws Exception {
                try{
                    server.updateIngestionResourceBatch(list);
                }catch(Exception e){
                    log.error("Could not ingest:" + mapper.writeValueAsString(list),e);
                    throw e;
                }
            }
        });
    }

    private void sendUpdateLastCheckBatch(final Map<String, Long> map) throws Exception {
        runWithRetries(retryNum, retryWaitMs, new IRunWithRetries()  {
            public void run() throws Exception {
                server.updateLastCheckedResource(map);
            }
        });
    }

    // /////////////////////////////////////////////
    // SENDING RESOURCES
    // ////////////////////////////////////////////
    public void addNewResource(Resource resource) throws Exception {
        resourceCreateQue.add(resource);
        if (resourceCreateQue.size() == batchSize) {
            Resource[] array = new Resource[batchSize];
            List<Resource> list = Arrays.asList(resourceCreateQue
                    .toArray(array));
            resourceCreateQue.clear();
            sendCreateBatch(list);
        }

    }
    
    public void addUpdatedResource(Resource ...resources) throws Exception {
        List<Resource> list=null;
        synchronized (resourceUpdateQue) {
            for(Resource resource:resources){
                resourceUpdateQue.add(resource);
            }
            if (resourceUpdateQue.size() >= batchSize) {
                list=new ArrayList<Resource>(resourceUpdateQue);
                resourceUpdateQue.clear();
            }
        }
        if(list!=null && list.size()>0){
            sendUpdateBatch(list);
        }

    }
    
    public void updateLastChecked(String urn, Long checkTime) throws Exception {
        checkResourceMap.put(urn, checkTime);
        if (checkResourceMap.size() == batchSize) {
            Map<String, Long> map = new HashMap<String, Long>();
            synchronized (checkResourceMap) {
                map.putAll(checkResourceMap);
                checkResourceMap.clear();
            }
            sendUpdateLastCheckBatch(map);
        }

    }

    public void emptyQueues() throws Exception {
        List<Resource> list=null;
        synchronized (resourceCreateQue) {
            if (!resourceCreateQue.isEmpty()) {
                list= new ArrayList<Resource>(resourceCreateQue);
                resourceCreateQue.clear();
            }
        }
        if(list!=null && list.size()>0){
            sendCreateBatch(list);
        }

        List<Resource> list2=null;
        synchronized (resourceUpdateQue) {
            if (!resourceUpdateQue.isEmpty()) {
                list2= new ArrayList<Resource>(resourceUpdateQue);
                resourceUpdateQue.clear();
            }
        }
        if(list2!=null && list2.size()>0){
            sendUpdateBatch(list2);
        }

        if (!checkResourceMap.isEmpty()) {
            Map<String, Long> map = new HashMap<String, Long>();
            synchronized (checkResourceMap) {
                map.putAll(checkResourceMap);
                checkResourceMap.clear();
            }
            sendUpdateLastCheckBatch(map);
        }
    }
    
    public Resource pullResource(String bpName, Map<String,Object> filterCondition, Map<String,Map<String,String>> selectors) throws Exception {
    	List<Resource> res = server.advancedQueryForResource("_index", filterCondition);
    	if (!res.isEmpty()) {
            return res.get(0);
        }
        return null;
    }
    
    // /////////////////////////////////////////////
    // LOADING RESOURCES
    // ////////////////////////////////////////////

    public IngestionService getServer() {
        return server;
    }

    public void setServer(IngestionService server) {
        this.server = server;
    }
}