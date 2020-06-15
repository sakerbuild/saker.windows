package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;

public class SimpleLaunchAppxWorkerTaskFactory extends LaunchAppxWorkerTaskFactoryBase {
	private static final long serialVersionUID = 1L;

	private String appUserModelId;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleLaunchAppxWorkerTaskFactory() {
	}

	public SimpleLaunchAppxWorkerTaskFactory(String appUserModelId) {
		this.appUserModelId = appUserModelId;
	}

	@Override
	protected String getAppUserModelId(TaskContext taskcontext, NavigableMap<String, SDKReference> sdkrefs)
			throws Exception {
		return appUserModelId;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(appUserModelId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		appUserModelId = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((appUserModelId == null) ? 0 : appUserModelId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleLaunchAppxWorkerTaskFactory other = (SimpleLaunchAppxWorkerTaskFactory) obj;
		if (appUserModelId == null) {
			if (other.appUserModelId != null)
				return false;
		} else if (!appUserModelId.equals(other.appUserModelId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleLaunchAppxWorkerTaskFactory[" + (appUserModelId != null ? "appUserModelId=" + appUserModelId : "")
				+ "]";
	}

}
