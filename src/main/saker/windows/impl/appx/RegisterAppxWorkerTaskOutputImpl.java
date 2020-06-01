package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.windows.api.appx.RegisterAppxWorkerTaskOutput;

final class RegisterAppxWorkerTaskOutputImpl implements Externalizable, RegisterAppxWorkerTaskOutput {
	private static final long serialVersionUID = 1L;

	private SakerPath appxManifestLocalPath;

	/**
	 * For {@link Externalizable}.
	 */
	public RegisterAppxWorkerTaskOutputImpl() {
	}

	public RegisterAppxWorkerTaskOutputImpl(SakerPath appxManifestLocalPath) {
		this.appxManifestLocalPath = appxManifestLocalPath;
	}

	@Override
	public SakerPath getAppxManifestLocalPath() {
		return appxManifestLocalPath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(appxManifestLocalPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		appxManifestLocalPath = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appxManifestLocalPath == null) ? 0 : appxManifestLocalPath.hashCode());
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
		RegisterAppxWorkerTaskOutputImpl other = (RegisterAppxWorkerTaskOutputImpl) obj;
		if (appxManifestLocalPath == null) {
			if (other.appxManifestLocalPath != null)
				return false;
		} else if (!appxManifestLocalPath.equals(other.appxManifestLocalPath))
			return false;
		return true;
	}

}