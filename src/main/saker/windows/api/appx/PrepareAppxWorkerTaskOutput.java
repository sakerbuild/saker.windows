package saker.windows.api.appx;

import java.util.NavigableMap;

import saker.build.file.path.SakerPath;

/**
 * Output of the appx preparation task.
 */
public interface PrepareAppxWorkerTaskOutput {
	/**
	 * Gets the path to the output application directory.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getAppxDirectory();

	//relative bundle paths to absolute execution paths
	//files only
	/**
	 * Gets the application file content mappings.
	 * <p>
	 * The returned map has relative path keys which specify their location in the application bundle. The associated
	 * values are the absolute execution paths where the files reside in the build system.
	 * <p>
	 * The map contains entries only for files, not for directories.
	 * 
	 * @return An unmodifiable map.
	 */
	public NavigableMap<SakerPath, SakerPath> getMappings();
}