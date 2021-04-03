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
package io.github.project.openubl.xmlsenderws.webservices.wrappers;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SunatServiceFactory {

    private final static Map<ServiceConfig, Map<Class<?>, Object>> instances = Collections.synchronizedMap(new CacheLinkedHashMap<>());

    private SunatServiceFactory() {
        // Just static methods
    }

    public static <T> T getInstance(Class<T> serviceClass, ServiceConfig config) {
        instances.putIfAbsent(config, new ConcurrentHashMap<>());

        if (!instances.get(config).containsKey(serviceClass)) {
            T t = createWsService(serviceClass, config);
            instances.get(config).putIfAbsent(serviceClass, t);
        }

        return (T) instances.get(config).get(serviceClass);
    }

    private static <T> T createWsService(Class<T> serviceClass, ServiceConfig config) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(config.getUrl());

        // Auth
        WSS4JOutInterceptor wss4JOutInterceptor = configAuth(config.getUsername(), config.getPassword());
        factory.getOutInterceptors().add(wss4JOutInterceptor);

        // Create client
        T t = factory.create(serviceClass);

        // Http policy
        configHttpPolicy(t);

        return t;
    }

    private static WSS4JOutInterceptor configAuth(String username, String password) {
        Map<String, Object> outProps = new HashMap<>();
        outProps.put(ConfigurationConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(ConfigurationConstants.PASSWORD_TYPE, "PasswordText");

        outProps.put(ConfigurationConstants.USER, username);
        outProps.put(ConfigurationConstants.PW_CALLBACK_REF, (CallbackHandler) callbacks -> {
            for (Callback callback : callbacks) {
                WSPasswordCallback wpc = (WSPasswordCallback) callback;
                wpc.setPassword(password);
            }
        });

        return new WSS4JOutInterceptor(outProps);
    }

    private static void configHttpPolicy(Object serviceClass) {
        Client client = ClientProxy.getClient(serviceClass);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        // TSL config
        TLSClientParameters tlsParams = new TLSClientParameters();
        tlsParams.setDisableCNCheck(true);

        conduit.setTlsClientParameters(tlsParams);

        // HTTP policy
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(30_000L);
        policy.setReceiveTimeout(15_000L);
        policy.setAllowChunking(false);

        conduit.setClient(policy);
    }

}
