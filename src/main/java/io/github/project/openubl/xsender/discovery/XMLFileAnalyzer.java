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

import io.github.project.openubl.xsender.company.CompanyURLs;
import io.github.project.openubl.xsender.discovery.xml.DocumentType;
import io.github.project.openubl.xsender.discovery.xml.XmlContentModel;
import io.github.project.openubl.xsender.discovery.xml.XmlContentProvider;
import io.github.project.openubl.xsender.exceptions.UnsupportedXMLFileException;
import io.github.project.openubl.xsender.sunat.catalog.Catalog1;
import jodd.io.ZipBuilder;
import org.apache.cxf.helpers.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Pattern;

public class XMLFileAnalyzer implements FileAnalyzer {

    private final static String FILENAME_FORMAT1 = "{0}-{1}-{2}";
    private final static String FILENAME_FORMAT2 = "{0}-{1}";

    private final ZipFile zipFile;
    private final FileDeliveryTarget fileDeliveryTarget;
    private final TicketDeliveryTarget ticketDeliveryTarget;

    public XMLFileAnalyzer(File file, CompanyURLs urLs) throws IOException, ParserConfigurationException, UnsupportedXMLFileException, SAXException {
        this(file.toPath(), urLs);
    }

    public XMLFileAnalyzer(Path path, CompanyURLs urLs) throws IOException, ParserConfigurationException, UnsupportedXMLFileException, SAXException {
        this(Files.readAllBytes(path), urLs);
    }

    public XMLFileAnalyzer(InputStream is, CompanyURLs urLs) throws IOException, ParserConfigurationException, UnsupportedXMLFileException, SAXException {
        this(IOUtils.readBytesFromStream(is), urLs);
    }

    public XMLFileAnalyzer(byte[] file, CompanyURLs urls) throws ParserConfigurationException, IOException, SAXException, UnsupportedXMLFileException {
        XmlContentModel xmlContentModel = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(file));

        String fileNameWithoutExtension = XMLFileAnalyzer.getFileNameWithoutExtension(xmlContentModel)
                .orElseThrow(() -> new UnsupportedXMLFileException("Couldn't infer the file name"));
        FileDeliveryTarget fileDeliveryTarget = XMLFileAnalyzer.getFileDeliveryTarget(urls, xmlContentModel)
                .orElseThrow(() -> new UnsupportedXMLFileException("Couldn't infer the delivery data"));
        TicketDeliveryTarget ticketDeliveryTarget = XMLFileAnalyzer.getTicketDeliveryTarget(urls, xmlContentModel)
                .orElse(null);

        String zipFileName = fileNameWithoutExtension + ".zip";
        byte[] zipFile = ZipBuilder.createZipInMemory()
                .add(file)
                .path(fileNameWithoutExtension + ".xml")
                .save()
                .toBytes();

        this.zipFile = new ZipFile(zipFile, zipFileName);
        this.fileDeliveryTarget = fileDeliveryTarget;
        this.ticketDeliveryTarget = ticketDeliveryTarget;
    }

    @Override
    public ZipFile getZipFile() {
        return zipFile;
    }

    @Override
    public FileDeliveryTarget getFileDeliveryTarget() {
        return fileDeliveryTarget;
    }

    @Override
    public TicketDeliveryTarget getTicketDeliveryTarget() {
        return ticketDeliveryTarget;
    }

    private static Optional<String> getFileNameWithoutExtension(XmlContentModel xmlContentModel) {
        String documentType = xmlContentModel.getDocumentType();
        String documentID = xmlContentModel.getDocumentID();
        String ruc = xmlContentModel.getRuc();

        String result = null;
        String codigoDocumento;
        switch (documentType) {
            case DocumentType.INVOICE:
                if (Pattern.compile("^[F|f].*$").matcher(documentID).find()) {
                    codigoDocumento = Catalog1.FACTURA.getCode();
                } else if (Pattern.compile("^[B|b].*$").matcher(documentID).find()) {
                    codigoDocumento = Catalog1.BOLETA.getCode();
                } else {
                    throw new IllegalStateException("Invalid Serie, can not detect code");
                }

                result = MessageFormat.format(FILENAME_FORMAT1, ruc, codigoDocumento, documentID);
                break;
            case DocumentType.CREDIT_NOTE:
                codigoDocumento = Catalog1.NOTA_CREDITO.getCode();
                result = MessageFormat.format(FILENAME_FORMAT1, ruc, codigoDocumento, documentID);
                break;
            case DocumentType.DEBIT_NOTE:
                codigoDocumento = Catalog1.NOTA_DEBITO.getCode();
                result = MessageFormat.format(FILENAME_FORMAT1, ruc, codigoDocumento, documentID);
                break;
            case DocumentType.VOIDED_DOCUMENT:
            case DocumentType.SUMMARY_DOCUMENT:
                result = MessageFormat.format(FILENAME_FORMAT2, ruc, documentID);
                break;
            case DocumentType.PERCEPTION:
                codigoDocumento = Catalog1.PERCEPCION.getCode();
                result = MessageFormat.format(FILENAME_FORMAT1, ruc, codigoDocumento, documentID);
                break;
            case DocumentType.RETENTION:
                codigoDocumento = Catalog1.RETENCION.getCode();
                result = MessageFormat.format(FILENAME_FORMAT1, ruc, codigoDocumento, documentID);
                break;
            case DocumentType.DESPATCH_ADVICE:
                codigoDocumento = Catalog1.GUIA_REMISION_REMITENTE.getCode();
                result = MessageFormat.format(FILENAME_FORMAT1, ruc, codigoDocumento, documentID);
                break;
        }

        return Optional.ofNullable(result);
    }

    private static Optional<FileDeliveryTarget> getFileDeliveryTarget(CompanyURLs urls, XmlContentModel xmlContentModel) {
        FileDeliveryTarget fileDeliveryTarget = null;

        switch (xmlContentModel.getDocumentType()) {
            case DocumentType.INVOICE:
            case DocumentType.CREDIT_NOTE:
            case DocumentType.DEBIT_NOTE:
                fileDeliveryTarget = new FileDeliveryTarget(urls.getInvoice(), FileDeliveryTarget.Method.SEND_BILL);
                break;
            case DocumentType.SUMMARY_DOCUMENT:
                fileDeliveryTarget = new FileDeliveryTarget(urls.getInvoice(), FileDeliveryTarget.Method.SEND_SUMMARY);
                break;
            case DocumentType.VOIDED_DOCUMENT:
                String tipoDocumentoAfectado = xmlContentModel.getVoidedLineDocumentTypeCode();
                Optional<Catalog1> catalog1Optional = Catalog1.valueOfCode(tipoDocumentoAfectado);
                if (!catalog1Optional.isPresent()) {
                    return Optional.empty();
                }

                Catalog1 catalog1 = catalog1Optional.get();
                String deliveryUrl;
                if (catalog1.equals(Catalog1.PERCEPCION) || catalog1.equals(Catalog1.RETENCION)) {
                    deliveryUrl = urls.getPerceptionRetention();
                } else if (catalog1.equals(Catalog1.GUIA_REMISION_REMITENTE)) {
                    deliveryUrl = urls.getDespatch();
                } else {
                    deliveryUrl = urls.getInvoice();
                }

                fileDeliveryTarget = new FileDeliveryTarget(deliveryUrl, FileDeliveryTarget.Method.SEND_SUMMARY);
                break;
            case DocumentType.PERCEPTION:
            case DocumentType.RETENTION:
                fileDeliveryTarget = new FileDeliveryTarget(urls.getPerceptionRetention(), FileDeliveryTarget.Method.SEND_BILL);
                break;
            case DocumentType.DESPATCH_ADVICE:
                fileDeliveryTarget = new FileDeliveryTarget(urls.getDespatch(), FileDeliveryTarget.Method.SEND_BILL);
                break;
        }

        return Optional.ofNullable(fileDeliveryTarget);
    }

    private static Optional<TicketDeliveryTarget> getTicketDeliveryTarget(CompanyURLs urls, XmlContentModel xmlContentModel) {
        boolean shouldVerifyTicket = true;
        switch (xmlContentModel.getDocumentType()) {
            case DocumentType.VOIDED_DOCUMENT:
            case DocumentType.SUMMARY_DOCUMENT:
                shouldVerifyTicket = false;
                break;
        }

        if (!shouldVerifyTicket) {
            return Optional.empty();
        }

        TicketDeliveryTarget ticketDeliveryTarget;

        Catalog1 catalog1 = Catalog1.valueOfCode(xmlContentModel.getVoidedLineDocumentTypeCode())
                .orElse(Catalog1.FACTURA);
        switch (catalog1) {
            case PERCEPCION:
            case RETENCION:
                ticketDeliveryTarget = new TicketDeliveryTarget(urls.getPerceptionRetention());
                break;
//            // No se pueden dar bajas de guias de remision
//            case GUIA_REMISION_REMITENTE:
//                ticketDeliveryTarget = new TicketDeliveryTarget(urls.getDespatch());
//                break;
            default:
                ticketDeliveryTarget = new TicketDeliveryTarget(urls.getInvoice());
        }

        return Optional.of(ticketDeliveryTarget);
    }
}
