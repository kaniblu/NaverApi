package com.kaniblu.naver.api;

import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RSA
{
    private static final Logger logger = Logger.getLogger(RSA.class.getCanonicalName());

    private static final String BI_RM = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static long[] BI_RC = {
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L,
            0L,  1L,  2L,  3L,  4L,  5L,  6L,  7L,  8L,  9L,  -1L, -1L, -1L, -1L, -1L, -1L,
            -1L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L,
            25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, -1L, -1L, -1L, -1L, -1L,
            -1L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L,
            25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, -1L, -1L, -1L, -1L, -1L
    };
    private static final long BI_FP = 52L;
    private static final long dbits = 28L; //Netscape: 26L, IE: 30L

    private static char int2char(long a)
    {
        return BI_RM.charAt((int)a);
    }

    private static class BigInteger
    {
        public static final BigInteger ZERO = nbv(0L);
        public static final BigInteger ONE = nbv(1L);

        private static final Long DB = dbits; //26L, 28L, 30L
        private static final Long DM = (1L << dbits) -1L;
        private static final Long DV = 1L << dbits;
        private static final Long FV = 0x10000000000000L;
        private static final Long F1 = BI_FP - dbits;
        private static final Long F2 = 2L * dbits - BI_FP;

        public Hashtable<Long, Long> array = new Hashtable<Long, Long>();
        public long t;
        public long s;

        public BigInteger(Object a, Long b, Object c)
        {
            if (a != null) {
                if (a.getClass() == Long.class) {
                    logger.log(Level.SEVERE, "fromNumber is undefined."); //this.fromNumber(a, b, c);
                    this.fromNumber((Long)a, b, c);
                }
                else if (b == null && a.getClass() != String.class)
                    this.fromString(a, 256L);
                else
                    this.fromString(a, b);
            }
        }

        public void fromNumber(Long a, Long b, Object c)
        {
            logger.log(Level.SEVERE, "This function is just a placeholder and should not be called from anywhere.");
        }

        private static long intAt(String a, long b)
        {
            long c = a.charAt((int)b);

            if (c < BI_RC.length)
                return BI_RC[(int)c];
            else
                return -1L;
        }

        public BigInteger modPowInt(Long a, BigInteger b)
        {
            ModArithmetic c;
            if (a < 256L || b.isEven())
                c = new Classic(b);
            else
                c = new Montgomery(b);

            return this.exp(a, c);
        }

        public boolean isEven()
        {
            return (this.t > 0L ? this.array.get(0L) : this.s) == 0L;
        }

        public BigInteger exp(Long a, ModArithmetic b)
        {
            if (a < 1L) //if (a > 0xFFFFFFFF)
                return BigInteger.ONE;

            BigInteger c = nbi();
            BigInteger d = nbi();
            BigInteger e = b.convert(this);
            long f = nbits(a) - 1L;
            e.copyTo(c);

            while (--f >= 0L) {
                b.sqrTo(c, d);
                if ((a & 1L << f) > 0L)
                    b.mulTo(d, e, c);
                else {
                    BigInteger g = c;
                    c = d;
                    d = g;
                }
            }
            return b.revert(c);
        }

        public long compareTo(BigInteger a)
        {
            Long b = this.s - a.s;

            if (b != 0L)
                return b;

            long c = this.t;
            b = c - a.t;

            if (b != 0L)
                return b;

            while (--c >= 0L)
                if ((b = this.array.get(c) - a.array.get(c)) != 0L)
                    return b;

            return 0L;
        }

        /*public BigInteger mod(BigInteger a)
        {
            BigInteger b = nbi();
            this.abs().divRemTo(a, null, b);

            if (this.s < 0L && b.compareTo(BigInteger.ZERO) > 0L)
                a.subTo(b, b);

            return b;
        }*/

        public void copyTo(BigInteger a)
        {
            for (Long b = this.t - 1L; b >= 0L; --b)
                a.array.put(b, this.array.get(b));
            a.t = this.t;
            a.s = this.s;
        }

        public static long nbits(Long a)
        {
            Long b = 1L;
            Long c;

            if ((c = a >>> 16L) != 0L) {
                a = c;
                b += 16L;
            }

            if ((c = a >> 8L) != 0L) {
                a = c;
                b += 8L;
            }

            if ((c = a >> 4L) != 0L) {
                a = c;
                b += 4L;
            }

            if ((c = a >> 2L) != 0L) {
                a = c;
                b += 2L;
            }

            if ((a >> 1L) != 0L) {
                //a = c;
                b += 1L;
            }

            return b;
        }
        public void divRemTo(BigInteger a, BigInteger b, BigInteger c)
        {
            BigInteger d = a.abs();

            if (d.t <= 0L)
                return;

            BigInteger e = this.abs();

            if (e.t < d.t) {
                if (b != null)
                    b.fromInt(0L);
                if (c != null)
                    this.copyTo(c);
                return;
            }

            if (c == null)
                c = nbi();
            BigInteger f = nbi();
            Long g = this.s;
            Long h = a.s;

            Long i = DB - nbits(d.array.get(d.t - 1L));
            if (i > 0L) {
                d.lShiftTo(i, f);
                e.lShiftTo(i, c);
            } else {
                d.copyTo(f);
                e.copyTo(c);
            }

            long j = f.t;
            long k = f.array.get(j - 1L);

            if (k == 0L)
                return;

            long l = k * (1L << F1) + (j > 1L ? f.array.get(j - 2L) >> F2 : 0L);
            double m = (double)FV / l;
            double n = (double)(1L << F1) / l;
            long o = 1L << F2;
            long p = c.t;
            long q = p - j;
            BigInteger r = b == null ? nbi() : b;

            f.dlShiftTo(q, r);
            if (c.compareTo(r) >= 0L) {
                c.array.put(c.t++, 1L);
                c.subTo(r, c);
            }

            BigInteger.ONE.dlShiftTo(j, r);

            r.subTo(f, f);

            while (f.t < j)
                f.array.put(f.t++, 0L);

            while (--q >= 0L) {
                long s = c.array.get(--p) == k ? DM : (long)Math.floor(c.array.get(p) * m + (c.array.get(p - 1L) + o) * n);
                c.array.put(p, c.array.get(p) + f.am(0L, s, c, q, 0L, j));

                if (c.array.get(p) < s) {
                    f.dlShiftTo(q, r);
                    c.subTo(r, c);
                    while (c.array.get(p) < --s)
                        c.subTo(r, c);
                }
            }
            if (b != null) {
                c.drShiftTo(j, b);
                if (!g.equals(h)) BigInteger.ZERO.subTo(b, b);
            }
            c.t = j;
            c.clamp();
            if (i > 0L)
                c.rShiftTo(i, c);
            if (g < 0L)
                BigInteger.ZERO.subTo(c, c);
        }

        public void lShiftTo(Long a, BigInteger b)
        {
            long c = a % DB;
            long d = DB - c;
            long e = (1L << d) - 1L;
            long f = (long)Math.floor(a / DB);
            long g = this.s << c & DM;
            long h;

            for (h = this.t - 1L; h >= 0L; --h) {
                b.array.put(h + f + 1L, this.array.get(h) >> d | g);
                g = (this.array.get(h) & e) << c;
            }

            for (h = f - 1L; h >= 0L; --h)
                b.array.put(h, 0L);

            b.array.put(f, g);
            b.t = this.t + f + 1L;
            b.s = this.s;
            b.clamp();
        }

        public void rShiftTo(Long a, BigInteger b)
        {
            b.s = this.s;
            long c = (long)Math.floor(a / DB);

            if (c >= this.t) {
                b.t = 0L;
                return;
            }

            long d = a % DB;
            long e = DB - d;
            long f = (1L << d) - 1L;
            b.array.put(0L, this.array.get(c) >> d);

            for (long g = c + 1L; g < this.t; ++g) {
                b.array.put(g - c - 1L, b.array.get(g - c - 1L) | (this.array.get(g) & f) << e);
                b.array.put(g - c, this.array.get(g) >> d);
            }

            if (d > 0L)
                b.array.put(this.t - c - 1L, b.array.get(this.t - c - 1L | (this.s & f) << e));
            b.t = this.t - c;
            b.clamp();
        }

        public long bitLength()
        {
            if (this.t <= 0L)
                return 0L;

            return DB * (this.t - 1L) + nbits(this.array.get(this.t - 1L) ^ this.s & DM);
        }

        public void dlShiftTo(Long a, BigInteger b)
        {
            long c;
            for (c = this.t - 1L; c >= 0L; --c)
                b.array.put(c + a, this.array.get(c));

            for (c = a - 1L; c >= 0L; --c)
                b.array.put(c, 0L);

            b.t = this.t + a;
            b.s = this.s;
        }

        public void drShiftTo(Long a, BigInteger b) {
            for (long c = a; c < this.t; ++c)
                b.array.put(c - a, this.array.get(c));

            b.t = Math.max(this.t - a, 0L);
            b.s = this.s;
        }

        public static BigInteger nbi()
        {
            return new BigInteger(null, null, null);
        }

        public BigInteger abs()
        {
            return this.s < 0L ? this.negate() : this;
        }

        public BigInteger negate()
        {
            BigInteger a = nbi();
            BigInteger.ZERO.subTo(this, a);
            return a;
        }

        public void clamp()
        {
            Long a = this.s & DM;
            while (this.t > 0L && this.array.get(this.t - 1L).equals(a))
                --this.t;
        }

        public void subTo(BigInteger a, BigInteger b)
        {
            Long c = 0L;
            Long d = 0L;
            Long e = Math.min(a.t, this.t);
            while (c < e) {
                d += this.array.get(c) - a.array.get(c);
                b.array.put(c++, d & DM);
                d >>= DB;
            }

            if (a.t < this.t) {
                d -= a.s;
                while (c < this.t) {
                    d += this.array.get(c);
                    b.array.put(c++, d & DM);
                    d >>= DB;
                }
                d += this.s;
            } else {
                d += this.s;
                while (c < a.t) {
                    d -= a.array.get(c);
                    b.array.put(c++, d & DM);
                    d >>= DB;
                }
                d -= a.s;
            }

            b.s = d < 0L ? -1L : 0L;

            if (d < -1L)
                b.array.put(c++, DV + d);
            else if (d > 0L)
                b.array.put(c++, d);

            b.t = c;
            b.clamp();
        }


        public static BigInteger nbv(Long a)
        {
            BigInteger b = nbi();
            b.fromInt(a);
            return b;
        }

        public void fromInt(Long a)
        {
            this.t = 1L;
            this.s = a < 0L ? -1L : 0L;

            if (a > 0L)
                this.array.put(0L, a);
            else if (a < -1L)
                this.array.put(0L, a + DV);
            else
                this.t = 0L;
        }

        public void fromString(Object a, Long b)
        {
            if (b == 256L)
                fromString((Hashtable<Long, Long>)a);
            else
                fromString((String)a, b);
        }
        public void fromString(Hashtable<Long, Long> a)
        {
            long c = 8L;
            this.t = 0L;
            this.s = 0L;

            Long d = (long)a.size();
            Boolean e = false;
            Long f = 0L;

            while (--d >= 0L) {
                long g = a.get(d) & 255L;

                if (g < 0L) {
                    if (a.get(d) == '-')
                        e = true;
                    continue;
                }

                e = false;

                if (f == 0L)
                    array.put(t++, g);
                else if (f + c > DB) {
                    array.put(t - 1L, array.get(t - 1L) | (g & (1L << DB - f) - 1L) << f);
                    array.put(t++, g >> DB - f);
                } else
                    array.put(t - 1L, array.get(t - 1L) | g << f);
                f += c;
                if (f >= DB)
                    f -= DB;
            }

            if ((a.get(0L) & 128L) != 0L) {
                this.s = -1L;
                if (f > 0L)
                    this.array.put(this.t - 1L, this.array.get(this.t - 1L) | (1L << DB - f) - 1L << f);
            }

            this.clamp();

            if (e)
                BigInteger.ZERO.subTo(this, this);
        }

        public void fromString(String a, Long b)
        {
            Long c;

            if (b == 16L)
                c = 4L;
            else if (b == 8L)
                c = 3L;
            else if (b == 256L)
                c = 8L;
            else if (b == 2L)
                c = 1L;
            else if (b == 32L)
                c = 5L;
            else if (b == 4L)
                c = 2L;
            else {
                logger.log(Level.SEVERE, "Undefined radix.");
                return;
            }

            this.t = 0L;
            this.s = 0L;

            Long d = (long)a.length();
            Boolean e = false;
            Long f = 0L;

            while (--d >= 0L) {
                long g = intAt(a, d);

                if (g < 0L) {
                    if (a.charAt((int)(long)d) == '-')
                        e = true;
                    continue;
                }

                e = false;

                if (f == 0L)
                    array.put(t++, g);
                else if (f + c > DB) {
                    array.put(t - 1L, array.get(t - 1L) | (g & (1L << DB - f) - 1L) << f);
                    array.put(t++, g >> DB - f);
                } else
                    array.put(t - 1L, array.get(t - 1L) | g << f);
                f += c;
                if (f >= DB)
                    f -= DB;
            }

            if (c == 8L && (a.charAt(0) & 128L) != 0L) {
                this.s = -1L;
                if (f > 0L)
                    this.array.put(this.t - 1L, this.array.get(this.t - 1L) | (1L << DB - f) - 1L << f);
            }

            this.clamp();

            if (e)
                BigInteger.ZERO.subTo(this, this);
        }

        public Long invDigit()
        {
            if (this.t < 1L)
                return 0L;

            Long a = this.array.get(0L);

            if ((a & 1L) == 0L)
                return 0L;

            Long b = a & 3L;

            b = b * (2L - (a & 15L) * b) & 15L;
            b = b * (2L - (a & 255L) * b) & 255L;
            b = b * (2L - ((a & 65535L) * b & 65535L)) & 65535L;
            b = b * (2L - a * b % DV) % DV;

            return b > 0L ? DV - b : -b;
        }

        /*public Long am(Long a, Long b, BigInteger c, Long d, Long e, Long f)
        {
            while (--f >= 0L) {
                Long g = b * this.array.get(a++) + c.array.get(d) + e;
                e = (long)Math.floor(g / 67108864L);
                c.array.put(d++, g & 67108863L);
            }
            return e;
        }*/

        /*public Long am(Long a, Long b, BigInteger c, Long d, Long e, Long f)
        {
            long g = b & 32767L;
            long h = b >> 15L;

            while (--f >= 0L) {
                long i = this.array.get(a) & 32767L;
                long j = this.array.get(a++) >> 15L;
                long k = h * i + j * g;
                i = g * i + ((k & 32767L) << 15L) + c.array.get(d) + (e & 1073741823L);
                e = (i >>> 30L) + (k >>> 15L) + h * j + (e >>> 30L);
                c.array.put(d++, i & 1073741823L);
            }

            return e;
        }*/

        public Long am(Long a, Long b, BigInteger c, Long d, Long e, Long f)
        {
            long g = b & 16383L, h = b >> 14L;

            while (--f >= 0L) {
                long i = this.array.get(a) & 16383L;
                long j = this.array.get(a++) >> 14L;
                long k = h * i + j * g;
                i = g * i + ((k & 16383L) << 14L) + c.array.get(d) + e;
                e = (i >> 28L) + (k >> 14L) + h * j;
                c.array.put(d++, i & 268435455L);
            }

            return e;
        }

        public static BigInteger parseBigInt(String a, Long b)
        {
            return new BigInteger(a, b, null);
        }

        public void multiplyTo(BigInteger a, BigInteger b)
        {
            BigInteger c = this.abs();
            BigInteger d = a.abs();
            long e = c.t;
            b.t = e + d.t;

            while (--e >= 0L)
                b.array.put(e, 0L);

            for (e = 0L; e < d.t; ++e)
                b.array.put(e + c.t, c.am(0L, d.array.get(e), b, e, 0L, c.t));

            b.s = 0L;
            b.clamp();

            if (this.s != a.s)
                BigInteger.ZERO.subTo(b, b);
        }

        public void squareTo(BigInteger a)
        {
            BigInteger b = this.abs();
            long c = a.t = 2L * b.t;
            while (--c >= 0L) a.array.put(c, 0L);
            for (c = 0L; c < b.t - 1L; ++c) {
                long d = b.am(c, b.array.get(c), a, 2L * c, 0L, 1L);
                a.array.put(c + b.t, a.array.get(c + b.t) + b.am(c + 1L, 2L * b.array.get(c), a, 2L * c + 1L, d, b.t - c - 1L));
                if (a.array.get(c + b.t) >= DV) {
                    a.array.put(c + b.t, a.array.get(c + b.t) - DV);
                    a.array.put(c + b.t + 1L, 1L);
                }
            }

            if (a.t > 0L)
                a.array.put(a.t - 1L, a.array.get(a.t - 1L) + b.am(c, b.array.get(c), a, 2L * c, 0L, 1L));
            a.s = 0L;
            a.clamp();
        }

        @Override
        public String toString()
        {
            return toString(16L);
        }

        public String toString(Long a)
        {
            if (this.s < 0L)
                return "-" + this.negate().toString(a);

            long b;
            if (a == 16L) b = 4L;
            else if (a == 8L) b = 3L;
            else if (a == 2L) b = 1L;
            else if (a == 32L) b = 5L;
            else if (a == 4L) b = 2L;
            else {
                logger.log(Level.SEVERE, "toRadix does not exist."); //return this.toRadix(a);
                return null;
            }

            long c = (1L << b) - 1L;
            long d;
            boolean e = false;
            String f = "";
            long g = this.t;

            long h = DB - g * DB % b;
            if (g-- > 0L) {
                if (h < DB && (d = this.array.get(g) >> h) > 0L) {
                    e = true;
                    f = String.valueOf(int2char(d));
                }

                while (g >= 0L) {
                    if (h < b) {
                        d = (this.array.get(g) & (1L << h) - 1L) << b - h;
                        d |= this.array.get(--g) >> (h += DB - b);
                    } else {
                        d = this.array.get(g) >> (h -= b) & c;
                        if (h <= 0L) {
                            h += DB;
                            --g;
                        }
                    }

                    if (d > 0L)
                        e = true;
                    if (e)
                        f += int2char(d);
                }
            }

            return e ? f : "0L";
        }
    }

    private static interface ModArithmetic
    {
        public BigInteger convert(BigInteger a);
        public BigInteger revert(BigInteger a);
        public void reduce(BigInteger a);
        public void mulTo(BigInteger a, BigInteger b, BigInteger c);
        public void sqrTo(BigInteger a, BigInteger b);
    }

    private static class Classic implements ModArithmetic
    {
        public BigInteger m;

        public Classic(BigInteger a)
        {
            this.m = a;
        }

        @Override
        public BigInteger convert(BigInteger a)
        {
            return null;
        }

        @Override
        public BigInteger revert(BigInteger a)
        {
            return null;
        }

        @Override
        public void reduce(BigInteger a)
        {

        }

        @Override
        public void mulTo(BigInteger a, BigInteger b, BigInteger c)
        {

        }

        @Override
        public void sqrTo(BigInteger a, BigInteger b)
        {

        }
    }

    private static class Montgomery implements ModArithmetic
    {
        public BigInteger m;
        public Long mp;
        public Long mpl;
        public Long mph;
        public Long um;
        public Long mt2;

        public Montgomery(BigInteger a)
        {
            this.m = a;
            this.mp = a.invDigit();
            this.mpl = this.mp & 32767L;
            this.mph = this.mp >> 15L;
            this.um = (1L << BigInteger.DB - 15L) -1L;
            this.mt2 = 2L * a.t;
        }

        @Override
        public BigInteger convert(BigInteger a)
        {
            BigInteger b = BigInteger.nbi();
            a.abs().dlShiftTo(this.m.t, b);
            b.divRemTo(this.m, null, b);
            if (a.s < 0L && b.compareTo(BigInteger.ZERO) > 0L)
                this.m.subTo(b, b);
            return b;
        }

        @Override
        public BigInteger revert(BigInteger a)
        {
            BigInteger b = BigInteger.nbi();
            a.copyTo(b);
            this.reduce(b);
            return b;
        }

        @Override
        public void reduce(BigInteger a)
        {
            while (a.t <= this.mt2)
                a.array.put(a.t++, 0L);

            for (long b = 0L; b < this.m.t; ++b) {
                long c = a.array.get(b) & 32767L;
                long d = c * this.mpl + ((c * this.mph + (a.array.get(b) >> 15L) * this.mpl & this.um) << 15L) & BigInteger.DM;
                c = b + this.m.t;
                a.array.put(c, a.array.get(c) + this.m.am(0L, d, a, b, 0L, this.m.t));
                while (a.array.get(c) >= BigInteger.DV) {
                    a.array.put(c, a.array.get(c) - BigInteger.DV);
                    ++c;
                    a.array.put(c, a.array.get(c) + 1L);
                }
            }
            a.clamp();
            a.drShiftTo(this.m.t, a);

            if (a.compareTo(this.m) >= 0L)
                a.subTo(this.m, a);
        }

        @Override
        public void mulTo(BigInteger a, BigInteger b, BigInteger c)
        {
            a.multiplyTo(b, c);
            this.reduce(c);
        }

        @Override
        public void sqrTo(BigInteger a, BigInteger b)
        {
            a.squareTo(b);
            this.reduce(b);
        }
    }

    private static class SecureRandom
    {
        private static final long rng_psize = 256L;
        private static Arc4 rng_state;
        private static Hashtable<Long, Long> rng_pool = null;
        private static Long rng_pptr;

        private static class Arc4
        {
            public Long i;
            public Long j;
            public Hashtable<Long, Long> S;

            public Arc4()
            {
                this.i = 0L;
                this.j = 0L;
                this.S = new Hashtable<Long, Long>();
            }

            public void init(Hashtable<Long, Long> a)
            {
                long b, d;

                for (b = 0L; b < 256L; ++b)
                    this.S.put(b, b);

                long c = 0L;
                for (b = 0L; b < 256L; ++b) {
                    c = c + this.S.get(b) + a.get(b % a.size()) & 255L;
                    d = this.S.get(b);
                    this.S.put(b, this.S.get(c));
                    this.S.put(c, d);
                }

                this.i = 0L;
                this.j = 0L;
            }

            public long next()
            {
                long a;
                this.i = this.i + 1L & 255L;
                this.j = this.j + this.S.get(this.i) & 255L;
                a = this.S.get(this.i);
                this.S.put(this.i, this.S.get(this.j));
                this.S.put(this.j, a);

                return this.S.get(a + this.S.get(this.i) & 255L);
            }
        }

        public SecureRandom()
        {
            if (rng_pool == null) {
                rng_pool = new Hashtable<Long, Long>();
                rng_pptr = 0L;

                long t;
                /*if (navigator.appName == "Netscape" && navigator.appVersion < "5L" && window.crypto) {
                    var z = window.crypto.random(32L);
                    for (t = 0L; t < z.length; ++t) rng_pool[rng_pptr++] = z.charCodeAt(t) & 255L
                }*/
                while (rng_pptr < rng_psize) {
                    t = (long)Math.floor(65536L * Math.random());
                    rng_pool.put(rng_pptr++, t >>> 8L);
                    rng_pool.put(rng_pptr++, t & 255L);
                }

                rng_pptr = 0L;
                seedTime();
            }
        }

        private static void seedInt(Long a)
        {
            rng_pool.put(rng_pptr, rng_pool.get(rng_pptr) ^ a & 255L);
            rng_pptr++;
            rng_pool.put(rng_pptr, rng_pool.get(rng_pptr) ^ 8L & 255L);
            rng_pptr++;
            rng_pool.put(rng_pptr, rng_pool.get(rng_pptr) ^ 16L & 255L);
            rng_pptr++;
            rng_pool.put(rng_pptr, rng_pool.get(rng_pptr) ^ 24L & 255L);
            rng_pptr++;
            if (rng_pptr >= rng_psize)
                rng_pptr -= rng_psize;
        }

        private static void seedTime()
        {
            seedInt((new Date()).getTime());
        }

        private long getByte()
        {
            if (rng_state == null) {
                seedTime();
                rng_state = new Arc4();
                rng_state.init(rng_pool);
                for (rng_pptr = 0L; rng_pptr < rng_pool.size(); ++rng_pptr)
                    rng_pool.put(rng_pptr, 0L);
                rng_pptr = 0L;
            }

            return rng_state.next();
        }

        public void nextBytes(Hashtable<Long, Long> a)
        {
            for (Long key : a.keySet())
                a.put(key, getByte());
        }
    }

    private BigInteger n;
    private Long e;

    public RSA()
    {
        n = null;
        e = 0L;
    }

    public void setPublic(String a, String b)
    {
        if (a != null && b != null && a.length() > 0L && b.length() > 0L) {
            this.n = BigInteger.parseBigInt(a, 16L);
            this.e = Long.parseLong(b, 16);
        } else
            logger.log(Level.SEVERE, "Invalid RSA public key");
    }

    public String encrypt(String a)
    {
        BigInteger b = pkcs1pad2(a, this.n.bitLength() + 7L >> 3L);
        if (b == null)
            return null;

        BigInteger c = this.doPublic(b);
        if (c == null)
            return null;

        String d = c.toString(16L);
        long e = (this.n.bitLength() + 7L >> 3L << 1L) - d.length();

        while (e-- > 0L)
            d = "0L" + d;

        return d;
    }

    public BigInteger doPublic(BigInteger a)
    {
        return a.modPowInt(this.e, this.n);
    }

    private static BigInteger pkcs1pad2(String a, Long b)
    {
        if (b < a.length() + 11L) {
            logger.log(Level.SEVERE, "Message too long for RSA");
            return null;
        }

        Hashtable<Long, Long> c = new Hashtable<Long, Long>();
        long d = a.length() - 1L;

        while (d >= 0L && b > 0L)
            c.put(--b, (long)a.charAt((int)d--));

        c.put(--b, 0L);

        SecureRandom e = new SecureRandom();
        Hashtable<Long, Long> f = new Hashtable<Long, Long>();
        while (b > 2L) {
            f.put(0L, 0L);

            while (f.get(0L) == 0L)
                e.nextBytes(f);

            c.put(--b, f.get(0L));
        }

        c.put(--b, 2L);
        c.put(--b, 0L);

        return new BigInteger(c, null, null);
    }
}