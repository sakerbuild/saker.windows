package saker.windows.api.appx;

import saker.build.file.path.SakerPath;

/**
 * Output of the appx registration task.
 */
public interface RegisterAppxWorkerTaskOutput {
	/**
	 * Gets the local filesystem path of the AppxManifest.xml file of the package.
	 * 
	 * @return The absolute local filesystem path.
	 */
	public SakerPath getAppxManifestLocalPath();
}