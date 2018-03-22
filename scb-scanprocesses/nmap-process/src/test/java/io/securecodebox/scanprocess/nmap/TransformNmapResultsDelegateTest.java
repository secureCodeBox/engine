/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package io.securecodebox.scanprocess.nmap;

import io.securecodebox.constants.DefaultFields;
import io.securecodebox.model.execution.ScanProcessExecution;
import io.securecodebox.model.execution.ScanProcessExecutionFactory;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Severity;
import io.securecodebox.scanprocess.NmapScanProcessExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 21.03.18
 */
public class TransformNmapResultsDelegateTest {

    String findingCache = "";

    @Mock
    ScanProcessExecutionFactory processExecutionFactory;

    @Spy
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    @Mock
    DelegateExecution executionMock;
    @InjectMocks
    TransformNmapResultsDelegate underTest = new TransformNmapResultsDelegate();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(processExecutionFactory.get(executionMock)).thenReturn(new NmapScanProcessExecution(executionMock));
        when(processExecutionFactory.get(executionMock, NmapScanProcessExecution.class)).thenReturn(
                new NmapScanProcessExecution(executionMock));
        when(executionMock.getVariableTyped(eq(DefaultFields.PROCESS_FINDINGS.name()))).thenAnswer(
                (answer) -> new PrimitiveTypeValueImpl.StringValueImpl(findingCache));
        doAnswer((Answer) invocation -> {
            findingCache = invocation.getArgumentAt(1, String.class);
            return Void.TYPE;
        }).when(executionMock).setVariable(eq(DefaultFields.PROCESS_FINDINGS.name()), anyString());
    }

    @Test
    public void testRawFindings() throws Exception {
        when(executionMock.getVariableTyped(DefaultFields.PROCESS_RAW_FINDINGS.name())).thenReturn(
                new PrimitiveTypeValueImpl.StringValueImpl(nmapResult));
        underTest.execute(executionMock);

        Mockito.verify(executionMock, times(2)).setVariable(eq(DefaultFields.PROCESS_FINDINGS.name()), anyString());

        ScanProcessExecution processExecution = processExecutionFactory.get(executionMock);

        assertEquals(2, processExecution.getFindings().size());

        // First Finding
        assertEquals("Port 3306 is open using tcp protocol.", processExecution.getFindings().get(0).getDescription());
        assertEquals("Open Port", processExecution.getFindings().get(0).getCategory());
        assertEquals(OsiLayer.NETWORK, processExecution.getFindings().get(0).getOsiLayer());
        assertEquals(Severity.INFORMATIONAL, processExecution.getFindings().get(0).getServerity());
        assertEquals("Open mysql Port", processExecution.getFindings().get(0).getName());
        assertNotNull(processExecution.getFindings().get(0).getId());
        assertEquals("tcp://127.0.0.1:3306", processExecution.getFindings().get(0).getLocation());
        assertEquals(7, processExecution.getFindings().get(0).getAttributes().size());

        // Secound Finding
        assertEquals("Port 7778 is open using tcp protocol.", processExecution.getFindings().get(1).getDescription());
        assertEquals("Open Port", processExecution.getFindings().get(1).getCategory());
        assertEquals(OsiLayer.NETWORK, processExecution.getFindings().get(1).getOsiLayer());
        assertEquals(Severity.INFORMATIONAL, processExecution.getFindings().get(1).getServerity());
        assertEquals("Open interwise Port", processExecution.getFindings().get(1).getName());
        assertNotNull(processExecution.getFindings().get(1).getId());
        assertEquals("tcp://127.0.0.1:7778", processExecution.getFindings().get(1).getLocation());
        assertEquals(7, processExecution.getFindings().get(1).getAttributes().size());
        assertEquals("interwise", processExecution.getFindings().get(1).getAttributes().get("SERVICE"));
        assertEquals(7778, processExecution.getFindings().get(1).getAttributes().get("PORT"));
        assertEquals("1520606104", processExecution.getFindings().get(1).getAttributes().get("START"));
        assertEquals("tcp", processExecution.getFindings().get(1).getAttributes().get("PROTOCOL"));
        assertEquals("127.0.0.1", processExecution.getFindings().get(1).getAttributes().get("HOST"));
        assertEquals("open", processExecution.getFindings().get(1).getAttributes().get("STATE"));
        assertEquals("1520606118", processExecution.getFindings().get(1).getAttributes().get("END"));
    }

    private final static String nmapResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!DOCTYPE nmaprun>\n"
            + "<?xml-stylesheet href=\"file:///usr/local/bin/../share/nmap/nmap.xsl\" type=\"text/xsl\"?>\n"
            + "<!-- Nmap 7.40 scan initiated Fri Mar  9 15:35:04 2018 as: nmap -oX - -O localhost -->\n"
            + "<nmaprun scanner=\"nmap\" args=\"nmap -oX - -O localhost\" start=\"1520606104\" startstr=\"Fri Mar  9 15:35:04 2018\" version=\"7.40\" xmloutputversion=\"1.04\">\n"
            + "<scaninfo type=\"syn\" protocol=\"tcp\" numservices=\"1000\" services=\"1,3-4,6-7,9,13,17,19-26,30,32-33,37,42-43,49,53,70,79-85,88-90,99-100,106,109-111,113,119,125,135,139,143-144,146,161,163,179,199,211-212,222,254-256,259,264,280,301,306,311,340,366,389,406-407,416-417,425,427,443-445,458,464-465,481,497,500,512-515,524,541,543-545,548,554-555,563,587,593,616-617,625,631,636,646,648,666-668,683,687,691,700,705,711,714,720,722,726,749,765,777,783,787,800-801,808,843,873,880,888,898,900-903,911-912,981,987,990,992-993,995,999-1002,1007,1009-1011,1021-1100,1102,1104-1108,1110-1114,1117,1119,1121-1124,1126,1130-1132,1137-1138,1141,1145,1147-1149,1151-1152,1154,1163-1166,1169,1174-1175,1183,1185-1187,1192,1198-1199,1201,1213,1216-1218,1233-1234,1236,1244,1247-1248,1259,1271-1272,1277,1287,1296,1300-1301,1309-1311,1322,1328,1334,1352,1417,1433-1434,1443,1455,1461,1494,1500-1501,1503,1521,1524,1533,1556,1580,1583,1594,1600,1641,1658,1666,1687-1688,1700,1717-1721,1723,1755,1761,1782-1783,1801,1805,1812,1839-1840,1862-1864,1875,1900,1914,1935,1947,1971-1972,1974,1984,1998-2010,2013,2020-2022,2030,2033-2035,2038,2040-2043,2045-2049,2065,2068,2099-2100,2103,2105-2107,2111,2119,2121,2126,2135,2144,2160-2161,2170,2179,2190-2191,2196,2200,2222,2251,2260,2288,2301,2323,2366,2381-2383,2393-2394,2399,2401,2492,2500,2522,2525,2557,2601-2602,2604-2605,2607-2608,2638,2701-2702,2710,2717-2718,2725,2800,2809,2811,2869,2875,2909-2910,2920,2967-2968,2998,3000-3001,3003,3005-3007,3011,3013,3017,3030-3031,3052,3071,3077,3128,3168,3211,3221,3260-3261,3268-3269,3283,3300-3301,3306,3322-3325,3333,3351,3367,3369-3372,3389-3390,3404,3476,3493,3517,3527,3546,3551,3580,3659,3689-3690,3703,3737,3766,3784,3800-3801,3809,3814,3826-3828,3851,3869,3871,3878,3880,3889,3905,3914,3918,3920,3945,3971,3986,3995,3998,4000-4006,4045,4111,4125-4126,4129,4224,4242,4279,4321,4343,4443-4446,4449,4550,4567,4662,4848,4899-4900,4998,5000-5004,5009,5030,5033,5050-5051,5054,5060-5061,5080,5087,5100-5102,5120,5190,5200,5214,5221-5222,5225-5226,5269,5280,5298,5357,5405,5414,5431-5432,5440,5500,5510,5544,5550,5555,5560,5566,5631,5633,5666,5678-5679,5718,5730,5800-5802,5810-5811,5815,5822,5825,5850,5859,5862,5877,5900-5904,5906-5907,5910-5911,5915,5922,5925,5950,5952,5959-5963,5987-5989,5998-6007,6009,6025,6059,6100-6101,6106,6112,6123,6129,6156,6346,6389,6502,6510,6543,6547,6565-6567,6580,6646,6666-6669,6689,6692,6699,6779,6788-6789,6792,6839,6881,6901,6969,7000-7002,7004,7007,7019,7025,7070,7100,7103,7106,7200-7201,7402,7435,7443,7496,7512,7625,7627,7676,7741,7777-7778,7800,7911,7920-7921,7937-7938,7999-8002,8007-8011,8021-8022,8031,8042,8045,8080-8090,8093,8099-8100,8180-8181,8192-8194,8200,8222,8254,8290-8292,8300,8333,8383,8400,8402,8443,8500,8600,8649,8651-8652,8654,8701,8800,8873,8888,8899,8994,9000-9003,9009-9011,9040,9050,9071,9080-9081,9090-9091,9099-9103,9110-9111,9200,9207,9220,9290,9415,9418,9485,9500,9502-9503,9535,9575,9593-9595,9618,9666,9876-9878,9898,9900,9917,9929,9943-9944,9968,9998-10004,10009-10010,10012,10024-10025,10082,10180,10215,10243,10566,10616-10617,10621,10626,10628-10629,10778,11110-11111,11967,12000,12174,12265,12345,13456,13722,13782-13783,14000,14238,14441-14442,15000,15002-15004,15660,15742,16000-16001,16012,16016,16018,16080,16113,16992-16993,17877,17988,18040,18101,18988,19101,19283,19315,19350,19780,19801,19842,20000,20005,20031,20221-20222,20828,21571,22939,23502,24444,24800,25734-25735,26214,27000,27352-27353,27355-27356,27715,28201,30000,30718,30951,31038,31337,32768-32785,33354,33899,34571-34573,35500,38292,40193,40911,41511,42510,44176,44442-44443,44501,45100,48080,49152-49161,49163,49165,49167,49175-49176,49400,49999-50003,50006,50300,50389,50500,50636,50800,51103,51493,52673,52822,52848,52869,54045,54328,55055-55056,55555,55600,56737-56738,57294,57797,58080,60020,60443,61532,61900,62078,63331,64623,64680,65000,65129,65389\"/>\n"
            + "<verbose level=\"0\"/>\n" + "<debugging level=\"0\"/>\n"
            + "<host starttime=\"1520606104\" endtime=\"1520606118\"><status state=\"up\" reason=\"localhost-response\" reason_ttl=\"0\"/>\n"
            + "<address addr=\"127.0.0.1\" addrtype=\"ipv4\"/>\n" + "<hostnames>\n"
            + "<hostname name=\"localhost\" type=\"user\"/>\n" + "<hostname name=\"localhost\" type=\"PTR\"/>\n"
            + "</hostnames>\n" + "<ports><extraports state=\"closed\" count=\"499\">\n"
            + "<extrareasons reason=\"resets\" count=\"499\"/>\n" + "</extraports>\n"
            + "<extraports state=\"filtered\" count=\"499\">\n"
            + "<extrareasons reason=\"no-responses\" count=\"499\"/>\n" + "</extraports>\n"
            + "<port protocol=\"tcp\" portid=\"3306\"><state state=\"open\" reason=\"syn-ack\" reason_ttl=\"64\"/><service name=\"mysql\" method=\"table\" conf=\"3\"/></port>\n"
            + "<port protocol=\"tcp\" portid=\"7778\"><state state=\"open\" reason=\"syn-ack\" reason_ttl=\"64\"/><service name=\"interwise\" method=\"table\" conf=\"3\"/></port>\n"
            + "</ports>\n" + "<os><portused state=\"open\" proto=\"tcp\" portid=\"3306\"/>\n"
            + "<portused state=\"closed\" proto=\"tcp\" portid=\"4\"/>\n"
            + "<portused state=\"closed\" proto=\"udp\" portid=\"44494\"/>\n"
            + "<osmatch name=\"Apple OS X 10.10 (Yosemite) - 10.11 (El Capitan) (Darwin 14.0.0 - 15.4.0)\" accuracy=\"97\" line=\"7015\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"OS X\" osgen=\"10.10.X\" accuracy=\"97\"><cpe>cpe:/o:apple:mac_os_x:10.10</cpe></osclass>\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"OS X\" osgen=\"10.11.X\" accuracy=\"97\"><cpe>cpe:/o:apple:mac_os_x:10.11</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple macOS 10.12 (Sierra) (Darwin 16.0.0)\" accuracy=\"94\" line=\"6800\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"macOS\" osgen=\"10.12.X\" accuracy=\"94\"><cpe>cpe:/o:apple:mac_os_x:10.12</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple OS X 10.10.3 (Yosemite) - 10.11.0 (El Capitan) (Darwin 14.3.0 - 15.0.0)\" accuracy=\"94\" line=\"7079\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"OS X\" osgen=\"10.10.X\" accuracy=\"94\"><cpe>cpe:/o:apple:mac_os_x:10.10</cpe></osclass>\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"OS X\" osgen=\"10.11.X\" accuracy=\"94\"><cpe>cpe:/o:apple:mac_os_x:10.11</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple Mac OS X 10.7.0 - 10.7.4 (Lion) (Darwin 11.0.0 - 11.4.0) or iPhone mobile phone (iOS 4.3.2)\" accuracy=\"93\" line=\"6322\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"Mac OS X\" osgen=\"10.7.X\" accuracy=\"93\"><cpe>cpe:/o:apple:mac_os_x:10.7</cpe></osclass>\n"
            + "<osclass type=\"phone\" vendor=\"Apple\" osfamily=\"iOS\" osgen=\"4.X\" accuracy=\"93\"><cpe>cpe:/o:apple:iphone_os:4.3.2</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple Mac OS X 10.7.0 - 10.7.4 (Lion) (Darwin 11.0.0 - 11.3.0)\" accuracy=\"93\" line=\"6298\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"Mac OS X\" osgen=\"10.7.X\" accuracy=\"93\"><cpe>cpe:/o:apple:mac_os_x:10.7</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple Mac OS X 10.7.2 - 10.7.3 (Lion) (Darwin 11.2.0 - 11.3.0)\" accuracy=\"93\" line=\"6498\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"Mac OS X\" osgen=\"10.7.X\" accuracy=\"93\"><cpe>cpe:/o:apple:mac_os_x:10.7</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple OS X 10.8 (Mountain Lion) - 10.9 (Mavericks) (Darwin 12.0.0 - 13.4.0) or iOS 5.0.1\" accuracy=\"93\" line=\"7212\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"OS X\" osgen=\"10.8.X\" accuracy=\"93\"><cpe>cpe:/o:apple:mac_os_x:10.8</cpe></osclass>\n"
            + "<osclass type=\"phone\" vendor=\"Apple\" osfamily=\"iOS\" osgen=\"5.X\" accuracy=\"93\"><cpe>cpe:/o:apple:iphone_os:5.0.1</cpe></osclass>\n"
            + "<osclass type=\"media device\" vendor=\"Apple\" osfamily=\"iOS\" osgen=\"5.X\" accuracy=\"93\"><cpe>cpe:/o:apple:iphone_os:5.0.1</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple Mac OS X 10.7.5 (Mountain Lion) (Darwin 11.4.2)\" accuracy=\"92\" line=\"6684\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"Mac OS X\" osgen=\"10.7.X\" accuracy=\"92\"><cpe>cpe:/o:apple:mac_os_x:10.7.5</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple OS X 10.8 - 10.8.1 (Mountain Lion) (Darwin 12.0.0 - 12.1.0)\" accuracy=\"92\" line=\"7292\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"OS X\" osgen=\"10.8.X\" accuracy=\"92\"><cpe>cpe:/o:apple:mac_os_x:10.8</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osmatch name=\"Apple Mac OS X 10.7.2 (Lion) (Darwin 11.2.0)\" accuracy=\"91\" line=\"6384\">\n"
            + "<osclass type=\"general purpose\" vendor=\"Apple\" osfamily=\"Mac OS X\" osgen=\"10.7.X\" accuracy=\"91\"><cpe>cpe:/o:apple:mac_os_x:10.7.2</cpe></osclass>\n"
            + "</osmatch>\n"
            + "<osfingerprint fingerprint=\"OS:SCAN(V=7.40%E=4%D=3/9%OT=3306%CT=4%CU=44494%PV=N%DS=0%DC=L%G=Y%TM=5AA29B&#xa;OS:A6%P=x86_64-apple-darwin16.3.0)SEQ(SP=101%GCD=2%ISR=10C%TI=Z%CI=RD%II=RI&#xa;OS:%TS=A)OPS(O1=M3FD8NW5NNT11SLL%O2=M3FD8NW5NNT11SLL%O3=M3FD8NW5NNT11%O4=M3&#xa;OS:FD8NW5NNT11SLL%O5=M3FD8NW5NNT11SLL%O6=M3FD8NNT11SLL)WIN(W1=FFFF%W2=FFFF%&#xa;OS:W3=FFFF%W4=FFFF%W5=FFFF%W6=FFFF)ECN(R=Y%DF=Y%T=40%W=FFFF%O=M3FD8NW5SLL%C&#xa;OS:C=N%Q=)T1(R=Y%DF=Y%T=40%S=O%A=S+%F=AS%RD=0%Q=)T2(R=N)T3(R=N)T4(R=Y%DF=Y%&#xa;OS:T=40%W=0%S=A%A=Z%F=R%O=%RD=0%Q=)T5(R=Y%DF=N%T=40%W=0%S=Z%A=S+%F=AR%O=%RD&#xa;OS:=0%Q=)T6(R=Y%DF=Y%T=40%W=0%S=A%A=Z%F=R%O=%RD=0%Q=)T7(R=Y%DF=N%T=40%W=0%S&#xa;OS:=Z%A=S%F=AR%O=%RD=0%Q=)U1(R=Y%DF=N%T=40%IPL=38%UN=0%RIPL=G%RID=G%RIPCK=Z&#xa;OS:%RUCK=0%RUD=G)IE(R=Y%DFI=S%T=40%CD=S)&#xa;\"/>\n"
            + "</os>\n" + "<uptime seconds=\"113720\" lastboot=\"Thu Mar  8 07:59:58 2018\"/>\n"
            + "<distance value=\"0\"/>\n"
            + "<tcpsequence index=\"263\" difficulty=\"Good luck!\" values=\"99408954,1F3984AF,1E2A2902,FC3E5FCC,B7C8257C,56DE3EEC\"/>\n"
            + "<ipidsequence class=\"All zeros\" values=\"0,0,0,0,0,0\"/>\n"
            + "<tcptssequence class=\"1000HZ\" values=\"6C7383E,6C738A6,6C7390E,6C73971,6C739D7,6C73A3E\"/>\n"
            + "<times srtt=\"78\" rttvar=\"23\" to=\"100000\"/>\n" + "</host>\n"
            + "<runstats><finished time=\"1520606118\" timestr=\"Fri Mar  9 15:35:18 2018\" elapsed=\"14.91\" summary=\"Nmap done at Fri Mar  9 15:35:18 2018; 1 IP address (1 host up) scanned in 14.91 seconds\" exit=\"success\"/><hosts up=\"1\" down=\"0\" total=\"1\"/>\n"
            + "</runstats>\n" + "</nmaprun>";
}
