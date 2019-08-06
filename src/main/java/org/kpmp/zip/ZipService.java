package org.kpmp.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class ZipService {

	private static final String TMP_FILE_EXTENSION = ".tmp";

	public static void main(String[] args) {
		// get the args
		// create a new ZipService object

		List<String> files = Arrays.asList("/Users/rlreamy/temp/barcodes.tsv", "/Users/rlreamy/temp/genes.tsv");

		ZipService zipper = new ZipService();
		try {
			zipper.zipFiles("/Users/rlreamy/temp/ziptest/test.zip", files);
//			Map<String, String> additionalInfo = new HashMap<String, String>();
//			additionalInfo.put("metadata.json", "{here is the data}");
//			zipper.zipFiles("/Users/rlreamy/temp/ziptest/testAdditional.zip", files, additionalInfo);
			// log that the zip file was created (and timing?)
		} catch (IOException e) {
			// TODO Log the error
			System.err.println("Trouble zipping: " + e.getMessage());
		}
	}

	public void zipFiles(String zipFilePath, List<String> filePaths, Map<String, String> additionalFileInformation)
			throws IOException {
		createZipFile(zipFilePath, filePaths, additionalFileInformation);
	}

	public void zipFiles(String zipFilePath, List<String> filePaths) throws IOException {
		createZipFile(zipFilePath, filePaths, new HashMap<String, String>());
	}

	private void createZipFile(String zipFilePath, List<String> filePaths,
			Map<String, String> additionalFileInformation) throws IOException {
		File tempZipFileHandle = new File(zipFilePath + TMP_FILE_EXTENSION);
		try (ZipArchiveOutputStream zipFile = new ZipArchiveOutputStream(new File(zipFilePath))) {
			zipFile.setMethod(ZipArchiveOutputStream.DEFLATED);
			zipFile.setEncoding(StandardCharsets.UTF_8.name());
			for (String filePath : filePaths) {
				File file = new File(filePath);
				ZipArchiveEntry entry = new ZipArchiveEntry(file.getName());
				entry.setSize(file.length());
				zipFile.putArchiveEntry(entry);
				try (FileInputStream fileInputStream = new FileInputStream(file)) {
					try (BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
						byte[] buffer = new byte[32768];
						int data = 0;
						while ((data = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
							zipFile.write(buffer, 0, data);
						}
						zipFile.flush();
						bufferedInputStream.close();
					}
					fileInputStream.close();
				}
				zipFile.closeArchiveEntry();
			}

			Set<String> keys = additionalFileInformation.keySet();
			for (String additionalFileName : keys) {
				ZipArchiveEntry additionalEntry = new ZipArchiveEntry(additionalFileName);
				String additionaFileContents = additionalFileInformation.get(additionalFileName);
				additionalEntry.setSize(additionaFileContents.getBytes().length);
				zipFile.putArchiveEntry(additionalEntry);
				zipFile.write(additionaFileContents.getBytes(StandardCharsets.UTF_8));
				zipFile.closeArchiveEntry();
			}

			File zipFileHandle = new File(zipFilePath);
			tempZipFileHandle.renameTo(zipFileHandle);
		}
	}
}