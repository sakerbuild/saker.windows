package saker.windows.main.appx;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;
import saker.windows.api.appx.PackageAppxWorkerTaskOutput;
import saker.windows.api.signtool.SigntoolSignWorkerTaskOutput;
import saker.windows.impl.appx.BundleAppxWorkerTaskFactory;
import saker.windows.impl.appx.BundleAppxWorkerTaskIdentifier;
import saker.windows.main.TaskDocs;
import saker.windows.main.TaskDocs.DocBundleAppxWorkerTaskOutput;

@NestTaskInformation(returnType = @NestTypeUsage(DocBundleAppxWorkerTaskOutput.class))
@NestInformation("Creates an .appxbundle consisting of multiple .appx packages.\n"
		+ "The task is used to create an .appxbundle package that consists of multiple .appx applications. "
		+ "It uses the makeappx tool to perform its operations.")

@NestParameterInformation(value = "Mappings",
		aliases = "",
		required = true,
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { BundleAppxTaskFactory.MappingKeyTaskOption.class, SakerPath.class }),
		info = @NestInformation("Specifies the contents of the .appxbundle with mappings.\n"
				+ "Each key in the parameter map is a path to a file that should be placed in the .appxbundle. The "
				+ "corresponding values are the paths that the entry should have in the resulting bundle.\n"
				+ "If a value is null or the empty path, it will be the name of the file.\n"
				+ "If you intend to sign the output .appxbundle, you should also sign the .appx inputs before "
				+ "creating the bundle."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the .appxbundle.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation(TaskDocs.SDKS))
public class BundleAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	private static final SakerPath DEFAULT_OUTPUT_PATH = SakerPath.valueOf("default.appxbundle");

	public static final String TASK_NAME = "saker.appx.bundle";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Mappings" }, required = true)
			public Map<MappingKeyTaskOption, SakerPath> mappingsOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				NavigableMap<String, SDKDescription> sdks = SDKSupportFrontendUtils.toSDKDescriptionMap(sdksOption);

				NavigableMap<SakerPath, SakerPath> mappings = new TreeMap<>();
				for (Entry<MappingKeyTaskOption, SakerPath> entry : mappingsOption.entrySet()) {
					SakerPath keypath = entry.getKey().toSakerPath(taskcontext);
					SakerPath valpath = entry.getValue();
					if (valpath == null || SakerPath.EMPTY.equals(valpath)) {
						valpath = SakerPath.valueOf(keypath.getFileName());
					} else {
						TaskOptionUtils.requireForwardRelativePathWithFileName(valpath, "Mapping target path");
					}
					mappings.put(valpath, keypath);
				}

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME)
							.resolve(inferDefaultAppxBundleOutputPathFromInputPaths(mappings.values()));
				}

				BundleAppxWorkerTaskIdentifier workertaskid = new BundleAppxWorkerTaskIdentifier(outputpath);
				BundleAppxWorkerTaskFactory workertask = new BundleAppxWorkerTaskFactory(mappings);
				workertask.setSdks(sdks);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	protected static SakerPath inferDefaultAppxBundleOutputPathFromInputPaths(Iterable<? extends SakerPath> inputs) {
		if (inputs == null) {
			return DEFAULT_OUTPUT_PATH;
		}
		Iterator<? extends SakerPath> it = inputs.iterator();
		if (!it.hasNext()) {
			return DEFAULT_OUTPUT_PATH;
		}
		String s = it.next().getFileName();
		while (it.hasNext()) {
			String n = it.next().getFileName();
			int idx = mismatchIndex(s, n);
			if (idx < 0) {
				continue;
			}
			if (idx == 0) {
				//no common sequence
				return DEFAULT_OUTPUT_PATH;
			}
			s = s.substring(0, idx);
		}
		if (s.isEmpty()) {
			//check just in case
			return DEFAULT_OUTPUT_PATH;
		}
		if (FileUtils.hasExtensionIgnoreCase(s, "appxbundle")) {
			return SakerPath.valueOf(s);
		}
		if (FileUtils.hasExtensionIgnoreCase(s, "appx")) {
			//replace the extension
			return SakerPath.valueOf(s.substring(0, s.length() - 4) + "appxbundle");
		}
		return SakerPath.valueOf(s + ".appxbundle");
	}

	private static int mismatchIndex(CharSequence first, CharSequence second) {
		int flen = first.length();
		int slen = second.length();
		int minlen = Math.min(flen, slen);
		for (int i = 0; i < minlen; i++) {
			if (first.charAt(i) != second.charAt(i)) {
				return i;
			}
		}
		if (flen == slen) {
			return -1;
		}
		return minlen;
	}

	@NestInformation("Path of an input file that should be placed in the output .appxbundle.")
	public static abstract class MappingKeyTaskOption {
		public abstract SakerPath toSakerPath(TaskContext taskcontext);

		public static MappingKeyTaskOption valueOf(String input) {
			return valueOf(SakerPath.valueOf(input));
		}

		public static MappingKeyTaskOption valueOf(SakerPath input) {
			return new MappingKeyTaskOption() {
				@Override
				public SakerPath toSakerPath(TaskContext taskcontext) {
					if (input.isRelative()) {
						return taskcontext.getTaskWorkingDirectoryPath().resolve(input);
					}
					return input;
				}
			};
		}

		public static MappingKeyTaskOption valueOf(PackageAppxWorkerTaskOutput input) {
			return valueOf(input.getPath());
		}

		public static MappingKeyTaskOption valueOf(SigntoolSignWorkerTaskOutput input) {
			return valueOf(input.getPath());
		}

	}
}
