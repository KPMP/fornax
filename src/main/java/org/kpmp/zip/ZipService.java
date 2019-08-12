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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZipService implements ApplicationRunner {

	private static final String TMP_FILE_EXTENSION = ".tmp";
	private static final Log log = LogFactory.getLog(ZipService.class);

	public static void main(String... args) {
		SpringApplication.run(ZipService.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		log.info("ZipService Arguments: " + Arrays.toString(args.getSourceArgs()));

		List<String> filePaths = args.getOptionValues("zip.fileNames");
		if (filePaths == null || filePaths.size() <= 0) {
			log.error("ERROR: Missing --zip.fileNames.  No files to zip.");
			throw new IllegalArgumentException("No files to zip");
		}

		List<String> zipFilePath = args.getOptionValues("zip.zipFilePath");
		if (zipFilePath == null || zipFilePath.size() <= 0) {
			log.error("ERROR: Missing --zip.zipFilePath.  Missing zip filename to create");
			throw new IllegalArgumentException("Missing zip file name and path");
		}

		List<String> additionalFileDatas = args.getOptionValues("zip.additionalFileData");
		Map<String, String> additionalFileInformation = new HashMap<String, String>();
		if (additionalFileDatas != null) {
			for (String additionalFileData : additionalFileDatas) {
				if (!additionalFileData.contains("|")) {
					log.error(
							"ERROR: --zip.additionalFileData malformed.  Argument should be in form \"filename|data for file\"");
					throw new IllegalArgumentException("Missing filename in additional data");
				} else {
					String[] fileParts = additionalFileData.split("\\|");
					additionalFileInformation.put(fileParts[0], fileParts[1]);
				}
			}
		}

		try {
			zipFiles(zipFilePath.get(0), filePaths, additionalFileInformation);
		} catch (IOException e) {
			log.error("ERROR: " + e.getMessage());
			throw e;
		}

	}

	void zipFiles(String zipFilePath, List<String> filePaths, Map<String, String> additionalFileInformation)
			throws IOException {
		createZipFile(zipFilePath, filePaths, additionalFileInformation);
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