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

public class XmlContentModel {

    private String documentType;
    private String documentID;
    private String ruc;
    private String voidedLineDocumentTypeCode;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getVoidedLineDocumentTypeCode() {
        return voidedLineDocumentTypeCode;
    }

    public void setVoidedLineDocumentTypeCode(String voidedLineDocumentTypeCode) {
        this.voidedLineDocumentTypeCode = voidedLineDocumentTypeCode;
    }

    public static final class Builder {
        private String documentType;
        private String documentID;
        private String ruc;
        private String voidedLineDocumentTypeCode;

        private Builder() {
        }

        public static Builder aXmlContentModel() {
            return new Builder();
        }

        public Builder withDocumentType(String documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder withDocumentID(String documentID) {
            this.documentID = documentID;
            return this;
        }

        public Builder withRuc(String ruc) {
            this.ruc = ruc;
            return this;
        }

        public Builder withVoidedLineDocumentTypeCode(String voidedLineDocumentTypeCode) {
            this.voidedLineDocumentTypeCode = voidedLineDocumentTypeCode;
            return this;
        }

        public XmlContentModel build() {
            XmlContentModel xmlContentModel = new XmlContentModel();
            xmlContentModel.setDocumentType(documentType);
            xmlContentModel.setDocumentID(documentID);
            xmlContentModel.setRuc(ruc);
            xmlContentModel.setVoidedLineDocumentTypeCode(voidedLineDocumentTypeCode);
            return xmlContentModel;
        }
    }
}
