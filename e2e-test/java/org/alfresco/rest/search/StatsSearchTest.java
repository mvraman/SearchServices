/*
 * Copyright (C) 2017 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.rest.search;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stats search test.
 * @author Gethin James
 *
 */
public class StatsSearchTest extends AbstractSearchTest
{

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH  }, executionType = ExecutionType.REGRESSION,
              description = "Checks errors with stats using Search api")
    public void searchWithBasicStats() throws Exception
    {
        SearchRequest query = carsQuery();
        Pagination pagination = new Pagination();
        pagination.setMaxItems(2);
        List<RestRequestStatsModel> statsModels = new ArrayList<>();
        RestRequestStatsModel statsModel1 = new RestRequestStatsModel();
        statsModels.add(statsModel1);
        query.setStats(statsModels);
        query.setPaging(pagination);

        SearchResponse response =  query(query);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                    .containsSummary(String.format(RestErrorModel.MANDATORY_PARAM, "stats field"));

        statsModel1.setField("DBID");
        response =  query(query);
        //8 metrics by default for a numeric field
        Set<String> metricTypes = assertStatsFacetedResponse(response, "DBID", 8);
        assertTrue(metricTypes.containsAll(Arrays.asList("missing", "count", "sum","min","max", "sumOfSquares", "mean", "stddev")));

        statsModel1.setField("creator");
        response =  query(query);
        //4 metrics by default for a string field
        metricTypes = assertStatsFacetedResponse(response, "creator", 4);
        assertTrue(metricTypes.containsAll(Arrays.asList("missing", "count", "min","max")));

        statsModel1.setField("modified");
        response =  query(query);
        //8 metrics by default for a date field
        metricTypes = assertStatsFacetedResponse(response, "modified", 8);
        assertTrue(metricTypes.containsAll(Arrays.asList("missing", "count", "sum","min","max", "sumOfSquares", "mean", "stddev")));

        statsModel1.setField("modifier");
        statsModel1.setMin(false);
        statsModel1.setMax(false);
        statsModel1.setMissing(false);
        response =  query(query);
        metricTypes = assertStatsFacetedResponse(response, "modifier", 1);
        assertFalse(metricTypes.containsAll(Arrays.asList("missing", "min","max")));

        statsModel1.setField("DBID");
        statsModel1.setMin(true);
        statsModel1.setMax(true);
        statsModel1.setCount(false);
        statsModel1.setMissing(false);
        statsModel1.setSum(false);
        statsModel1.setSumOfSquares(false);
        statsModel1.setMean(false);
        statsModel1.setStddev(false);
        response =  query(query);
        metricTypes = assertStatsFacetedResponse(response, "DBID", 2);
        assertTrue(metricTypes.containsAll(Arrays.asList("min","max")));
        assertFalse(metricTypes.containsAll(Arrays.asList("missing", "count", "sum","sumOfSquares", "mean", "stddev")));

        statsModel1.setField("modifier");
        statsModel1.setMin(false);
        statsModel1.setMax(false);
        statsModel1.setCountDistinct(true);
        statsModel1.setDistinctValues(true);
        statsModel1.setCardinality(true);
        response =  query(query);
        metricTypes = assertStatsFacetedResponse(response, "modifier", 3);
        assertTrue(metricTypes.containsAll(Arrays.asList("countDistinct", "distinctValues", "cardinality")));

        statsModel1.setField("DBID");
        statsModel1.setPercentiles(Arrays.asList(1,75,99,99.9));
        statsModel1.setCountDistinct(false);
        statsModel1.setDistinctValues(false);
        statsModel1.setCardinality(false);
        response =  query(query);
        assertStatsFacetedResponse(response, "DBID", 1);
        RestGenericMetricModel percMetric = response.getContext().getFacets().get(0).getBuckets().get(0).getMetrics().get(0);
        assertEquals(percMetric.getType(),"percentiles");
        Map percVal = (Map) percMetric.getValue();
        Map percentiles = (Map) percVal.get("percentiles");
        assertEquals(percentiles.size(),4);
        assertTrue(percentiles.keySet().containsAll(Arrays.asList("1.0","75.0","99.0","99.9")));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH  }, executionType = ExecutionType.REGRESSION,
              description = "Checks errors with stats labels using Search api")
    public void searchWithStatsLabel() throws Exception
    {
        SearchRequest query = carsQuery();
        Pagination pagination = new Pagination();
        pagination.setMaxItems(2);
        List<RestRequestStatsModel> statsModels = new ArrayList<>();
        RestRequestStatsModel statsModel1 = new RestRequestStatsModel();
        statsModel1.setField("modified");
        statsModel1.setLabel("DateChanged");
        statsModels.add(statsModel1);
        query.setStats(statsModels);
        query.setPaging(pagination);
        SearchResponse response = query(query);
        assertStatsFacetedResponse(response, "DateChanged", 8);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH  }, executionType = ExecutionType.REGRESSION,
              description = "Checks errors with stats fitlers using Search api")
    public void searchWithStatsFilters() throws Exception
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("content.mimetype:text/plain");
        query.setQuery(queryReq);
        List<RestRequestStatsModel> statsModels = new ArrayList<>();
        RestRequestStatsModel statsModel1 = new RestRequestStatsModel();
        statsModel1.setField("creator");
        statsModel1.setMin(false);
        statsModel1.setMax(false);
        statsModel1.setMissing(false);
        statsModels.add(statsModel1);
        query.setFilterQueries(new RestRequestFilterQueryModel("cars", Arrays.asList("justCars")));
        query.setStats(statsModels);
        SearchResponse response = query(query);
        assertStatsFacetedResponse(response, "creator", 1);
        RestGenericMetricModel countMetric = response.getContext().getFacets().get(0).getBuckets().get(0).getMetrics().get(0);
        Integer count = response.getPagination().getTotalItems();
        Map metricCount = (Map) countMetric.getValue();
        assertEquals(count, metricCount.get("count"));

        statsModel1.setExcludeFilters(Arrays.asList("justCars"));
        response = query(query);
        assertStatsFacetedResponse(response, "creator", 1);
        countMetric = response.getContext().getFacets().get(0).getBuckets().get(0).getMetrics().get(0);
        count = response.getEntries().size();
        metricCount = (Map) countMetric.getValue();
        assertTrue((Integer)metricCount.get("count") > count, "With the exclude filter there will be more documents than returned");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH  }, executionType = ExecutionType.REGRESSION,
              description = "Checks errors with stats with Pivot using Search api")
    public void searchWithStatsAndMutlilevelPivot() throws Exception
    {
        SearchRequest query = carsQuery();

        Pagination pagination = new Pagination();
        pagination.setMaxItems(1);
        query.setPaging(pagination);
        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        RestRequestFacetFieldModel b0 = new RestRequestFacetFieldModel("SITE");
        b0.setLabel("b0");
        list.add(b0);
        list.add(new RestRequestFacetFieldModel("created"));
        RestRequestFacetFieldModel b2 = new RestRequestFacetFieldModel("modifier");
        b2.setLabel("b2");
        list.add(b2);
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);

        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        RestRequestPivotModel pivots = new RestRequestPivotModel();
        pivots.setKey("b0");
        RestRequestPivotModel pivotn = new RestRequestPivotModel();
        pivotn.setKey("created");
        RestRequestPivotModel pivot2 = new RestRequestPivotModel();
        pivot2.setKey("aNumber");

        pivotn.setPivots(Arrays.asList(pivot2));
        pivots.setPivots(Arrays.asList(pivotn));
        pivotModelList.add(pivots);
        query.setPivots(pivotModelList);

        List<RestRequestStatsModel> statsModels = new ArrayList<>();
        RestRequestStatsModel statsModel1 = new RestRequestStatsModel();
        statsModels.add(statsModel1);
        query.setStats(statsModels);
        statsModel1.setField("DBID");
        statsModel1.setLabel("aNumber");

        SearchResponse response =  query(query);
        response.getContext().assertThat().field("facetsFields").isNotNull();
        response.getContext().assertThat().field("facets").isNotEmpty();
        assertEquals(response.getContext().getFacetsFields().size(), 1, "There should be 1 facet field for modifier");
        assertEquals(response.getContext().getFacets().size(), 2, "There should be 1 pivot facet with  stats on the end and a high level stats facet");

        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().get(0);
        facetResponseModel.assertThat().field("type").is("pivot");
        facetResponseModel.assertThat().field("label").is("b0");

        //pivot created
        RestGenericFacetResponseModel created = facetResponseModel.getBuckets().get(0).getFacets().get(0);
        created.assertThat().field("type").is("pivot");
        created.assertThat().field("label").is("created");
        //Another nested stats
        assertEquals(created.getBuckets().get(0).getMetrics().size(), 8, "Metrics are on the end of a pivot bucket");

    }


    @Test(groups = { TestGroup.REST_API, TestGroup.SEARCH })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH  }, executionType = ExecutionType.REGRESSION,
              description = "Checks errors with stats with Pivot using Search api")
    public void searchWithStatsAndPivot() throws Exception
    {
        SearchRequest query = carsQuery();

        Pagination pagination = new Pagination();
        pagination.setMaxItems(2);
        query.setPaging(pagination);
        RestRequestFacetFieldsModel facetFields = new RestRequestFacetFieldsModel();
        List<RestRequestFacetFieldModel> list = new ArrayList<>();
        list.add(new RestRequestFacetFieldModel("creator"));
        facetFields.setFacets(list);
        query.setFacetFields(facetFields);
        query.setIncludeRequest(false);

        SearchResponse response =  query(query);
        response.getContext().assertThat().field("facetsFields").isNotNull();

        List<RestRequestPivotModel> pivotModelList = new ArrayList<>();
        RestRequestPivotModel pivots = new RestRequestPivotModel();
        pivots.setKey("creator");
        RestRequestPivotModel pivotn = new RestRequestPivotModel();
        pivotn.setKey("numericId");

        pivots.setPivots(Arrays.asList(pivotn));
        pivotModelList.add(pivots);
        query.setPivots(pivotModelList);

        List<RestRequestStatsModel> statsModels = new ArrayList<>();
        RestRequestStatsModel statsModel1 = new RestRequestStatsModel();
        statsModels.add(statsModel1);
        query.setStats(statsModels);
        statsModel1.setField("DBID");
        statsModel1.setLabel("numericId");

        response =  query(query);

        response.getContext().assertThat().field("facets").isNotEmpty();
        assertEquals(response.getContext().getFacets().size(), 2, "There should be 1 pivot facet with  stats on the end and a high level stats facet");
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().get(0);
        facetResponseModel.assertThat().field("type").is("pivot");
        facetResponseModel.assertThat().field("label").is("creator");
        assertEquals(facetResponseModel.getBuckets().get(0).getMetrics().size(), 8, "Metrics are on the end of a pivot bucket");
        RestGenericFacetResponseModel statsFacet = response.getContext().getFacets().get(1);
        statsFacet.assertThat().field("type").is("stats");
        statsFacet.assertThat().field("label").is("numericId");

    }

    private Set<String> assertStatsFacetedResponse(SearchResponse response, String label, int metricsCount) throws Exception
    {
        response.getContext().assertThat().field("facets").isNotEmpty();
        RestGenericFacetResponseModel facetResponseModel = response.getContext().getFacets().get(0);
        facetResponseModel.assertThat().field("type").is("stats");
        facetResponseModel.assertThat().field("label").is(label);
        RestGenericBucketModel bucket = facetResponseModel.getBuckets().get(0);
        List<RestGenericMetricModel> metrics = bucket.getMetrics();
        assertEquals(metrics.size(),metricsCount);
        return metrics.stream().map(m -> m.getType()).collect(Collectors.toSet());
    }

}
