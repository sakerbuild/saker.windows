package saker.windows.api.appx.manifest;

import saker.build.file.path.SakerPath;

/**
 * Output of the AppxManifest.xml patching task.
 */
public interface PatchAppxManifestWorkerTaskOutput {
	/**
	 * Gets the path of the output patched manifest.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();
}