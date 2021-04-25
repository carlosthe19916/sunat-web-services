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
package io.github.project.openubl.xsender.manager;

import io.github.project.openubl.xsender.discovery.ZipFile;
import org.apache.cxf.attachment.ByteDataSource;
import service.sunat.gob.pe.billservice.BillService;
import service.sunat.gob.pe.billservice.StatusResponse;

import javax.activation.DataHandler;
import javax.activation.DataSource;

public class BillServiceAdapter {

    private final static String ZIP_MYME_TYPE = "application/zip";

    private final BillService billService;

    public BillServiceAdapter(BillService billService) {
        this.billService = billService;
    }

    public byte[] sendBill(ZipFile zipFile) {
        DataSource dataSource = new ByteDataSource(zipFile.getFile(), ZIP_MYME_TYPE);
        DataHandler dataHandler = new DataHandler(dataSource);
        return billService.sendBill(zipFile.getFilename(), dataHandler, null);
    }

    public String sendSummary(ZipFile zipFile) {
        DataSource dataSource = new ByteDataSource(zipFile.getFile(), ZIP_MYME_TYPE);
        DataHandler dataHandler = new DataHandler(dataSource);
        return billService.sendSummary(zipFile.getFilename(), dataHandler, null);
    }

    public String sendPack(ZipFile zipFile) {
        DataSource dataSource = new ByteDataSource(zipFile.getFile(), ZIP_MYME_TYPE);
        DataHandler dataHandler = new DataHandler(dataSource);
        return billService.sendPack(zipFile.getFilename(), dataHandler, null);
    }

    public StatusResponse getStatus(String ticket) {
        return billService.getStatus(ticket);
    }

}
