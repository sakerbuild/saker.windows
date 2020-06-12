package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.windows.api.appx.PrepareAppxWorkerTaskOutput;

final class PrepareAppxWorkerTaskOutputImpl implements Externalizable, PrepareAppxWorkerTaskOutput {
	private static final long serialVersionUID = 1L;

	private SakerPath appxDirectory;

	private NavigableMap<SakerPath, SakerPath> mappings;

	/**
	 * For {@link Externalizable}.
	 */
	public PrepareAppxWorkerTaskOutputImpl() {
	}

	public PrepareAppxWorkerTaskOutputImpl(SakerPath appxDirectory, NavigableMap<SakerPath, SakerPath> mappingpaths) {
		this.appxDirectory = appxDirectory;
		this.mappings = mappingpaths;
	}

	@Override
	public SakerPath getAppxDirectory() {
		return appxDirectory;
	}

	@Override
	public NavigableMap<SakerPath, SakerPath> getMappings() {
		return mappings;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(appxDirectory);
		SerialUtils.writeExternalMap(out, mappings);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		appxDirectory = SerialUtils.readExternalObject(in);
		mappings = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appxDirectory == null) ? 0 : appxDirectory.hashCode());
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
		PrepareAppxWorkerTaskOutputImpl other = (PrepareAppxWorkerTaskOutputImpl) obj;
		if (appxDirectory == null) {
			if (other.appxDirectory != null)
				return false;
		} else if (!appxDirectory.equals(other.appxDirectory))
			return false;
		if (mappings == null) {
			if (other.mappings != null)
				return false;
		} else if (!mappings.equals(other.mappings))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + appxDirectory + "]";
	}
}