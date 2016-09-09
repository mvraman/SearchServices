package org.alfresco.rest;

import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.SiteMember;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.social.alfresco.api.entities.Role;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "rest-api", "people", "sanity" })
public class SampleSitesTest extends RestTest
{
    @Autowired
    RestSitesApi siteAPI;

    @Autowired
    DataUser dataUser;

    @Autowired
    DataSite dataSite;

    private UserModel userModel;
    private SiteModel siteModel;

    @BeforeClass
    public void initTest() throws DataPreparationException
    {
        userModel = dataUser.getAdminUser();
        restClient.authenticateUser(userModel);
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        siteAPI.useRestClient(restClient);
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldGetSiteDetails() throws JsonToModelConversionException, Exception
    {
        siteAPI.getSite(siteModel.getId())
            .assertResponseIsNotEmpty();
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldGetSites() throws JsonToModelConversionException, Exception
    {
        siteAPI.getSite(siteModel.getId());
        siteAPI.usingRestWrapper()
            .assertStatusCodeIs(HttpStatus.OK.toString());
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldAccessSites() throws JsonToModelConversionException, Exception
    {
        siteAPI.getSites()
            .assertThatResponseIsNotEmpty();
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldRetrieveSites() throws JsonToModelConversionException, Exception
    {
        siteAPI.getSites();
        siteAPI.usingRestWrapper()
            .assertStatusCodeIs(HttpStatus.OK.toString());
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldAccessResponsePagination() throws JsonToModelConversionException, Exception
    {
        siteAPI.getSites()
            .assertResponseHasPagination();
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldAddNewSiteMember() throws JsonToModelConversionException, DataPreparationException, Exception
    {
        UserModel newMember = dataUser.createRandomTestUser();
        SiteMember siteMember = new SiteMember(Role.SiteCollaborator.toString(), newMember.getUsername());

        siteAPI.addPerson(siteModel.getId(), siteMember);
        siteAPI.usingRestWrapper()
            .assertStatusCodeIs(HttpStatus.CREATED.toString());
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldGetSiteFromSitesList() throws JsonToModelConversionException, Exception
    {
        siteAPI.getAllSites()
            .assertThatResponseHasSite(siteModel.getId());
    }

    @TestRail(section={"rest-api", "sites"}, executionType= ExecutionType.SANITY)
    public void adminShouldAccessSiteDetails1() throws JsonToModelConversionException, Exception
    {
        siteAPI.getSite(siteModel.getId())
            .assertResponseIsNotEmpty()
            .assertSiteHasDescription(siteModel.getDescription())
            .assertSiteHasTitle(siteModel.getTitle())
            .assertSiteHasVisibility(siteModel.getVisibility());
    }

}