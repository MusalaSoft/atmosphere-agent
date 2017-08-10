package com.musala.atmosphere.agent.devicewrapper.util;


import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests a buffer with the logCat log.
 * 
 * @author dimcho.nedev
 *
 */
public class BufferTest {
    private static final int WAIT_UNTIL_CONDITION_TIMEOUT = 100;
    
    @Test
    public void variousTimeoutsAndSizesTest() throws InterruptedException {
        testBuffer(0, 10, 100_000);

        testBuffer(0, 0, 100_000);

        testBuffer(0, 0, 3000);

        testBuffer(10, 5, 35);

        testBuffer(10, 5, 35);

        testBuffer(0, 10, 35);

        testBuffer(10, 0, 35);

        testBuffer(0, 0, 2500);
    }
    
    private void testBuffer(final int add, final int get, final int size) throws InterruptedException {
        List<String> expectedResult = new ArrayList<String>();
        List<String> actualResult = new ArrayList<String>();
        
        for (int i = 0; i < size; i++) {
            expectedResult.add(String.valueOf(i));
        }
        
        Buffer<String> buffer = new Buffer<String>(WAIT_UNTIL_CONDITION_TIMEOUT);

        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < size; i++) {
                    buffer.addValue(String.valueOf(i));
                    try {
                        Thread.sleep(add);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        
        int sum = 0;
        
        while (true) {
            List<String> current = buffer.getBuffer();
            actualResult.addAll(current);
            final int currentSize = current.size();
            sum += currentSize;     
            
            if (actualResult.size() == size) {
                System.out.println(currentSize);
                break;
            }
            
            System.out.println(currentSize + "+");
            Assert.assertNotEquals(0, currentSize);

            Thread.sleep(get);
        }
        
        Assert.assertEquals(expectedResult, actualResult);
        Assert.assertEquals(0, buffer.size());
        Assert.assertEquals(sum, size);
        System.out.println("sum: " + sum);
    }
 
}
