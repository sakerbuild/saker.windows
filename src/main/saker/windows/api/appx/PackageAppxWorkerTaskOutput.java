package saker.windows.api.appx;

import saker.build.file.path.SakerPath;

/**
 * Output of the .appx packager task.
 */
public interface PackageAppxWorkerTaskOutput {
	/**
	 * Gets the output path of the .appx archive.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();
}
