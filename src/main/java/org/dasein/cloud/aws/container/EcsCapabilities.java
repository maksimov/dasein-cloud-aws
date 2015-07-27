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

import org.dasein.cloud.AbstractCapabilities;
import org.dasein.cloud.aws.AWSCloud;
import org.dasein.cloud.compute.container.ContainerCapabilities;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * @author Stas Maksimov (stas.maksimov@software.dell.com)
 * @since 2015.09
 */
public class EcsCapabilities extends AbstractCapabilities<AWSCloud> implements ContainerCapabilities {

    public EcsCapabilities(@Nonnull AWSCloud provider) {
        super(provider);
    }

    @Nonnull
    @Override
    public String getProviderTermForCluster(Locale locale) {
        return "cluster";
    }

    @Nonnull
    @Override
    public String getProviderTermForScheduler(@Nonnull Locale locale) {
        return "service";
    }

    @Nonnull
    @Override
    public String getProviderTermForTask(@Nonnull Locale locale) {
        return "task";
    }
}
