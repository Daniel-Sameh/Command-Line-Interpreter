package org.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestTest {
    @Test
    void onePlusTwo(){
        var adder = new org.os.Test();
        assertEquals(adder.getSum(2,2),4);

    }
}