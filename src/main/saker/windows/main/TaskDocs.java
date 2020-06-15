package saker.windows.main;

import java.util.Map;

import saker.build.file.path.SakerPath;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

public class TaskDocs {
	public static final String SDKS = "Specifies the SDKs (Software Development Kits) used by the task.\n"
			+ "SDKs represent development kits that are available in the build environment and to the task.\n"
			+ "The SDK names are compared in a case-insensitive way.";

	@NestTypeInformation(qualifiedName = "PatchAppxManifestWorkerTaskOutput")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The output path of the patched AppxManifest.xml"))
	public static class DocPatchAppxManifestWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "AppxProcessorArchitecture",
			enumValues = {

					@NestFieldInformation(value = "x86", info = @NestInformation("The x86 architecture.")),
					@NestFieldInformation(value = "x64", info = @NestInformation("The x64 architecture.")),
					@NestFieldInformation(value = "arm", info = @NestInformation("The arm architecture.")),
					@NestFieldInformation(value = "arm64", info = @NestInformation("The arm64 architecture.")),
					@NestFieldInformation(value = "neutral", info = @NestInformation("The neutral architecture.")),

			})
	@NestInformation("Describes the architecture of the code contained in the package.")
	public static class DocAppxProcessorArchitecture {
	}

	@NestTypeInformation(qualifiedName = "PrepareAppxWorkerTaskOutput")
	@NestInformation("Output of the appx preparation task.")
	@NestFieldInformation(value = "AppxDirectory",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The output path of the prepared appx directory."))
	@NestFieldInformation(value = "Mappings",
			type = @NestTypeUsage(value = Map.class, elementTypes = { SakerPath.class, SakerPath.class }),
			info = @NestInformation("The mappings of the application contents.\n"
					+ "The field contains relative keys which represent the path of a file in the application bundle. The associated "
					+ "values are the absolute execution paths where the files reside in the build system."))
	public static class DocPrepareAppxWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "RegisterAppxWorkerTaskOutput")
	@NestInformation("Output of an appx registration task.")
	@NestFieldInformation(value = "AppxManifestLocalPath",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The local filesystem path of the AppxManifest.xml of the registered appx package."))
	public static class DocRegisterAppxWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "PackageAppxWorkerTaskOutput")
	@NestInformation("Output of the .appx packager task.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The absolute output path of the .appx archive."))
	public static class DocPackageAppxWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "BundleAppxWorkerTaskOutput")
	@NestInformation("Output of the .appxbundle packager task.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The absolute output path of the .appxbundle archive."))
	public static class DocBundleAppxWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "SigntoolSignWorkerTaskOutput")
	@NestInformation("Output of the signtool signer task.")
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The absolute output path of the signed file."))
	public static class DocSigntoolSignWorkerTaskOutput {
	}

	@NestInformation("Signing algorithm for signtool.")
	@NestTypeInformation(qualifiedName = "SigntoolAlgorithm",
			enumValues = {

					@NestFieldInformation(value = "SHA1", info = @NestInformation("The SHA1 algorithm.")),
					@NestFieldInformation(value = "SHA256", info = @NestInformation("The SHA256 algorithm.")),
					@NestFieldInformation(value = "SHA512", info = @NestInformation("The SHA512 algorithm.")),

			})
	public static class DocSigntoolAlgorithm {
	}
}
