package io.securecodebox.persistence.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.Report;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.persistence.PersistenceProvider;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


//Todo: Add Error Handling to the ES Operations

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "elasticsearch")
public class ElasticSearchPersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchPersistenceProvider.class);

    private static final String scbIndexPrefix = "securecodebox";
    private static final String dateTimeFormat = "yyyy-MM-dd";
    private static final String TYPE_REPORT = "report";
    private static final String TYPE_FINDING = "finding_entry";

    @Value("${securecodebox.persistence.elasticsearch.host}")
    private String host;

    @Value("${securecodebox.persistence.elasticsearch.port}")
    private int port;

    @Value("${securecodebox.persistence.elasticsearch.index.prefix}")
    private String indexPrefix;

    private boolean initialized = false;
    private RestHighLevelClient highLevelClient;
    private boolean connected = false;

    private String tenantId = null;

    /**
     * For developing convenience
     * If this is true then the index, where the data will be saved will be deleted and freshly recreated before
     * saving anything
     * TODO: REMOVE THIS BEFORE GOING INTO PRODUCTION
     */
    private boolean deleteBeforeCreate = true;

    private void init(){

        LOG.info("Initializing ElasticSearchPersistenceProvider");
        highLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        String indexName = getElasticIndexName();

        try {

            connected = highLevelClient.ping();

            LOG.info("ElasticSearch connected?: " + connected);
            if (connected) {
                if(indexExists(indexName) && deleteBeforeCreate){
                    /**
                     * The next lines are just for developing purposes and will be removed later
                     * TODO: REMOVE THESE LINES BEFORE GOING INTO PRODUCTION
                     */

                    LOG.info("Deleting Index " + indexName);
                    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
                    highLevelClient.indices().delete(deleteIndexRequest);
                }
                if (!indexExists(indexName)){

                    //The index doesn't exist until now, so we create it
                    LOG.info("Index " + indexName + " doesn't exist. Creating it...");
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

                    //todo: maybe declare the mapping file name in the properties (Not sure if we need the mapping anymore)
//                    String mapping = readFileResource("mapping.json");
//                    LOG.info("Initialize with mapping: " + mapping);
//                    if(mapping != null) {
//                        createIndexRequest.mapping("_doc", mapping, XContentType.JSON);
//                    }
                    highLevelClient.indices().create(createIndexRequest);
                }

                //Checking once more, in case anything went wrong during index creation
                if(indexExists(indexName)){
                    initialized = true;
                }
            }
            else {
                LOG.error("ElasticSearch doesn't respond. Please check if it is up and running");
            }
        }
        catch (IOException e) {
            LOG.error(e.getMessage());
            initialized = false;
        }
    }

    @Override
    public void persist(Report report) {

        if(report == null){
            LOG.info("Report is null, nothing to persist.");
            return;
        }

        this.tenantId = report.getTenantId();

        if(!initialized){
            init();
        }

        try {
            connected = highLevelClient.ping();
        }
        catch (IOException ioe){
            LOG.error("Error pinging ElasticSearch: " + ioe.getMessage());
            connected = false;
        }

        //Second check because, if the initialization wasn't successful, it's still false
        if(initialized && connected) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {

                boolean uuidAlreadyExists = true;

                /*
                This is typically executed only once because we create random UUIDs which are very unlikely to ever be
                the same (if not impossible)
                Anyway, we want to make sure that we don't save the same Report Id twice
                 */
                while (uuidAlreadyExists){
                    SearchRequest searchRequest = new SearchRequest();
                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    searchSourceBuilder.query(QueryBuilders.matchQuery("report_id", report.getId()));
                    searchRequest.source(searchSourceBuilder);
                    SearchResponse searchResponse = highLevelClient.search(searchRequest);
                    LOG.info("Search Response Status: " + searchResponse.status());
                    boolean searchFailure = searchResponse.isTimedOut() || (searchResponse.status() != RestStatus.OK);
                    if(searchFailure){
                        LOG.error("Searching the index failed. Skipping persisting...");
                        return;
                    }

                    LOG.info("SearchResponse from UUID Search: " + searchResponse);
                    if(searchResponse.getHits().totalHits > 0 ){
                        report.setId(UUID.randomUUID());
                        uuidAlreadyExists = true;
                    }
                    else {
                        uuidAlreadyExists = false;
                    }
                }

                //Persisting the Report and the Findings
                String jsonReport = objectMapper.writeValueAsString(report);

                Map<String, Object> reportAsMap = objectMapper.readValue(jsonReport, new TypeReference<Map<String, Object>>(){});
                reportAsMap.put("type", TYPE_REPORT);

                IndexRequest reportIndexRequest = new IndexRequest(getElasticIndexName(), "_doc");
                reportIndexRequest.source(objectMapper.writeValueAsString(reportAsMap), XContentType.JSON);

                BulkRequest bulkRequest = new BulkRequest();
                for(Finding f : report.getFindings()){
                    String jsonFinding = objectMapper.writeValueAsString(f);

                    Map<String, Object> findingAsMap = objectMapper.readValue(jsonFinding, new TypeReference<Map<String, Object>>(){});
                    findingAsMap.put("type", TYPE_FINDING);
                    findingAsMap.put("execution", report.getExecution());
                    findingAsMap.put("report_id", report.getId());

                    IndexRequest findingIndexRequest = new IndexRequest(getElasticIndexName(), "_doc");
                    findingIndexRequest.source(objectMapper.writeValueAsString(findingAsMap), XContentType.JSON);
                    bulkRequest.add(findingIndexRequest);
                }
                bulkRequest.add(reportIndexRequest);

                LOG.info("Persisting Report and Findings...");
                highLevelClient.bulkAsync(bulkRequest, new ActionListener<BulkResponse>() {
                    @Override
                    public void onResponse(BulkResponse bulkItemResponses) {
                        if(bulkItemResponses.hasFailures()){
                            LOG.warn("Warning: Some findings may not have been persisted correctly!");
                        }
                        else {
                            LOG.info("Successfully saved findings to " + getElasticIndexName());
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        LOG.error("Error persisting findings. Reason: " + e);
                        LOG.error(e.getMessage());
                    }
                });
            } catch (JsonProcessingException e) {
                LOG.error(e.getMessage());
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
        else {
            LOG.error("Could not persist data. It seems like ElasticSearch is not reachable.");
        }
    }

    /**
     * Returns the elasticsearch indexName, based on the current dateTime and configuration.
     * @return
     */
    private String getElasticIndexName() {
        Date date = Date.from(Instant.now());

        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        String dateAsString = sdf.format(date);
        String indexName = scbIndexPrefix + "_" + ((tenantId != null) ? tenantId + "_" : "") + dateAsString;
        return indexName.toLowerCase();
    }
    
    private boolean indexExists(String indexName){
        
        try {
            //Indices Exist API is currently not supported in the high level client
            highLevelClient.getLowLevelClient().performRequest("GET", "/" + indexName);
            return true;
        }
        catch (ResponseException e){
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
            LOG.error(e.getMessage());
            return false;
        }
        catch (IOException ioe){
            LOG.error(ioe.getMessage());
            return false;
        }
    }

    /**
     * This was initially created for reading the mapping file
     * @param file the file to read
     * @return a string containing the file content
     */
    private String readFileResource(String file){

        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader
                    (new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(file)));
            String line;
            while((line = reader.readLine()) != null){
                result.append(line);
            }
            return result.toString();
        }
        catch (IOException ioe){
            LOG.error(ioe.getMessage());
            return null;
        }
    }
}
