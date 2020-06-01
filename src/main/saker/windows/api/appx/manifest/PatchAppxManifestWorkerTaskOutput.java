package saker.windows.api.appx.manifest;

import saker.build.file.path.SakerPath;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;

public interface PatchAppxManifestWorkerTaskOutput {
	public SakerPath getPath();

	//for auto-conversion
	public default SakerPath toSakerPath() {
		return getPath();
	}

	//for auto-conversion
	public default FileLocation toFileLocation() {
		return ExecutionFileLocation.create(getPath());
	}
}