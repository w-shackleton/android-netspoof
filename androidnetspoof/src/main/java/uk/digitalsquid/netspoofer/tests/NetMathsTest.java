package uk.digitalsquid.netspoofer.tests;

import android.test.InstrumentationTestCase;

import java.net.UnknownHostException;

import uk.digitalsquid.netspoofer.config.NetHelpers;

/**
 * Tests all the maths to do with networking, IP addresses etc.
 */
public class NetMathsTest extends InstrumentationTestCase {

    public void testInetFromInt() throws UnknownHostException {
        assertEquals("10.4.1.1", NetHelpers.inetFromInt(0x0101040AL).getHostAddress());
        assertEquals("192.168.1.42", NetHelpers.inetFromInt(0x2A01A8C0L).getHostAddress());
        assertEquals("192.168.1.242", NetHelpers.inetFromInt(0xF201A8C0L).getHostAddress());

        assertEquals("10.4.1.1", NetHelpers.inetFromInt(0x0101040A).getHostAddress());
        assertEquals("192.168.1.42", NetHelpers.inetFromInt(0x2A01A8C0).getHostAddress());
        assertEquals("192.168.1.242", NetHelpers.inetFromInt(0xF201A8C0).getHostAddress());
    }

    public void testReverseInetFromInt() throws UnknownHostException {
        assertEquals("10.4.1.1", NetHelpers.reverseInetFromInt(0x0A040101L).getHostAddress());
        assertEquals("192.168.1.42", NetHelpers.reverseInetFromInt(0xC0A8012AL).getHostAddress());
        assertEquals("192.168.1.242", NetHelpers.reverseInetFromInt(0xC0A801F2L).getHostAddress());

        assertEquals("10.4.1.1", NetHelpers.reverseInetFromInt(0x0A040101).getHostAddress());
        assertEquals("192.168.1.42", NetHelpers.reverseInetFromInt(0xC0A8012A).getHostAddress());
        assertEquals("192.168.1.242", NetHelpers.reverseInetFromInt(0xC0A801F2).getHostAddress());
    }
}
