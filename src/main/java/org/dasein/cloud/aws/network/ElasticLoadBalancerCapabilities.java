/**
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

package org.dasein.cloud.aws.network;

import org.dasein.cloud.*;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.network.*;
import org.dasein.cloud.util.NamingConstraints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Describes the capabilities of AWS with respect to Dasein load balancer operations.
 * <p>Created by Stas Maksimov: 04/03/2014 13:12</p>
 *
 * @author Stas Maksimov
 * @version 2014.03 initial version
 * @since 2014.03
 */
public class ElasticLoadBalancerCapabilities extends AbstractCapabilities<AWSCloud> implements LoadBalancerCapabilities {

    public ElasticLoadBalancerCapabilities(@Nonnull AWSCloud cloud) {
        super(cloud);
    }

    @Nonnull
    @Override
    public LoadBalancerAddressType getAddressType() throws CloudException, InternalException {
        return LoadBalancerAddressType.DNS;
    }

    @Override
    public int getMaxPublicPorts() throws CloudException, InternalException {
        return 0;
    }

    @Nonnull
    @Override
    public String getProviderTermForLoadBalancer(@Nonnull Locale locale) {
        return "load balancer";
    }

    @Nullable
    @Override
    public VisibleScope getLoadBalancerVisibleScope() {
        return null;
    }

    @Override
    public boolean healthCheckRequiresLoadBalancer() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean healthCheckRequiresListener() throws CloudException, InternalException {
        return false;
    }

    @Override
    public Requirement healthCheckRequiresName() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull
    @Override
    public Requirement identifyEndpointsOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.OPTIONAL;
    }

    @Nonnull
    @Override
    public Requirement identifyListenersOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.REQUIRED;
    }

    @Override
    public Requirement identifyVlanOnCreateRequirement() { return Requirement.NONE; }

    @Override
    public @Nonnull Requirement identifyHealthCheckOnCreateRequirement() throws CloudException, InternalException {
        return Requirement.OPTIONAL;
    }

    @Override
    public boolean isAddressAssignedByProvider() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean isDataCenterLimited() throws CloudException, InternalException {
        return true;
    }

    @Nonnull
    @Override
    public Iterable<LbAlgorithm> listSupportedAlgorithms() throws CloudException, InternalException {
        return Collections.singletonList(LbAlgorithm.ROUND_ROBIN);
    }

    @Nonnull
    @Override
    public Iterable<LbEndpointType> listSupportedEndpointTypes() throws CloudException, InternalException {
        return Collections.singletonList(LbEndpointType.VM);
    }

    static private volatile List<IPVersion> versions;

    @Nonnull
    @Override
    public Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
        if( versions == null ) {
            versions = Collections.unmodifiableList(Arrays.asList(
                    IPVersion.IPV4,
                    IPVersion.IPV6
            ));
        }
        return versions;
    }

    @Nonnull
    @Override
    public Iterable<LbPersistence> listSupportedPersistenceOptions() throws CloudException, InternalException {
        return Collections.singletonList(LbPersistence.NONE);
    }

    static private volatile List<LbProtocol> protocols;

    @Nonnull
    @Override
    public Iterable<LbProtocol> listSupportedProtocols() throws CloudException, InternalException {
        if( protocols == null ) {
            protocols = Collections.unmodifiableList(Arrays.asList(
                    LbProtocol.HTTP,
                    LbProtocol.HTTPS,
                    LbProtocol.RAW_TCP
            ));
        }
        return protocols;
    }

    @Override
    public boolean supportsAddingEndpoints() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean supportsMonitoring() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean supportsMultipleTrafficTypes() throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean supportsSslCertificateStore() throws CloudException, InternalException {
        return true;
    }

    @Override
    public @Nonnull NamingConstraints getLoadBalancerNamingConstraints() {
        return NamingConstraints.getAlphaOnly(1, 32).constrainedBy('-');
    }
}
