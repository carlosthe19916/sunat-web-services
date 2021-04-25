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
package io.github.project.openubl.xsender.discovery;

public class FileDeliveryTarget {

    private final String url;
    private final Method method;

    public FileDeliveryTarget(String url, Method method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public Method getMethod() {
        return method;
    }

    public enum Method {
        SEND_BILL,
        SEND_SUMMARY,
        SEND_PACK,
    }
}
