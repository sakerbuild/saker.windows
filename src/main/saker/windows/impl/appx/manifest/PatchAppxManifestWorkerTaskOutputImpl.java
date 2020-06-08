package saker.windows.impl.appx.manifest;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.windows.api.appx.manifest.PatchAppxManifestWorkerTaskOutput;

final class PatchAppxManifestWorkerTaskOutputImpl implements Externalizable, PatchAppxManifestWorkerTaskOutput {
	private static final long serialVersionUID = 1L;

	private SakerPath outputPath;

	/**
	 * For {@link Externalizable}.
	 */
	public PatchAppxManifestWorkerTaskOutputImpl() {
	}

	public PatchAppxManifestWorkerTaskOutputImpl(SakerPath outputPath) {
		this.outputPath = outputPath;
	}

	//for auto-conversion
	public SakerPath toSakerPath() {
		return getPath();
	}

	//for auto-conversion
	public FileLocation toFileLocation() {
		return ExecutionFileLocation.create(getPath());
	}

	@Override
	public SakerPath getPath() {
		return outputPath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputPath = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputPath == null) ? 0 : outputPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatchAppxManifestWorkerTaskOutputImpl other = (PatchAppxManifestWorkerTaskOutputImpl) obj;
		if (outputPath == null) {
			if (other.outputPath != null)
				return false;
		} else if (!outputPath.equals(other.outputPath))
			return false;
		return true;
	}

}