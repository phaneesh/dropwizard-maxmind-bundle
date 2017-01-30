package io.dropwizard.maxmind.geoip2;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharactersTest {

    @Test
    public void testAsciiOnlyAsciiString(){
        String s = "example abcd";
        assertEquals(s, Characters.toAscii(s));
    }

    @Test
    public void testAsciiNullString(){
        assertEquals(null, Characters.toAscii(null));
    }

    @Test
    public void testAsciiWithNonAsciiCharacters(){
        assertEquals("orpsd", Characters.toAscii("orčpžsíáýd"));
    }

}