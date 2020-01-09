package org.narrative.common.persistence;

import org.narrative.common.util.IPDateUtil;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 28, 2005
 * Time: 5:18:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class OIDGenerator implements IdentifierGenerator {
    public static final String NAME = "oidgen";
    // this value is for May 25, 2006
    private static final long FIRST_TIME = 1148595489093l;
    private static OIDGenerator gen = new OIDGenerator();
    private static long nextOID;
    private static boolean init = false;

//    static {
//        //get the last two bytes from the system ip addresss and have them occupy bits 62-47
//        long ip = 0;
//        try {
//            byte [] addr = InetAddress.getLocalHost().getAddress();
//            long fourth = addr[3] >=0?addr[3]:256+addr[3];
//            long third = addr[2] >=0?addr[2]:256+addr[2];
//            ip = fourth << 55;
//            ip |= third << 47;
//        } catch (UnknownHostException e) {
//            throw UnexpectedError.getRuntimeException("Unable to obtain IP Address for Unique OIDs");
//        }
//
//
//        //next get the current time and shift off the fist 24 bits then move back 17 bits to make room for the ip address and the sign.
//        //Time will occupy bits 46-9.  This will give us 16 years if date accuracy.  The last 8 bits are to allow
//        //us to assign up to 256 oids a millisecond between restarts without fear of overlap
//        long time = System.currentTimeMillis() - firstTime;
//        time = (time << 24) >>> 17;
//
//        //set the first oid for this jvm run
//        nextOID = time | ip;
//    }

    public static boolean isInitialized() {
        return init;
    }

    public static void init(long highBits) {
        //take the high bits and shift them 47 spaces to make room for the time seeded increment value below
        long highbitVal = highBits << 47;

        //next get the current time and shift off the fist 24 bits then move back 17 bits to make room for the hiBits and the sign.
        //Time will occupy bits 46-9.  This will give us 16 years if date accuracy.  The last 8 bits are to allow
        //us to assign up to 256 oids a millisecond between restarts without fear of overlap

        // bl: i'm not sure that the statement above is accurate. here's the breakdown (based on 0-index bits):
        // bit 63: sign bit
        // bits 62-47: high bits (bit 62 is actually never used)
        // bits 46-7: date accuracy bits
        // bits 6-0: millisecond/restart accuracy bits - 7 bits = 127 values

        // note that the getNextHighbit stored procedure loops through highbit values from 10 to 32000.
        // i suspect that we could increase that up to 64000 since it seems that bit 62 is completely wasted
        // and never used (i.e. always 0). per above, we have 16 high bits in play, but 32000 only requires 15 bits.
        // similarly, we only use the value 9 for MySQL OID generation.

        // ultimately, it looks like we have until 2040 before the date values overflow. the discrepancy with the original
        // comment above is that it assumed that the least significant 8 bits were reserved, but in actuality,
        // only the least significant 7 bits (6-0) are reserved (which effectively doubles the amount of time we have until overflow).
        // in the tests below, we can clearly see that bit #7 _is_ used by the date accuracy bits.

        // bits 6-0 are used so that we can average up to 127 OIDs generated per millisecond while a servlet is running.
        // if we exceed that average for the life of the servlet, then there is potential for an OID collision if the
        // same highbit value is used. that is extremely unlikely since there are almost 32k highbit values that need
        // to be cycled through. so, it's nearly statistically impossible to have a real OID collision, as it would
        // require a servlet running and generating an extremely high number of OIDs (over 127 per millisecond on average)
        // AND it would require the highbit cycling all the way through 32k values before the current time "caught up"
        // with the "future" date accuracy bits...
        long time = System.currentTimeMillis() - FIRST_TIME;
        time = (time << 24) >>> 17;

        //set the first oid for this jvm run
        nextOID = highbitVal | time;

        init = true;
    }

    public static OID getNextOID() {
        return (OID) gen.generate(null, null);
    }

    /**
     * Generate a new identifier.
     *
     * @param session The session from which the request originates
     * @param object  the entity or collection (idbag) for which the id is being generated
     * @return a new identifier
     * @throws HibernateException Indicates trouble generating the identifier
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        assert init : "Can't attempt to get an OID before the OIDGenerator is initialized!";

        //bk: This is needed so we don't generate a new OID for objects that we manually set the OID on already
        if (object != null && object instanceof DAOObject && ((DAOObject) object).getOid() != null) {
            return ((DAOObject) object).getOid();
        }

        long oidVal;
        // bl: need to synchronize here or else we get into race conditions when generating OIDs at too fast a rate.
        // note that we have our own internal OIDGenerator instance, but Hibernate will also have one, as configured
        // in the package-info.java class. thus, we should synchronize on the class, not just the instance.
        // note that nextOID is static, so this makes complete sense. also, I'm not sure that Hibernate is guaranteed
        // to create just a single instance of OIDGenerator, so if there are multiple instances, then we can have
        // race conditions when multiple threads are generating OIDs at the same time.
        synchronized (OIDGenerator.class) {
            oidVal = nextOID++;
        }
        return new OID(oidVal);
    }

    public static void main(String[] args) {
        System.out.println(Long.toHexString(Long.MAX_VALUE));
        // bl: check the date accuracy bit values from 2005 through 2044. you should be able to see
        // that we do not overflow until 2040. so, Narrative has Y2040 "bug" that will rear its ugly
        // head 26 years from the time of this writing. at that time, we can address the issue and buy
        // another 34 years by utilizing the 62nd OID bit value, which is currently completely unused.
        long now = System.currentTimeMillis();
        for (int i = 0; i < 40; i++) {
            System.out.println();
            int year = 2005 + i;
            System.out.println("Year " + year);
            long val = now + ((year - 2014) * IPDateUtil.YEAR_IN_MS);
            System.out.println("Year value: " + Long.toHexString(val));
            //System.out.println(": " + Long.toHexString(Long.MAX_VALUE - val));
            long maxOid = val - FIRST_TIME;
            //System.out.println(Long.toHexString(maxOid));
            maxOid = (maxOid << 24);
            //System.out.println(Long.toHexString(maxOid));
            System.out.println("maxOid value: " + Long.toHexString(maxOid >>> 17));
            System.out.println("maxOid value: " + Long.toHexString(maxOid >> 17));
        }
        // 2^40 = 34 years of date accuracy
        System.out.println(1099511627776L / IPDateUtil.YEAR_IN_MS);
        // 2^39 = 17 years of date accuracy
        System.out.println(549755813888L / IPDateUtil.YEAR_IN_MS);

        // bl: this little loop proves that by looping at a value of 32000, we are completely wasting
        // the second most significant bit (bit 62). that's one way to get more OIDs into play if/when we hit OID rollover
        // based on the date values
        /*for(int i=10; i<=32000; i++) {
            System.out.println(Long.toHexString(i << 47));
        }*/
    }
}
