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
package io.github.project.openubl.xsender.sunat;

import io.github.project.openubl.xsender.company.CompanyCredentials;
import io.github.project.openubl.xsender.cxf.WsClientAuth;
import io.github.project.openubl.xsender.cxf.WsClientAuthBuilder;
import io.github.project.openubl.xsender.discovery.FileDeliveryTarget;
import io.github.project.openubl.xsender.discovery.TicketDeliveryTarget;
import io.github.project.openubl.xsender.discovery.ZipFile;
import io.github.project.openubl.xsender.flyweight.WsClientFactory;
import io.github.project.openubl.xsender.manager.BillServiceAdapter;
import service.sunat.gob.pe.billservice.BillService;
import service.sunat.gob.pe.billservice.StatusResponse;

public class Sunat {

    private final WsClientFactory wsClientFactory;

    public Sunat(WsClientFactory wsClientFactory) {
        this.wsClientFactory = wsClientFactory;
    }

    public SendFileResponse sendFile(ZipFile zipFile, FileDeliveryTarget fileDeliveryTarget, CompanyCredentials credentials) {
        WsClientAuth clientAuth = WsClientAuthBuilder.aWsClientAuth()
                .withAddress(fileDeliveryTarget.getUrl())
                .withUsername(credentials.getUsername())
                .withPassword(credentials.getPassword())
                .build();

        BillService billService = wsClientFactory.getInstance(BillService.class, clientAuth);
        BillServiceAdapter billServiceAdapter = new BillServiceAdapter(billService);

        byte[] cdr = null;
        String ticket = null;
        switch (fileDeliveryTarget.getMethod()) {
            case SEND_BILL:
                cdr = billServiceAdapter.sendBill(zipFile);
                break;
            case SEND_SUMMARY:
                ticket = billServiceAdapter.sendSummary(zipFile);
                break;
            case SEND_PACK:
                ticket = billServiceAdapter.sendPack(zipFile);
                break;
            default:
                throw new IllegalStateException("Could not determine the correct service for the document");
        }

        SendFileResponse response = new SendFileResponse();
        response.setCdr(cdr);
        response.setTicket(ticket);

        return response;
    }

    public VerifyTicketResponse verifyTicketStatus(String ticket, TicketDeliveryTarget target, CompanyCredentials credentials)  {
        WsClientAuth clientAuth = WsClientAuthBuilder.aWsClientAuth()
                .withAddress(target.getUrl())
                .withUsername(credentials.getUsername())
                .withPassword(credentials.getPassword())
                .build();

        BillService billService = wsClientFactory.getInstance(BillService.class, clientAuth);
        BillServiceAdapter billServiceAdapter = new BillServiceAdapter(billService);
        StatusResponse statusResponse = billServiceAdapter.getStatus(ticket);

        VerifyTicketResponse response = new VerifyTicketResponse();
        response.setCdr(statusResponse.getContent());
        response.setStatusCode(statusResponse.getStatusCode());

        return response;
    }

}
