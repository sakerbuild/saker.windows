package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class MappingsContentDescriptor implements ContentDescriptor, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableMap<SakerPath, SakerPath> mappings;

	/**
	 * For {@link Externalizable}.
	 */
	public MappingsContentDescriptor() {
	}

	public MappingsContentDescriptor(NavigableMap<SakerPath, SakerPath> mappings) {
		this.mappings = mappings;
	}

	public NavigableMap<SakerPath, SakerPath> getMappings() {
		return mappings;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, mappings);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		mappings = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappings == null) ? 0 : mappings.hashCode());
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
		MappingsContentDescriptor other = (MappingsContentDescriptor) obj;
		if (!ObjectUtils.mapOrderedEquals(this.mappings, other.mappings)) {
			return false;
		}
		return true;
	}

}