package org.kpmp.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipServiceTest extends ZipService {

	private ZipService service;

	@Before
	public void setUp() throws Exception {
		service = new ZipService();
	}

	@After
	public void tearDown() throws Exception {
		service = null;
	}

	@Test
	public void testZipFiles() throws IOException {
		Path parentDirectory = Files.createTempDirectory("234");
		File attachment1Path = File.createTempFile("fornax", ".txt", parentDirectory.toFile());
		attachment1Path.deleteOnExit();

		HashMap<String, String> additionalFileInformation = new HashMap<String, String>();
		additionalFileInformation.put("anotherFile.txt", "with some data");

		String zipFileLocation = parentDirectory.toString() + File.separator + "testZipFile.zip";
		service.zipFiles(zipFileLocation, Arrays.asList(attachment1Path.getAbsolutePath()), additionalFileInformation);

		File zipFile = new File(zipFileLocation);
		assertEquals(true, zipFile.exists());
		ZipFile zip = new ZipFile(zipFileLocation);
		assertEquals(2, zip.size());
		Enumeration<? extends ZipEntry> entries = zip.entries();
		List<String> filenames = new ArrayList<>();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			filenames.add(entry.getName());
		}

		assertEquals(true, filenames.contains("anotherFile.txt"));
		assertEquals(true, filenames.contains(attachment1Path.getName()));
		zip.close();
	}

	@Test
	public void testZipFiles_withNoAdditionaFiles() throws IOException {
		Path parentDirectory = Files.createTempDirectory("234");
		File attachment1Path = File.createTempFile("fornax", ".txt", parentDirectory.toFile());
		attachment1Path.deleteOnExit();

		HashMap<String, String> additionalFileInformation = new HashMap<String, String>();

		String zipFileLocation = parentDirectory.toString() + File.separator + "testZipFile.zip";
		service.zipFiles(zipFileLocation, Arrays.asList(attachment1Path.getAbsolutePath()), additionalFileInformation);

		File zipFile = new File(zipFileLocation);
		assertEquals(true, zipFile.exists());
		ZipFile zip = new ZipFile(zipFileLocation);
		assertEquals(1, zip.size());
		Enumeration<? extends ZipEntry> entries = zip.entries();
		List<String> filenames = new ArrayList<>();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			filenames.add(entry.getName());
		}

		assertEquals(false, filenames.contains("anotherFile.txt"));
		assertEquals(true, filenames.contains(attachment1Path.getName()));
		zip.close();
	}

	@Test
	public void testMain_whenAllArgumentsPresent() throws Exception {
		Path parentDirectory = Files.createTempDirectory("234");
		File attachment1Path = File.createTempFile("fornax", ".txt", parentDirectory.toFile());
		attachment1Path.deleteOnExit();
		String zipFileLocation = parentDirectory.toString() + File.separator + "testZipFile.zip";

		try {
			ZipService.main("--zip.zipFilePath=" + zipFileLocation, "--zip.additionalFileData=filename|data",
					"--zip.fileNames=" + attachment1Path.getAbsolutePath());

			File zipFile = new File(zipFileLocation);
			assertEquals(true, zipFile.exists());
			ZipFile zip = new ZipFile(zipFileLocation);
			assertEquals(2, zip.size());
			Enumeration<? extends ZipEntry> entries = zip.entries();
			List<String> filenames = new ArrayList<>();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				filenames.add(entry.getName());
			}

			assertEquals(true, filenames.contains("filename"));
			assertEquals(true, filenames.contains(attachment1Path.getName()));
			zip.close();
		} catch (Exception e) {
			fail("Should have zipped successfully");
		}
	}

	@Test
	public void testMain_whenNoAdditionalFileData() throws Exception {
		Path parentDirectory = Files.createTempDirectory("234");
		File attachment1Path = File.createTempFile("fornax", ".txt", parentDirectory.toFile());
		attachment1Path.deleteOnExit();
		String zipFileLocation = parentDirectory.toString() + File.separator + "testZipFile.zip";

		try {
			ZipService.main("--zip.zipFilePath=" + zipFileLocation,
					"--zip.fileNames=" + attachment1Path.getAbsolutePath());

			File zipFile = new File(zipFileLocation);
			assertEquals(true, zipFile.exists());
			ZipFile zip = new ZipFile(zipFileLocation);
			assertEquals(1, zip.size());
			Enumeration<? extends ZipEntry> entries = zip.entries();
			List<String> filenames = new ArrayList<>();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				filenames.add(entry.getName());
			}

			assertEquals(true, filenames.contains(attachment1Path.getName()));
			zip.close();
		} catch (Exception e) {
			fail("Should have zipped successfully");
		}
	}

	@Test
	public void testMain_whenMissingZipFileName() throws Exception {
		try {
			ZipService.main("--zip.fileNames=filename1.txt", "--zip.additionalFileData=filename|data");
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception expected) {
			assertEquals("Failed to execute ApplicationRunner", expected.getMessage());
			Throwable cause = expected.getCause();
			assertEquals("Missing zip file name and path", cause.getMessage());
		}
	}

	@Test
	public void testMain_whenMissingFilesToZip() throws Exception {
		try {
			ZipService.main("--zip.zipFilePath=filename1.txt", "--zip.additionalFileData=filename|data");
			fail("Should have thrown IllegalArgumentException");
		} catch (Exception expected) {
			assertEquals("Failed to execute ApplicationRunner", expected.getMessage());
			Throwable cause = expected.getCause();
			assertEquals("No files to zip", cause.getMessage());
		}
	}

}
