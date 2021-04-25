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

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.util.HashMap;
import java.util.Map;

public class SimpleClientServiceFactory implements ProxyClientServiceFactory {

    public <T> T create(Class<T> serviceClass, WsClientAuth auth, WsClientConfig config) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(auth.getAddress());

        if (config.getFeatures() != null) {
            factory.getFeatures().addAll(config.getFeatures());
        }
        if (config.getInInterceptors() != null) {
            factory.getInInterceptors().addAll(config.getInInterceptors());
        }
        if (config.getOutFaultInterceptors() != null) {
            factory.getOutInterceptors().addAll(config.getOutInterceptors());
        }
        if (config.getInFaultInterceptors() != null) {
            factory.getInFaultInterceptors().addAll(config.getInFaultInterceptors());
        }
        if (config.getOutFaultInterceptors() != null) {
            factory.getOutFaultInterceptors().addAll(config.getOutFaultInterceptors());
        }

        // Auth
        WSS4JOutInterceptor wss4JOutInterceptor = configAuth(auth);
        factory.getOutInterceptors().add(wss4JOutInterceptor);

        // Create client
        T t = factory.create(serviceClass);

        // Http policy
        configHttpPolicy(t, config);

        return t;
    }

    private static WSS4JOutInterceptor configAuth(WsClientAuth auth) {
        Map<String, Object> outProps = new HashMap<>();
        outProps.put(ConfigurationConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(ConfigurationConstants.PASSWORD_TYPE, "PasswordText");

        outProps.put(ConfigurationConstants.USER, auth.getUsername());
        outProps.put(ConfigurationConstants.PW_CALLBACK_REF, (CallbackHandler) callbacks -> {
            for (Callback callback : callbacks) {
                WSPasswordCallback wpc = (WSPasswordCallback) callback;
                wpc.setPassword(auth.getPassword());
            }
        });

        return new WSS4JOutInterceptor(outProps);
    }

    private static void configHttpPolicy(Object serviceClass, WsClientConfig config) {
        Client client = ClientProxy.getClient(serviceClass);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        // TSL config
        TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setDisableCNCheck(true);

        conduit.setTlsClientParameters(tlsParams);

        // HTTP policy
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30_000L);
        policy.setReceiveTimeout(config.getReceiveTimeout() != null ? config.getReceiveTimeout() : 15_000L);
        policy.setAllowChunking(false);

        conduit.setClient(policy);
    }
}
