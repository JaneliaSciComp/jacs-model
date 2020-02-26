
package org.janelia.model.access.domain;

import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated
 * @see org.janelia.model.util.TimebasedIdentifierGenerator
 *
 * Created by IntelliJ IDEA.
 * User: smurphy
 * Date: Sep 29, 2006
 * Time: 10:02:26 AM
 *
 * This class generates a time-based UUID encoded as a long.
 * <p/>
 * new Date().getTime() returns a long such as:
 * <p/>
 * 1,159,540,149,899  (a)
 * <p/>
 * We would like our UUIDs to be positive longs, which give us 63 bits of
 * encoding space. We would also like to encode time, IP subnet to
 * allow multiple JVMs to be running in parallel without perfect clock
 * sychronization, and also some context key to distinguish different
 * deployments, such as development, validation, production, etc.
 * <p/>
 * The number 2^41 is:
 * <p/>
 * 2,199,023,255,552
 * <p/>
 * Our approach will be to subtract an offset from the current time
 * of 921,700,000,000 (b), which is equivalient to ~90% of the current time
 * since 1970.
 * <p/>
 * Subtracting (b) from (a) will render the date of 10/6/2006 the approximate
 * starting time such that the first uuid begins at the value (including the
 * full 22 trailing bits below):
 * <p/>
 * 1,000,156,264,556,134,746
 * <p/>
 * Thus, all valid IDs are greater than 1x10^18
 * <p/>
 * This will give the generator a lifetime of ~70 years.
 * <p/>
 * We will use the following byte-order encoding:
 * <p/>
 * positive bit                1 bit  (constant)
 * Date.getTime()             41 bits
 * intra-millisecond index:   10 bits (1024 values)
 * deployment context:         4 bits (16 values)
 * IP subnet:                  8 bits (256 values)
 * <p/>
 * Total:                     64 bits
 * <p/>
 * Within each millisecond, the class will deliver up to 2^10 values.
 * If it runs out of values for the current millisecond, it will block
 * for 1 millisecond with Thread.sleep(1L) and continue.
 */
public class TimebasedIdentifierGenerator implements IdentifierGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TimebasedIdentifierGenerator.class);

    private static final Long CURRENT_TIME_OFFSET = 921700000000L;
    public static final String PROPERTIES_FILE = "jacs.properties";
    public static final String DEPLOYMENT_CONTEXT_PROP = "TimebasedIdentifierGenerator.DeploymentContextNumber";
    private static boolean valid = true;
    private static long millisecond;
    private static long intraMillisecondCounter;
    private static long lastUid;
    private static String deploymentContextComponent;
    private static String subnetComponent;

    static {
        try {
            millisecond = getTime();
            int subnet = getIPSubnet();
            int deploymentContext = getDeploymentContext();
            Long deploymentContextLong = new Long("" + deploymentContext);
            logger.info("Using deployment context " + deploymentContextLong);
            deploymentContextComponent = padBinaryStringToBits(Long.toBinaryString(deploymentContextLong), 4);
            Long subnetLong = new Long("" + subnet);
            logger.info("Using subnet " + subnetLong);
            subnetComponent = padBinaryStringToBits(Long.toBinaryString(subnetLong), 8);
        }
        catch (Throwable t) {
            valid = false;
            logger.error("Exception starting up TimebasedIdentifierGenerator. Id generator is not valid.", t);
        }
    }

    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        return generateSync(1L);
    }

    public static synchronized Serializable generate(long blockSize) {
        return generateSync(blockSize);
    }

    /*
     * This method will generate an id list of arbitrary size
     */
    public static synchronized List<Long> generateIdList(long blocksize) {
        List<Long> idList = new ArrayList<Long>();
        long total = 0L;
        long incrementSize = 1000;
        while (total < blocksize) {
            long blockStart = (Long) generate(incrementSize);
            for (long i = blockStart; i < blockStart + incrementSize; i++) {
                if (total < blocksize)
                    idList.add(i);
                total++;
            }
        }
        return idList;
    }

    private static synchronized Serializable generateSync(long blockSize)
            throws HibernateException {
        if (!valid) throw new HibernateException("This generator has an illegal state - last uid issued: " + lastUid);
        Long uid;
        try {
            updateMillisecond(blockSize);
            // We can now assume ('intraMillisecondCounter' - blockSize) is our return value
            String timeComponent = Long.toBinaryString(millisecond);
            //logger.info("Using millisecond " + millisecond + " and binary string " + timeComponent);
            String intraMillisecondComponent = "0000000000"; // 10 zeros - most common case
            long intraMillisecondReturnValue = intraMillisecondCounter - blockSize;
            if (intraMillisecondReturnValue > 0) {
                intraMillisecondComponent = padBinaryStringToBits(Long.toBinaryString(intraMillisecondReturnValue), 10);
            }
            StringBuffer uidStringBuffer = new StringBuffer(timeComponent);
            uidStringBuffer.append(intraMillisecondComponent);
            uidStringBuffer.append(deploymentContextComponent);
            uidStringBuffer.append(subnetComponent);
            String uidString = uidStringBuffer.toString();
            if (uidString.length() > 63) {
                valid = false;
                throw new Exception("Length of uid string invalid: " + uidString);
            }
            //logger.info("Using uid binary string: " + uidString);
            uid = Long.parseLong(uidString, 2);
            lastUid = uid;
        }
        catch (Throwable t) {
            valid = false;
            logger.error("Error generating ids. Id generator is not valid.", t);
            throw new HibernateException(t.toString(), t);
        }
//        long nTimeEnd = System.nanoTime();
        //if(doLog) {
//            logger.info("  nanoseconds: " + (nTimeEnd-nTimeStart));
//            logger.info("          uid: " + uid);
//            logger.info("    timestamp: " + getTimestamp(uid.longValue()));
//            logger.info("        index: " + getIntraMillisecondIndex(uid.longValue()));
//            logger.info("      context: " + getDeploymentContext(uid.longValue()));
//            logger.info("       subnet: " + getSubnet(uid.longValue()));
        //}
        return uid;
    }

    private static synchronized void updateMillisecond(long blockSize) throws Exception {
        if (blockSize > 1000)
            throw new Exception("Maximum block size is 1000");
        long currentMillisecond = getTime();
        if (currentMillisecond < millisecond) {

            Thread.sleep(1);
            updateMillisecond(blockSize);

            // Note: we used to use the following code to error out under this condition, but
            // one day had an error (backward by 1ms) that we suspect may have been due to
            // some OS clock readjustment. Therefore, we will try to recover with above wait step.

//            valid=false;
//            throw new IllegalStateException("Previous millisecond was " + millisecond + " but current is "
//                    + currentMillisecond);

        }
        else if (currentMillisecond > millisecond) {
            millisecond = currentMillisecond;
            intraMillisecondCounter = 0L;
        }
        else {
            // we are still in the currentMillisecond - simply proceed
        }
        intraMillisecondCounter += blockSize;
        if (intraMillisecondCounter >= 1024) { // 2^10 limit
            Thread.sleep(1);
            updateMillisecond(blockSize);  // start over
        }
    }

    private static synchronized int getIPSubnet() {
        byte[] ipAddr = new byte[1];
        try {
            InetAddress addr = InetAddress.getLocalHost();
            ipAddr = addr.getAddress();
        }
        catch (UnknownHostException e) {
            logger.warn("Error getting local IP address. Id generator is not valid.", e);
            valid = false;
        }
        int subnet = ipAddr[ipAddr.length - 1];
        if (subnet < 0) subnet = 256 + subnet;
        return subnet;
    }

    private static synchronized String padBinaryStringToBits(String binaryString, int bits) throws Exception {
        if (binaryString.length() > bits) throw new Exception("Binary string is " + binaryString +
                " but requested bit pad length is " + bits);
        int neededZeros = bits - binaryString.length();
        StringBuffer zeroBuffer = new StringBuffer();
        for (int i = 0; i < neededZeros; i++) {
            zeroBuffer.append("0");
        }
        return zeroBuffer.toString() + binaryString;
    }

    private static synchronized int getDeploymentContext() {
        int deploymentContext = 0;
        try {
            InputStream is = TimebasedIdentifierGenerator.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
            if (is == null) {
                String message = "Could not get resource stream for properties file '" + PROPERTIES_FILE + "'";
                logger.warn(message);
            }
            Properties props = new Properties();
            props.load(is);
            deploymentContext = Integer.parseInt(props.getProperty(DEPLOYMENT_CONTEXT_PROP));
        } catch (Exception ex) {
            logger.error("Error loading deployment context property " + DEPLOYMENT_CONTEXT_PROP +
                    " will look for system property...");
            String depString = System.getProperties().getProperty(DEPLOYMENT_CONTEXT_PROP);
            if (depString == null) {
                logger.error("Could not find system property " + DEPLOYMENT_CONTEXT_PROP);
            }
            else {
                deploymentContext = Integer.parseInt(depString);
            }
        }
        if (deploymentContext < 0 || deploymentContext > 255) {
            logger.error("Deployment context " + deploymentContext + " out of range. Id generator is not valid.");
            valid = false;
        }
        return deploymentContext;
    }

    public static synchronized Date getTimestamp(long uuid) {
        String uuidString = Long.toBinaryString(uuid);
        String binDateString = uuidString.substring(0, uuidString.length() - 22);
        return new Date(Long.parseLong(binDateString, 2) + CURRENT_TIME_OFFSET);
    }

    public static synchronized long getIntraMillisecondIndex(long uuid) {
        String uuidString = Long.toBinaryString(uuid);
        String indexString = uuidString.substring(uuidString.length() - 22, uuidString.length() - 12);
        return Long.parseLong(indexString, 2);
    }

    public static synchronized int getDeploymentContext(long uuid) {
        String uuidString = Long.toBinaryString(uuid);
        String contextString = uuidString.substring(uuidString.length() - 12, uuidString.length() - 8);
        return Integer.parseInt(contextString, 2);
    }

    public static synchronized void setDeploymentContext(int contextValue) throws Exception {
        Long deploymentContextLong = new Long(String.valueOf(contextValue));
        deploymentContextComponent = padBinaryStringToBits(Long.toBinaryString(deploymentContextLong), 4);
    }

    public static synchronized int getSubnet(long uuid) {
        String uuidString = Long.toBinaryString(uuid);
        String subnetString = uuidString.substring(uuidString.length() - 8);
        return Integer.parseInt(subnetString, 2);
    }

    private static synchronized Long getTime() {
        return System.currentTimeMillis() - CURRENT_TIME_OFFSET;
    }

    /*
     * This method takes a date, and returns a "virtual" Uid which matches
     * the equivalent time. Note that this returned Uid is NOT an "official"
     * uid, but is intended for providing uids for timestamp searches.
     */
    public static synchronized Long getUidApproximationOfDate(Date date) {
        Long longDate = date.getTime() - CURRENT_TIME_OFFSET;
        String timeComponent = Long.toBinaryString(longDate);
        String intraMillisecondComponent = "0000000000";
        StringBuffer uidStringBuffer = new StringBuffer(timeComponent);
        uidStringBuffer.append(intraMillisecondComponent);
        uidStringBuffer.append(deploymentContextComponent);
        uidStringBuffer.append(subnetComponent);
        String uidString = uidStringBuffer.toString();
        return Long.parseLong(uidString, 2);
    }

}
