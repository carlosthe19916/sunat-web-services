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
package io.github.project.openubl.xmlsenderws.webservices.managers;

import io.github.project.openubl.xmlsenderws.webservices.wrappers.ServiceConfig;
import io.github.project.openubl.xmlsenderws.webservices.models.BillConsultModel;
import org.junit.jupiter.api.Test;

import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BillConsultServiceManagerTest {

    private String USERNAME = "20494637074MODDATOS";
    private String PASSWORD = "MODDATOS";
    private String URL_CONSULTA = "https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService";

    @Test
    public void getStatus() throws IOException {
        ServiceConfig config = new ServiceConfig.Builder()
                .url(URL_CONSULTA)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        BillConsultModel consult = new BillConsultModel.Builder()
                .ruc("20494918910")
                .tipo("01")
                .serie("F001")
                .numero(102)
                .build();

        try {
            service.sunat.gob.pe.billconsultservice.StatusResponse response = BillConsultServiceManager.getStatus(consult, config);
        } catch (SOAPFaultException e) {
            // Las consultas deben de hacerse con un usuario y constraseña de produccion.
            assertEquals(e.getMessage(), "El Usuario ingresado no existe");
        }
    }

    @Test
    public void getStatusCdr() throws IOException {
        ServiceConfig config = new ServiceConfig.Builder()
                .url(URL_CONSULTA)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        BillConsultModel consult = new BillConsultModel.Builder()
                .ruc("20494918910")
                .tipo("01")
                .serie("F001")
                .numero(102)
                .build();

        try {
            service.sunat.gob.pe.billconsultservice.StatusResponse response = BillConsultServiceManager.getStatusCdr(consult, config);
        } catch (SOAPFaultException e) {
            // Las consultas deben de hacerse con un usuario y constraseña de produccion.
            assertEquals(e.getMessage(), "El Usuario ingresado no existe");
        }
    }

}
