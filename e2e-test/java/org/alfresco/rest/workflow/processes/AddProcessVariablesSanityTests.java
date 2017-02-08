package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableCollection;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddProcessVariablesSanityTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee, adminUser, adminTenantUser, tenantUserAssignee, tenantUser;
    private RestProcessModel processModel;
    private RestProcessVariableModel variableModel, variableModel1, variableModel2, processVariable;
    private RestProcessVariableCollection processVariableCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Create non-existing variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void addProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();

        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(processVariable.getName())
                       .and().field("type").is(processVariable.getType())
                       .and().field("value").is(processVariable.getValue());

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                .assertThat().entriesListContains("name", processVariable.getName());
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Create non-existing variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void addMultipleProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel2 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();

        processVariableCollection = restClient.withWorkflowAPI().usingProcess(processModel)
                                    .addProcessVariables(variableModel, variableModel1, variableModel2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        processVariableCollection.getEntries().get(0).onModel()
                        .assertThat().field("name").is( processVariableCollection.getEntries().get(0).onModel().getName())
                        .and().field("type").is( processVariableCollection.getEntries().get(0).onModel().getType())
                        .and().field("value").is( processVariableCollection.getEntries().get(0).onModel().getValue());
        
        processVariableCollection.getEntries().get(1).onModel()
                        .assertThat().field("name").is( processVariableCollection.getEntries().get(1).onModel().getName())
                        .and().field("type").is( processVariableCollection.getEntries().get(1).onModel().getType())
                        .and().field("value").is( processVariableCollection.getEntries().get(1).onModel().getValue());
        
        processVariableCollection.getEntries().get(2).onModel()
                        .assertThat().field("name").is( processVariableCollection.getEntries().get(2).onModel().getName())
                        .and().field("type").is( processVariableCollection.getEntries().get(2).onModel().getType())
                        .and().field("value").is( processVariableCollection.getEntries().get(2).onModel().getValue());

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                .assertThat().entriesListContains("name",  processVariableCollection.getEntries().get(1).onModel().getName())
                .assertThat().entriesListContains("name",  processVariableCollection.getEntries().get(0).onModel().getName())
                .assertThat().entriesListContains("name",  processVariableCollection.getEntries().get(2).onModel().getName());
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Update existing variables")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void updateExistingProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
       
        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(processVariable.getName())
                        .and().field("type").is(processVariable.getType())
                        .and().field("value").is(processVariable.getValue());

        String newValue = RandomData.getRandomName("value");
        variableModel.setValue(newValue);
        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("value").is(newValue);
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Update existing variables")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void updateExistingMultipleProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
       
        processVariableCollection = restClient.withWorkflowAPI().usingProcess(processModel)
                                              .addProcessVariables(variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariableCollection.getEntries().get(0).onModel()
                                 .assertThat().field("name").is(variableModel.getName())
                                 .and().field("type").is(variableModel.getType())
                                 .and().field("value").is(variableModel.getValue());
        
        processVariableCollection.getEntries().get(1).onModel()
                                 .assertThat().field("name").is(variableModel1.getName())
                                 .and().field("type").is(variableModel1.getType())
                                 .and().field("value").is(variableModel1.getValue());

        String newValueVar = RandomData.getRandomName("valueVar");
        variableModel.setValue(newValueVar);
        String newValueVar1 = RandomData.getRandomName("valueVar1");
        variableModel1.setValue(newValueVar1);
        
        processVariableCollection = restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariables(variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariableCollection.getEntries().get(0).onModel().assertThat().field("value").is(newValueVar);
        processVariableCollection.getEntries().get(1).onModel().assertThat().field("value").is(newValueVar1);
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Adding process variables is falling in case invalid variableBody is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void failedAddingProcessVariableIfInvalidBodyIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("incorrect type");
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("Unsupported type of variable: 'incorrect type'.");
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Add process variables using admin user from same network")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY, TestGroup.NETWORKS })
    public void addProcessVariableByAdminSameNetwork() throws Exception
    {
        adminTenantUser = UserModel.getAdminTenantUser();
        restClient.authenticateUser(adminUser).usingTenant().createTenant(adminTenantUser);
        tenantUser = dataUser.usingUser(adminTenantUser).createUserWithTenant("uTenant");
        tenantUserAssignee = dataUser.usingUser(adminTenantUser).createUserWithTenant("uTenantAssignee");

        restClient.authenticateUser(tenantUser).withWorkflowAPI().addProcess("activitiAdhoc", tenantUserAssignee, false, Priority.Normal);
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(adminTenantUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
       
        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(processVariable.getName())
                        .and().field("type").is(processVariable.getType())
                        .and().field("value").is(processVariable.getValue());
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Add multiple process variables using admin user from same network")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY, TestGroup.NETWORKS })
    public void addMultipleProcessVariablesByAdminSameNetwork() throws Exception
    {
        adminTenantUser = UserModel.getAdminTenantUser();
        restClient.authenticateUser(adminUser).usingTenant().createTenant(adminTenantUser);
        tenantUser = dataUser.usingUser(adminTenantUser).createUserWithTenant("uTenant");
        tenantUserAssignee = dataUser.usingUser(adminTenantUser).createUserWithTenant("uTenantAssignee");

        restClient.authenticateUser(tenantUser).withWorkflowAPI().addProcess("activitiAdhoc", tenantUserAssignee, false, Priority.Normal);
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(adminTenantUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
       
        processVariableCollection = restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariables(variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        processVariableCollection.assertThat().entriesListContains("name", variableModel.getName())
                                 .assertThat().entriesListContains("name", variableModel1.getName());

        processVariableCollection.getEntries().get(0).onModel().assertThat()
                                 .field("name").is(variableModel.getName()).and()
                                 .field("value").is(variableModel.getValue()).and()
                                 .field("type").is(variableModel.getType());
        processVariableCollection.getEntries().get(1).onModel().assertThat()
                                 .field("name").is(variableModel1.getName()).and()
                                 .field("value").is(variableModel1.getValue()).and()
                                 .field("type").is(variableModel1.getType());
    }

}
