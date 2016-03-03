/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2016 
 *******************************************************************************/

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.03.05 at 01:29:11 PM CET 
//

package org.oscm.billingservice.business.model.billingresult;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.oscm.billingservice.business.BigDecimalAdapter;

/**
 * Discount.
 * 
 * <p>
 * Java class for DiscountType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DiscountType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="percent" use="required" type="{http://www.w3.org/2001/XMLSchema}float" />
 *       &lt;attribute name="discountNetAmount" use="required" type="{}NormalizedCosts" />
 *       &lt;attribute name="netAmountAfterDiscount" use="required" type="{}NormalizedCosts" />
 *       &lt;attribute name="netAmountBeforeDiscount" use="required" type="{}NormalizedCosts" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiscountType")
public class DiscountType {

    @XmlAttribute(required = true)
    protected float percent;

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal discountNetAmount;

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal netAmountAfterDiscount;

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(BigDecimalAdapter.class)
    protected BigDecimal netAmountBeforeDiscount;

    /**
     * Gets the value of the percent property.
     * 
     */
    public float getPercent() {
        return percent;
    }

    /**
     * Sets the value of the percent property.
     * 
     */
    public void setPercent(float value) {
        this.percent = value;
    }

    /**
     * Gets the value of the discountNetAmount property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getDiscountNetAmount() {
        return discountNetAmount;
    }

    /**
     * Sets the value of the discountNetAmount property.
     * 
     * @param value
     *            allowed object is {@link BigDecimal }
     * 
     */
    public void setDiscountNetAmount(BigDecimal value) {
        this.discountNetAmount = value;
    }

    /**
     * Gets the value of the netAmountAfterDiscount property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getNetAmountAfterDiscount() {
        return netAmountAfterDiscount;
    }

    /**
     * Sets the value of the netAmountAfterDiscount property.
     * 
     * @param value
     *            allowed object is {@link BigDecimal }
     * 
     */
    public void setNetAmountAfterDiscount(BigDecimal value) {
        this.netAmountAfterDiscount = value;
    }

    /**
     * Gets the value of the netAmountBeforeDiscount property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getNetAmountBeforeDiscount() {
        return netAmountBeforeDiscount;
    }

    /**
     * Sets the value of the netAmountBeforeDiscount property.
     * 
     * @param value
     *            allowed object is {@link BigDecimal }
     * 
     */
    public void setNetAmountBeforeDiscount(BigDecimal value) {
        this.netAmountBeforeDiscount = value;
    }

}
