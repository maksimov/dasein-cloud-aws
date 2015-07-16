/*
 * Copyright (C) 2009-2015 Dell, Inc.
 * See annotations for authorship information
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.aws.container;

import org.apache.log4j.Logger;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.aws.compute.EC2Exception;
import org.dasein.cloud.aws.compute.EC2Method;
import org.dasein.cloud.aws.identity.IAMMethod;
import org.dasein.cloud.container.AbstractContainerSupport;
import org.dasein.cloud.container.Cluster;
import org.dasein.cloud.container.ContainerCapabilities;
import org.dasein.cloud.identity.CloudGroup;
import org.dasein.cloud.identity.CloudPermission;
import org.dasein.cloud.util.APITrace;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Stas Maksimov (stas.maksimov@software.dell.com)
 * @since 2015.09
 */
public class ElasticContainerSupport extends AbstractContainerSupport<AWSCloud> {
    public static final String SERVICE_ID = "ecs";
    static private final Logger logger = AWSCloud.getLogger(ElasticContainerSupport.class);

    private volatile transient EcsCapabilities capabilities;

    protected ElasticContainerSupport(AWSCloud provider) {
        super(provider);
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return true; // FIXME
    }

    @Nonnull
    @Override
    public ContainerCapabilities getCapabilities() throws CloudException, InternalException {
        if( capabilities == null ) {
            capabilities = new EcsCapabilities(getProvider());
        }
        return capabilities;
    }

    @Nonnull
    @Override
    public Iterable<Cluster> listClusters() throws CloudException, InternalException {
        return super.listClusters();
    }

    @Nonnull
    @Override
    public String createCluster(@Nonnull String clusterName) throws CloudException, InternalException {
        APITrace.begin(getProvider(), "Containers.createCluster");
        try {
            Map<String,String> parameters = getProvider().getStandardParameters(getContext(), EC2Method.CREATE_CLUSTER, getProvider().getEcsVersion());
            clusterName = validateName(clusterName);
            parameters.put("clusterName", clusterName);
            if( logger.isDebugEnabled() ) {
                logger.debug("parameters=" + parameters);
            }

            EC2Method method = new EC2Method(SERVICE_ID, getProvider(), parameters);
            try {
                if( logger.isInfoEnabled() ) {
                    logger.info("Creating container cluster " + clusterName + "...");
                }
                Document doc = method.invoke();
                NodeList blocks = doc.getElementsByTagName("cluster");
                NodeList attributes = blocks.item(0).getChildNodes();
                String clusterArn = null;
                for( int i=0; i<attributes.getLength(); i++ ) {
                    String name = attributes.item(i).getNodeName();
                    if( "clusterArn".equals(name) ) {
                        clusterArn = attributes.item(i).getFirstChild().getNodeValue();
                    }
                    if( logger.isDebugEnabled() ) {
                        logger.debug("clusterArn=" + clusterArn);
                    }
                    if( clusterArn != null) {
                        if( logger.isInfoEnabled() ) {
                            logger.info("Created.");
                        }
                        return clusterArn;
                    }
                }
                logger.error("No container cluster was created as a result of the request");
                throw new CloudException("No container cluster was created as a result of the request");
            }
            catch( EC2Exception e ) {
                logger.error(e.getSummary());
                throw new CloudException(e);
            }
        }
        finally {
            APITrace.end();
        }
    }

    @Nullable
    @Override
    public Cluster getCluster(@Nonnull String providerClusterId) throws CloudException, InternalException {
        APITrace.begin(getProvider(), "Containers.getCluster");
        try {
            Map<String,String> parameters = getProvider().getStandardParameters(getContext(), EC2Method.DESCRIBE_CLUSTERS, getProvider().getEcsVersion());
            parameters.put("clusters.member.1", providerClusterId);
            if( logger.isDebugEnabled() ) {
                logger.debug("parameters=" + parameters);
            }

            EC2Method method = new EC2Method(SERVICE_ID, getProvider(), parameters);
            try {
                Document doc = method.invoke();
                NodeList blocks = doc.getElementsByTagName("member");
                for( int i=0; i<blocks.getLength(); i++ ) {
                    return toCluster(blocks.item(i));
                }
            }
            catch( EC2Exception e ) {
                logger.error(e.getSummary());
                throw new CloudException(e);
            }
            return null;
        }
        finally {
            APITrace.end();
        }
    }

    private Cluster toCluster(Node item) {
        if( item == null ) {
            return null;
        }
        NodeList attributes = item.getChildNodes();
        String clusterId = null, name = null, status = null;

        for( int i=0; i<attributes.getLength(); i++ ) {
            Node attribute = attributes.item(i);
            String attrName = attribute.getNodeName();

            if( attrName.equalsIgnoreCase("clusterName") && attribute.hasChildNodes() ) {
                name = attribute.getFirstChild().getNodeValue().trim();
            }
            else if( attrName.equalsIgnoreCase("clusterArn") && attribute.hasChildNodes() ) {
                clusterId = attribute.getFirstChild().getNodeValue().trim();
            }
            else if( attrName.equalsIgnoreCase("status") && attribute.hasChildNodes() ) {
                status = attribute.getFirstChild().getNodeValue().trim();
            }
        }
        if( name == null || clusterId == null ) {
            return null;
        }
        return new Cluster(clusterId, name);
    }

    @Override
    public void removeCluster(@Nonnull String providerClusterId) throws CloudException, InternalException {
        APITrace.begin(getProvider(), "Containers.removeCluster");
        try {
            Map<String,String> parameters = getProvider().getStandardParameters(getContext(), EC2Method.DELETE_CLUSTER, getProvider().getEcsVersion());
            parameters.put("cluster", providerClusterId);

            EC2Method method = new EC2Method(SERVICE_ID, getProvider(), parameters);
            try {
                method.invoke();
            }
            catch( EC2Exception e ) {
                logger.error(e.getSummary());
                throw new CloudException(e);
            }
        }
        finally {
            APITrace.end();
        }
    }

    private @Nonnull String validateName(@Nonnull String name) {
        // It must contain only alphanumeric characters and/or the following: _-
        StringBuilder str = new StringBuilder();

        for( int i=0; i< name.length(); i++ ) {
            char c = name.charAt(i);

            if( Character.isLetterOrDigit(c) ) {
                str.append(c);
            }
            else if( c == '_' || c == '-' ) {
                str.append(c);
            }
            else if( c == ' ' ) {
                str.append("-");
            }
        }
        if( str.length() < 1 ) {
            return String.valueOf(System.currentTimeMillis());
        }
        return str.toString();
    }

}
