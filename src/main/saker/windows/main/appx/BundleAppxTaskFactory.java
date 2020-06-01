package saker.windows.main.appx;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.windows.api.appx.PackageAppxWorkerTaskOutput;
import saker.windows.api.signtool.SigntoolSignWorkerTaskOutput;
import saker.windows.impl.appx.BundleAppxWorkerTaskFactory;
import saker.windows.impl.appx.BundleAppxWorkerTaskIdentifier;

public class BundleAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.bundle";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Mappigns" }, required = true)
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

				SakerPath outputpath;
				if (outputOption != null) {
					if (!outputOption.isForwardRelative()) {
						throw new InvalidPathFormatException("Output" + " must be forward relative: " + outputOption);
					}
					if (outputOption.getFileName() == null) {
						throw new InvalidPathFormatException("Output" + " must have a file name: " + outputOption);
					}
					outputpath = outputOption;
				} else {
					outputpath = SakerPath.valueOf("default.appxbundle");
				}
				NavigableMap<SakerPath, SakerPath> mappings = new TreeMap<>();
				for (Entry<MappingKeyTaskOption, SakerPath> entry : mappingsOption.entrySet()) {
					SakerPath keypath = entry.getKey().toSakerPath(taskcontext);
					SakerPath valpath = entry.getValue();
					if (valpath == null || SakerPath.EMPTY.equals(valpath)) {
						valpath = SakerPath.valueOf(keypath.getFileName());
					} else {
						if (!valpath.isForwardRelative()) {
							throw new InvalidPathFormatException(
									"Mapping target path" + " must be forward relative: " + valpath);
						}
						if (valpath.getFileName() == null) {
							throw new InvalidPathFormatException(
									"Mapping target path" + " must have a file name: " + valpath);
						}
					}
					mappings.put(keypath, valpath);
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
