package com.freedomotic.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class UnzipTests {

	@Test 
	public void validUnzip() throws Exception {
		String testFile = getClass().getResource("hello.zip").getFile();
		File helloTest = new File(new File(testFile).getParentFile(), "hello.txt");
		assertEquals(false, helloTest.exists());
		helloTest.deleteOnExit();
		Unzip.unzip(testFile);
		assertEquals(true, helloTest.exists());
	}
	
}
