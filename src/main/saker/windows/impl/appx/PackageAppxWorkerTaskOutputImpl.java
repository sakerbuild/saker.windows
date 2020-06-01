package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.windows.api.appx.PackageAppxWorkerTaskOutput;

final class PackageAppxWorkerTaskOutputImpl implements PackageAppxWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public PackageAppxWorkerTaskOutputImpl() {
	}

	public PackageAppxWorkerTaskOutputImpl(SakerPath outputpath) {
		this.path = outputpath;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		PackageAppxWorkerTaskOutputImpl other = (PackageAppxWorkerTaskOutputImpl) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (path != null ? "path=" + path : "") + "]";
	}

}