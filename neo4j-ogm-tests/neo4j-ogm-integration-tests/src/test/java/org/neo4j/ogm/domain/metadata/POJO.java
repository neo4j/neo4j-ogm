/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.metadata;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @author vince
 */
public class POJO<S, T extends Comparable, U extends Serializable> {

    // Strings
    private String x;
    private String[] xx;

    // primitives
    private short ps;
    private int pi;
    private long pl;
    private char pc;
    private byte pb;
    private double pd;
    private float pf;
    private boolean pz;

    // primitive arrays (
    private short[] pss;
    private int[] pii;
    private long[] pll;
    private char[] pcc;
    private byte[] pbb;
    private double[] pdd;
    private float[] pff;
    private boolean[] pzz;

    // autobox
    private Short s;
    private Integer i;
    private Long l;
    private Character c;
    private Byte b;
    private Double d;
    private Float f;
    private Boolean z;

    // untyped object
    private Object o;

    public Object[] getOo() {
        return oo;
    }

    public void setOo(Object[] oo) {
        this.oo = oo;
    }

    public Object getO() {
        return o;
    }

    public void setO(Object o) {
        this.o = o;
    }

    public boolean isPz() {
        return pz;
    }

    // autobox arrays
    private Short[] ss;
    private Integer[] ii;
    private Long[] ll;
    private Character[] cc;
    private Byte[] bb;
    private Double[] dd;
    private Float[] ff;
    private Boolean[] zz;

    // untyped object array
    private Object[] oo;

    // generic types
    private S genericObject;
    private T genericComparable;
    private U genericSerializable;

    // objects and collections of generic types
    private POJO<S, T, U> next;
    private List<S> elements;
    private List<POJO<S, T, U>> neighbours;

    // wildcards
    private List<? super Integer> subIntegers;
    private List<? extends Integer> superIntegers;
    private List<? super S> subS;
    private List<? extends S> superS;

    private List<?> listOfAnything;
    private Set<?> setOfAnything;
    private Vector<?> vectorOfAnything;

    private Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
    private Iterable<? extends Long> map;

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String[] getXx() {
        return xx;
    }

    public void setXx(String[] xx) {
        this.xx = xx;
    }

    public short getPs() {
        return ps;
    }

    public void setPs(short ps) {
        this.ps = ps;
    }

    public int getPi() {
        return pi;
    }

    public void setPi(int pi) {
        this.pi = pi;
    }

    public long getPl() {
        return pl;
    }

    public void setPl(long pl) {
        this.pl = pl;
    }

    public char getPc() {
        return pc;
    }

    public void setPc(char pc) {
        this.pc = pc;
    }

    public byte getPb() {
        return pb;
    }

    public void setPb(byte pb) {
        this.pb = pb;
    }

    public double getPd() {
        return pd;
    }

    public void setPd(double pd) {
        this.pd = pd;
    }

    public float getPf() {
        return pf;
    }

    public void setPf(float pf) {
        this.pf = pf;
    }

    public boolean getPz() {
        return pz;
    }

    public void setPz(boolean pz) {
        this.pz = pz;
    }

    public short[] getPss() {
        return pss;
    }

    public void setPss(short[] pss) {
        this.pss = pss;
    }

    public int[] getPii() {
        return pii;
    }

    public void setPii(int[] pii) {
        this.pii = pii;
    }

    public long[] getPll() {
        return pll;
    }

    public void setPll(long[] pll) {
        this.pll = pll;
    }

    public char[] getPcc() {
        return pcc;
    }

    public void setPcc(char[] pcc) {
        this.pcc = pcc;
    }

    public byte[] getPbb() {
        return pbb;
    }

    public void setPbb(byte[] pbb) {
        this.pbb = pbb;
    }

    public double[] getPdd() {
        return pdd;
    }

    public void setPdd(double[] pdd) {
        this.pdd = pdd;
    }

    public float[] getPff() {
        return pff;
    }

    public void setPff(float[] pff) {
        this.pff = pff;
    }

    public boolean[] getPzz() {
        return pzz;
    }

    public void setPzz(boolean[] pzz) {
        this.pzz = pzz;
    }

    public Short getS() {
        return s;
    }

    public void setS(Short s) {
        this.s = s;
    }

    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public Long getL() {
        return l;
    }

    public void setL(Long l) {
        this.l = l;
    }

    public Character getC() {
        return c;
    }

    public void setC(Character c) {
        this.c = c;
    }

    public Byte getB() {
        return b;
    }

    public void setB(Byte b) {
        this.b = b;
    }

    public Double getD() {
        return d;
    }

    public void setD(Double d) {
        this.d = d;
    }

    public Float getF() {
        return f;
    }

    public void setF(Float f) {
        this.f = f;
    }

    public Boolean getZ() {
        return z;
    }

    public void setZ(Boolean z) {
        this.z = z;
    }

    public Short[] getSs() {
        return ss;
    }

    public void setSs(Short[] ss) {
        this.ss = ss;
    }

    public Integer[] getIi() {
        return ii;
    }

    public void setIi(Integer[] ii) {
        this.ii = ii;
    }

    public Long[] getLl() {
        return ll;
    }

    public void setLl(Long[] ll) {
        this.ll = ll;
    }

    public Character[] getCc() {
        return cc;
    }

    public void setCc(Character[] cc) {
        this.cc = cc;
    }

    public Byte[] getBb() {
        return bb;
    }

    public void setBb(Byte[] bb) {
        this.bb = bb;
    }

    public Double[] getDd() {
        return dd;
    }

    public void setDd(Double[] dd) {
        this.dd = dd;
    }

    public Float[] getFf() {
        return ff;
    }

    public void setFf(Float[] ff) {
        this.ff = ff;
    }

    public Boolean[] getZz() {
        return zz;
    }

    public void setZz(Boolean[] zz) {
        this.zz = zz;
    }

    public S getGenericObject() {
        return genericObject;
    }

    public void setGenericObject(S genericObject) {
        this.genericObject = genericObject;
    }

    public T getGenericComparable() {
        return genericComparable;
    }

    public void setGenericComparable(T genericComparable) {
        this.genericComparable = genericComparable;
    }

    public U getGenericSerializable() {
        return genericSerializable;
    }

    public void setGenericSerializable(U genericSerializable) {
        this.genericSerializable = genericSerializable;
    }

    public POJO<S, T, U> getNext() {
        return next;
    }

    public void setNext(POJO<S, T, U> next) {
        this.next = next;
    }

    public List<S> getElements() {
        return elements;
    }

    public void setElements(List<S> elements) {
        this.elements = elements;
    }

    public List<POJO<S, T, U>> getNeighbours() {
        return neighbours;
    }

    public <X extends Long> Iterable<X> getMap() {
        return (Iterable<X>) map;
    }

    public void setMap(Iterable<? extends Long> map) {
        this.map = map;
    }

    public void setNeighbours(List<POJO<S, T, U>> neighbours) {
        this.neighbours = neighbours;
    }

    public List<? super Integer> getSubIntegers() {
        return subIntegers;
    }

    public void setSubIntegers(List<? super Integer> subIntegers) {
        this.subIntegers = subIntegers;
    }

    public List<? extends Integer> getSuperIntegers() {
        return superIntegers;
    }

    public void setSuperIntegers(List<? extends Integer> superIntegers) {
        this.superIntegers = superIntegers;
    }

    public List<? super S> getSubS() {
        return subS;
    }

    public void setSubS(List<? super S> subS) {
        this.subS = subS;
    }

    public List<? extends S> getSuperS() {
        return superS;
    }

    public void setSuperS(List<? extends S> superS) {
        this.superS = superS;
    }

    public List<?> getListOfAnything() {
        return listOfAnything;
    }

    public void setListOfAnything(List<?> listOfAnything) {
        this.listOfAnything = listOfAnything;
    }

    public Set<?> getSetOfAnything() {
        return setOfAnything;
    }

    public void setSetOfAnything(Set<?> setOfAnything) {
        this.setOfAnything = setOfAnything;
    }

    public Vector<?> getVectorOfAnything() {
        return vectorOfAnything;
    }

    public void setVectorOfAnything(Vector<?> vectorOfAnything) {
        this.vectorOfAnything = vectorOfAnything;
    }

    public Iterable<Map<Class<S>, POJO<S, T, U>>> getIterable() {
        return iterable;
    }

    public void setIterable(Iterable<Map<Class<S>, POJO<S, T, U>>> iterable) {
        this.iterable = iterable;
    }
}
