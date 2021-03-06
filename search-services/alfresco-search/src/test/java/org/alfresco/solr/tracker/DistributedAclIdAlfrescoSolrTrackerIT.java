/*
 * #%L
 * Alfresco Search Services
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.solr.tracker;

import org.alfresco.repo.index.shard.ShardMethodEnum;
import org.alfresco.solr.AbstractAlfrescoDistributedIT;
import org.alfresco.solr.SolrInformationServer;
import org.alfresco.solr.client.Acl;
import org.alfresco.solr.client.AclChangeSet;
import org.alfresco.solr.client.AclReaders;
import org.alfresco.solr.client.Node;
import org.alfresco.solr.client.NodeMetaData;
import org.alfresco.solr.client.Transaction;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.alfresco.repo.search.adaptor.lucene.QueryConstants.FIELD_DOC_TYPE;
import static org.alfresco.solr.AlfrescoSolrUtils.getAcl;
import static org.alfresco.solr.AlfrescoSolrUtils.getAclChangeSet;
import static org.alfresco.solr.AlfrescoSolrUtils.getAclReaders;
import static org.alfresco.solr.AlfrescoSolrUtils.getNode;
import static org.alfresco.solr.AlfrescoSolrUtils.getNodeMetaData;
import static org.alfresco.solr.AlfrescoSolrUtils.getTransaction;
import static org.alfresco.solr.AlfrescoSolrUtils.indexAclChangeSet;
import static org.alfresco.solr.AlfrescoSolrUtils.list;

/**
 * @author Joel
 */
@SolrTestCaseJ4.SuppressSSL
public class DistributedAclIdAlfrescoSolrTrackerIT extends AbstractAlfrescoDistributedIT
{

    @BeforeClass
    public static void initData() throws Throwable
    {
        initSolrServers(2, DistributedAclIdAlfrescoSolrTrackerIT.class.getSimpleName(), getShardMethod());
    }

    @AfterClass
    public static void destroyData()
    {
        dismissSolrServers();
    }
    
    @Test
    public void testAclId() throws Exception
    {
        putHandleDefaults();

        int numAcls = 250;
        AclChangeSet bulkAclChangeSet = getAclChangeSet(numAcls);

        List<Acl> bulkAcls = new ArrayList<>();
        List<AclReaders> bulkAclReaders = new ArrayList<>();


        for(int i=0; i<numAcls; i++)
        {
            Acl bulkAcl = getAcl(bulkAclChangeSet);
            bulkAcls.add(bulkAcl);
            bulkAclReaders.add(getAclReaders(bulkAclChangeSet,
                                             bulkAcl,
                                             Collections.singletonList("joel"+bulkAcl.getId()),
                                                Collections.singletonList("phil"+bulkAcl.getId()),
                                             null));
        }

        indexAclChangeSet(bulkAclChangeSet,
                          bulkAcls,
                          bulkAclReaders);

        int numNodes = 1000;
        List<Node> nodes = new ArrayList<>();
        List<NodeMetaData> nodeMetaDatas = new ArrayList<>();

        Transaction bigTxn = getTransaction(0, numNodes);

        for(int i=0; i<numNodes; i++)
        {
            int aclIndex = i % numAcls;
            Node node = getNode(bigTxn, bulkAcls.get(aclIndex), Node.SolrApiNodeStatus.UPDATED);
            nodes.add(node);
            NodeMetaData nodeMetaData = getNodeMetaData(node, bigTxn, bulkAcls.get(aclIndex), "mike", null, false);
            nodeMetaDatas.add(nodeMetaData);
        }

        indexTransaction(bigTxn, nodes, nodeMetaDatas);
        waitForDocCount(new TermQuery(new Term("content@s___t@{http://www.alfresco.org/model/content/1.0}content", "world")), numNodes, 100000);
        waitForDocCount(new TermQuery(new Term(FIELD_DOC_TYPE, SolrInformationServer.DOC_TYPE_ACL)), numAcls, 100000);

        for(int i=0; i<numAcls; i++)
        {
            Acl acl = bulkAcls.get(i);
            long aclId = acl.getId();

            QueryResponse response = query(getDefaultTestClient(), true,
                    "{\"locales\":[\"en\"], \"templates\": [{\"name\":\"t1\", \"template\":\"%cm:content\"}], \"authorities\": [\"joel" + aclId + "\"], \"tenants\": [ \"\" ]}",
                    params("q", "t1:world", "qt", "/afts", "shards.qt", "/afts", "start", "0", "rows", "100", "sort", "id asc","fq","{!afts}AUTHORITY_FILTER_FROM_JSON"));

            assertTrue(response.getResults().getNumFound() > 0);
        }
    }

    protected static Properties getShardMethod() 
    {
        Random random = random();
        List<ShardMethodEnum> methods = new ArrayList<>();
        methods.add(ShardMethodEnum.ACL_ID);
        methods.add(ShardMethodEnum.MOD_ACL_ID);
        Collections.shuffle(methods, random);
        Properties prop = new Properties();
        prop.put("shard.method", methods.get(0).toString());
        return prop;
    }
}

