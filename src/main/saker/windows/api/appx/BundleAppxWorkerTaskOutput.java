package saker.windows.api.appx;

import saker.build.file.path.SakerPath;

/**
 * Output of the .appxbundle packager task.
 */
public interface BundleAppxWorkerTaskOutput {
	/**
	 * Gets the output path of the .appxbundle archive.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();
}
