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
package io.github.project.openubl.xmlsenderws.webservices.utils;

import io.github.project.openubl.xmlsenderws.webservices.models.CdrModel;
import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    private Utils() {
        // Just static methods
    }

    public static Optional<Integer> getErrorCode(SOAPFaultException exception) {
        String errorCode = "";

        SOAPFault fault = exception.getFault();
        if (fault != null) {
            String faultCode = fault.getFaultCode();
            if (faultCode != null) {
                errorCode = faultCode.replaceAll("soap-env:Client.", "");
            }
        }

        if (!errorCode.matches("-?\\d+")) {
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null) {
                errorCode = exceptionMessage.replaceAll("soap-env:Client.", "");
            }
        }

        if (!errorCode.matches("-?\\d+")) {
            return Optional.empty();
        }

        return Optional.of(Integer.parseInt(errorCode));
    }

    public static String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }

    public static byte[] getFirstXmlFileFromZip(byte[] data) throws IOException {
        try (
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
                ByteArrayOutputStream os = new ByteArrayOutputStream()
        ) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".xml")) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        os.write(buffer, 0, len);
                    }
                    return os.toByteArray();
                }
            }

        }
        return null;
    }

    public static Document getDocumentFromBytes(byte[] cdrXml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(cdrXml));
    }

    public static CdrModel extractResponse(Document document) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("ar".equals(prefix)) {
                    return "urn:oasis:names:specification:ubl:schema:xsd:ApplicationResponse-2";
                } else if ("ext".equals(prefix)) {
                    return "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
                } else if ("cbc".equals(prefix)) {
                    return "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
                } else if ("cac".equals(prefix)) {
                    return "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
                }
                return null;
            }

            @Override
            public String getPrefix(String s) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String s) {
                return null;
            }
        });

        XPathExpression codeXPathExpression = xPath.compile("//ar:ApplicationResponse/cac:DocumentResponse/cac:Response/cbc:ResponseCode");
        String code = (String) codeXPathExpression.evaluate(document, XPathConstants.STRING);

        XPathExpression descriptionXPathExpression = xPath.compile("//ar:ApplicationResponse/cac:DocumentResponse/cac:Response/cbc:Description");
        String description = (String) descriptionXPathExpression.evaluate(document, XPathConstants.STRING);

        XPathExpression notesXPathExpression = xPath.compile("//ar:ApplicationResponse/cbc:Note");
        NodeList noteNodes = (NodeList) notesXPathExpression.evaluate(document, XPathConstants.NODESET);
        List<String> notes = new ArrayList<>();
        for (int index = 0; index < noteNodes.getLength(); index++) {
            Node node = noteNodes.item(index);
            notes.add(node.getTextContent());
        }

        return new CdrModel(Integer.parseInt(code), description, notes);
    }

    public static BillServiceModel toModel(byte[] zip) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        byte[] xml = Utils.getFirstXmlFileFromZip(zip);
        Document document = Utils.getDocumentFromBytes(xml);
        CdrModel cdrContent = extractResponse(document);

        BillServiceModel result = new BillServiceModel();
        result.setCdr(zip);
        result.setCode(cdrContent.getResponseCode());
        result.setDescription(cdrContent.getDescription());
        result.setStatus(BillServiceModel.Status.fromCode(cdrContent.getResponseCode()));
        result.setNotes(cdrContent.getNotes());
        return result;
    }

    public static BillServiceModel toModel(String ticket) {
        BillServiceModel model = new BillServiceModel();
        model.setTicket(ticket);
        return model;
    }

}
