/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

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
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "securecodebox.persistence.provider", havingValue = "elasticsearch")
public class ElasticSearchPersistenceProvider implements PersistenceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchPersistenceProvider.class);

    private static final String dateTimeFormat = "yyyy-MM-dd";
    private static final String TYPE_REPORT = "report";
    private static final String TYPE_FINDING = "finding_entry";

    @Value("${securecodebox.persistence.elasticsearch.host}")
    private String host;

    @Value("${securecodebox.persistence.elasticsearch.port}")
    private int port;

    @Value("${securecodebox.persistence.elasticsearch.index.prefix}")
    private String indexPrefix;

    /**
     * For developing convenience
     * If this is true then the index where findings
     * will be saved will be deleted and freshly recreated before
     * saving anything
     */
    @Value("${securecodebox.persistence.elasticsearch.index.delete_on_init}")
    private boolean deleteOnInit;

    private boolean initialized = false;
    private RestHighLevelClient highLevelClient;
    private boolean connected = false;

    private String tenantId = null;

    private void init() {

        LOG.info("Initializing ElasticSearchPersistenceProvider");
        highLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        String indexName = getElasticIndexName();

        try {

            connected = highLevelClient.ping();

            LOG.info("ElasticSearch connected?: " + connected);
            if (connected) {
                if (indexExists(indexName) && deleteOnInit) {
                    LOG.info("Deleting Index " + indexName);
                    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
                    highLevelClient.indices().delete(deleteIndexRequest);
                }
                if (!indexExists(indexName)) {

                    //The index doesn't exist until now, so we create it
                    LOG.info("Index " + indexName + " doesn't exist. Creating it...");
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

                    // TODO maybe declare the mapping file name in the properties
                    String mapping = readFileResource("mapping.json");
                    LOG.info("Initialize with mapping: " + mapping);
                    if (mapping != null) {
                        createIndexRequest.mapping("_doc", mapping, XContentType.JSON);
                    }
                    highLevelClient.indices().create(createIndexRequest);
                }

                //Checking once more, in case anything went wrong during index creation
                if (indexExists(indexName)) {
                    initialized = true;
                    initializeKibana();
                }
            } else {
                LOG.error("ElasticSearch doesn't respond. Please check if it is up and running");
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            initialized = false;
        }
    }

    @Override
    public void persist(Report report) {

        if (report == null) {
            LOG.info("Report is null, nothing to persist.");
            return;
        }

        this.tenantId = report.getTenantId();

        if (!initialized || !indexExists(getElasticIndexName())) {
            init();
        }

        try {
            connected = highLevelClient.ping();
        } catch (IOException ioe) {
            LOG.error("Error pinging ElasticSearch: " + ioe.getMessage());
            connected = false;
        }

        //Second check because, if the initialization wasn't successful, it's still false
        if (initialized && connected) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {

                boolean uuidAlreadyExists = true;

                /*
                This is typically executed only once because we create random UUIDs which are very unlikely to ever be
                the same (if not impossible)
                Anyway, we want to make sure that we don't save the same Report Id twice
                 */
                while (uuidAlreadyExists) {
                    SearchRequest searchRequest = new SearchRequest();
                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    searchSourceBuilder.query(QueryBuilders.matchQuery("report_id", report.getId()));
                    searchRequest.source(searchSourceBuilder);
                    SearchResponse searchResponse = highLevelClient.search(searchRequest);
                    LOG.info("Search Response Status: " + searchResponse.status());
                    boolean searchFailure = searchResponse.isTimedOut() || (searchResponse.status() != RestStatus.OK);
                    if (searchFailure) {
                        LOG.error("Searching the index failed. Skipping persisting...");
                        return;
                    }

                    LOG.info("SearchResponse from UUID Search: " + searchResponse);
                    if (searchResponse.getHits().totalHits > 0) {
                        report.setId(UUID.randomUUID());
                        uuidAlreadyExists = true;
                    } else {
                        uuidAlreadyExists = false;
                    }
                }

                String dateTimeFormatToPersist = "yyyy-MM-dd'T'HH:mm:ss";

				// TODO create separate data types for reports and findings
                Map<String, Object> reportAsMap = serializeAndRemove(report, "findings", "execution");
                Map<String, Object> execution = serializeAndRemove(report.getExecution(), "findings", "scanners");
                execution.put("scanners",
                        serializeAndRemoveList(report.getExecution().getScanners(), "findings", "rawFindings"));
                reportAsMap.put("type", TYPE_REPORT);
                reportAsMap.put("execution", execution);
                // TODO remove this. scanner_type is accessible via execution attribute
                reportAsMap.put("scanner_type", report.getExecution().getScannerType());
                reportAsMap.put("@timestamp", new SimpleDateFormat(dateTimeFormatToPersist).format(new Date()));

                LOG.info("Timestamp: " + new SimpleDateFormat(dateTimeFormatToPersist).format(new Date()));

                IndexRequest reportIndexRequest = new IndexRequest(getElasticIndexName(), "_doc");
                reportIndexRequest.source(objectMapper.writeValueAsString(reportAsMap), XContentType.JSON);

                BulkRequest bulkRequest = new BulkRequest();
                for (Finding f : report.getFindings()) {

                    Map<String, Object> findingAsMap = serializeAndRemove(f);
                    findingAsMap.put("type", TYPE_FINDING);
                    findingAsMap.put("execution", execution);
                    findingAsMap.put("report_id", report.getId());
					// TODO remove this. scanner_type is accessible via execution attribute
                    findingAsMap.put("scanner_type", report.getExecution().getScannerType());
                    findingAsMap.put("@timestamp", new SimpleDateFormat(dateTimeFormatToPersist).format(new Date()));

                    IndexRequest findingIndexRequest = new IndexRequest(getElasticIndexName(), "_doc");
                    findingIndexRequest.source(objectMapper.writeValueAsString(findingAsMap), XContentType.JSON);
                    bulkRequest.add(findingIndexRequest);
                }
                bulkRequest.add(reportIndexRequest);

                LOG.info("Persisting Report and Findings...");
                highLevelClient.bulkAsync(bulkRequest, new ActionListener<BulkResponse>() {
                    @Override
                    public void onResponse(BulkResponse bulkItemResponses) {
                        if (bulkItemResponses.hasFailures()) {
                            LOG.warn("Warning: Some findings may not have been persisted correctly!");
                            LOG.warn(bulkItemResponses.buildFailureMessage());
                        } else {
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
        } else {
            LOG.error("Could not persist data. It seems like ElasticSearch is not reachable.");
        }
    }

    /**
     * Returns the elasticsearch indexName, based on the current dateTime and configuration.
     *
     * @return
     */
    private String getElasticIndexName() {
        Date date = Date.from(Instant.now());

        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
        String dateAsString = sdf.format(date);
        String indexName = indexPrefix + "_" + ((tenantId != null) ? tenantId + "_" : "") + dateAsString;
        return indexName.toLowerCase();
    }

    private boolean indexExists(String indexName) {

        try {
            //Indices Exist API is currently not supported in the high level client
            highLevelClient.getLowLevelClient().performRequest("GET", "/" + indexName);
            return true;
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == 404) {
                return false;
            }
            LOG.error(e.getMessage());
            return false;
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage());
            return false;
        }
    }

    /**
     * Read a file from the resources directory and store the content in a string
     *
     * @param file the file to read
     *
     * @return a string containing the file content
     */
    private String readFileResource(String file) {

        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage());
            return null;
        }
    }

    private Map<String, Object> serializeAndRemove(Object object, String... toRemove) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            Map<String, Object> result = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
            for (String s : toRemove) {
                result.remove(s);
            }
            return result;
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    private List<Map<String, Object>> serializeAndRemoveList(List<?> objects, String... toRemove) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (Object o : objects) {
            result.add(serializeAndRemove(o, toRemove));
        }
        return result;
    }

    /**
     * A prerequisite for calling this method is that there exists at least one index in ES with the name "securecodebox..."
     * @throws IOException
     */
    private void initializeKibana() throws IOException {

        if(!indexExists(".kibana")) {

            LOG.info(".kibana index doesn't exist. Creating it...");

            //Create Kibana Index
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(".kibana");
            String mapping = readFileResource("kibana-mapping.json");
            if (mapping != null) {
                createIndexRequest.mapping("doc", mapping, XContentType.JSON);
            }
            highLevelClient.indices().create(createIndexRequest);
        }

        SearchRequest searchRequest = new SearchRequest(".kibana");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("type", "index-pattern"))
                        .must(QueryBuilders.matchQuery("index-pattern.title", "securecodebox*")));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = highLevelClient.search(searchRequest);
        boolean searchFailure = searchResponse.isTimedOut() || (searchResponse.status() != RestStatus.OK);
        if (searchFailure) {
            LOG.error("Searching the index failed. Skipping kibana initialization...");
            return;
        }

        LOG.info("SearchResponse from .kibana index-pattern Search: " + searchResponse);

        if (searchResponse.getHits().totalHits == 0) {

            LOG.info("Index Pattern securecodebox* doesn't exist. Creating it...");

            //The index-pattern "securecodebox*" doesn't exist, we need to create it along with the import objects

            ObjectMapper objectMapper = new ObjectMapper();

            String kibanaFile = readFileResource("kibana-imports.json");
            List<KibanaData> dataElements = objectMapper.readValue(kibanaFile, objectMapper.getTypeFactory().constructCollectionType(List.class, KibanaData.class));

            BulkRequest bulkRequest = new BulkRequest();
            for(KibanaData data: dataElements) {
                IndexRequest indexRequest = new IndexRequest(data.getIndex(), data.getType(), data.getId());
                indexRequest.source(objectMapper.writeValueAsString(data.getSource()), XContentType.JSON);
                bulkRequest.add(indexRequest);
            }
            highLevelClient.bulkAsync(bulkRequest, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    if (bulkItemResponses.hasFailures()) {
                        LOG.error("There were failures in creating the kibana data. Kibana index may be corrupted. Deleting..");
                        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(".kibana");
                        try {
                            highLevelClient.indices().delete(deleteIndexRequest);
                        }
                        catch (IOException e){
                            LOG.error("Kibana index could not be successfully deleted and might be corrupted. Delete it manually!");
                        }
                    } else {
                        LOG.info("Successfully created kibana data");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    LOG.error("Could not import kibana data");
                }
            });
        }
        else {
            LOG.info("Index Pattern securecodebox* exists. Assuming that searches, visualizations and dashboards are imported.");
        }
    }
}
