package driverway.nb.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * Remove old Satellite images as USB stick is small
 */
public class SatImageRemover extends SimpleFileVisitor<Path> {

	private NumberFormat myFormat = NumberFormat.getInstance();
	private Instant oldest;
	private static Logger LOGGER = LogManager.getLogger();

	public SatImageRemover(int limit) {
		oldest = Instant.now().minus(limit, ChronoUnit.DAYS);
		LOGGER.info("Oldest image to keep "+oldest.toString());
	}

	@Override
	public FileVisitResult visitFile(Path filePath, BasicFileAttributes attr) {

		if (attr.creationTime().toInstant().isBefore(oldest)) {
			try {
				File f = filePath.toFile();
				LOGGER.info("deleting " + f.getName());
				/////f.delete();
			} catch (Exception e) {
				LOGGER.error("unable to remove file " + e.getMessage());
			}
		}

		return CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
		return CONTINUE;
	}

	// Print each directory visited.
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		return CONTINUE;

	}

	// Handle failures
	@Override
	public FileVisitResult visitFileFailed(Path dir, IOException exc) {
		return CONTINUE;
	}

}
