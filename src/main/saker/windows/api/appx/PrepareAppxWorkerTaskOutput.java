package saker.windows.api.appx;

import java.util.NavigableMap;

import saker.build.file.path.SakerPath;

public interface PrepareAppxWorkerTaskOutput {
	public SakerPath getAppxDirectory();

	//TODO this should be swapped. the relative paths should be mapped to absolutes
	//absolute execution paths to relative in the bundle
	public NavigableMap<SakerPath, SakerPath> getMappings();
}