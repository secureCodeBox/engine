package io.securecodebox.persistence.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.model.Report;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.persistence.PersistenceProvider;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

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

    //todo: Initialize the connection to elasticSearch
    private void init(){

        highLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
        String indexName = getElasticIndexName();

        try {
            try {

                //Indices Exist API is currently not supported in the high level client
                highLevelClient.getLowLevelClient().performRequest("GET", "/" + indexName);
            }
            catch (ResponseException e){
                if(e.getResponse().getStatusLine().getStatusCode() == 404) {

                    //The index doesn't exist until now, so we create it
                    LOG.info("Index " + indexName + " doesn't exist. Creating it...");
                    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                    CreateIndexResponse createIndexResponse = highLevelClient.indices().create(createIndexRequest);
                    LOG.info("Successfully created index " + indexName);
                }
            }
            finally {
                initialized = true;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void persist(Report report) {

        if(!initialized){
            init();
        }

        //Second check because, if the initialization wasn't successful, it's still false
        if(initialized) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {

                //Persisting the Report
                String jsonReport = objectMapper.writeValueAsString(report);
                IndexRequest indexRequest = new IndexRequest(getElasticIndexName(), TYPE_REPORT);
                indexRequest.source(jsonReport, XContentType.JSON);
                IndexResponse response = highLevelClient.index(indexRequest);
                LOG.info("Successfully saved report to " + getElasticIndexName());

                //Persisting the Findings
                //Doesn't work at the moment because there is only one type per index allowed since elasticsearch 6
//                BulkRequest bulkRequest = new BulkRequest();
//                for(Finding f : report.getFindings()){
//                    String jsonFinding = objectMapper.writeValueAsString(f);
//                    IndexRequest findingIndexRequest = new IndexRequest(getElasticIndexName(), TYPE_FINDING);
//                    findingIndexRequest.source(jsonFinding, XContentType.JSON);
//                    bulkRequest.add(findingIndexRequest);
//                }
//                BulkResponse bulkResponse = highLevelClient.bulk(bulkRequest);
                LOG.info("Successfully saved findings to " + getElasticIndexName());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        String indexName = scbIndexPrefix + "_" + dateAsString;
        return indexName.toLowerCase();
    }
}
