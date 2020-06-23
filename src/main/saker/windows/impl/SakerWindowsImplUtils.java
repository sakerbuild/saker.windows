package saker.windows.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.LocalFileProvider;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.nest.bundle.BundleIdentifier;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.windows.impl.sdk.WindowsAppCertKitSDKReference;
import saker.windows.impl.sdk.WindowsKitsSDKReference;
import saker.windows.impl.sdk.WindowsUapSDKReference;
import saker.windows.impl.thirdparty.org.json.JSONTokener;

public class SakerWindowsImplUtils {
	public static final SakerPath PATH_PROGRAM_FILES = SakerPath.valueOf("c:/Program Files");
	public static final SakerPath PATH_PROGRAM_FILES_X86 = SakerPath.valueOf("c:/Program Files (x86)");

	private SakerWindowsImplUtils() {
		throw new UnsupportedOperationException();
	}

	public static Predicate<? super String> getSDKVersionsPredicate(Set<String> versions) {
		if (versions == null) {
			return Functionals.alwaysPredicate();
		}
		return versions::contains;
	}

	public static WindowsKitsSDKReference searchWindowsKitsInProgramFiles(SakerPath programfiles,
			Predicate<? super String> versionpredicate) {
		LocalFileProvider fp = LocalFileProvider.getInstance();

		SakerPath winkitsdir = programfiles.resolve("Windows Kits");
		NavigableMap<String, ? extends FileEntry> entries;
		try {
			//Expected to contain version numbers directories. E.g. 10, 8.1
			entries = fp.getDirectoryEntries(winkitsdir);
		} catch (IOException e) {
			return null;
		}
		NavigableSet<String> descendingverdirectories = new TreeSet<>(
				Collections.reverseOrder(BundleIdentifier::compareVersionNumbers));
		for (Entry<String, ? extends FileEntry> verentry : entries.entrySet()) {
			if (!verentry.getValue().isDirectory()) {
				continue;
			}
			String dirname = verentry.getKey();
			//same version number semantics as bundle identifiers
			if (!BundleIdentifier.isValidVersionNumber(dirname)) {
				continue;
			}
			descendingverdirectories.add(dirname);
		}
		//Search for Windows.h to validate the SDK install.
		//expect it to be at Include/<sdkversion>/um/Windows.h
		for (String versiondir : descendingverdirectories) {
			SakerPath versionedwinkitsdir = winkitsdir.resolve(versiondir);
			SakerPath includedir = versionedwinkitsdir.resolve("Include");
			NavigableMap<String, ? extends FileEntry> includedirentries;
			try {
				includedirentries = fp.getDirectoryEntries(includedir);
			} catch (IOException e) {
				continue;
			}
			NavigableSet<String> descendingincludeverdirectories = new TreeSet<>(
					Collections.reverseOrder(BundleIdentifier::compareVersionNumbers));
			for (Entry<String, ? extends FileEntry> incdirentry : includedirentries.entrySet()) {
				if (!incdirentry.getValue().isDirectory()) {
					continue;
				}
				String version = incdirentry.getKey();
				if (!BundleIdentifier.isValidVersionNumber(version)) {
					continue;
				}
				if (!versionpredicate.test(version)) {
					continue;
				}
				descendingincludeverdirectories.add(version);
			}
			for (String includeverdir : descendingincludeverdirectories) {
				SakerPath winhpath = includedir.resolve(includeverdir, "um", "Windows.h");
				try {
					if (fp.getFileAttributes(winhpath).isRegularFile()) {
						return new WindowsKitsSDKReference(versionedwinkitsdir, includeverdir);
					}
				} catch (IOException e) {
					continue;
				}
			}
		}
		return null;
	}

	public static WindowsUapSDKReference searchWindowsUapInProgramFiles(SakerPath programfiles,
			Predicate<? super String> versionpredicate) {
		LocalFileProvider fp = LocalFileProvider.getInstance();

		SakerPath winkitsdir = programfiles.resolve("Windows Kits");
		NavigableMap<String, ? extends FileEntry> entries;
		try {
			//Expected to contain version numbers directories. E.g. 10, 8.1
			entries = fp.getDirectoryEntries(winkitsdir);
		} catch (IOException e) {
			return null;
		}
		NavigableSet<String> descendingverdirectories = new TreeSet<>(
				Collections.reverseOrder(BundleIdentifier::compareVersionNumbers));
		for (Entry<String, ? extends FileEntry> verentry : entries.entrySet()) {
			if (!verentry.getValue().isDirectory()) {
				continue;
			}
			String dirname = verentry.getKey();
			//same version number semantics as bundle identifiers
			if (!BundleIdentifier.isValidVersionNumber(dirname)) {
				continue;
			}
			descendingverdirectories.add(dirname);
		}
		//Search for Platform.xml to validate the SDK install.
		//expect it to be at Platforms/UAP/<sdkversion>/Platform.xml
		for (String versiondir : descendingverdirectories) {
			SakerPath versionedwinkitsdir = winkitsdir.resolve(versiondir);
			SakerPath platformsdir = versionedwinkitsdir.resolve("Platforms", "UAP");
			NavigableMap<String, ? extends FileEntry> includedirentries;
			try {
				includedirentries = fp.getDirectoryEntries(platformsdir);
			} catch (IOException e) {
				continue;
			}
			NavigableSet<String> descendingincludeverdirectories = new TreeSet<>(
					Collections.reverseOrder(BundleIdentifier::compareVersionNumbers));
			for (Entry<String, ? extends FileEntry> incdirentry : includedirentries.entrySet()) {
				if (!incdirentry.getValue().isDirectory()) {
					continue;
				}
				String version = incdirentry.getKey();
				if (!BundleIdentifier.isValidVersionNumber(version)) {
					continue;
				}
				if (!versionpredicate.test(version)) {
					continue;
				}
				descendingincludeverdirectories.add(version);
			}
			for (String includeverdir : descendingincludeverdirectories) {
				SakerPath platformxmlpath = platformsdir.resolve(includeverdir, "Platform.xml");
				try {
					if (fp.getFileAttributes(platformxmlpath).isRegularFile()) {
						return new WindowsUapSDKReference(versionedwinkitsdir, includeverdir);
					}
				} catch (IOException e) {
					continue;
				}
			}
		}
		return null;
	}

	public static WindowsAppCertKitSDKReference searchWindowsAppCertKitInProgramFiles(SakerPath programfiles) {
		LocalFileProvider fp = LocalFileProvider.getInstance();

		SakerPath winkitsdir = programfiles.resolve("Windows Kits");
		NavigableMap<String, ? extends FileEntry> entries;
		try {
			//Expected to contain version numbers directories. E.g. 10, 8.1
			entries = fp.getDirectoryEntries(winkitsdir);
		} catch (IOException e) {
			return null;
		}
		NavigableSet<String> descendingverdirectories = new TreeSet<>(
				Collections.reverseOrder(BundleIdentifier::compareVersionNumbers));
		for (Entry<String, ? extends FileEntry> verentry : entries.entrySet()) {
			if (!verentry.getValue().isDirectory()) {
				continue;
			}
			String dirname = verentry.getKey();
			//same version number semantics as bundle identifiers
			if (!BundleIdentifier.isValidVersionNumber(dirname)) {
				continue;
			}
			descendingverdirectories.add(dirname);
		}
		//Search for appcert.exe to validate the SDK install.
		//expect it to be at App Certification Kit/appcert.exe
		for (String versiondir : descendingverdirectories) {
			SakerPath versionedwinkitsdir = winkitsdir.resolve(versiondir);
			SakerPath appcertexepath = versionedwinkitsdir.resolve("App Certification Kit/appcert.exe");
			try {
				if (fp.getFileAttributes(appcertexepath).isRegularFile()) {
					return new WindowsAppCertKitSDKReference(appcertexepath.getParent());
				}
			} catch (IOException e) {
				continue;
			}
		}
		return null;
	}

	public static String getAppxManifestIdentityName(InputStream is) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//not namespace aware
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		Element rootelem = doc.getDocumentElement();
		if (!"Package".equals(rootelem.getNodeName())) {
			throw new IllegalArgumentException(
					"Invalid AppxManifest.xml, expected Package root element instead of " + rootelem.getNodeName());
		}
		NodeList children = rootelem.getChildNodes();
		for (int i = 0, clen = children.getLength(); i < clen; i++) {
			Node item = children.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (!"Identity".equals(item.getNodeName())) {
				continue;
			}
			Element elem = (Element) item;
			String name = elem.getAttribute("Name");
			if (ObjectUtils.isNullOrEmpty(name)) {
				throw new IllegalArgumentException("Name attribute not found in Identity element of AppxManifest.xml.");
			}
			return name;
		}
		throw new IllegalArgumentException("Identity element not found in AppxManifest.");
	}

	public static String getAppxPackagePackageFullName(TaskContext taskcontext, String identityName) throws Exception {
		Object val = runPowershellJSONCommand(taskcontext, "(Get-AppxPackage " + identityName + ").PackageFullName");
		if (val instanceof String) {
			return (String) val;
		}
		throw new UnsupportedOperationException(
				"Failed to determine appx package full name. Unexpected output: " + val);
	}

	public static Object runPowershellJSONCommand(TaskContext taskcontext, String command) throws Exception {
		SakerProcessBuilder pb = SakerProcessBuilder.create();
		pb.setCommand(ImmutableUtils.asUnmodifiableArrayList("powershell", "-NoProfile", "-NonInteractive", "-Command",
				command + " | ConvertTo-Json -Compress"));
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		CollectingProcessIOConsumer errconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);
		pb.setStandardErrorConsumer(errconsumer);
		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
				taskcontext.getStandardOut().write(errconsumer.getByteArrayRegion());
				throw new IOException("Failed to run powershell command: " + command + " Exit code: " + ec);
			}
		}
		String outputstr = outconsumer.getOutputString();
		try {
			return new JSONTokener(outputstr).nextValue();
		} catch (Exception e) {
			taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
			taskcontext.getStandardOut().write(errconsumer.getByteArrayRegion());
			throw new IOException("Failed to parse powershell cmdlet JSON output: " + command);
		}
	}
}
