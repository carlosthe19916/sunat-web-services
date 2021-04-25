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
package io.github.project.openubl.xsender;

import io.github.project.openubl.xsender.company.CompanyCredentials;
import io.github.project.openubl.xsender.cxf.ProxyClientServiceFactory;
import io.github.project.openubl.xsender.cxf.SimpleClientServiceFactory;
import io.github.project.openubl.xsender.cxf.WsClientConfig;
import io.github.project.openubl.xsender.cxf.WsClientConfigBuilder;
import io.github.project.openubl.xsender.discovery.*;
import io.github.project.openubl.xsender.flyweight.SimpleWsClientFactory;
import io.github.project.openubl.xsender.flyweight.WsClientFactory;
import io.github.project.openubl.xsender.response.CdrReader;
import io.github.project.openubl.xsender.sunat.SendFileResponse;
import io.github.project.openubl.xsender.sunat.Sunat;
import io.github.project.openubl.xsender.sunat.VerifyTicketResponse;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class XSender {

    private static XSender instance;

    private final WsClientFactory wsClientFactory;

    private XSender() {
        ProxyClientServiceFactory proxyClientServiceFactory = new SimpleClientServiceFactory();
        WsClientConfig wsClientConfig = WsClientConfigBuilder.aWsClientConfig()
                .withConnectionTimeout(15_000L)
                .withReceiveTimeout(10_000L)
                .build();

        this.wsClientFactory = new SimpleWsClientFactory(proxyClientServiceFactory, wsClientConfig);
    }

    public static XSender getInstance() {
        if (instance == null) {
            synchronized (XSender.class) {
                if (instance == null) {
                    instance = new XSender();
                }
            }
        }
        return instance;
    }

    public XSenderFileResponse sendXmlFile(ZipFile zipFile, FileDeliveryTarget deliveryTarget, CompanyCredentials credentials) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        Sunat sunat = new Sunat(this.wsClientFactory);
        SendFileResponse fileResponse = sunat.sendFile(zipFile, deliveryTarget, credentials);

        byte[] cdr = fileResponse.getCdr();
        String ticket = fileResponse.getTicket();

        return new XSenderFileResponse(new CdrReader(cdr), ticket);
    }

    public XSenderTicketResponse ticket(String ticket, TicketDeliveryTarget deliveryTarget, CompanyCredentials credentials) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        Sunat sunat = new Sunat(this.wsClientFactory);
        VerifyTicketResponse ticketResponse = sunat.verifyTicketStatus(ticket, deliveryTarget, credentials);

        byte[] cdr = ticketResponse.getCdr();
        String statusCode = ticketResponse.getStatusCode();

        return new XSenderTicketResponse(new CdrReader(cdr), statusCode);
    }

}
