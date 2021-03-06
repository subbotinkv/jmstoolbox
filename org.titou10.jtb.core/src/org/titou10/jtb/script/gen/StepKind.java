
package org.titou10.jtb.script.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stepKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="stepKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="PAUSE"/&gt;
 *     &lt;enumeration value="REGULAR"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "stepKind")
@XmlEnum
public enum StepKind {

    PAUSE,
    REGULAR;

    public String value() {
        return name();
    }

    public static StepKind fromValue(String v) {
        return valueOf(v);
    }

}
