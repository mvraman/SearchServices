package org.alfresco.cmis.search;

import org.alfresco.cmis.CmisTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.provider.XMLDataConfig;
import org.alfresco.utility.data.provider.XMLTestData;
import org.alfresco.utility.data.provider.XMLTestDataProvider;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.QueryModel;
import org.alfresco.utility.model.TestGroup;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { TestGroup.CMIS, TestGroup.QUERIES })
public class SolrSearchByAspectTests extends CmisTest
{
    XMLTestData testData;

    @BeforeClass(alwaysRun = true)
    public void deployCustomModel() throws TestConfigurationException
    {
        dataContent.deployContentModel("shared-resources/model/tas-model.xml");
        cmisApi.authenticateUser(dataUser.getAdminUser());
    }
    
    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() throws Exception
    {
        testData.cleanup(dataContent);
    }

    @Test(dataProviderClass = XMLTestDataProvider.class, dataProvider = "getAllData")
    @XMLDataConfig(file = "src/main/resources/shared-resources/testdata/search-by-aspect.xml")
    public void prepareDataForAspectSearch(XMLTestData testData) throws Exception
    {
        this.testData = testData;
        this.testData.createUsers(dataUser);
        this.testData.createSitesStructure(dataSite, dataContent, dataUser);
        // wait for solr index
        Utility.waitToLoopTime(10);
    }

    @Test(dataProviderClass = XMLTestDataProvider.class, dataProvider = "getQueriesData", dependsOnMethods = "prepareDataForAspectSearch")
    @XMLDataConfig(file = "src/main/resources/shared-resources/testdata/search-by-aspect.xml")
    public void executeSearchByAspect(QueryModel query) throws Exception
    {
        cmisApi.withQuery(query.getValue())
            .applyNodeRefsFrom(testData).assertResultsCountIs(query.getResults());
    }
}
