package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;

public class InstallLocationLaunchAppxWorkerTaskFactory extends LaunchAppxWorkerTaskFactoryBase {
	private static final long serialVersionUID = 1L;

	private SakerPath installLocation;
	private String applicationId;

	/**
	 * For {@link Externalizable}.
	 */
	public InstallLocationLaunchAppxWorkerTaskFactory() {
	}

	public InstallLocationLaunchAppxWorkerTaskFactory(SakerPath installLocation) {
		this.installLocation = installLocation;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Override
	protected String getAppUserModelId(TaskContext taskcontext, NavigableMap<String, SDKReference> sdkrefs)
			throws Exception {
		AppxPackageInformation packageinfo = DynamicLaunchAppxWorkerTaskFactory.getPackageInformation(taskcontext,
				null, installLocation);
		if (packageinfo == null) {
			throw new IllegalArgumentException(
					"Failed to determine package full name for appx install location: " + installLocation);
		}
		String appid = getApplicationId(taskcontext, packageinfo);
		return packageinfo.getPackageFamilyName() + "!" + appid;
	}

	private String getApplicationId(TaskContext taskcontext, AppxPackageInformation packageinfo) throws Exception {
		if (applicationId != null) {
			return applicationId;
		}
		List<String> appids = DynamicLaunchAppxWorkerTaskFactory.getAppxApplicationIds(taskcontext,
				packageinfo.getPackageFullName());
		if (appids.size() != 1) {
			throw new IllegalArgumentException("Failed to select Application Id to launch for appx: "
					+ packageinfo.getPackageFullName() + " with Ids: " + appids);
		}
		String appid = appids.get(0);
		return appid;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(installLocation);
		out.writeObject(applicationId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		installLocation = SerialUtils.readExternalObject(in);
		applicationId = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
		result = prime * result + ((installLocation == null) ? 0 : installLocation.hashCode());
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
		InstallLocationLaunchAppxWorkerTaskFactory other = (InstallLocationLaunchAppxWorkerTaskFactory) obj;
		if (applicationId == null) {
			if (other.applicationId != null)
				return false;
		} else if (!applicationId.equals(other.applicationId))
			return false;
		if (installLocation == null) {
			if (other.installLocation != null)
				return false;
		} else if (!installLocation.equals(other.installLocation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InstallLocationLaunchAppxWorkerTaskFactory["
				+ (installLocation != null ? "installLocation=" + installLocation + ", " : "")
				+ (applicationId != null ? "applicationId=" + applicationId : "") + "]";
	}

}
