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
import io.github.project.openubl.xsender.company.CompanyCredentialsBuilder;
import io.github.project.openubl.xsender.company.CompanyURLs;
import io.github.project.openubl.xsender.company.CompanyURLsBuilder;
import io.github.project.openubl.xsender.discovery.FileAnalyzer;
import io.github.project.openubl.xsender.discovery.XMLFileAnalyzer;
import io.github.project.openubl.xsender.exceptions.UnsupportedXMLFileException;
import io.github.project.openubl.xsender.response.CdrStatus;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Example {

    public static void main(String[] args) throws ParserConfigurationException, IOException, UnsupportedXMLFileException, SAXException, XPathExpressionException {
        // On for company
        CompanyURLs companyURLs = CompanyURLsBuilder.aCompanyURLs()
                .withInvoice("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService")
                .withDespatch("https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService")
                .withPerceptionRetention("https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService")
                .build();

        CompanyCredentials credentials = CompanyCredentialsBuilder.aCompanyCredentials()
                .withUsername("12345678959MODDATOS")
                .withPassword("MODDATOS")
                .build();

        // Using the facade
        Path path = Paths.get("/home/cferiavi/Documents/ubl/1/10288508432-01-FFF1-1.xml");
        FileAnalyzer fileAnalyzer = new XMLFileAnalyzer(path, companyURLs);

        XSenderFileResponse fileResponse = XSender.getInstance()
                .sendXmlFile(fileAnalyzer.getZipFile(), fileAnalyzer.getFileDeliveryTarget(), credentials);

        if (fileResponse.getTicket() != null) {
            XSenderTicketResponse ticketResponse = XSender.getInstance().ticket(
                    fileResponse.getTicket(),
                    fileAnalyzer.getTicketDeliveryTarget(),
                    credentials
            );
        }

        //

        // Using manual
//        sunat = new Sunat(wsClientFactory);
//        sunat.sendFile(
//                new ZipFile(new byte[]{}, "123456789.xml"),
//                new FileDeliveryTarget("", FileDeliveryTarget.Method.SEND_BILL),
//                credentials
//        );
    }
}
