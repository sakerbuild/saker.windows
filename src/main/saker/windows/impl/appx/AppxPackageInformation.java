package saker.windows.impl.appx;

import saker.windows.impl.thirdparty.org.json.JSONObject;

public class AppxPackageInformation {
	private String name;
	private String packageFamilyName;
	private String packageFullName;
	private String version;
	private String installLocation;

	public AppxPackageInformation(JSONObject json) {
		name = json.optString("Name", null);
		packageFamilyName = json.optString("PackageFamilyName", null);
		packageFullName = json.optString("PackageFullName", null);
		version = json.optString("Version", null);
		installLocation = json.optString("InstallLocation", null);
	}

	public String getName() {
		return name;
	}

	public String getPackageFamilyName() {
		return packageFamilyName;
	}

	public String getPackageFullName() {
		return packageFullName;
	}

	public String getVersion() {
		return version;
	}

	public String getInstallLocation() {
		return installLocation;
	}
}
