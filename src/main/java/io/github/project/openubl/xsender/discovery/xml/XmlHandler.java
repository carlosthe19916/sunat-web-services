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
package io.github.project.openubl.xsender.discovery.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {

    private static final String CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String SAC = "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1";
    private static final String EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    private static final String ACCOUNTING_SUPPLIER_PARTY = "AccountingSupplierParty";
    private static final String PARTY = "Party";
    private static final String PARTY_IDENTIFICATION = "PartyIdentification";
    private static final String ID = "ID";

    private static final String CUSTOMER_ASSIGNED_ACCOUNT_ID = "CustomerAssignedAccountID"; // voided-document, summary-document
    private static final String AGENT_PARTY = "AgentParty"; // perception, retention

    private static final String UBL_EXTENSIONS = "UBLExtensions"; // Signature is located before DOCUMENT_ID on perception and retention, will use this as a workaround
    private static final String SIGNATURE = "Signature"; // Signature is located before DOCUMENT_ID on perception and retention, will use this as a workaround

    private static final String VOIDED_DOCUMENTS_LINE = "VoidedDocumentsLine";
    private static final String DOCUMENT_TYPE_CODE = "DocumentTypeCode";


    private String documentType;
    private String documentID;
    private String ruc;
    private String voidedLineDocumentTypeCode; // Only valid for voided-documents: '//sac:VoidedDocumentsLine/cbc:DocumentTypeCode/text()'


    private boolean isAccountingSupplierPartyBeingRead;
    private boolean isPartyBeingRead;
    private boolean isPartyIdentificationBeingRead;
    private boolean isAgentPartyBeingRead;

    private boolean isUBLExtensionsBeingRead;
    private boolean isSignatureBeingRead;

    private boolean isVoidedDocumentsLineBeingRead;

    private String currentElement;
    private StringBuilder currentText;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        //
        currentElement = localName;

        // Root element
        if (documentType == null) {
            documentType = currentElement;
        }

        //
        if (currentElement.equals(ID) && uri.equals(CBC)) {
            currentText = new StringBuilder();
        } else if (currentElement.equals(ACCOUNTING_SUPPLIER_PARTY) && uri.equals(CAC)) {
            isAccountingSupplierPartyBeingRead = true;
        } else if (currentElement.equals(PARTY) && uri.equals(CAC)) {
            isPartyBeingRead = true;
        } else if (currentElement.equals(PARTY_IDENTIFICATION) && uri.equals(CAC)) {
            isPartyIdentificationBeingRead = true;
        } else if (currentElement.equals(AGENT_PARTY) && uri.equals(CAC)) {
            isAgentPartyBeingRead = true;
        } else if (currentElement.equals(UBL_EXTENSIONS) && uri.equals(EXT)) {
            isUBLExtensionsBeingRead = true;
        } else if (currentElement.equals(SIGNATURE) && uri.equals(CAC)) {
            isSignatureBeingRead = true;
        } else if (currentElement.equals(VOIDED_DOCUMENTS_LINE) && uri.equals(SAC)) {
            isVoidedDocumentsLineBeingRead = true;
        } else if (currentElement.equals(CUSTOMER_ASSIGNED_ACCOUNT_ID) && uri.equals(CBC)) {
            currentText = new StringBuilder();
        } else if (currentElement.equals(DOCUMENT_TYPE_CODE) && uri.equals(CBC)) {
            currentText = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentText != null) {
            String content = currentText.toString().trim();

            if (currentElement.equals(ID) && uri.equals(CBC)) {
                if (documentID == null && !isSignatureBeingRead && !isUBLExtensionsBeingRead) {
                    documentID = content;
                }

                if ((isAccountingSupplierPartyBeingRead && isPartyBeingRead && isPartyIdentificationBeingRead)
                        || (isAgentPartyBeingRead && isPartyIdentificationBeingRead)
                ) {
                    // invoice, credit-note, debit-note
                    ruc = content;
                }
            } else if (currentElement.equals(CUSTOMER_ASSIGNED_ACCOUNT_ID) && uri.equals(CBC)) {
                // voided-document, summary-document, despatch-advice
                if (ruc == null) {
                    ruc = content;
                }
            } else if (currentElement.equals(DOCUMENT_TYPE_CODE) && uri.equals(CBC)) {
                if (voidedLineDocumentTypeCode == null && isVoidedDocumentsLineBeingRead) {
                    // voided-document
                    voidedLineDocumentTypeCode = content;
                }
            }

            currentText = null;
        }

        // set 'beingRead' var to false
        if (localName.equals(ACCOUNTING_SUPPLIER_PARTY) && uri.equals(CAC)) {
            isAccountingSupplierPartyBeingRead = false;
        } else if (localName.equals(PARTY) && uri.equals(CAC)) {
            isPartyBeingRead = false;
        } else if (localName.equals(PARTY_IDENTIFICATION) && uri.equals(CAC)) {
            isPartyIdentificationBeingRead = false;
        } else if (localName.equals(AGENT_PARTY) && uri.equals(CAC)) {
            isAgentPartyBeingRead = false;
        } else if (localName.equals(VOIDED_DOCUMENTS_LINE) && uri.equals(SAC)) {
            isVoidedDocumentsLineBeingRead = false;
        } else if (localName.equals(UBL_EXTENSIONS) && uri.equals(EXT)) {
            isUBLExtensionsBeingRead = false;
        } else if (localName.equals(SIGNATURE) && uri.equals(CAC)) {
            isSignatureBeingRead = false;
        }

        currentElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentText != null) {
            currentText.append(ch, start, length);
        }
    }

    public XmlContentModel getModel() {
        return XmlContentModel.Builder.aXmlContentModel()
                .withDocumentType(documentType)
                .withDocumentID(documentID)
                .withRuc(ruc)
                .withVoidedLineDocumentTypeCode(voidedLineDocumentTypeCode)
                .build();
    }

}
