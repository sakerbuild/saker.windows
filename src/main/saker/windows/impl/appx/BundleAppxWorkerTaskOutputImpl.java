package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.windows.api.appx.BundleAppxWorkerTaskOutput;

final class BundleAppxWorkerTaskOutputImpl implements BundleAppxWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath outputPath;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleAppxWorkerTaskOutputImpl() {
	}

	public BundleAppxWorkerTaskOutputImpl(SakerPath outputpath) {
		this.outputPath = outputpath;
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
		BundleAppxWorkerTaskOutputImpl other = (BundleAppxWorkerTaskOutputImpl) obj;
		if (outputPath == null) {
			if (other.outputPath != null)
				return false;
		} else if (!outputPath.equals(other.outputPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (outputPath != null ? "outputPath=" + outputPath : "") + "]";
	}

}