/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to make sure that AsyncListener#onTimeout is called after the
 * specified async timeout interval.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-async-listener-on-timeout";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = "Hello world";
    private static final String EXPECTED_SB_RESULT = "A";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for AsyncListener#onTimeout");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("wrap=true", EXPECTED_RESPONSE);
            webTest.doTest("result=1", EXPECTED_SB_RESULT);
            webTest.doTest("wrap=false", EXPECTED_RESPONSE);
            webTest.doTest("result=1", EXPECTED_SB_RESULT);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest(String mode, String expectedResponse) throws Exception {

        InputStream is = null;
        BufferedReader input = null;
       
        try {     
            URL url = new URL("http://" + host  + ":" + port +
                contextRoot + "/AsyncListenerTimeoutServlet?" + mode);
            System.out.println("Connecting to: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Default async timeout is 30000
            conn.setReadTimeout(40000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Unexpected return code: " + responseCode);
            }

            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            int count = 0;
            String response = null;
            while ((response = input.readLine()) != null) {
                System.out.println(response);
                if (expectedResponse.equals(response)) {
                    count++;
                }
            }
            if (count != 1) {
                throw new Exception("Missing or wrong response. Expected: " +
                    expectedResponse + ", received: " + response +
                    ". With count = " + count);
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
