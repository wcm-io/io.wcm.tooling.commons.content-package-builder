/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.tooling.commons.contentpackagebuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValueConverterTest {

  private ValueConverter underTest;
  private Date sampleDate;

  @BeforeEach
  void setUp() throws Exception {
    underTest = new ValueConverter();
    sampleDate = DateUtils.parseDate("05.09.2010 15:10:20", "dd.MM.yyyy HH:mm:ss");
  }

  @Test
  void testNull() {
    assertEquals("", underTest.toString("prop", null));
  }

  @Test
  void testInvalid() {
    assertThrows(IllegalArgumentException.class, () -> {
      underTest.toString("prop", new Object());
    });
  }

  @Test
  void testString() {
    assertEquals("myString", underTest.toString("prop", "myString"));
    assertEquals("myString [ ] { } \\\\ ,", underTest.toString("prop", "myString [ ] { } \\ ,"));
    assertEquals("\\{myString}", underTest.toString("prop", "{myString}"));
    assertEquals("aaa{myString}", underTest.toString("prop", "aaa{myString}"));
    assertEquals("\\[myString]", underTest.toString("prop", "[myString]"));
    assertEquals("aaa[myString]", underTest.toString("prop", "aaa[myString]"));
  }

  @Test
  void testStringArray() {
    assertEquals("[myString1,myString2]", underTest.toString("prop", new String[] {
        "myString1", "myString2"
    }));
    assertEquals("[myString1\\,[]\\\\äöüß€,myString2,myString3 [ ] { } \\\\ \\,,,[myString5],{myString6}]", underTest.toString("prop", new String[] {
        "myString1,[]\\äöüß€", "myString2", "myString3 [ ] { } \\ ,", "", "[myString5]", "{myString6}"
    }));
  }

  @Test
  void testBoolean() {
    assertEquals("{Boolean}true", underTest.toString("prop", true));
    assertEquals("{Boolean}true", underTest.toString("prop", Boolean.TRUE));
  }

  @Test
  void testBooleanArray() {
    assertEquals("{Boolean}[true,false]", underTest.toString("prop", new boolean[] {
        true, false
    }));
    assertEquals("{Boolean}[true,false]", underTest.toString("prop", new Boolean[] {
        Boolean.TRUE, Boolean.FALSE
    }));
  }

  @Test
  void testInteger() {
    assertEquals("{Long}1", underTest.toString("prop", 1));
    assertEquals("{Long}2", underTest.toString("prop", Integer.valueOf(2)));
  }

  @Test
  void testIntegerArray() {
    assertEquals("{Long}[1,2]", underTest.toString("prop", new int[] {
        1, 2
    }));
    assertEquals("{Long}[1,2]", underTest.toString("prop", new Integer[] {
        Integer.valueOf(1), Integer.valueOf(2)
    }));
  }

  @Test
  void testLong() {
    assertEquals("{Long}10000000000", underTest.toString("prop", 10000000000L));
    assertEquals("{Long}20000000000", underTest.toString("prop", Long.valueOf(20000000000L)));
  }

  @Test
  void testLongArray() {
    assertEquals("{Long}[10000000000,20000000000]", underTest.toString("prop", new long[] {
        10000000000L, 20000000000L
    }));
    assertEquals("{Long}[10000000000,20000000000]", underTest.toString("prop", new Long[] {
        Long.valueOf(10000000000L), Long.valueOf(20000000000L)
    }));
  }

  @Test
  void testDouble() {
    assertEquals("{Decimal}1.234", underTest.toString("prop", 1.234d));
    assertEquals("{Decimal}2.345", underTest.toString("prop", Double.valueOf(2.345d)));
  }

  @Test
  void testDoubleArray() {
    assertEquals("{Decimal}[1.234,2.345]", underTest.toString("prop", new double[] {
        1.234d, 2.345d
    }));
    assertEquals("{Decimal}[1.234,2.345]", underTest.toString("prop", new Double[] {
        Double.valueOf(1.234d), Double.valueOf(2.345d)
    }));
  }

  @Test
  void testBigDecimal() {
    assertEquals("{Decimal}2.345", underTest.toString("prop", new BigDecimal("2.345")));
  }

  @Test
  void testBigDecimalArray() {
    assertEquals("{Decimal}[1.234,2.345]", underTest.toString("prop", new BigDecimal[] {
        new BigDecimal("1.234"), new BigDecimal("2.345")
    }));
  }

  @Test
  void testDate() {
    assertTrue(StringUtils.startsWith(underTest.toString("prop", sampleDate), "{Date}2010-09-05T15:10:20"));
  }

  @Test
  void testCalendar() {
    assertTrue(StringUtils.startsWith(underTest.toString("prop", DateUtils.toCalendar(sampleDate)), "{Date}2010-09-05T15:10:20"));
  }

  @Test
  void testStringArrayRepPrivileges() {
    assertEquals("{Name}[rep:write,crx:replicate,jcr:read]", underTest.toString("rep:privileges", new String[] {
        "rep:write", "crx:replicate", "jcr:read"
    }));
  }

  @Test
  void testUUID() {
    UUID uuid = UUID.randomUUID();
    assertEquals("{Reference}" + uuid.toString(), underTest.toString("prop", uuid));
  }

  @Test
  void testURI() throws Exception {
    URI uri = new URI("http://localhost");
    assertEquals("{URI}" + uri.toString(), underTest.toString("prop", uri));
  }

}
