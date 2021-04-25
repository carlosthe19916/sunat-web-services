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
package io.github.project.openubl.xsender.flyweight;

import io.github.project.openubl.xsender.cxf.ProxyClientServiceFactory;
import io.github.project.openubl.xsender.cxf.WsClientAuth;
import io.github.project.openubl.xsender.cxf.WsClientConfig;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleWsClientFactory implements WsClientFactory {

    private final Map<WsClientAuth, Map<Class<?>, Object>> instances = Collections.synchronizedMap(new CacheLinkedHashMap<>());

    private final ProxyClientServiceFactory factory;
    private final WsClientConfig config;

    public SimpleWsClientFactory(ProxyClientServiceFactory factory, WsClientConfig config) {
        this.factory = factory;
        this.config = config;
    }

    @Override
    public <T> T getInstance(Class<T> tClass, WsClientAuth auth) {
        instances.putIfAbsent(auth, new ConcurrentHashMap<>());

        if (!instances.get(auth).containsKey(tClass)) {
            T t = factory.create(tClass, auth, config);
            instances.get(auth).putIfAbsent(tClass, t);
        }

        return (T) instances.get(auth).get(tClass);
    }
}
