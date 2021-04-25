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
package io.github.project.openubl.xsender.response;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DocumentToModel {

    private DocumentToModel(){
        // Just static methods
    }

    public static CdrModel toCdrModel(Document document) throws XPathExpressionException {
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
}
