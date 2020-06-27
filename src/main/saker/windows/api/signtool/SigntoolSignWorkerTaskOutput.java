package saker.windows.api.signtool;

import saker.build.file.path.SakerPath;

/**
 * Output of the signtool signer task.
 */
public interface SigntoolSignWorkerTaskOutput {
	/**
	 * Gets the path to the signed file.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();
}
