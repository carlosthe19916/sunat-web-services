/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.cxf;

import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.Interceptor;

import java.util.List;

public final class WsClientConfigBuilder {
    private Long connectionTimeout;
    private Long receiveTimeout;
    private List<Feature> features;
    private List<Interceptor<?>> inInterceptors;
    private List<Interceptor<?>> outInterceptors;
    private List<Interceptor<?>> inFaultInterceptors;
    private List<Interceptor<?>> outFaultInterceptors;

    private WsClientConfigBuilder() {
    }

    public static WsClientConfigBuilder aWsClientConfig() {
        return new WsClientConfigBuilder();
    }

    public WsClientConfigBuilder withConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public WsClientConfigBuilder withReceiveTimeout(Long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
        return this;
    }

    public WsClientConfigBuilder withFeatures(List<Feature> features) {
        this.features = features;
        return this;
    }

    public WsClientConfigBuilder withInInterceptors(List<Interceptor<?>> inInterceptors) {
        this.inInterceptors = inInterceptors;
        return this;
    }

    public WsClientConfigBuilder withOutInterceptors(List<Interceptor<?>> outInterceptors) {
        this.outInterceptors = outInterceptors;
        return this;
    }

    public WsClientConfigBuilder withInFaultInterceptors(List<Interceptor<?>> inFaultInterceptors) {
        this.inFaultInterceptors = inFaultInterceptors;
        return this;
    }

    public WsClientConfigBuilder withOutFaultInterceptors(List<Interceptor<?>> outFaultInterceptors) {
        this.outFaultInterceptors = outFaultInterceptors;
        return this;
    }

    public WsClientConfig build() {
        WsClientConfig wsClientConfig = new WsClientConfig();
        wsClientConfig.setConnectionTimeout(connectionTimeout);
        wsClientConfig.setReceiveTimeout(receiveTimeout);
        wsClientConfig.setFeatures(features);
        wsClientConfig.setInInterceptors(inInterceptors);
        wsClientConfig.setOutInterceptors(outInterceptors);
        wsClientConfig.setInFaultInterceptors(inFaultInterceptors);
        wsClientConfig.setOutFaultInterceptors(outFaultInterceptors);
        return wsClientConfig;
    }
}
