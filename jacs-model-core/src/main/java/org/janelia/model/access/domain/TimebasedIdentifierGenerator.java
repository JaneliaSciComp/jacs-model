package org.janelia.model.access.domain;

import com.google.common.base.Preconditions;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a non-static re-implementation of Sean's original TimebasedIdentifierGenerator from VICS/JACSv1.
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
 *
 * @author Cristian Goina
 * @author Sean Murphy
 */
public class TimebasedIdentifierGenerator implements IdGenerator<Long> {

    private static final Long CURRENT_TIME_OFFSET = 921700000000L;
    private static final int MAX_DEPLOYMENT_CONTEXT = 15; // 4 bits only

    private final int ipComponent;
    private final int deploymentContext;
    private IDBlock lastIDBlock;

    private static final class IDBlock {
        private static final int BLOCK_SIZE = 1024;
        private long timeComponent;
        private long deploymentContext;
        private long ipComponent;
        private long currentIndex = 0;

        private synchronized boolean hasNext() {
            return currentIndex < BLOCK_SIZE;
        }

        private synchronized BigInteger next() {
            BigInteger nextId = BigInteger.valueOf(timeComponent).shiftLeft(22)
                                    .add(BigInteger.valueOf(currentIndex << 12))
                                    .add(BigInteger.valueOf(deploymentContext << 8))
                                    .add(BigInteger.valueOf(ipComponent));
            currentIndex++;
            return nextId;
        }

    }

    TimebasedIdentifierGenerator() {
        // constructor needed by CDI so that the bean can be injected as a singleton with ApplicationScope
        this(0);
    }

    public TimebasedIdentifierGenerator(Integer deploymentContext) {
        this(deploymentContext, false);
    }

    public TimebasedIdentifierGenerator(Integer deploymentContext, boolean useLoopback) {
        Preconditions.checkArgument(deploymentContext >= 0 && deploymentContext <= MAX_DEPLOYMENT_CONTEXT,
                "Deployment context value is out of range. It's current value is "
                        + deploymentContext + " and the allowed values are between 0 and " + MAX_DEPLOYMENT_CONTEXT);
        this.deploymentContext = deploymentContext;
        this.ipComponent = getIpAddrCompoment(useLoopback);
    }

    @Override
    public Long generateId() {
        IDBlock idBlock = getIDBlock();
        return idBlock.next().longValue();
    }

    @Override
    public List<Long> generateIdList(int n) {
        List<Long> idList = new ArrayList<>();
        long total = 0L;
        while (total < n) {
            IDBlock idBlock = getIDBlock();
            for (; total < n && idBlock.hasNext(); total++) {
                idList.add(idBlock.next().longValue());
            }
        }
        return idList;
    }

    private synchronized IDBlock getIDBlock() {
        if (lastIDBlock != null && lastIDBlock.hasNext()) {
            return lastIDBlock;
        }
        IDBlock idBlock = new IDBlock();
        idBlock.ipComponent = ipComponent;
        idBlock.deploymentContext = deploymentContext;
        idBlock.timeComponent = System.currentTimeMillis() - CURRENT_TIME_OFFSET;
        if (lastIDBlock != null && lastIDBlock.timeComponent == idBlock.timeComponent) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            idBlock.timeComponent = System.currentTimeMillis() - CURRENT_TIME_OFFSET;
        }
        lastIDBlock = idBlock;
        return idBlock;
    }

    private int getIpAddrCompoment(boolean useLoopback) {

        if (useLoopback) {
            // This is basically guaranteed to be very fast, because it doesn't do any hostname lookups.
            byte[] ipAddress = InetAddress.getLoopbackAddress().getAddress();
            return ((int)ipAddress[ipAddress.length - 1] & 0xFF);
        }

        try {
            // Could be slow on MacOS due to misconfigured /etc/hosts
            byte[] ipAddress = InetAddress.getLocalHost().getAddress();
            return ((int)ipAddress[ipAddress.length - 1] & 0xFF);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

}
