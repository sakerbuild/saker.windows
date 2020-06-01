package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.SDKReference;
import saker.windows.impl.SakerWindowsImplUtils;
import saker.windows.impl.thirdparty.org.json.JSONArray;
import saker.windows.impl.thirdparty.org.json.JSONObject;

public class DynamicLaunchAppxWorkerTaskFactory extends LaunchAppxWorkerTaskFactory {
	private static final long serialVersionUID = 1L;

	private String input;
	private String applicationId;

	/**
	 * For {@link Externalizable}.
	 */
	public DynamicLaunchAppxWorkerTaskFactory() {
	}

	public DynamicLaunchAppxWorkerTaskFactory(String input) {
		this.input = input;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Override
	protected String getAppUserModelId(TaskContext taskcontext, NavigableMap<String, SDKReference> sdkrefs)
			throws Exception {
		SakerPath searchinstallloc = getSearchInstallLocation(taskcontext);

		AppxPackageInformation packageinfo = getPackageInformation(taskcontext, input, searchinstallloc);
		if (packageinfo == null) {
			throw new IllegalArgumentException(
					"Failed to determine package full name for appx launching input: " + input);
		}
		String appid = getApplicationId(taskcontext, packageinfo);
		return packageinfo.getPackageFamilyName() + "!" + appid;
	}

	private String getApplicationId(TaskContext taskcontext, AppxPackageInformation packageinfo) throws Exception {
		if (applicationId != null) {
			return applicationId;
		}
		List<String> appids = getAppxApplicationIds(taskcontext, packageinfo.getPackageFullName());
		if (appids.size() != 1) {
			throw new IllegalArgumentException("Failed to select Application Id to launch for appx: "
					+ packageinfo.getPackageFullName() + " with Ids: " + appids);
		}
		String string = appids.get(0);
		return string;
	}

	public static List<String> getAppxApplicationIds(TaskContext taskcontext, String packagefullname) throws Exception {
		Object val = SakerWindowsImplUtils.runPowershellJSONCommand(taskcontext,
				"(Get-AppxPackageManifest " + packagefullname + ").package.applications.application.id");
		if (val instanceof String) {
			return ImmutableUtils.singletonList((String) val);
		}
		if (val instanceof JSONArray) {
			List<String> result = new ArrayList<>();
			JSONArray array = (JSONArray) val;
			for (Object elem : array) {
				if (!(elem instanceof String)) {
					throw new UnsupportedOperationException(
							"Failed to determine appx application id. Unexpected array element type: " + elem);
				}
				result.add((String) elem);
			}
			return result;
		}
		throw new UnsupportedOperationException(
				"Failed to interpret appx application id information. Unexpected output: " + val);
	}

	public static AppxPackageInformation getPackageInformation(TaskContext taskcontext, String input,
			SakerPath searchinstallloc) throws Exception {
		if (input == null && searchinstallloc == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Get-AppxPackage | Select-Object -Property PackageFullName, PackageFamilyName");
		if (input != null) {
			sb.append(", Name");
		}
		if (searchinstallloc != null) {
			sb.append(", InstallLocation");
		}
		Object val = SakerWindowsImplUtils.runPowershellJSONCommand(taskcontext, sb.toString());
		if (val instanceof JSONObject) {
			JSONArray narray = new JSONArray();
			narray.put(val);
			val = narray;
		}
		if (val instanceof JSONArray) {
			JSONArray arr = (JSONArray) val;
			for (Object o : arr) {
				if (!(o instanceof JSONObject)) {
					throw new UnsupportedOperationException(
							"Failed to interpret appx package information. Unexpected array element type: " + o);
				}
				JSONObject jsono = (JSONObject) o;
				AppxPackageInformation appxpackageinfo = new AppxPackageInformation(jsono);
				if (input != null) {
					if (input.equals(appxpackageinfo.getName()) || input.equals(appxpackageinfo.getPackageFamilyName())
							|| input.equals(appxpackageinfo.getPackageFullName())) {
						return appxpackageinfo;
					}
				}
				if (searchinstallloc != null) {
					String installloc = appxpackageinfo.getInstallLocation();
					try {
						if (installloc != null && searchinstallloc.equals(SakerPath.valueOf(installloc))) {
							return appxpackageinfo;
						}
					} catch (Exception e) {
						//path parsing error
						taskcontext.getTaskUtilities().reportIgnoredException(e);
					}
				}
			}
		} else {
			throw new UnsupportedOperationException(
					"Failed to interpret appx package information. Unexpected output: " + val);
		}
		return null;
	}

	private SakerPath getSearchInstallLocation(TaskContext taskcontext) {
		SakerPath searchinstallloc = null;
		try {
			SakerPath inputpath = SakerPath.valueOf(input);
			if (inputpath.isAbsolute()) {
				if ("AppxManifest.xml".equalsIgnoreCase(inputpath.getFileName())) {
					searchinstallloc = inputpath.getParent();
				} else {
					searchinstallloc = inputpath;
				}
			}
		} catch (Exception e) {
		}
		return searchinstallloc;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(input);
		out.writeObject(applicationId);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		input = SerialUtils.readExternalObject(in);
		applicationId = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
		result = prime * result + ((input == null) ? 0 : input.hashCode());
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
		DynamicLaunchAppxWorkerTaskFactory other = (DynamicLaunchAppxWorkerTaskFactory) obj;
		if (applicationId == null) {
			if (other.applicationId != null)
				return false;
		} else if (!applicationId.equals(other.applicationId))
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DynamicLaunchAppxWorkerTaskFactory[" + (input != null ? "input=" + input + ", " : "")
				+ (applicationId != null ? "applicationId=" + applicationId : "") + "]";
	}

}
